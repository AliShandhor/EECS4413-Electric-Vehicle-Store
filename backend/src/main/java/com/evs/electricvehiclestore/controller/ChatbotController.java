package com.evs.electricvehiclestore.controller;

import com.evs.electricvehiclestore.service.ChatbotService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public String askQuestion() {
        return chatbotService.askQuestion();
    }
}