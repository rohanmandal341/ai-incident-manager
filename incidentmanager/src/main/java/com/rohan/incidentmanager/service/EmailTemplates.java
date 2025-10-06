package com.rohan.incidentmanager.service;

import com.rohan.incidentmanager.entity.Incident;
import com.rohan.incidentmanager.entity.Project;
import java.time.Duration;
import java.time.LocalDateTime;

public class EmailTemplates {

    public static String subjectNewDown(Project p) {
        return "[INCIDENT] " + p.getProjectName() + " is DOWN";
    }

    public static String bodyNewDown(Project p, Incident inc) {
        return """
Service is DOWN.

Project: %s
Health URL: %s
Detected at: %s
Reason: %s

Action:
- Developer: start investigation
- Lead: may pause if maintenance
- CTO: visibility only

This incident is now active (level 1).
""".formatted(p.getProjectName(), p.getHealthUrl(), inc.getCreatedAt(), inc.getReason());
    }

    public static String subjectEscLead(Project p) {
        return "[ESCALATION L2] Lead attention required - " + p.getProjectName();
    }

    public static String bodyEscLead(Project p, Incident inc) {
        long mins = Duration.between(inc.getCreatedAt(), LocalDateTime.now()).toMinutes();
        return """
Incident still OPEN.

Project: %s
Health URL: %s
Open since: %s
Elapsed: %d minutes

Action:
- Lead: acknowledge, coordinate fix, pause if maintenance
- Dev: continue remediation
""".formatted(p.getProjectName(), p.getHealthUrl(), inc.getCreatedAt(), mins);
    }

    public static String subjectEscCTO(Project p) {
        return "[ESCALATION L3] CTO escalation - " + p.getProjectName();
    }

    public static String bodyEscCTO(Project p, Incident inc) {
        long mins = Duration.between(inc.getCreatedAt(), LocalDateTime.now()).toMinutes();
        return """
High priority escalation.

Project: %s
Health URL: %s
Open since: %s
Elapsed: %d minutes

Action:
- CTO: coordinate with Lead for incident command
- Lead/Dev: provide recovery ETA
""".formatted(p.getProjectName(), p.getHealthUrl(), inc.getCreatedAt(), mins);
    }

    public static String subjectResolved(Project p) {
        return "[RESOLVED] " + p.getProjectName() + " is UP";
    }

    public static String bodyResolved(Project p, Incident inc) {
        long mins = Duration.between(inc.getCreatedAt(), LocalDateTime.now()).toMinutes();
        return """
Service recovered.

Project: %s
Health URL: %s
Total downtime: %d minutes

Incident marked resolved.
""".formatted(p.getProjectName(), p.getHealthUrl(), mins);
    }
}
