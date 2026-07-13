package com.evs.electricvehiclestore.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatbotServiceTest {

    private final ChatbotService chatbotService =
            new ChatbotService();

    @Test
    void greetingReturnsWelcomeMessage() {

        String response = chatbotService.getResponse("Hello");

        assertEquals(
                "Hello! Welcome to the Electric Vehicle Store. How can I help you today?",
                response
        );
    }

    @Test
    void teslaQuestionReturnsVehicleInformation() {

        String response =
                chatbotService.getResponse("Tell me about Tesla");

        assertEquals(
                "You can browse Tesla vehicles from the Vehicle Catalog.",
                response
        );
    }

    @Test
    void loanQuestionReturnsLoanInformation() {

        String response =
                chatbotService.getResponse("How do I finance a vehicle?");

        assertEquals(
                "Our loan calculator helps estimate monthly financing payments.",
                response
        );
    }

    @Test
    void unknownQuestionReturnsDefaultMessage() {

        String response =
                chatbotService.getResponse("What is the weather today?");

        assertEquals(
                "Sorry, I couldn't understand your question. Please try asking about vehicles, financing, checkout, reviews, or your cart.",
                response
        );
    }
}