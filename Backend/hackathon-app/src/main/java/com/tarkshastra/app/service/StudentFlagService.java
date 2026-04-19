package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.request.FlagStudentRequest;
import com.tarkshastra.app.dto.response.StudentFlagResponse;
import com.tarkshastra.app.entity.StudentFlag;
import com.tarkshastra.app.entity.Subject;
import com.tarkshastra.app.entity.User;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.StudentFlagRepository;
import com.tarkshastra.app.repository.StudentProfileRepository;
import com.tarkshastra.app.repository.SubjectRepository;
import com.tarkshastra.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentFlagService {

    private final StudentFlagRepository studentFlagRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public StudentFlag flagStudent(FlagStudentRequest request, User flaggedBy) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Subject subject = request.getSubjectId() != null
                ? subjectRepository.findById(request.getSubjectId()).orElse(null)
                : null;

        StudentFlag flag = StudentFlag.builder()
                .student(student)
                .flaggedBy(flaggedBy)
                .subject(subject)
                .note(request.getNote())
                .isResolved(false)
                .build();

        return studentFlagRepository.save(flag);
    }

    public List<StudentFlagResponse> getUnresolvedFlagsForStudent(UUID studentId) {
        return studentFlagRepository.findByStudentIdAndIsResolvedFalse(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<StudentFlagResponse> getUnresolvedFlagsForMentor(UUID mentorId) {
        return studentFlagRepository.findUnresolvedByMentorId(mentorId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void resolveFlag(UUID flagId) {
        StudentFlag flag = studentFlagRepository.findById(flagId)
                .orElseThrow(() -> new ResourceNotFoundException("Flag not found"));
        flag.setIsResolved(true);
        studentFlagRepository.save(flag);
    }

    private StudentFlagResponse toResponse(StudentFlag f) {
        String studentName = studentProfileRepository.findByUserId(f.getStudent().getId())
                .map(sp -> sp.getFullName())
                .orElse(f.getStudent().getEmail());

        return StudentFlagResponse.builder()
                .id(f.getId())
                .studentName(studentName)
                .studentId(f.getStudent().getId())
                .flaggedByName(f.getFlaggedBy().getFullName())
                .subjectName(f.getSubject() != null ? f.getSubject().getName() : null)
                .note(f.getNote())
                .resolved(f.getIsResolved())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
