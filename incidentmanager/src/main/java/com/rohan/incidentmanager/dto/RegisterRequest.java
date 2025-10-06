package com.rohan.incidentmanager.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role; // CTO / LEAD / DEV
    private String organizationName;
}
