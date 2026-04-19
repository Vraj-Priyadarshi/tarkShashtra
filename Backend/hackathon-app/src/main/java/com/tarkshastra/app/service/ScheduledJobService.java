package com.tarkshastra.app.service;

import com.tarkshastra.app.entity.Institute;
import com.tarkshastra.app.entity.Intervention;
import com.tarkshastra.app.enums.NotificationType;
import com.tarkshastra.app.repository.InstituteRepository;
import com.tarkshastra.app.repository.InterventionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledJobService {

    private final InstituteRepository instituteRepository;
    private final RiskScoreService riskScoreService;
    private final ConsistencyStreakService consistencyStreakService;
    private final AlertService alertService;
    private final InterventionRepository interventionRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
    public void nightlyRiskRecompute() {
        log.info("Starting nightly risk recompute...");
        List<Institute> institutes = instituteRepository.findAll();
        for (Institute inst : institutes) {
            try {
                riskScoreService.batchRecomputeAllStudents(inst.getId());
            } catch (Exception e) {
                log.error("Nightly risk recompute failed for institute {}: {}", inst.getName(), e.getMessage());
            }
        }
        log.info("Nightly risk recompute completed.");
    }

    @Scheduled(cron = "0 0 8 * * MON") // Monday 8 AM
    public void weeklyStreakUpdate() {
        log.info("Starting weekly streak update...");
        List<Institute> institutes = instituteRepository.findAll();
        for (Institute inst : institutes) {
            try {
                consistencyStreakService.weeklyBatchUpdateAllStreaks(inst.getId());
            } catch (Exception e) {
                log.error("Weekly streak update failed for institute {}: {}", inst.getName(), e.getMessage());
            }
        }
        log.info("Weekly streak update completed.");
    }

    @Scheduled(cron = "0 0 9 * * *") // 9 AM daily
    public void dailyAlertCheck() {
        log.info("Starting daily alert check...");
        List<Institute> institutes = instituteRepository.findAll();
        for (Institute inst : institutes) {
            try {
                alertService.sendPreExamAlerts(inst.getId());
            } catch (Exception e) {
                log.error("Pre-exam alert failed for institute {}: {}", inst.getName(), e.getMessage());
            }
        }
        log.info("Daily alert check completed.");
    }

    @Scheduled(cron = "0 0 10 * * *") // 10 AM daily
    public void processInterventionFollowUps() {
        log.info("Processing intervention follow-ups...");
        List<Intervention> dueFollowUps = interventionRepository
                .findByFollowUpDateLessThanEqualAndPostRiskScoreIsNull(LocalDate.now());

        for (Intervention intervention : dueFollowUps) {
            try {
                String message = "Follow-up due for intervention with student " +
                        intervention.getStudent().getFullName() + " (scheduled for " +
                        intervention.getFollowUpDate() + ").";

                notificationService.sendNotification(
                        intervention.getMentor(),
                        "Intervention Follow-Up Due",
                        message,
                        NotificationType.INTERVENTION_FOLLOW_UP,
                        intervention.getId(),
                        "INTERVENTION");
            } catch (Exception e) {
                log.error("Failed to process follow-up for intervention {}: {}",
                        intervention.getId(), e.getMessage());
            }
        }
        log.info("Follow-up processing completed. {} follow-ups processed.", dueFollowUps.size());
    }
}
