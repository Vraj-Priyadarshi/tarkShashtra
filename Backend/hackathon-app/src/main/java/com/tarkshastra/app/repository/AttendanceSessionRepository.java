package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {

    List<AttendanceSession> findBySubjectIdAndClassEntityId(UUID subjectId, UUID classId);

    Optional<AttendanceSession> findBySubjectIdAndClassEntityIdAndSessionDate(UUID subjectId, UUID classId, LocalDate date);
}
