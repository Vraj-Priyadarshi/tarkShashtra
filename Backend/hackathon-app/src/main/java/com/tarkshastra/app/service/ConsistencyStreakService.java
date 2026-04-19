package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.response.ConsistencyStreakResponse;
import com.tarkshastra.app.entity.ConsistencyStreak;
import com.tarkshastra.app.entity.StudentProfile;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.ConsistencyStreakRepository;
import com.tarkshastra.app.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsistencyStreakService {

    private final ConsistencyStreakRepository consistencyStreakRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AggregationService aggregationService;
    private final com.tarkshastra.app.repository.SubjectClassMappingRepository subjectClassMappingRepository;

    @Value("${app.streak-thresholds.attendance:75}")
    private double attendanceThreshold;

    @Value("${app.streak-thresholds.assignment:80}")
    private double assignmentThreshold;

    @Value("${app.streak-thresholds.lms:50}")
    private double lmsThreshold;

    @Transactional
    public void updateStreak(UUID studentId) {
        StudentProfile sp = studentProfileRepository.findByUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        ConsistencyStreak streak = consistencyStreakRepository.findByStudentId(studentId)
                .orElse(ConsistencyStreak.builder()
                        .student(sp.getUser())
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());

        // Check if current week qualifies
        boolean qualifies = checkWeekQualifies(studentId, sp);

        LocalDate currentWeekStart = getWeekStart(LocalDate.now());

        if (qualifies) {
            LocalDate lastWeek = streak.getLastQualifyingWeek();
            LocalDate previousWeekStart = getWeekStart(LocalDate.now().minusWeeks(1));

            if (previousWeekStart.equals(lastWeek)) {
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            } else {
                streak.setCurrentStreak(1);
            }

            if (streak.getCurrentStreak() > streak.getLongestStreak()) {
                streak.setLongestStreak(streak.getCurrentStreak());
            }
            streak.setLastQualifyingWeek(currentWeekStart);
        }

        consistencyStreakRepository.save(streak);
    }

    public ConsistencyStreakResponse getStreak(UUID studentId) {
        ConsistencyStreak streak = consistencyStreakRepository.findByStudentId(studentId)
                .orElse(ConsistencyStreak.builder().currentStreak(0).longestStreak(0).build());

        return ConsistencyStreakResponse.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .lastQualifyingWeek(streak.getLastQualifyingWeek())
                .build();
    }

    @Transactional
    public void weeklyBatchUpdateAllStreaks(UUID instituteId) {
        List<StudentProfile> students = studentProfileRepository.findByInstituteId(instituteId,
                Pageable.unpaged()).getContent();
        for (StudentProfile sp : students) {
            try {
                updateStreak(sp.getUser().getId());
            } catch (Exception e) {
                log.error("Failed to update streak for student {}: {}", sp.getRollNumber(), e.getMessage());
            }
        }
        log.info("Weekly streak update completed for institute {}: {} students", instituteId, students.size());
    }

    private boolean checkWeekQualifies(UUID studentId, StudentProfile sp) {
        var mappings = subjectClassMappingRepository.findByClassEntityId(sp.getClassEntity().getId());
        if (mappings.isEmpty()) return false;

        double totalAtt = 0, totalAsg = 0, totalLms = 0;
        int count = 0;

        for (var mapping : mappings) {
            UUID subjectId = mapping.getSubject().getId();
            Double att = aggregationService.getAttendanceScore(studentId, subjectId);
            Double asg = aggregationService.getAssignmentScore(studentId, subjectId);
            Double lms = aggregationService.getLmsScore(studentId, subjectId);

            totalAtt += att != null ? att : 0;
            totalAsg += asg != null ? asg : 0;
            totalLms += lms != null ? lms : 0;
            count++;
        }

        if (count == 0) return false;

        double avgAtt = totalAtt / count;
        double avgAsg = totalAsg / count;
        double avgLms = totalLms / count;

        return avgAtt >= attendanceThreshold && avgAsg >= assignmentThreshold && avgLms >= lmsThreshold;
    }

    private LocalDate getWeekStart(LocalDate date) {
        return date.with(java.time.DayOfWeek.MONDAY);
    }
}
