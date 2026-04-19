package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.request.IAMarksEntryRequest;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.exception.BadRequestException;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.IAMarksRepository;
import com.tarkshastra.app.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IAMarksService {

    private final IAMarksRepository iaMarksRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SubjectService subjectService;
    private final ClassService classService;

    @Transactional
    public List<IAMarks> enterIAMarks(IAMarksEntryRequest request, User teacher) {
        Subject subject = subjectService.getSubjectById(request.getSubjectId());
        ClassEntity classEntity = classService.getClassById(request.getClassId());

        List<IAMarks> savedMarks = new ArrayList<>();

        for (IAMarksEntryRequest.IAMarkEntry entry : request.getEntries()) {
            StudentProfile sp = studentProfileRepository.findByUserId(entry.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + entry.getStudentId()));

            // Check for duplicate
            iaMarksRepository.findByStudentIdAndSubjectIdAndIaRound(
                    entry.getStudentId(), request.getSubjectId(), request.getIaRound()
            ).ifPresent(existing -> {
                throw new BadRequestException("IA marks already exist for student " +
                        sp.getRollNumber() + " in round " + String.valueOf(request.getIaRound()));
            });

            double normalizedScore = 0;
            if (!entry.isAbsent() && request.getMaxMarks() > 0) {
                normalizedScore = (entry.getObtainedMarks() * 100.0) / request.getMaxMarks();
            }

            IAMarks marks = IAMarks.builder()
                    .student(sp.getUser())
                    .subject(subject)
                    .classEntity(classEntity)
                    .teacher(teacher)
                    .iaRound(request.getIaRound())
                    .maxMarks(request.getMaxMarks())
                    .obtainedMarks(entry.getObtainedMarks())
                    .isAbsent(entry.isAbsent())
                    .normalizedScore(normalizedScore)
                    .build();

            savedMarks.add(iaMarksRepository.save(marks));
        }

        return savedMarks;
    }

    public Double getAverageNormalizedScore(UUID studentId, UUID subjectId) {
        return iaMarksRepository.avgNormalizedScoreByStudentIdAndSubjectId(studentId, subjectId);
    }

    public List<IAMarks> getMarksBySubjectClassRound(UUID subjectId, UUID classId, int iaRound) {
        return iaMarksRepository.findBySubjectIdAndClassEntityIdAndIaRound(subjectId, classId, String.valueOf(iaRound));
    }
}
