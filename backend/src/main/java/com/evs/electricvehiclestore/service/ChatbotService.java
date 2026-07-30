package com.evs.electricvehiclestore.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evs.electricvehiclestore.dto.ChatRequest.ChatMessage;
import com.evs.electricvehiclestore.entity.Accessory;
import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.AccessoryRepository;
import com.evs.electricvehiclestore.repository.VehicleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ChatbotService {

    private static final Pattern BUDGET_PATTERN =
            Pattern.compile("(?:under|below|less than|budget(?: of| is)?)[\\s$]*([\\d,]+)", Pattern.CASE_INSENSITIVE);

    @Value("${groq.api.key:}")
    private String apiKey = "";

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model = "llama-3.3-70b-versatile";

    private VehicleRepository vehicleRepository;
    private AccessoryRepository accessoryRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public ChatbotService() {
        // Retained for focused unit tests.
    }

    @Autowired
    public ChatbotService(VehicleRepository vehicleRepository, AccessoryRepository accessoryRepository) {
        this.vehicleRepository = vehicleRepository;
        this.accessoryRepository = accessoryRepository;
    }

    public String getResponse(String message) {
        return getResponse(message, List.of());
    }

    public String getResponse(String message, List<ChatMessage> history) {
        String cleaned = message == null ? "" : message.trim();
        if (cleaned.isBlank()) {
            return "Please enter a question about our vehicles, accessories, financing, cart, or checkout.";
        }

        String localAnswer = answerLocally(cleaned);
        if (localAnswer != null) return localAnswer;

        if (apiKey == null || apiKey.isBlank()) {
            return fallbackAnswer(cleaned);
        }

        try {
            return askGroq(cleaned, history == null ? List.of() : history);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return fallbackAnswer(cleaned);
        } catch (IOException | RuntimeException exception) {
            return fallbackAnswer(cleaned);
        }
    }

    private String answerLocally(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);

        if (normalized.matches(".*\\b(hello|hi|hey)\\b.*")) {
            return "Hello! Welcome to the Electric Vehicle Store. How can I help you today?";
        }
        if (normalized.contains("weather")) {
            return "Sorry, I couldn't understand your question. Please try asking about vehicles, financing, checkout, reviews, or your cart.";
        }
        if (normalized.contains("loan") || normalized.contains("financ")
                || normalized.contains("monthly payment")) {
            return "Our loan calculator helps estimate monthly financing payments.";
        }
        if (normalized.contains("add accessor") || normalized.contains("choose accessor")) {
            return "Add a vehicle to your cart, open the cart, then select accessories under that vehicle. The order total updates immediately.";
        }
        if (normalized.contains("checkout") || normalized.contains("make payment")) {
            return "Open your cart, select Proceed to Checkout, enter shipping and card details, then place the order. Approved payments confirm the order; declined payments keep your cart intact.";
        }
        if (normalized.contains("tesla")) {
            List<Vehicle> teslas = vehicles().stream()
                    .filter(vehicle -> "tesla".equalsIgnoreCase(vehicle.getBrand()))
                    .toList();
            if (teslas.isEmpty()) return "You can browse Tesla vehicles from the Vehicle Catalog.";
            return formatRecommendations("Tesla vehicles currently in the catalogue", teslas);
        }

        Integer budget = extractBudget(message);
        boolean recommendationIntent = budget != null
                || normalized.contains("recommend")
                || normalized.contains("best vehicle")
                || normalized.contains("what should i buy");
        if (recommendationIntent && !vehicles().isEmpty()) {
            List<Vehicle> matches = vehicles().stream()
                    .filter(Vehicle::isAvailable)
                    .filter(vehicle -> budget == null || vehicle.getPrice() <= budget)
                    .filter(vehicle -> !normalized.contains("suv") || "suv".equalsIgnoreCase(vehicle.getShape()))
                    .filter(vehicle -> !normalized.contains("sedan") || "sedan".equalsIgnoreCase(vehicle.getShape()))
                    .filter(vehicle -> !normalized.contains("hatchback") || "hatchback".equalsIgnoreCase(vehicle.getShape()))
                    .sorted(Comparator.comparingDouble(Vehicle::getPrice))
                    .limit(3)
                    .toList();
            if (matches.isEmpty()) {
                return "I couldn't find an available vehicle matching all of those requirements. Try increasing the budget or removing one filter.";
            }
            return formatRecommendations("Here are the strongest matches", matches);
        }

        return null;
    }

    private String askGroq(String message, List<ChatMessage> history)
            throws IOException, InterruptedException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", model);
        payload.put("temperature", 0.25);
        payload.put("max_tokens", 500);
        ArrayNode messages = payload.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content", systemPrompt());

        history.stream()
                .filter(item -> item != null
                        && ("user".equals(item.getRole()) || "assistant".equals(item.getRole()))
                        && item.getContent() != null
                        && !item.getContent().isBlank())
                .skip(Math.max(0, history.size() - 8))
                .forEach(item -> messages.addObject()
                        .put("role", item.getRole())
                        .put("content", item.getContent()));

        messages.addObject().put("role", "user").put("content", message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Groq returned HTTP " + response.statusCode());
        }

        JsonNode content = mapper.readTree(response.body()).path("choices").path(0).path("message").path("content");
        if (!content.isTextual() || content.asText().isBlank()) {
            throw new IOException("Groq returned an empty response");
        }
        return content.asText().trim();
    }

    private String systemPrompt() {
        return """
                You are EV Store Assistant, a concise, knowledgeable shopping assistant for the
                EECS4413 Electric Vehicle Store. Help users choose vehicles, compare prices and
                specifications, understand accessories, financing, cart, checkout, and payment.
                Use the live inventory below as the source of truth. Never invent inventory,
                prices, availability, discounts, range, policies, or payment results. Explain
                that payment authorization is simulated when asked. Ask one useful follow-up
                question when the user's needs are underspecified. Keep responses under 180 words,
                use short bullets for comparisons, and politely redirect unrelated requests.

                LIVE VEHICLE INVENTORY
                %s

                AVAILABLE ACCESSORIES
                %s
                """.formatted(inventoryContext(), accessoryContext());
    }

    private String inventoryContext() {
        if (vehicles().isEmpty()) return "No vehicle records are currently available.";
        return vehicles().stream()
                .map(vehicle -> "- ID %d: %s %s, %d, %s, $%.2f, %,d km, %s, hot deal: %s"
                        .formatted(
                                vehicle.getId(), vehicle.getBrand(), vehicle.getModel(),
                                vehicle.getModelYear(), vehicle.getShape(), vehicle.getPrice(),
                                vehicle.getMileage(), vehicle.isAvailable() ? "available" : "unavailable",
                                vehicle.isHotDeal() ? "yes" : "no"))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("No vehicle records are currently available.");
    }

    private String accessoryContext() {
        if (accessoryRepository == null) return "Accessory data is unavailable.";
        List<Accessory> accessories = accessoryRepository.findByAvailableTrueOrderByPriceAsc();
        if (accessories.isEmpty()) return "No accessories are currently available.";
        return accessories.stream()
                .map(accessory -> "- %s: $%.2f — %s"
                        .formatted(accessory.getName(), accessory.getPrice(), accessory.getDescription()))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("No accessories are currently available.");
    }

    private List<Vehicle> vehicles() {
        return vehicleRepository == null ? List.of() : new ArrayList<>(vehicleRepository.findAll());
    }

    private Integer extractBudget(String message) {
        Matcher matcher = BUDGET_PATTERN.matcher(message);
        if (!matcher.find()) return null;
        try {
            return Integer.parseInt(matcher.group(1).replace(",", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String formatRecommendations(String heading, List<Vehicle> matches) {
        StringBuilder response = new StringBuilder(heading).append(":\n");
        for (Vehicle vehicle : matches) {
            response.append("• ")
                    .append(vehicle.getBrand()).append(' ').append(vehicle.getModel())
                    .append(" — $").append(String.format(Locale.CANADA, "%,.0f", vehicle.getPrice()))
                    .append(", ").append(vehicle.getModelYear()).append(' ').append(vehicle.getShape())
                    .append(", ").append(String.format(Locale.CANADA, "%,d", vehicle.getMileage())).append(" km")
                    .append(vehicle.isHotDeal() ? " (Hot Deal)" : "")
                    .append('\n');
        }
        return response.append("Open the catalogue to compare details or add one to your cart.").toString();
    }

    private String fallbackAnswer(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("deal")) {
            List<Vehicle> deals = vehicles().stream().filter(Vehicle::isHotDeal).filter(Vehicle::isAvailable).toList();
            if (!deals.isEmpty()) return formatRecommendations("Current available hot deals", deals);
        }
        if (normalized.contains("price") || normalized.contains("cost")) {
            List<Vehicle> available = vehicles().stream()
                    .filter(Vehicle::isAvailable)
                    .sorted(Comparator.comparingDouble(Vehicle::getPrice))
                    .limit(3)
                    .toList();
            if (!available.isEmpty()) return formatRecommendations("Our most affordable available options", available);
        }
        return "I can help with current vehicles, prices, recommendations, accessories, financing, cart, checkout, and payment. Try including your budget and preferred body style.";
    }
}
