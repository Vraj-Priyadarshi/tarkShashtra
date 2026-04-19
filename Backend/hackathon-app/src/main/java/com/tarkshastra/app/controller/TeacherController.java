package com.tarkshastra.app.controller;

import com.tarkshastra.app.dto.request.*;
import com.tarkshastra.app.dto.response.MessageResponse;
import com.tarkshastra.app.dto.response.StudentProfileResponse;
import com.tarkshastra.app.dto.response.SubjectAnalyticsResponse;
import com.tarkshastra.app.dto.response.TeacherDashboardResponse;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final AttendanceService attendanceService;
    private final IAMarksService iaMarksService;
    private final AssignmentService assignmentService;
    private final LMSScoreService lmsScoreService;
    private final SubjectService subjectService;
    private final StudentFlagService studentFlagService;
    private final TeacherDashboardService teacherDashboardService;

    // ─── Dashboard ───

    @GetMapping("/dashboard")
    public ResponseEntity<TeacherDashboardResponse> getDashboard(@AuthenticationPrincipal User teacher) {
        return ResponseEntity.ok(teacherDashboardService.getDashboard(teacher.getId()));
    }

    @GetMapping("/subject-analytics")
    public ResponseEntity<SubjectAnalyticsResponse> getSubjectAnalytics(
            @RequestParam UUID subjectId,
            @RequestParam UUID classId) {
        return ResponseEntity.ok(teacherDashboardService.getSubjectAnalytics(subjectId, classId));
    }

    @GetMapping("/students-by-class")
    public ResponseEntity<List<StudentProfileResponse>> getStudentsByClass(
            @RequestParam UUID classId) {
        return ResponseEntity.ok(teacherDashboardService.getStudentsByClass(classId));
    }

    // ─── Attendance ───

    @PostMapping("/attendance")
    public ResponseEntity<MessageResponse> createAttendanceSession(
            @AuthenticationPrincipal User teacher,
            @Valid @RequestBody AttendanceSessionRequest request) {
        attendanceService.createAttendanceSession(request, teacher);
        return ResponseEntity.ok(new MessageResponse("Attendance recorded successfully"));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceSession>> getAttendanceSessions(
            @RequestParam UUID subjectId,
            @RequestParam UUID classId) {
        return ResponseEntity.ok(attendanceService.getSessionsBySubjectAndClass(subjectId, classId));
    }

    // ─── IA Marks ───

    @PostMapping("/ia-marks")
    public ResponseEntity<MessageResponse> enterIAMarks(
            @AuthenticationPrincipal User teacher,
            @Valid @RequestBody IAMarksEntryRequest request) {
        iaMarksService.enterIAMarks(request, teacher);
        return ResponseEntity.ok(new MessageResponse("IA marks submitted successfully"));
    }

    @GetMapping("/ia-marks")
    public ResponseEntity<List<IAMarks>> getIAMarks(
            @RequestParam UUID subjectId,
            @RequestParam UUID classId,
            @RequestParam int iaRound) {
        return ResponseEntity.ok(iaMarksService.getMarksBySubjectClassRound(subjectId, classId, iaRound));
    }

    // ─── Assignments ───

    @PostMapping("/assignments")
    public ResponseEntity<MessageResponse> createAssignment(
            @AuthenticationPrincipal User teacher,
            @Valid @RequestBody CreateAssignmentRequest request) {
        assignmentService.createAssignment(request, teacher);
        return ResponseEntity.ok(new MessageResponse("Assignment created successfully"));
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<MessageResponse> markSubmissions(
            @PathVariable UUID assignmentId,
            @Valid @RequestBody MarkSubmissionRequest request) {
        assignmentService.markSubmissions(assignmentId, request);
        return ResponseEntity.ok(new MessageResponse("Submissions recorded successfully"));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<Assignment>> getAssignments(
            @RequestParam UUID subjectId,
            @RequestParam UUID classId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsBySubjectAndClass(subjectId, classId));
    }

    // ─── LMS Scores ───

    @PostMapping("/lms-scores")
    public ResponseEntity<MessageResponse> enterLMSScores(
            @AuthenticationPrincipal User teacher,
            @Valid @RequestBody LMSScoreBulkRequest request) {
        lmsScoreService.enterLMSScores(request, teacher);
        return ResponseEntity.ok(new MessageResponse("LMS scores submitted successfully"));
    }

    @GetMapping("/lms-scores")
    public ResponseEntity<List<LMSScore>> getLMSScores(
            @RequestParam UUID subjectId,
            @RequestParam UUID classId) {
        return ResponseEntity.ok(lmsScoreService.getScoresBySubjectAndClass(subjectId, classId));
    }

    // ─── Subject-Teacher Mappings ───

    @GetMapping("/my-subjects")
    public ResponseEntity<List<SubjectTeacherMapping>> getMySubjects(
            @AuthenticationPrincipal User teacher,
            @RequestParam String academicYear) {
        return ResponseEntity.ok(subjectService.getTeacherSubjects(teacher.getId(), academicYear));
    }

    // ─── Flag Student ───

    @PostMapping("/flag-student")
    public ResponseEntity<MessageResponse> flagStudent(
            @AuthenticationPrincipal User teacher,
            @Valid @RequestBody FlagStudentRequest request) {
        studentFlagService.flagStudent(request, teacher);
        return ResponseEntity.ok(new MessageResponse("Student flagged successfully"));
    }
}
