package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.IAMarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IAMarksRepository extends JpaRepository<IAMarks, UUID> {

    List<IAMarks> findByStudentIdAndSubjectId(UUID studentId, UUID subjectId);

    List<IAMarks> findBySubjectIdAndClassEntityIdAndIaRound(UUID subjectId, UUID classId, String round);

    Optional<IAMarks> findByStudentIdAndSubjectIdAndIaRound(UUID studentId, UUID subjectId, String round);

    @Query("SELECT AVG(ia.normalizedScore) FROM IAMarks ia WHERE ia.student.id = :studentId AND ia.subject.id = :subjectId")
    Double avgNormalizedScoreByStudentIdAndSubjectId(@Param("studentId") UUID studentId, @Param("subjectId") UUID subjectId);
}
