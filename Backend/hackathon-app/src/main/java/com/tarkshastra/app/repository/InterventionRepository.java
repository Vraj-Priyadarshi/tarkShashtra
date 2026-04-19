package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.Intervention;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, UUID> {

    List<Intervention> findByStudentId(UUID studentId);

    List<Intervention> findByMentorId(UUID mentorId);

    Page<Intervention> findByStudent_Institute_Id(UUID instituteId, Pageable pageable);

    List<Intervention> findByFollowUpDateLessThanEqualAndPostRiskScoreIsNull(LocalDate date);

    long countByStudent_Institute_Id(UUID instituteId);
}
