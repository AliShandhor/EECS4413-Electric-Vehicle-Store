package com.evs.electricvehiclestore.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ashik Acharya
 */
public class ChatRequest {

    private String message;
    private List<ChatMessage> history = new ArrayList<>();

    public ChatRequest() {}

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatMessage> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessage> history) {
        this.history = history == null ? new ArrayList<>() : history;
    }

    public static class ChatMessage {
        private String role;
        private String content;

        public ChatMessage() {
        }

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
        public void setRole(String role) { this.role = role; }
        public void setContent(String content) { this.content = content; }
    }
}
