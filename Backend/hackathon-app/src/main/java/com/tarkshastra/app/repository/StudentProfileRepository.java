package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.StudentProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    Optional<StudentProfile> findByUserId(UUID userId);

    Optional<StudentProfile> findByRollNumberAndInstituteId(String rollNumber, UUID instituteId);

    List<StudentProfile> findByMentorId(UUID mentorId);

    List<StudentProfile> findByClassEntityId(UUID classId);

    List<StudentProfile> findByDepartmentId(UUID deptId);

    Page<StudentProfile> findByInstituteId(UUID instituteId, Pageable pageable);

    boolean existsByRollNumberAndInstituteId(String rollNumber, UUID instituteId);

    long countByInstituteId(UUID instituteId);
}
