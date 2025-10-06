package com.rohan.incidentmanager.repository;

import com.rohan.incidentmanager.entity.Incident;
import com.rohan.incidentmanager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByProject(Project project);
    Optional<Incident> findFirstByProjectAndAcknowledgedFalseOrderByCreatedAtDesc(Project project);
}
