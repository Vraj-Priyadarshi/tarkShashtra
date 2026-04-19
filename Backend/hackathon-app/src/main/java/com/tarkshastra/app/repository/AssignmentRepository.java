package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    List<Assignment> findBySubjectIdAndClassEntityId(UUID subjectId, UUID classId);

    long countBySubjectIdAndClassEntityId(UUID subjectId, UUID classId);
}
