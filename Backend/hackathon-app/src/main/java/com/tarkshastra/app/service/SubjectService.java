package com.tarkshastra.app.service;

import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.exception.BadRequestException;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.SubjectClassMappingRepository;
import com.tarkshastra.app.repository.SubjectRepository;
import com.tarkshastra.app.repository.SubjectTeacherMappingRepository;
import com.tarkshastra.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectClassMappingRepository subjectClassMappingRepository;
    private final SubjectTeacherMappingRepository subjectTeacherMappingRepository;
    private final DepartmentService departmentService;
    private final ClassService classService;
    private final UserRepository userRepository;

    @Transactional
    public Subject createSubject(UUID departmentId, UUID instituteId, String name, String code) {
        Department department = departmentService.getDepartmentById(departmentId);

        if (subjectRepository.existsByInstituteIdAndCode(instituteId, code)) {
            throw new BadRequestException("Subject with code '" + code + "' already exists in this institute");
        }

        Subject subject = Subject.builder()
                .name(name)
                .code(code.toUpperCase())
                .department(department)
                .institute(department.getInstitute())
                .build();

        return subjectRepository.save(subject);
    }

    public List<Subject> getSubjectsByInstitute(UUID instituteId) {
        return subjectRepository.findByInstituteId(instituteId);
    }

    public List<Subject> getSubjectsByDepartment(UUID departmentId) {
        return subjectRepository.findByDepartmentId(departmentId);
    }

    public Subject getSubjectById(UUID id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }

    public Subject getSubjectByCode(UUID instituteId, String code) {
        return subjectRepository.findByInstituteIdAndCode(instituteId, code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Subject with code '" + code + "' not found"));
    }

    @Transactional
    public void mapSubjectToClass(UUID subjectId, UUID classId, int semester, String academicYear) {
        Subject subject = getSubjectById(subjectId);
        ClassEntity classEntity = classService.getClassById(classId);

        if (subjectClassMappingRepository.existsBySubjectIdAndClassEntityIdAndAcademicYear(subjectId, classId, academicYear)) {
            throw new BadRequestException("Subject is already mapped to this class for " + academicYear);
        }

        SubjectClassMapping mapping = SubjectClassMapping.builder()
                .subject(subject)
                .classEntity(classEntity)
                .semester(semester)
                .academicYear(academicYear)
                .build();

        subjectClassMappingRepository.save(mapping);
    }

    @Transactional
    public void mapTeacherToSubjectClass(UUID teacherId, UUID subjectId, UUID classId, String academicYear) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        Subject subject = getSubjectById(subjectId);
        ClassEntity classEntity = classService.getClassById(classId);

        if (subjectTeacherMappingRepository.existsByTeacherIdAndSubjectIdAndClassEntityIdAndAcademicYear(
                teacherId, subjectId, classId, academicYear)) {
            throw new BadRequestException("Teacher is already mapped to this subject+class for " + academicYear);
        }

        SubjectTeacherMapping mapping = SubjectTeacherMapping.builder()
                .teacher(teacher)
                .subject(subject)
                .classEntity(classEntity)
                .academicYear(academicYear)
                .build();

        subjectTeacherMappingRepository.save(mapping);
    }

    public List<SubjectTeacherMapping> getTeacherSubjects(UUID teacherId, String academicYear) {
        return subjectTeacherMappingRepository.findByTeacherIdAndAcademicYear(teacherId, academicYear);
    }

    public List<SubjectClassMapping> getSubjectsByClass(UUID classId) {
        return subjectClassMappingRepository.findByClassEntityId(classId);
    }
}
