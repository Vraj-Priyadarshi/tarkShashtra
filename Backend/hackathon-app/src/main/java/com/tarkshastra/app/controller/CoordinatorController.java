package com.tarkshastra.app.controller;

import com.tarkshastra.app.dto.request.CreateExamScheduleRequest;
import com.tarkshastra.app.dto.request.ManualStudentRequest;
import com.tarkshastra.app.dto.request.ManualTeacherRequest;
import com.tarkshastra.app.dto.request.ReassignMentorRequest;
import com.tarkshastra.app.dto.response.*;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.repository.ExamScheduleRepository;
import com.tarkshastra.app.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coordinator")
@RequiredArgsConstructor
public class CoordinatorController {

    private final DepartmentService departmentService;
    private final ClassService classService;
    private final SubjectService subjectService;
    private final CsvUploadService csvUploadService;
    private final CoordinatorDashboardService coordinatorDashboardService;
    private final UserManagementService userManagementService;
    private final PdfExportService pdfExportService;
    private final ExamScheduleRepository examScheduleRepository;
    private final RiskScoreService riskScoreService;

    // ─── Dashboard ───

    @GetMapping("/dashboard")
    public ResponseEntity<InstituteDashboardResponse> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(coordinatorDashboardService.getDashboard(user.getInstitute().getId()));
    }

    @GetMapping("/students")
    public ResponseEntity<Page<StudentProfileResponse>> getStudentList(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(coordinatorDashboardService.getStudentList(
                user.getInstitute().getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/intervention-effectiveness")
    public ResponseEntity<List<InterventionEffectivenessResponse>> getInterventionEffectiveness(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(coordinatorDashboardService.getInterventionEffectiveness(
                user.getInstitute().getId()));
    }

    // ─── Department Endpoints ───

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(departmentService.getDepartmentsByInstitute(user.getInstitute().getId()));
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(
            @AuthenticationPrincipal User user,
            @RequestParam @NotBlank String name,
            @RequestParam @NotBlank String code) {
        return ResponseEntity.ok(departmentService.createDepartment(user.getInstitute().getId(), name, code));
    }

    // ─── Class Endpoints ───

    @GetMapping("/classes")
    public ResponseEntity<List<ClassEntity>> getClasses(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) UUID departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(classService.getClassesByDepartment(departmentId));
        }
        return ResponseEntity.ok(classService.getClassesByInstitute(user.getInstitute().getId()));
    }

    @PostMapping("/classes")
    public ResponseEntity<ClassEntity> createClass(
            @RequestParam @NotNull UUID departmentId,
            @RequestParam @NotBlank String name,
            @RequestParam int semester,
            @RequestParam @NotBlank String academicYear) {
        return ResponseEntity.ok(classService.createClass(departmentId, name, semester, academicYear));
    }

    // ─── Subject Endpoints ───

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getSubjects(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) UUID departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(subjectService.getSubjectsByDepartment(departmentId));
        }
        return ResponseEntity.ok(subjectService.getSubjectsByInstitute(user.getInstitute().getId()));
    }

    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(
            @AuthenticationPrincipal User user,
            @RequestParam @NotNull UUID departmentId,
            @RequestParam @NotBlank String name,
            @RequestParam @NotBlank String code) {
        return ResponseEntity.ok(subjectService.createSubject(departmentId, user.getInstitute().getId(), name, code));
    }

    @PostMapping("/subjects/map-to-class")
    public ResponseEntity<MessageResponse> mapSubjectToClass(
            @RequestParam @NotNull UUID subjectId,
            @RequestParam @NotNull UUID classId,
            @RequestParam int semester,
            @RequestParam @NotBlank String academicYear) {
        subjectService.mapSubjectToClass(subjectId, classId, semester, academicYear);
        return ResponseEntity.ok(new MessageResponse("Subject mapped to class successfully"));
    }

    @PostMapping("/subjects/map-teacher")
    public ResponseEntity<MessageResponse> mapTeacherToSubject(
            @RequestParam @NotNull UUID teacherId,
            @RequestParam @NotNull UUID subjectId,
            @RequestParam @NotNull UUID classId,
            @RequestParam @NotBlank String academicYear) {
        subjectService.mapTeacherToSubjectClass(teacherId, subjectId, classId, academicYear);
        return ResponseEntity.ok(new MessageResponse("Teacher mapped to subject-class successfully"));
    }

    // ─── CSV Upload Endpoints ───

    @PostMapping("/upload/students")
    public ResponseEntity<CsvUploadResponse> uploadStudentCsv(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(csvUploadService.uploadStudentCsv(file, user.getInstitute().getId()));
    }

    @PostMapping("/upload/teachers")
    public ResponseEntity<CsvUploadResponse> uploadTeacherCsv(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(csvUploadService.uploadTeacherCsv(file, user.getInstitute().getId()));
    }

    // ─── CSV Template Downloads ───

    @GetMapping("/csv-templates/students")
    public ResponseEntity<byte[]> downloadStudentCsvTemplate() {
        String template = "roll_number,full_name,email,department_code,class_name,semester,mentor_email\n"
                + "STU001,Raj Mehta,raj@pdeu.ac.in,IT,SE-A,4,prof.shah@pdeu.ac.in\n";
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=student_template.csv")
                .body(template.getBytes());
    }

    @GetMapping("/csv-templates/teachers")
    public ResponseEntity<byte[]> downloadTeacherCsvTemplate() {
        String template = "employee_id,full_name,email,department_code,subjects_taught,mentor_to\n"
                + "EMP001,Prof. Shah,prof.shah@pdeu.ac.in,IT,\"CS301:SE-A,CS302:SE-B\",\"STU001,STU003\"\n";
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=teacher_template.csv")
                .body(template.getBytes());
    }

    // ─── Manual User Management ───

    @PostMapping("/students/manual")
    public ResponseEntity<StudentProfileResponse> addStudentManually(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ManualStudentRequest request) {
        return ResponseEntity.ok(userManagementService.addStudentManually(request, user.getInstitute().getId()));
    }

    @PostMapping("/teachers/manual")
    public ResponseEntity<TeacherProfileResponse> addTeacherManually(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ManualTeacherRequest request) {
        return ResponseEntity.ok(userManagementService.addTeacherManually(request, user.getInstitute().getId()));
    }

    @GetMapping("/teachers")
    public ResponseEntity<Page<TeacherProfileResponse>> getTeacherList(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userManagementService.listTeachers(
                user.getInstitute().getId(), PageRequest.of(page, size)));
    }

    @PutMapping("/students/reassign-mentor")
    public ResponseEntity<MessageResponse> reassignMentor(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReassignMentorRequest request) {
        userManagementService.reassignMentor(request.getStudentId(), request.getNewMentorId(),
                user.getInstitute().getId());
        return ResponseEntity.ok(new MessageResponse("Mentor reassigned successfully"));
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<MessageResponse> deactivateUser(@PathVariable UUID userId) {
        userManagementService.deactivateUser(userId);
        return ResponseEntity.ok(new MessageResponse("User deactivated"));
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<MessageResponse> activateUser(@PathVariable UUID userId) {
        userManagementService.activateUser(userId);
        return ResponseEntity.ok(new MessageResponse("User activated"));
    }

    // ─── Exam Schedules ───

    @PostMapping("/exam-schedules")
    public ResponseEntity<ExamSchedule> createExamSchedule(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateExamScheduleRequest request) {
        ExamSchedule schedule = ExamSchedule.builder()
                .subject(subjectService.getSubjectById(request.getSubjectId()))
                .classEntity(classService.getClassById(request.getClassId()))
                .institute(user.getInstitute())
                .examDate(request.getExamDate())
                .examType(request.getExamType())
                .build();
        return ResponseEntity.ok(examScheduleRepository.save(schedule));
    }

    @GetMapping("/exam-schedules")
    public ResponseEntity<List<ExamSchedule>> getExamSchedules(
            @AuthenticationPrincipal User user,
            @RequestParam @NotNull java.time.LocalDate startDate,
            @RequestParam @NotNull java.time.LocalDate endDate) {
        return ResponseEntity.ok(examScheduleRepository.findByInstituteIdAndExamDateBetween(
                user.getInstitute().getId(), startDate, endDate));
    }

    // ─── Export Endpoints ───

    @GetMapping("/export/risk-report")
    public ResponseEntity<byte[]> exportRiskReport(@AuthenticationPrincipal User user) {
        byte[] pdf = pdfExportService.generateStudentRiskReport(user.getInstitute().getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=risk_report.pdf")
                .body(pdf);
    }

    @GetMapping("/export/student-report/{studentId}")
    public ResponseEntity<byte[]> exportStudentReport(@PathVariable UUID studentId) {
        byte[] pdf = pdfExportService.generateStudentDetailReport(studentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student_report.pdf")
                .body(pdf);
    }

    // ─── Batch Recompute ───

    @PostMapping("/recompute-risk")
    public ResponseEntity<MessageResponse> batchRecompute(@AuthenticationPrincipal User user) {
        riskScoreService.batchRecomputeAllStudents(user.getInstitute().getId());
        return ResponseEntity.ok(new MessageResponse("Risk recomputation initiated"));
    }
}
