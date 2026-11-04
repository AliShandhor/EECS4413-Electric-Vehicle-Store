package com.evs.electricvehiclestore.controller;

import com.evs.electricvehiclestore.dto.ChatRequest;
import com.evs.electricvehiclestore.dto.ChatResponse;
import com.evs.electricvehiclestore.service.ChatbotService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin

/**
 * @author Ashik Acharya
 */
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @GetMapping
public String chatbotInfo() {

    return """
    <html>
    <head>
        <title>Electric Vehicle Store Chatbot API</title>
    </head>
    <body style="font-family: Arial, sans-serif; margin:40px;">

        <h1>Electric Vehicle Store Chatbot</h1>

        <p>The chatbot is running successfully.</p>

        <hr>

        <h2>Web Chatbot (Recommended)</h2>

        <p>
            Open the chatbot interface:
        </p>

        <a href="/chatbot.html">http://localhost:8080/chatbot.html</a>

        <hr>

        <h2>REST API</h2>

        <p>
            Send a <b>POST</b> request to:
        </p>

        <code>http://localhost:8080/api/chatbot</code>

        <p><b>Header</b></p>

        <pre>
Content-Type: application/json
        </pre>

        <p><b>Example JSON Body</b></p>

        <pre>
{
    "message": "Tell me about Tesla vehicles"
}
        </pre>

        <p>
            This endpoint can be tested using Postman or any REST API client.
        </p>

    </body>
    </html>
    """;
}

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = chatbotService.getResponse(request.getMessage(), request.getHistory());
        return new ChatResponse(answer);
    }
}
