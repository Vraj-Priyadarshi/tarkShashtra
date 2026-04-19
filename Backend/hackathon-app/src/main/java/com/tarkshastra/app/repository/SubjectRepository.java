package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    Optional<Subject> findByInstituteIdAndCode(UUID instituteId, String code);

    List<Subject> findByInstituteId(UUID instituteId);

    List<Subject> findByDepartmentId(UUID deptId);

    boolean existsByInstituteIdAndCode(UUID instituteId, String code);
}
