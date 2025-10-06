package com.rohan.incidentmanager.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "projects")
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;
    private String description;
    private String healthUrl;
    private Integer checkIntervalMinutes = 1; // default

    @ManyToOne
    @JoinColumn(name = "team_lead_id")
    private User teamLead;

    @ManyToOne
    @JoinColumn(name = "head_id")
    private User head;

    @ManyToMany
    @JoinTable(name = "project_developers",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> developers = new ArrayList<>();

    private LocalDateTime pausedUntil;
    private String pauseReason;

    private String lastStatus = "UNKNOWN";
    private LocalDateTime lastCheckedAt;

}
