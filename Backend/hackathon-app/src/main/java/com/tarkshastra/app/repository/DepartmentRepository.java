package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByInstituteIdAndCode(UUID instituteId, String code);

    List<Department> findByInstituteId(UUID instituteId);

    boolean existsByInstituteIdAndCode(UUID instituteId, String code);
}
