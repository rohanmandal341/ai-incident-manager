package com.rohan.incidentmanager.controller;

import com.rohan.incidentmanager.dto.LoginRequest;
import com.rohan.incidentmanager.dto.RegisterRequest;
import com.rohan.incidentmanager.entity.User;
import com.rohan.incidentmanager.repository.UserRepository;
import com.rohan.incidentmanager.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; // Import HashMap
import java.util.Map; // Import Map
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req, Authentication authentication) {
        Optional<String> requester = Optional.empty();
        if (authentication != null) requester = Optional.of(authentication.getName());
        try {
            String r = authService.register(req, requester);
            return ResponseEntity.status(201).body(r);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            String token = authService.login(req.getEmail(), req.getPassword());
            return ResponseEntity.ok(java.util.Map.of("token", token));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }

    /**
     * UPDATED to handle potential null values in the User object.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = authentication.getName();
        User u = userRepository.findByEmail(email).orElse(null);

        if (u == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // **THE FIX**: Use a HashMap which allows null values, instead of Map.of().
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", u.getId());
        responseBody.put("name", u.getName()); // Safely handles if name is null
        responseBody.put("email", u.getEmail());
        responseBody.put("role", u.getRole() != null ? u.getRole().name() : null); // Safely handles if role is null
        responseBody.put("verified", u.isVerified());
        responseBody.put("organizationName", u.getOrganizationName()); // Safely handles if organizationName is null

        return ResponseEntity.ok(responseBody);
    }
}
