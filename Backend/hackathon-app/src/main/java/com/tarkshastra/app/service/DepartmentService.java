package com.tarkshastra.app.service;

import com.tarkshastra.app.entity.Department;
import com.tarkshastra.app.entity.Institute;
import com.tarkshastra.app.exception.BadRequestException;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.DepartmentRepository;
import com.tarkshastra.app.repository.InstituteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final InstituteRepository instituteRepository;

    @Transactional
    public Department createDepartment(UUID instituteId, String name, String code) {
        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found"));

        if (departmentRepository.existsByInstituteIdAndCode(instituteId, code)) {
            throw new BadRequestException("Department with code '" + code + "' already exists in this institute");
        }

        Department department = Department.builder()
                .name(name)
                .code(code.toUpperCase())
                .institute(institute)
                .build();

        return departmentRepository.save(department);
    }

    public List<Department> getDepartmentsByInstitute(UUID instituteId) {
        return departmentRepository.findByInstituteId(instituteId);
    }

    public Department getDepartmentById(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    public Department getDepartmentByCode(UUID instituteId, String code) {
        return departmentRepository.findByInstituteIdAndCode(instituteId, code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Department with code '" + code + "' not found"));
    }
}
