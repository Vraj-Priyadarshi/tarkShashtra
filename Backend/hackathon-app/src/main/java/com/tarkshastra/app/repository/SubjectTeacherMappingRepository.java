package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.SubjectTeacherMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectTeacherMappingRepository extends JpaRepository<SubjectTeacherMapping, UUID> {

    List<SubjectTeacherMapping> findByTeacherId(UUID teacherId);

    List<SubjectTeacherMapping> findByTeacherIdAndAcademicYear(UUID teacherId, String year);

    List<SubjectTeacherMapping> findBySubjectIdAndClassEntityId(UUID subjectId, UUID classId);

    boolean existsByTeacherIdAndSubjectIdAndClassEntityIdAndAcademicYear(UUID teacherId, UUID subjectId, UUID classId, String year);
}
