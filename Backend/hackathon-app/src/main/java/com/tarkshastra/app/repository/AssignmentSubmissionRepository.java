package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.AssignmentSubmission;
import com.tarkshastra.app.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {

    List<AssignmentSubmission> findByStudentIdAndAssignment_SubjectId(UUID studentId, UUID subjectId);

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.student.id = :studentId AND s.assignment.subject.id = :subjectId AND s.status = :status")
    long countByStudentIdAndSubjectIdAndStatus(@Param("studentId") UUID studentId, @Param("subjectId") UUID subjectId, @Param("status") SubmissionStatus status);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.student.id = :studentId AND s.assignment.subject.id = :subjectId")
    long countByStudentIdAndSubjectId(@Param("studentId") UUID studentId, @Param("subjectId") UUID subjectId);
}
