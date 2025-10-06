package com.rohan.incidentmanager.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "incidents")
public class Incident {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Project project;

    private String reason; // last known error / message
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean acknowledged = false;
    private int escalationLevel = 0; // 0 initial, 1 dev called, 2 lead called, 3 cto called
    private LocalDateTime lastEscalationAt;

   }
