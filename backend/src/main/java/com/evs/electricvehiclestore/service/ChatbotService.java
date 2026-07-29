package com.evs.electricvehiclestore.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Ashik Acharya
 */
@Service
public class ChatbotService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public String getResponse(String message) {

        try {

            String prompt = """
You are the official AI assistant for the EECS4413 Electric Vehicle Store project.

ABOUT THE PROJECT

The Electric Vehicle Store is a modern online marketplace where customers can browse, compare, finance, and purchase electric vehicles.

Your purpose is to help customers, visitors, instructors, and developers understand both the application and how it works.

APPLICATION FEATURES

Customers can:
- Browse all available electric vehicles
- Search for vehicles
- Filter vehicles by brand
- Sort vehicles by price
- View Hot Deals
- View detailed vehicle information
- Save favourite vehicles
- Add vehicles to the shopping cart
- Remove vehicles from the cart
- Checkout and place orders
- Leave reviews and ratings
- Calculate financing using the loan calculator
- Create an account and manage their profile

The chatbot is integrated directly into the application to answer customer questions and explain the project.

WHEN USERS ASK ABOUT THE PROJECT

You should be able to explain:
- The purpose of the Electric Vehicle Store
- How each feature works
- How customers interact with the application
- The shopping process
- Checkout process
- Financing process
- Reviews
- Saved vehicles
- Hot Deals
- Vehicle search and filtering

WHEN USERS ASK ABOUT DEVELOPMENT

Explain the project from a software engineering perspective.

You know that the application contains:
- Controllers
- Services
- Repositories
- Entities
- DTOs
- REST APIs

You can explain:
- How requests move from the frontend to the backend
- How controllers communicate with services
- How services communicate with repositories
- How repositories access the database
- How data flows through the application
- How the chatbot communicates with the AI model
- How the REST API works
- The responsibilities of each component
- Why this architecture was chosen

WHEN USERS ASK ABOUT THE CHATBOT

Explain that:
- The chatbot is part of the Electric Vehicle Store.
- User messages are sent to the backend.
- The backend prepares a prompt.
- The prompt is sent to an AI model.
- The AI response is returned to the frontend.

IMPORTANT RULES

- Answer as the official assistant for this project.
- Stay focused on the Electric Vehicle Store.
- If users ask about project development, explain it in the context of this project.
- If users ask about application features, answer using the project context.
- If users ask unrelated questions, politely redirect them back to the Electric Vehicle Store.
- Never invent information that is not provided.
- If exact inventory or prices are unknown, clearly state that they depend on the application's current database.

User question:
""" + message;

            String json = """
            {
              "model": "llama-3.3-70b-versatile",
              "messages": [
                {
                  "role": "user",
                  "content": %s
                }
              ]
            }
            """.formatted(mapper.writeValueAsString(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());

            return root.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

        } catch (IOException | InterruptedException e) {

            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}