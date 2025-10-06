package com.rohan.incidentmanager.service;

import com.rohan.incidentmanager.dto.ProjectRequestDTO;
import com.rohan.incidentmanager.entity.Project;
import com.rohan.incidentmanager.entity.Role;
import com.rohan.incidentmanager.entity.User;
import com.rohan.incidentmanager.repository.ProjectRepository;
import com.rohan.incidentmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;

    public Project createProject(ProjectRequestDTO dto, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("Requester not found"));
        if (requester.getRole() != Role.LEAD) throw new RuntimeException("Only LEAD can create projects");

        if (!dto.getTeamLeadId().equals(requester.getId())) {
            throw new RuntimeException("teamLeadId must match the logged-in Lead");
        }

        User head = userRepository.findById(dto.getHeadId()).orElseThrow(() -> new RuntimeException("Head/CTO not found"));

        List<User> devs = userRepository.findAllById(dto.getDeveloperIds() == null ? List.of() : dto.getDeveloperIds());
        if (devs.size() != (dto.getDeveloperIds() == null ? 0 : dto.getDeveloperIds().size())) {
            throw new RuntimeException("One or more developer IDs not found");
        }
        for (User d : devs) {
            if (d.getRole() != Role.DEV) throw new RuntimeException("User " + d.getEmail() + " is not DEV");
            if (!d.isVerified()) throw new RuntimeException("User " + d.getEmail() + " not verified");
        }

        Project p = new Project();
        p.setProjectName(dto.getProjectName());
        p.setDescription(dto.getDescription());
        p.setHealthUrl(dto.getHealthUrl());
        if (dto.getCheckIntervalMinutes() != null) p.setCheckIntervalMinutes(dto.getCheckIntervalMinutes());
        p.setTeamLead(requester);
        p.setHead(head);
        p.setDevelopers(devs);
        p.setLastCheckedAt(LocalDateTime.now());
        p.setLastStatus("UNKNOWN");
        return projectRepository.save(p);
    }

    public List<Project> getProjectsForUser(String email) {
        User u = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (u.getRole() == Role.LEAD) {
            return projectRepository.findByTeamLead_Email(email);
        } else {
            return projectRepository.findByDevelopers_Id(u.getId());
        }
    }

    public Project pauseProject(Long projectId, Long minutes, String reason, String requesterEmail) {
        Project p = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        User req = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("Requester not found"));
        if (!p.getTeamLead().getId().equals(req.getId())) throw new RuntimeException("Only team lead can pause this project");

        p.setPauseReason(reason);
        p.setPausedUntil(LocalDateTime.now().plusMinutes(minutes));
        return projectRepository.save(p);
    }

    public Project resumeProject(Long projectId, String requesterEmail) {
        Project p = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        User req = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("Requester not found"));
        if (!p.getTeamLead().getId().equals(req.getId())) throw new RuntimeException("Only team lead can resume");
        p.setPausedUntil(null);
        p.setPauseReason(null);
        return projectRepository.save(p);
    }
}
