package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, UUID> {

    Optional<ClassEntity> findByDepartmentIdAndNameAndAcademicYear(UUID deptId, String name, String year);

    List<ClassEntity> findByDepartmentId(UUID deptId);

    List<ClassEntity> findByInstituteId(UUID instituteId);

    boolean existsByDepartmentIdAndNameAndAcademicYear(UUID deptId, String name, String year);
}
