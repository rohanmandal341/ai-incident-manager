package com.rohan.incidentmanager.scheduler;

import com.rohan.incidentmanager.entity.Incident;
import com.rohan.incidentmanager.entity.Project;
import com.rohan.incidentmanager.repository.ProjectRepository;
import com.rohan.incidentmanager.service.AlertService;
import com.rohan.incidentmanager.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class PingScheduler {

    @Autowired private ProjectRepository projectRepository;
    @Autowired private IncidentService incidentService;
    @Autowired private AlertService alertService;

    private RestTemplate restTemplate() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        return new RestTemplate(factory);
    }

    // runs every 30 seconds
    @Scheduled(fixedRate = 30000)
    public void runPing() {
        List<Project> projects = projectRepository.findAll();
        RestTemplate rt = restTemplate();
        for (Project p : projects) {
            try {
                // skip if paused
                if (p.getPausedUntil() != null && p.getPausedUntil().isAfter(LocalDateTime.now())) {
                    continue;
                }

                String status = "UNKNOWN";
                try {
                    var resp = rt.getForEntity(p.getHealthUrl(), String.class);
                    status = resp.getStatusCode().is2xxSuccessful() ? "UP" : "DOWN";
                } catch (Exception ex) {
                    status = "DOWN";
                }

                p.setLastStatus(status);
                p.setLastCheckedAt(LocalDateTime.now());
                projectRepository.save(p);

                if ("DOWN".equals(status)) {
                    Optional<Incident> activeOpt = incidentService.findActiveForProject(p);
                    if (activeOpt.isEmpty()) {
                        Incident inc = incidentService.createIncident(p, "Health endpoint returned DOWN or timed out");

                        // reload project with recipients (fetch-join) before alerting
                        Project pWithRecipients = projectRepository.findWithRecipientsById(p.getId()).orElse(p);
                        try {
                            alertService.notifyNewIncident(pWithRecipients, inc);
                        } catch (Exception mailEx) {
                            System.err.println("Alert notify (new) failed for project=" + p.getProjectName() + " err=" + mailEx.getMessage());
                        }
                    } else {
                        Incident inc = activeOpt.get();
                        long minutes = java.time.Duration.between(inc.getCreatedAt(), LocalDateTime.now()).toMinutes();

                        if (minutes >= 10 && inc.getEscalationLevel() < 2) {
                            inc.setEscalationLevel(2);
                            inc.setLastEscalationAt(LocalDateTime.now());
                            incidentService.save(inc);

                            Project pWithRecipients = projectRepository.findWithRecipientsById(p.getId()).orElse(p);
                            try {
                                alertService.notifyEscalationLead(pWithRecipients, inc);
                            } catch (Exception mailEx) {
                                System.err.println("Alert notify (lead) failed for project=" + p.getProjectName() + " err=" + mailEx.getMessage());
                            }
                        }

                        if (minutes >= 15 && inc.getEscalationLevel() < 3) {
                            inc.setEscalationLevel(3);
                            inc.setLastEscalationAt(LocalDateTime.now());
                            incidentService.save(inc);

                            Project pWithRecipients = projectRepository.findWithRecipientsById(p.getId()).orElse(p);
                            try {
                                alertService.notifyEscalationCTO(pWithRecipients, inc);
                            } catch (Exception mailEx) {
                                System.err.println("Alert notify (cto) failed for project=" + p.getProjectName() + " err=" + mailEx.getMessage());
                            }
                        }
                    }
                } else {
                    Optional<Incident> active = incidentService.findActiveForProject(p);
                    if (active.isPresent()) {
                        Incident inc = active.get();
                        if (!inc.isAcknowledged()) {
                            inc.setAcknowledged(true);
                            incidentService.save(inc);

                            Project pWithRecipients = projectRepository.findWithRecipientsById(p.getId()).orElse(p);
                            try {
                                alertService.notifyResolved(pWithRecipients, inc);
                            } catch (Exception mailEx) {
                                System.err.println("Alert notify (resolved) failed for project=" + p.getProjectName() + " err=" + mailEx.getMessage());
                            }
                            System.out.println("Incident resolved for project " + p.getProjectName());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("PingScheduler error for project id=" + p.getId() + " : " + e.getMessage());
            }
        }
    }
}
