# Backend Build Context & Session Summary

## Overview
All 15 phases (0–14) of the Backend_Final_Plan.md have been implemented. The backend compiles successfully with 141 source files and 0 errors.

## Files Created/Modified in This Session (Phases 6–14)

### New Request DTOs (3)
- `dto/request/ManualStudentRequest.java`
- `dto/request/ManualTeacherRequest.java`
- `dto/request/ReassignMentorRequest.java`

### New Response DTOs (5)
- `dto/response/TeacherDashboardResponse.java` (with SubjectSummary, ExamAlertInfo)
- `dto/response/StudentDashboardResponse.java` (with RiskFactor)
- `dto/response/MentorDashboardResponse.java`
- `dto/response/SubjectAnalyticsResponse.java`
- `dto/response/InterventionEffectivenessResponse.java`

### New Services (7)
- `service/AlertService.java` — threshold crossing, pre-exam alerts, data entry reminders
- `service/ConsistencyStreakService.java` — weekly streak tracking
- `service/CoordinatorDashboardService.java` — institute dashboard, student lists, intervention effectiveness
- `service/TeacherDashboardService.java` — teacher dashboard, subject analytics
- `service/StudentDashboardService.java` — student dashboard, academic data, intervention history
- `service/ScheduledJobService.java` — 4 cron jobs (nightly risk, weekly streak, daily alerts, follow-ups)
- `service/PdfExportService.java` — risk report PDF, student detail PDF (OpenPDF)
- `service/UserManagementService.java` — manual user creation, deactivation, mentor reassignment

### Modified Files
- `service/EmailService.java` — added `sendAlertEmail()` method
- `service/RiskScoreService.java` — added alert wiring, `batchRecomputeAllStudents()`, `computeWhatIf()`
- `controller/CoordinatorController.java` — added ~20 new endpoints (dashboard, user mgmt, exam schedules, export, recompute)
- `controller/TeacherController.java` — added dashboard + subject analytics endpoints
- `controller/MentorController.java` — added dashboard endpoint with risk breakdown
- `controller/StudentController.java` — added dashboard, academic-data, what-if, interventions, streak endpoints
- `repository/TeacherProfileRepository.java` — added paged `findByInstituteId` overload
- `dto/response/InstituteDashboardResponse.java` — updated fields to match service
- `dto/response/ConsistencyStreakResponse.java` — changed lastQualifyingWeek to LocalDate

### ML Service Changes
- `ML/ml/models.py` — added `PredictRequest`, `PredictResponse` models
- `ML/ml/main.py` — added `POST /api/predict` endpoint with weighted-average risk scoring

## Documentation Files Summary
| File | Purpose |
|------|---------|
| `Backend_Final_Plan.md` | Original plan document — reference only |
| `Backend_Tentative_Plan.md` | Early draft plan — reference only |
| `Backend_Progress_Till_Now.md` | **Live progress tracker** — updated after each phase, contains full API endpoint table |
| `Backend_Build_Context.md` | **This file** — session context, lists all files created/modified |
| `Authentication_And_Authorization.md` | Pre-existing auth documentation |
| `Progress_Backend.md` | Pre-existing earlier progress notes |
| `Workflow.md` | Pre-existing workflow documentation |

## Key Architecture Decisions
- **Multi-tenant scoping**: All data queries scoped by `institute_id`
- **Scheduled jobs**: @Scheduled with cron — nightly risk recompute, weekly streaks, daily alerts, intervention follow-ups
- **Alert system**: Threshold crossing triggers in-app notifications + optional email alerts
- **PDF export**: OpenPDF library for generating styled PDFs with tables and color-coded risk labels
- **ML fallback**: If ML service is down, Java client computes risk using same weighted average formula
- **What-if simulation**: Students can hypothesize scores and see predicted risk changes
