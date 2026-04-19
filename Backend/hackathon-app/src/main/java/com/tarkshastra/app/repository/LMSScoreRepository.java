package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.LMSScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LMSScoreRepository extends JpaRepository<LMSScore, UUID> {

    Optional<LMSScore> findByStudentIdAndSubjectId(UUID studentId, UUID subjectId);

    List<LMSScore> findByStudentId(UUID studentId);

    List<LMSScore> findBySubjectIdAndClassEntityId(UUID subjectId, UUID classId);
}
