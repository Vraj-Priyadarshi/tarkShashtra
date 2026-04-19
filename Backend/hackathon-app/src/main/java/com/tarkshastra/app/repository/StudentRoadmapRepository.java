package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.StudentRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRoadmapRepository extends JpaRepository<StudentRoadmap, UUID> {
    Optional<StudentRoadmap> findTopByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
