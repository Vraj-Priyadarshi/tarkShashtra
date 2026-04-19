package com.tarkshastra.app.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.tarkshastra.app.dto.response.CsvUploadResponse;
import com.tarkshastra.app.dto.response.CsvUploadResponse.CsvRowError;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.Role;
import com.tarkshastra.app.exception.BadRequestException;
import com.tarkshastra.app.repository.*;
import com.tarkshastra.app.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvUploadService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final DepartmentService departmentService;
    private final ClassService classService;
    private final SubjectService subjectService;
    private final SubjectTeacherMappingRepository subjectTeacherMappingRepository;
    private final SubjectClassMappingRepository subjectClassMappingRepository;
    private final InstituteRepository instituteRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenGenerator tokenGenerator;

    @Value("${app.csv.default-password:}")
    private String csvDefaultPassword;

    @Transactional
    public CsvUploadResponse uploadStudentCsv(MultipartFile file, UUID instituteId) {
        if (file.isEmpty()) {
            throw new BadRequestException("CSV file is empty");
        }

        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new BadRequestException("Institute not found"));

        List<CsvRowError> errors = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)).build()) {

            String[] header = reader.readNext(); // skip header
            if (header == null) {
                throw new BadRequestException("CSV file has no data");
            }

            String[] row;
            int rowNum = 1;
            while ((row = reader.readNext()) != null) {
                rowNum++;
                totalRows++;

                if (row.length < 6) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("row").message("Insufficient columns").build());
                    continue;
                }

                String rollNumber = row[0].trim();
                String fullName = row[1].trim();
                String email = row[2].trim().toLowerCase();
                String departmentCode = row[3].trim();
                String className = row[4].trim();
                String semesterStr = row[5].trim();
                String mentorEmail = row.length > 6 ? row[6].trim().toLowerCase() : null;

                // Validate
                if (rollNumber.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("required").message("Roll number, name, and email are required").build());
                    continue;
                }

                if (userRepository.existsByEmail(email)) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("email").message("Email already registered: " + email).build());
                    continue;
                }

                if (studentProfileRepository.existsByRollNumberAndInstituteId(rollNumber, instituteId)) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("roll_number").message("Roll number already exists: " + rollNumber).build());
                    continue;
                }

                Department department;
                try {
                    department = departmentService.getDepartmentByCode(instituteId, departmentCode);
                } catch (Exception e) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("department_code").message("Department not found: " + departmentCode).build());
                    continue;
                }

                int semester;
                try {
                    semester = Integer.parseInt(semesterStr);
                } catch (NumberFormatException e) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("semester").message("Invalid semester: " + semesterStr).build());
                    continue;
                }

                // Find class - try to find by department + name + current academic year
                ClassEntity classEntity;
                try {
                    List<ClassEntity> classes = classService.getClassesByDepartment(department.getId());
                    classEntity = classes.stream()
                            .filter(c -> c.getName().equalsIgnoreCase(className))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Class not found: " + className));
                } catch (Exception e) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("class_name").message("Class not found: " + className).build());
                    continue;
                }

                // Find mentor if specified
                User mentor = null;
                if (mentorEmail != null && !mentorEmail.isEmpty()) {
                    mentor = userRepository.findByEmail(mentorEmail).orElse(null);
                    if (mentor == null) {
                        log.warn("Mentor email not found: {} for student {}, will set to null", mentorEmail, rollNumber);
                    }
                }

                // Create user
                String tempPassword = (csvDefaultPassword != null && !csvDefaultPassword.isBlank())
                        ? csvDefaultPassword
                        : tokenGenerator.generateTemporaryPassword(12);
                User user = User.builder()
                        .email(email)
                        .passwordHash(passwordEncoder.encode(tempPassword))
                        .roles(Set.of(Role.STUDENT))
                        .institute(institute)
                        .fullName(fullName)
                        .mustChangePassword(true)
                        .isActive(true)
                        .build();
                user = userRepository.save(user);

                // Create student profile
                StudentProfile profile = StudentProfile.builder()
                        .user(user)
                        .fullName(fullName)
                        .rollNumber(rollNumber)
                        .department(department)
                        .classEntity(classEntity)
                        .semester(semester)
                        .mentor(mentor)
                        .institute(institute)
                        .build();
                studentProfileRepository.save(profile);

                // Send temp password email (fire-and-forget)
                log.info("[DEV] Student created — email: {}, tempPassword: {}", email, tempPassword);
                try {
                    emailService.sendTemporaryPasswordEmail(email, tempPassword);
                } catch (Exception e) {
                    log.warn("Failed to send temp password email to {}: {}", email, e.getMessage());
                }

                successCount++;
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to parse CSV: " + e.getMessage());
        }

        return CsvUploadResponse.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .errorCount(errors.size())
                .errors(errors)
                .build();
    }

    @Transactional
    public CsvUploadResponse uploadTeacherCsv(MultipartFile file, UUID instituteId) {
        if (file.isEmpty()) {
            throw new BadRequestException("CSV file is empty");
        }

        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new BadRequestException("Institute not found"));

        List<CsvRowError> errors = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)).build()) {

            String[] header = reader.readNext();
            if (header == null) {
                throw new BadRequestException("CSV file has no data");
            }

            String[] row;
            int rowNum = 1;
            while ((row = reader.readNext()) != null) {
                rowNum++;
                totalRows++;

                if (row.length < 4) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("row").message("Insufficient columns").build());
                    continue;
                }

                String employeeId = row[0].trim();
                String fullName = row[1].trim();
                String email = row[2].trim().toLowerCase();
                String departmentCode = row[3].trim();
                String subjectsTaught = row.length > 4 ? row[4].trim() : "";
                String mentorTo = row.length > 5 ? row[5].trim() : "";

                if (employeeId.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("required").message("Employee ID, name, and email are required").build());
                    continue;
                }

                if (userRepository.existsByEmail(email)) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("email").message("Email already registered: " + email).build());
                    continue;
                }

                Department department;
                try {
                    department = departmentService.getDepartmentByCode(instituteId, departmentCode);
                } catch (Exception e) {
                    errors.add(CsvRowError.builder().rowNumber(rowNum).field("department_code").message("Department not found: " + departmentCode).build());
                    continue;
                }

                // Determine roles
                Set<Role> roles = new HashSet<>();
                if (!subjectsTaught.isEmpty()) {
                    roles.add(Role.SUBJECT_TEACHER);
                }
                if (!mentorTo.isEmpty()) {
                    roles.add(Role.FACULTY_MENTOR);
                }
                if (roles.isEmpty()) {
                    roles.add(Role.SUBJECT_TEACHER); // default
                }

                // Create user
                String tempPassword = (csvDefaultPassword != null && !csvDefaultPassword.isBlank())
                        ? csvDefaultPassword
                        : tokenGenerator.generateTemporaryPassword(12);
                User user = User.builder()
                        .email(email)
                        .passwordHash(passwordEncoder.encode(tempPassword))
                        .roles(roles)
                        .institute(institute)
                        .fullName(fullName)
                        .mustChangePassword(true)
                        .isActive(true)
                        .build();
                final User savedUser = userRepository.save(user);

                // Create teacher profile
                TeacherProfile profile = TeacherProfile.builder()
                        .user(savedUser)
                        .fullName(fullName)
                        .employeeId(employeeId)
                        .department(department)
                        .institute(institute)
                        .build();
                teacherProfileRepository.save(profile);

                // Create subject-teacher mappings: format "CS301:SE-A,CS302:SE-B"
                if (!subjectsTaught.isEmpty()) {
                    String[] mappings = subjectsTaught.split(",");
                    for (String mapping : mappings) {
                        String[] parts = mapping.trim().split(":");
                        if (parts.length != 2) continue;

                        String subjectCode = parts[0].trim();
                        String className = parts[1].trim();

                        try {
                            Subject subject = subjectService.getSubjectByCode(instituteId, subjectCode);
                            List<ClassEntity> deptClasses = classService.getClassesByDepartment(department.getId());
                            ClassEntity classEntity = deptClasses.stream()
                                    .filter(c -> c.getName().equalsIgnoreCase(className))
                                    .findFirst().orElse(null);

                            if (classEntity != null) {
                                String academicYear = classEntity.getAcademicYear();
                                if (!subjectTeacherMappingRepository.existsByTeacherIdAndSubjectIdAndClassEntityIdAndAcademicYear(
                                        savedUser.getId(), subject.getId(), classEntity.getId(), academicYear)) {
                                    SubjectTeacherMapping stm = SubjectTeacherMapping.builder()
                                            .teacher(savedUser)
                                            .subject(subject)
                                            .classEntity(classEntity)
                                            .academicYear(academicYear)
                                            .build();
                                    subjectTeacherMappingRepository.save(stm);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Row {}: Could not map subject {} to class {}: {}", rowNum, subjectCode, className, e.getMessage());
                        }
                    }
                }

                // Assign mentor to students: format "STU001,STU003"
                if (!mentorTo.isEmpty()) {
                    String[] studentRolls = mentorTo.split(",");
                    for (String roll : studentRolls) {
                        roll = roll.trim();
                        if (roll.isEmpty()) continue;

                        studentProfileRepository.findByRollNumberAndInstituteId(roll, instituteId)
                                .ifPresent(sp -> {
                                    sp.setMentor(savedUser);
                                    studentProfileRepository.save(sp);
                                });
                    }
                }

                log.info("[DEV] Teacher created — email: {}, tempPassword: {}", email, tempPassword);
                try {
                    emailService.sendTemporaryPasswordEmail(email, tempPassword);
                } catch (Exception e) {
                    log.warn("Failed to send temp password email to {}: {}", email, e.getMessage());
                }

                successCount++;
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to parse CSV: " + e.getMessage());
        }

        return CsvUploadResponse.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .errorCount(errors.size())
                .errors(errors)
                .build();
    }
}
