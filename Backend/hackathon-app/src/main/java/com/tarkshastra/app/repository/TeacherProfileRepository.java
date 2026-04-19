package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.TeacherProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, UUID> {

    Optional<TeacherProfile> findByUserId(UUID userId);

    Optional<TeacherProfile> findByEmployeeIdAndInstituteId(String empId, UUID instituteId);

    List<TeacherProfile> findByInstituteId(UUID instituteId);

    Page<TeacherProfile> findByInstituteId(UUID instituteId, Pageable pageable);

    boolean existsByEmployeeIdAndInstituteId(String empId, UUID instituteId);

    long countByInstituteId(UUID instituteId);
}
