package com.rohan.incidentmanager.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String password; // bcrypt hashed

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean verified = true; // for demo, real system: email verify

    private String organizationName;
}
