package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.AttendanceRecord;
import com.tarkshastra.app.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    List<AttendanceRecord> findByAttendanceSessionId(UUID sessionId);

    List<AttendanceRecord> findByStudentIdAndAttendanceSession_SubjectId(UUID studentId, UUID subjectId);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student.id = :studentId AND ar.attendanceSession.subject.id = :subjectId AND ar.status = :status")
    long countByStudentIdAndSubjectIdAndStatus(@Param("studentId") UUID studentId, @Param("subjectId") UUID subjectId, @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student.id = :studentId AND ar.attendanceSession.subject.id = :subjectId AND ar.status IS NOT NULL")
    long countByStudentIdAndSubjectId(@Param("studentId") UUID studentId, @Param("subjectId") UUID subjectId);
}
