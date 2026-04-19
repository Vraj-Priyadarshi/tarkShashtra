package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.request.CreateAssignmentRequest;
import com.tarkshastra.app.dto.request.MarkSubmissionRequest;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.SubmissionStatus;
import com.tarkshastra.app.exception.BadRequestException;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.AssignmentRepository;
import com.tarkshastra.app.repository.AssignmentSubmissionRepository;
import com.tarkshastra.app.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SubjectService subjectService;
    private final ClassService classService;

    @Transactional
    public Assignment createAssignment(CreateAssignmentRequest request, User teacher) {
        Subject subject = subjectService.getSubjectById(request.getSubjectId());
        ClassEntity classEntity = classService.getClassById(request.getClassId());

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .subject(subject)
                .classEntity(classEntity)
                .teacher(teacher)
                .dueDate(request.getDueDate())
                .submissions(new ArrayList<>())
                .build();

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public List<AssignmentSubmission> markSubmissions(UUID assignmentId, MarkSubmissionRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        List<AssignmentSubmission> saved = new ArrayList<>();

        for (MarkSubmissionRequest.SubmissionEntry entry : request.getSubmissions()) {
            StudentProfile sp = studentProfileRepository.findByUserId(entry.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + entry.getStudentId()));

            // Check for duplicate
            submissionRepository.findByAssignmentIdAndStudentId(assignmentId, entry.getStudentId())
                    .ifPresent(existing -> {
                        throw new BadRequestException("Submission already recorded for student " + sp.getRollNumber());
                    });

            AssignmentSubmission submission = AssignmentSubmission.builder()
                    .assignment(assignment)
                    .student(sp.getUser())
                    .status(entry.getStatus())
                    .submittedAt(entry.getStatus() == SubmissionStatus.SUBMITTED ? LocalDateTime.now() : null)
                    .build();

            saved.add(submissionRepository.save(submission));
        }

        return saved;
    }

    public Double getAssignmentCompletionPercentage(UUID studentId, UUID subjectId) {
        long submitted = submissionRepository.countByStudentIdAndSubjectIdAndStatus(
                studentId, subjectId, SubmissionStatus.SUBMITTED);
        long total = submissionRepository.countByStudentIdAndSubjectId(studentId, subjectId);

        if (total == 0) return null;
        return (submitted * 100.0) / total;
    }

    public List<Assignment> getAssignmentsBySubjectAndClass(UUID subjectId, UUID classId) {
        return assignmentRepository.findBySubjectIdAndClassEntityId(subjectId, classId);
    }
}
