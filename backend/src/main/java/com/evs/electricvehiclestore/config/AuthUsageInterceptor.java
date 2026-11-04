package com.evs.electricvehiclestore.config;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.evs.electricvehiclestore.entity.User;
import com.evs.electricvehiclestore.repository.UserRepository;
import com.evs.electricvehiclestore.service.UsageTrackingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthUsageInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER = "evsAuthenticatedUser";

    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;
    private final UserRepository userRepository;
    private final UsageTrackingService usageTrackingService;

    public AuthUsageInterceptor(
            JwtUtil jwtUtil,
            TokenBlacklist tokenBlacklist,
            UserRepository userRepository,
            UsageTrackingService usageTrackingService
    ) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
        this.userRepository = userRepository;
        this.usageTrackingService = usageTrackingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;

        User user = resolveUser(request);
        if (user != null) request.setAttribute(AUTHENTICATED_USER, user);

        String path = request.getRequestURI();
        boolean publicRequest =
                path.startsWith("/api/health")
                || path.startsWith("/api/identity/register")
                || path.startsWith("/api/identity/login")
                || path.startsWith("/api/identity/check-email")
                || path.startsWith("/api/chatbot")
                || path.startsWith("/api/accessories") && HttpMethod.GET.matches(request.getMethod())
                || path.startsWith("/api/catalog") && HttpMethod.GET.matches(request.getMethod());

        if (publicRequest) return true;

        if (user == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "A valid sign-in token is required");
            return false;
        }

        boolean adminRequest = path.startsWith("/api/analytics")
                || path.startsWith("/api/catalog") && !HttpMethod.GET.matches(request.getMethod())
                || path.startsWith("/api/accessories") && !HttpMethod.GET.matches(request.getMethod());

        if (adminRequest && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            writeError(response, HttpStatus.FORBIDDEN, "Administrator access is required");
            return false;
        }

        if (path.startsWith("/api/cart")) {
            String requestedUserId = request.getParameter("userId");
            if (requestedUserId != null && !requestedUserId.equals(String.valueOf(user.getId()))) {
                writeError(response, HttpStatus.FORBIDDEN, "You cannot access another customer's cart");
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception
    ) {
        if (!request.getRequestURI().startsWith("/api/") || response.getStatus() >= 400) return;
        if (request.getRequestURI().startsWith("/api/analytics/usage")) return;

        User user = (User) request.getAttribute(AUTHENTICATED_USER);
        try {
            usageTrackingService.record(
                    user == null ? null : user.getId(),
                    classify(request),
                    request.getRequestURI()
            );
        } catch (RuntimeException ignored) {
            // Analytics must never break a customer request.
        }
    }

    private User resolveUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;

        String token = header.substring(7);
        if (token.isBlank() || tokenBlacklist.isRevoked(token) || !jwtUtil.isValid(token)) return null;

        return userRepository.findByEmail(jwtUtil.extractEmail(token)).orElse(null);
    }

    private String classify(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.endsWith("/login")) return "SIGN_IN";
        if (path.endsWith("/register")) return "REGISTRATION";
        if (path.endsWith("/logout")) return "SIGN_OUT";
        if (path.contains("/checkout")) return "CHECKOUT";
        if (path.contains("/confirm")) return "PAYMENT";
        if (path.startsWith("/api/cart")) return "CART_ACTION";
        if (path.contains("/search")) return "CATALOG_SEARCH";
        if (path.startsWith("/api/catalog")) return "CATALOG_VIEW";
        if (path.startsWith("/api/accessories")) return "ACCESSORY_VIEW";
        return "API_REQUEST";
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
