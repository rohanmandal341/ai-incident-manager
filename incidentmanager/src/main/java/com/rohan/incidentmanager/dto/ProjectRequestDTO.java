package com.rohan.incidentmanager.dto;

import lombok.Data;

import java.util.List;


@Data
public class ProjectRequestDTO {
    private String projectName;
    private String description;
    private String healthUrl;
    private Integer checkIntervalMinutes;
    private Long teamLeadId;
    private Long headId;
    private List<Long> developerIds;
   }
