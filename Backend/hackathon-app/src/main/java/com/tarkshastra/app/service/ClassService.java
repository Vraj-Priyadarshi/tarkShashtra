package com.tarkshastra.app.service;

import com.tarkshastra.app.entity.ClassEntity;
import com.tarkshastra.app.entity.Department;
import com.tarkshastra.app.exception.BadRequestException;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.ClassEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassEntityRepository classEntityRepository;
    private final DepartmentService departmentService;

    @Transactional
    public ClassEntity createClass(UUID departmentId, String name, int semester, String academicYear) {
        Department department = departmentService.getDepartmentById(departmentId);

        if (classEntityRepository.existsByDepartmentIdAndNameAndAcademicYear(departmentId, name, academicYear)) {
            throw new BadRequestException("Class '" + name + "' already exists in this department for " + academicYear);
        }

        ClassEntity classEntity = ClassEntity.builder()
                .name(name)
                .semester(semester)
                .academicYear(academicYear)
                .department(department)
                .institute(department.getInstitute())
                .build();

        return classEntityRepository.save(classEntity);
    }

    public List<ClassEntity> getClassesByDepartment(UUID departmentId) {
        return classEntityRepository.findByDepartmentId(departmentId);
    }

    public List<ClassEntity> getClassesByInstitute(UUID instituteId) {
        return classEntityRepository.findByInstituteId(instituteId);
    }

    public ClassEntity getClassById(UUID id) {
        return classEntityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
    }

    public ClassEntity getClassByDepartmentAndName(UUID departmentId, String name, String academicYear) {
        return classEntityRepository.findByDepartmentIdAndNameAndAcademicYear(departmentId, name, academicYear)
                .orElseThrow(() -> new ResourceNotFoundException("Class '" + name + "' not found in department for " + academicYear));
    }
}
