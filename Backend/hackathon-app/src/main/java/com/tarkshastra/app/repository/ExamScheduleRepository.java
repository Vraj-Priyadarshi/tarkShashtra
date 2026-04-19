package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, UUID> {

    List<ExamSchedule> findByInstituteIdAndExamDateBetween(UUID instituteId, LocalDate start, LocalDate end);

    List<ExamSchedule> findByClassEntityIdAndExamDateAfter(UUID classId, LocalDate today);

    Optional<ExamSchedule> findBySubjectIdAndClassEntityIdAndExamType(UUID subjectId, UUID classId, String type);
}
