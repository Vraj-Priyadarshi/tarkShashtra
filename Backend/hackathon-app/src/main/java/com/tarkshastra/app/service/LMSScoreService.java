package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.request.LMSScoreBulkRequest;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.LMSScoreRepository;
import com.tarkshastra.app.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LMSScoreService {

    private final LMSScoreRepository lmsScoreRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SubjectService subjectService;
    private final ClassService classService;

    @Transactional
    public List<LMSScore> enterLMSScores(LMSScoreBulkRequest request, User teacher) {
        Subject subject = subjectService.getSubjectById(request.getSubjectId());
        ClassEntity classEntity = classService.getClassById(request.getClassId());

        List<LMSScore> saved = new ArrayList<>();

        for (LMSScoreBulkRequest.LMSScoreEntry entry : request.getEntries()) {
            StudentProfile sp = studentProfileRepository.findByUserId(entry.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + entry.getStudentId()));

            // Upsert: update if exists, create if not
            LMSScore score = lmsScoreRepository.findByStudentIdAndSubjectId(entry.getStudentId(), request.getSubjectId())
                    .orElse(LMSScore.builder()
                            .student(sp.getUser())
                            .subject(subject)
                            .classEntity(classEntity)
                            .teacher(teacher)
                            .build());

            score.setScore(entry.getScore());
            saved.add(lmsScoreRepository.save(score));
        }

        return saved;
    }

    public Double getLMSScore(UUID studentId, UUID subjectId) {
        return lmsScoreRepository.findByStudentIdAndSubjectId(studentId, subjectId)
                .map(LMSScore::getScore)
                .orElse(null);
    }

    public List<LMSScore> getScoresBySubjectAndClass(UUID subjectId, UUID classId) {
        return lmsScoreRepository.findBySubjectIdAndClassEntityId(subjectId, classId);
    }
}
