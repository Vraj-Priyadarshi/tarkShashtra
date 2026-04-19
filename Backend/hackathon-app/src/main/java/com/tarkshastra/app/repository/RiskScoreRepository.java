package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.RiskScore;
import com.tarkshastra.app.enums.RiskLabel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskScoreRepository extends JpaRepository<RiskScore, UUID> {

    Optional<RiskScore> findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(UUID studentId);

    Optional<RiskScore> findByStudentIdAndSubjectIdAndIsLatestTrue(UUID studentId, UUID subjectId);

    List<RiskScore> findByStudentIdAndSubjectIdIsNullOrderByComputedAtAsc(UUID studentId);

    List<RiskScore> findByStudentIdAndIsLatestTrue(UUID studentId);

    @Query("SELECT COUNT(rs) FROM RiskScore rs WHERE rs.riskLabel = :label AND rs.student.institute.id = :instituteId AND rs.isLatest = true AND rs.subject IS NULL")
    long countByRiskLabelAndInstituteId(@Param("label") RiskLabel label, @Param("instituteId") UUID instituteId);

    @Query("SELECT AVG(rs.riskScore) FROM RiskScore rs WHERE rs.student.institute.id = :instituteId AND rs.isLatest = true AND rs.subject IS NULL")
    Double avgRiskScoreByInstituteId(@Param("instituteId") UUID instituteId);

    @Modifying
    @Query("UPDATE RiskScore rs SET rs.isLatest = false WHERE rs.student.id = :studentId AND rs.subject.id = :subjectId AND rs.isLatest = true")
    void markPreviousAsNotLatest(@Param("studentId") UUID studentId, @Param("subjectId") UUID subjectId);

    @Modifying
    @Query("UPDATE RiskScore rs SET rs.isLatest = false WHERE rs.student.id = :studentId AND rs.subject IS NULL AND rs.isLatest = true")
    void markPreviousOverallAsNotLatest(@Param("studentId") UUID studentId);

    Page<RiskScore> findByStudent_Institute_IdAndSubjectIdIsNullAndIsLatestTrue(UUID instituteId, Pageable pageable);
}
