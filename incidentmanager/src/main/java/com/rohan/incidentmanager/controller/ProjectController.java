package com.rohan.incidentmanager.controller;

import com.rohan.incidentmanager.dto.ProjectRequestDTO;
import com.rohan.incidentmanager.entity.Incident;
import com.rohan.incidentmanager.entity.Project;
import com.rohan.incidentmanager.repository.ProjectRepository;
import com.rohan.incidentmanager.service.IncidentService;
import com.rohan.incidentmanager.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired private ProjectService projectService;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private IncidentService incidentService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody ProjectRequestDTO dto, Authentication authentication) {
        try {
            Project p = projectService.createProject(dto, authentication.getName());
            return ResponseEntity.status(201).body(p);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> myProjects(Authentication authentication) {
        try {
            List<Project> list = projectService.getProjectsForUser(authentication.getName());
            return ResponseEntity.ok(list);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(400).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<?> pause(@PathVariable Long id, @RequestParam Long minutes, @RequestParam(required = false) String reason, Authentication authentication) {
        try {
            Project p = projectService.pauseProject(id, minutes, reason, authentication.getName());
            return ResponseEntity.ok(p);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<?> resume(@PathVariable Long id, Authentication authentication) {
        try {
            Project p = projectService.resumeProject(id, authentication.getName());
            return ResponseEntity.ok(p);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/ack")
    public ResponseEntity<?> acknowledge(@PathVariable Long id, Authentication authentication) {
        Project p = projectRepository.findById(id).orElse(null);
        if (p == null) return ResponseEntity.status(404).body("Project not found");
        // find active incident
        var opt = incidentService.findActiveForProject(p);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("No active incident");
        Incident inc = opt.get();
        // only dev assigned or lead can ack
        // check authenticated user is one of devs or lead
        String email = authentication.getName();
        boolean allowed = p.getTeamLead().getEmail().equals(email) || p.getDevelopers().stream().anyMatch(d -> d.getEmail().equals(email));
        if (!allowed) return ResponseEntity.status(403).body("Not allowed to acknowledge");
        incidentService.acknowledgeIncident(inc.getId());
        return ResponseEntity.ok("Incident acknowledged");
    }

    @GetMapping("/{id}/incidents")
    public ResponseEntity<?> incidents(@PathVariable Long id) {
        Project p = projectRepository.findById(id).orElse(null);
        if (p == null) return ResponseEntity.status(404).body("Project not found");
        return ResponseEntity.ok(incidentService.findActiveForProject(p).map(List::of).orElse(java.util.List.of()));
    }
}
