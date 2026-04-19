package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.SubjectClassMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectClassMappingRepository extends JpaRepository<SubjectClassMapping, UUID> {

    List<SubjectClassMapping> findByClassEntityId(UUID classId);

    List<SubjectClassMapping> findBySubjectId(UUID subjectId);

    boolean existsBySubjectIdAndClassEntityIdAndAcademicYear(UUID subjectId, UUID classId, String year);
}
