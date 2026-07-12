package com.evs.electricvehiclestore.service;

import org.springframework.stereotype.Service;
/**
 * @author Ashik Acharya
 */

@Service
public class ChatbotService {

    public String getResponse(String message) {

        message = message.toLowerCase();

        if (message.contains("hello") || message.contains("hi")) {
            return "Hello! Welcome to the Electric Vehicle Store. How can I help you today?";
        }

        if (message.contains("tesla")) {
            return "You can browse Tesla vehicles from the Vehicle Catalog.";
        }

        if (message.contains("price")) {
            return "Vehicle prices are displayed on each vehicle's details page.";
        }

        if (message.contains("loan") || message.contains("finance")) {
            return "Our loan calculator helps estimate monthly financing payments.";
        }

        if (message.contains("cart")) {
            return "You can add any vehicle to your shopping cart before checkout.";
        }

        if (message.contains("checkout")) {
            return "Proceed to Checkout after reviewing the items in your shopping cart.";
        }

        if (message.contains("review")) {
            return "Customers can submit ratings and reviews after purchasing a vehicle.";
        }

        if (message.contains("contact")) {
            return "Please contact our customer support team for additional assistance.";
        }

        return "Sorry, I couldn't understand your question. Please try asking about vehicles, financing, checkout, reviews, or your cart.";
    }
}