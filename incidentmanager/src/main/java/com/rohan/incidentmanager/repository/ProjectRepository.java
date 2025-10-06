package com.rohan.incidentmanager.repository;

import com.rohan.incidentmanager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByTeamLead_Email(String email);
    List<Project> findByDevelopers_Id(Long developerId);

    @Query("""
      select p from Project p
      left join fetch p.developers d
      left join fetch p.teamLead tl
      left join fetch p.head h
      where p.id = :id
    """)
    Optional<Project> findWithRecipientsById(@Param("id") Long id);
}
