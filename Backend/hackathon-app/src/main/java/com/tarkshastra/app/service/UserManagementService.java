package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.request.ManualStudentRequest;
import com.tarkshastra.app.dto.request.ManualTeacherRequest;
import com.tarkshastra.app.dto.request.StudentFilterRequest;
import com.tarkshastra.app.dto.response.StudentProfileResponse;
import com.tarkshastra.app.dto.response.TeacherProfileResponse;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.Role;
import com.tarkshastra.app.exception.BadRequestException;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.*;
import com.tarkshastra.app.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final InstituteRepository instituteRepository;
    private final DepartmentService departmentService;
    private final ClassService classService;
    private final RiskScoreService riskScoreService;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final EmailService emailService;

    @Transactional
    public StudentProfileResponse addStudentManually(ManualStudentRequest req, UUID instituteId) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered: " + req.getEmail());
        }
        if (studentProfileRepository.existsByRollNumberAndInstituteId(req.getRollNumber(), instituteId)) {
            throw new BadRequestException("Roll number already exists: " + req.getRollNumber());
        }

        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found"));
        Department department = departmentService.getDepartmentById(req.getDepartmentId());
        ClassEntity classEntity = classService.getClassById(req.getClassId());

        User mentor = null;
        if (req.getMentorId() != null) {
            mentor = userRepository.findById(req.getMentorId()).orElse(null);
        }

        String tempPassword = tokenGenerator.generateTemporaryPassword(12);
        User user = User.builder()
                .email(req.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .roles(Set.of(Role.STUDENT))
                .institute(institute)
                .fullName(req.getFullName())
                .mustChangePassword(true)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        StudentProfile sp = StudentProfile.builder()
                .user(user)
                .fullName(req.getFullName())
                .rollNumber(req.getRollNumber())
                .department(department)
                .classEntity(classEntity)
                .semester(req.getSemester())
                .mentor(mentor)
                .institute(institute)
                .build();
        studentProfileRepository.save(sp);

        try {
            emailService.sendTemporaryPasswordEmail(req.getEmail(), tempPassword);
        } catch (Exception ignored) {}

        return toStudentResponse(sp);
    }

    @Transactional
    public TeacherProfileResponse addTeacherManually(ManualTeacherRequest req, UUID instituteId) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered: " + req.getEmail());
        }

        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found"));
        Department department = departmentService.getDepartmentById(req.getDepartmentId());

        Set<Role> roles = new HashSet<>();
        if (req.isSubjectTeacher()) roles.add(Role.SUBJECT_TEACHER);
        if (req.isFacultyMentor()) roles.add(Role.FACULTY_MENTOR);
        if (roles.isEmpty()) roles.add(Role.SUBJECT_TEACHER);

        String tempPassword = tokenGenerator.generateTemporaryPassword(12);
        User user = User.builder()
                .email(req.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .roles(roles)
                .institute(institute)
                .fullName(req.getFullName())
                .mustChangePassword(true)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        TeacherProfile tp = TeacherProfile.builder()
                .user(user)
                .fullName(req.getFullName())
                .employeeId(req.getEmployeeId())
                .department(department)
                .institute(institute)
                .build();
        teacherProfileRepository.save(tp);

        try {
            emailService.sendTemporaryPasswordEmail(req.getEmail(), tempPassword);
        } catch (Exception ignored) {}

        return toTeacherResponse(tp);
    }

    public Page<StudentProfileResponse> listStudents(UUID instituteId, Pageable pageable) {
        return studentProfileRepository.findByInstituteId(instituteId, pageable)
                .map(this::toStudentResponse);
    }

    public Page<TeacherProfileResponse> listTeachers(UUID instituteId, Pageable pageable) {
        return teacherProfileRepository.findByInstituteId(instituteId, pageable)
                .map(this::toTeacherResponse);
    }

    @Transactional
    public void reassignMentor(UUID studentId, UUID newMentorId, UUID instituteId) {
        StudentProfile sp = studentProfileRepository.findByUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        User newMentor = userRepository.findById(newMentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        sp.setMentor(newMentor);
        studentProfileRepository.save(sp);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(true);
        userRepository.save(user);
    }

    private StudentProfileResponse toStudentResponse(StudentProfile sp) {
        var risk = riskScoreService.getRiskScoreResponse(sp.getUser().getId());
        return StudentProfileResponse.builder()
                .id(sp.getId())
                .userId(sp.getUser().getId())
                .fullName(sp.getFullName())
                .email(sp.getUser().getEmail())
                .rollNumber(sp.getRollNumber())
                .departmentName(sp.getDepartment().getName())
                .className(sp.getClassEntity().getName())
                .semester(sp.getSemester())
                .mentorName(sp.getMentor() != null ? sp.getMentor().getFullName() : null)
                .mentorEmail(sp.getMentor() != null ? sp.getMentor().getEmail() : null)
                .riskScore(risk.getRiskScore())
                .riskLabel(risk.getRiskLabel())
                .active(sp.getUser().getIsActive())
                .build();
    }

    private TeacherProfileResponse toTeacherResponse(TeacherProfile tp) {
        return TeacherProfileResponse.builder()
                .id(tp.getId())
                .userId(tp.getUser().getId())
                .fullName(tp.getFullName())
                .email(tp.getUser().getEmail())
                .employeeId(tp.getEmployeeId())
                .departmentName(tp.getDepartment().getName())
                .roles(tp.getUser().getRoles())
                .active(tp.getUser().getIsActive())
                .build();
    }
}
