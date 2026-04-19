package com.tarkshastra.app.service;

import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.NotificationType;
import com.tarkshastra.app.enums.RiskLabel;
import com.tarkshastra.app.enums.Role;
import com.tarkshastra.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final NotificationService notificationService;
    private final StudentProfileRepository studentProfileRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public void checkAndSendHighRiskAlert(UUID studentId, RiskLabel previousLabel, RiskLabel newLabel) {
        if (newLabel != RiskLabel.HIGH) return;
        if (previousLabel == RiskLabel.HIGH) return; // already high, no new alert

        StudentProfile sp = studentProfileRepository.findByUserId(studentId).orElse(null);
        if (sp == null) return;

        String studentName = sp.getFullName();
        String message = "Student " + studentName + " (" + sp.getRollNumber() + ") has crossed into HIGH risk.";

        // Notify mentor
        if (sp.getMentor() != null) {
            notificationService.sendNotification(
                    sp.getMentor(), "High Risk Alert", message,
                    NotificationType.HIGH_RISK_ALERT, studentId, "STUDENT");

            if (sp.getMentor().getEmailAlertsEnabled() != null && sp.getMentor().getEmailAlertsEnabled()) {
                try {
                    emailService.sendAlertEmail(sp.getMentor().getEmail(), "High Risk Alert", message);
                } catch (Exception e) {
                    log.warn("Failed to send high risk alert email: {}", e.getMessage());
                }
            }
        }

        // Notify coordinator(s)
        List<User> coordinators = userRepository.findAllByInstituteIdAndRole(
                sp.getInstitute().getId(), Role.ACADEMIC_COORDINATOR);
        for (User coord : coordinators) {
            notificationService.sendNotification(
                    coord, "High Risk Alert", message,
                    NotificationType.HIGH_RISK_ALERT, studentId, "STUDENT");
        }
    }

    public void sendPreExamAlerts(UUID instituteId) {
        LocalDate now = LocalDate.now();
        LocalDate twoWeeksAhead = now.plusDays(14);

        List<ExamSchedule> upcomingExams = examScheduleRepository.findByInstituteIdAndExamDateBetween(
                instituteId, now, twoWeeksAhead);

        for (ExamSchedule exam : upcomingExams) {
            List<StudentProfile> classStudents = studentProfileRepository.findByClassEntityId(
                    exam.getClassEntity().getId());

            for (StudentProfile sp : classStudents) {
                RiskScore risk = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(
                        sp.getUser().getId()).orElse(null);

                if (risk != null && risk.getRiskLabel() == RiskLabel.HIGH && sp.getMentor() != null) {
                    String message = "Exam alert: " + exam.getSubject().getName() + " exam on " +
                            exam.getExamDate() + ". Student " + sp.getFullName() + " is HIGH risk.";

                    notificationService.sendNotification(
                            sp.getMentor(), "Pre-Exam Alert", message,
                            NotificationType.PRE_EXAM_ALERT, sp.getUser().getId(), "STUDENT");

                    if (sp.getMentor().getEmailAlertsEnabled() != null && sp.getMentor().getEmailAlertsEnabled()) {
                        try {
                            emailService.sendAlertEmail(sp.getMentor().getEmail(), "Pre-Exam Alert", message);
                        } catch (Exception e) {
                            log.warn("Failed to send pre-exam alert: {}", e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public void sendDataEntryReminders(UUID instituteId) {
        // Find teachers in institute who haven't entered data recently
        List<User> teachers = userRepository.findAllByInstituteIdAndRole(instituteId, Role.SUBJECT_TEACHER);
        for (User teacher : teachers) {
            String message = "Reminder: Please ensure attendance, marks, and assignment data is up to date for your subjects.";
            notificationService.sendNotification(
                    teacher, "Data Entry Reminder", message,
                    NotificationType.DATA_ENTRY_REMINDER, null, null);

            if (teacher.getEmailAlertsEnabled() != null && teacher.getEmailAlertsEnabled()) {
                try {
                    emailService.sendAlertEmail(teacher.getEmail(), "Data Entry Reminder", message);
                } catch (Exception e) {
                    log.warn("Failed to send data entry reminder: {}", e.getMessage());
                }
            }
        }
    }
}
