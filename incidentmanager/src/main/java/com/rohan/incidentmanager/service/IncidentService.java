package com.rohan.incidentmanager.service;

import com.rohan.incidentmanager.entity.Incident;
import com.rohan.incidentmanager.entity.Project;
import com.rohan.incidentmanager.repository.IncidentRepository;
import com.rohan.incidentmanager.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class IncidentService {
    @Autowired private IncidentRepository incidentRepository;
    @Autowired private ProjectRepository projectRepository;

    public Incident createIncident(Project project, String reason) {
        Incident inc = new Incident();
        inc.setProject(project);
        inc.setReason(reason);
        inc.setEscalationLevel(0);
        inc.setAcknowledged(false);
        return incidentRepository.save(inc);
    }

    public Optional<Incident> findActiveForProject(Project project) {
        return incidentRepository.findFirstByProjectAndAcknowledgedFalseOrderByCreatedAtDesc(project);
    }

    // NEW: generic save so scheduler can persist updates (ack/escalation timestamps/levels)
    public Incident save(Incident inc) {
        return incidentRepository.save(inc);
    }

    public void acknowledgeIncident(Long incidentId) {
        Incident inc = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));
        inc.setAcknowledged(true);
        incidentRepository.save(inc);
    }
}
