package com.rohan.incidentmanager.service;

import com.rohan.incidentmanager.dto.RegisterRequest;
import com.rohan.incidentmanager.entity.Role;
// NOTE: Assuming User entity has a 'private String organizationName;' field
// and the necessary constructor/setters/getters.
import com.rohan.incidentmanager.entity.User;
// NOTE: Assuming UserRepository has findByOrganizationNameAndRole(String, Role)
import com.rohan.incidentmanager.repository.UserRepository;
import com.rohan.incidentmanager.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    // NOTE: You must have implemented the 'findByOrganizationNameAndRole'
    // method in your UserRepository for this logic to work correctly.
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    /**
     * Handles user registration with multi-tenancy checks:
     * 1. Allows unauthenticated users to register as a CTO only if the organizationName is unique.
     * 2. Allows authenticated CTOs to register LEADs/DEVs within their own organization.
     */
    public String register(RegisterRequest req, Optional<String> requesterEmail) {
        // Parse the target role and validate organization name presence
        Role targetRole = Role.valueOf(req.getRole().toUpperCase());
        String orgName = req.getOrganizationName();

        if (orgName == null || orgName.trim().isEmpty()) {
            throw new RuntimeException("Organization Name is required.");
        }

        // --- Multi-Tenancy Security Logic ---

        // Scenario 1: Unauthenticated request (Attempting to register a new CTO/Organization)
        if (requesterEmail.isEmpty()) {
            // Must be registering as a CTO to start a new organization
            if (targetRole != Role.CTO) {
                throw new RuntimeException("Unauthenticated users can only register as a CTO to start a new organization.");
            }

            // Check if another CTO already exists for this organization (meaning the organization name is taken)
            // This is the core multi-tenancy check.
            if (userRepository.findByOrganizationNameIgnoreCaseAndRole(orgName, Role.CTO).isPresent()) {
                throw new RuntimeException("An organization with that name already exists. Please login or use a different name.");
            }

            // Scenario 2: Authenticated request (CTO adding team members)
        } else {
            User r = userRepository.findByEmail(requesterEmail.get())
                    .orElseThrow(() -> new RuntimeException("Requester not found"));

            // Requester must be a CTO
            if (r.getRole() != Role.CTO) {
                throw new RuntimeException("Only a CTO can create users.");
            }

            // New user's organization name must match the CTO's organization
            if (!r.getOrganizationName().equalsIgnoreCase(orgName)) {
                throw new RuntimeException("Cannot create users outside your own organization.");
            }

            // A CTO cannot register another CTO within the same organization
            if (targetRole == Role.CTO) {
                throw new RuntimeException("This organization already has a registered CTO.");
            }
        }

        // --- Core Registration Logic ---
        if (userRepository.existsByEmail(req.getEmail())) throw new RuntimeException("Email exists");

        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(targetRole);
        // CRITICAL: Save the organization name with the user
        u.setOrganizationName(orgName);
        u.setVerified(true);
        userRepository.save(u);

        return "Registered: " + targetRole.name() + " for " + orgName;
    }

    // Login remains the same
    public String login(String email, String password) {
        User u = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(password, u.getPassword())) throw new RuntimeException("Invalid credentials");
        // Token now implicitly includes organizationName since it is tied to the User entity.
        return jwtUtil.generateToken(u.getEmail(), u.getRole().name());
    }
}
