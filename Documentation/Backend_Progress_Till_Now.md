# Backend Progress Till Now

## Status: ALL PHASES COMPLETE ✅ | Compilation: PASSING (141 files, 0 errors)

---

## Phase 0 — Setup & Configuration ✅
- Added dependencies to `pom.xml`: OpenCSV 5.9, OpenPDF 2.0.3, spring-boot-starter-webflux
- Updated `application.properties`: ML service config, risk thresholds (low=35, medium=55), streak thresholds, file upload limits (10MB), pagination defaults
- Added `@EnableScheduling` to `HackathonAppApplication.java`
- Added `fullName` and `emailAlertsEnabled` fields to `User.java`
- Created 7 new enums: `RiskLabel`, `InterventionType`, `SubmissionStatus`, `AttendanceStatus`, `AttendanceEntryMode`, `NotificationType`, `ActionItemStatus`

## Phase 1 — Entities & Repositories ✅
- Created 20 new entities: Department, ClassEntity, Subject, SubjectClassMapping, SubjectTeacherMapping, StudentProfile, TeacherProfile, AttendanceSession, AttendanceRecord, IAMarks, Assignment, AssignmentSubmission, LMSScore, RiskScore, Intervention, ActionItem, StudentFlag, Notification, ExamSchedule, ConsistencyStreak
- Created 20 new repositories with custom queries
- Updated `UserRepository` with institute-scoped queries

## Phase 2 — DTOs, Structure Services & Controller ✅
- Created 10 request DTOs: AttendanceSessionRequest, IAMarksEntryRequest, CreateAssignmentRequest, MarkSubmissionRequest, LMSScoreBulkRequest, CreateInterventionRequest, FlagStudentRequest, WhatIfRequest, CreateExamScheduleRequest, StudentFilterRequest
- Created 13 response DTOs: PagedResponse, StudentProfileResponse, TeacherProfileResponse, RiskScoreResponse, RiskTrendResponse, WhatIfResponse, CsvUploadResponse, InstituteDashboardResponse, InterventionResponse, StudentFlagResponse, NotificationResponse, StudentAcademicDetailResponse, ConsistencyStreakResponse
- Created 3 structure services: DepartmentService, ClassService, SubjectService
- Created `CoordinatorController` at `/api/coordinator` with department, class, subject CRUD + mapping + CSV upload + template download endpoints
- Updated `SecurityConfig` with role-based access rules for `/api/teacher/**`, `/api/mentor/**`, `/api/student/**`, `/api/notifications/**`

## Phase 3 — CSV Upload ✅
- Created `CsvUploadService` with student and teacher CSV upload + validation + user creation + subject-teacher mapping + mentor assignment
- Integrated with `EmailService.sendTemporaryPasswordEmail()` for temp password delivery
- Uses `TokenGenerator.generateTemporaryPassword(12)` for secure password generation

## Phase 4 — Academic Data Entry ✅
- Created `AttendanceService`: session creation with PER_SESSION/BULK_PERCENTAGE modes, attendance percentage calculation
- Created `IAMarksService`: IA marks entry with normalization, average score calculation
- Created `AssignmentService`: assignment creation, submission marking, completion percentage
- Created `LMSScoreService`: bulk LMS score entry with upsert logic
- Created `TeacherController` at `/api/teacher` with endpoints for all academic data entry + flag student

## Phase 5 — Risk Engine & ML Integration ✅
- Created `AggregationService`: aggregates attendance, marks, assignment, LMS scores
- Created `MLServiceClient`: calls ML FastAPI `/api/predict`, falls back to weighted average (30% attendance, 30% marks, 25% assignment, 15% LMS)
- Created `RiskScoreService`: per-subject and overall risk computation, risk classification, trend data, institute-level queries
- Configured `WebClient` bean in `AppConfig` for ML service communication
- Created `NotificationService`, `StudentFlagService`, `InterventionService`
- Created `MentorController` at `/api/mentor` with mentee management, risk views, interventions, flags
- Created `StudentController` at `/api/student` with risk views and flag views
- Created `NotificationController` at `/api/notifications` with pagination, unread count, mark-all-read

---

## API Endpoint Summary

| Path | Role | Description |
|------|------|-------------|
| `/api/auth/**` | Public/Auth | Login, password reset, change password |
| `/api/coordinator/departments` | ACADEMIC_COORDINATOR | CRUD departments |
| `/api/coordinator/classes` | ACADEMIC_COORDINATOR | CRUD classes |
| `/api/coordinator/subjects` | ACADEMIC_COORDINATOR | CRUD subjects + mappings |
| `/api/coordinator/upload/students` | ACADEMIC_COORDINATOR | CSV upload students |
| `/api/coordinator/upload/teachers` | ACADEMIC_COORDINATOR | CSV upload teachers |
| `/api/coordinator/csv-templates/*` | ACADEMIC_COORDINATOR | Download CSV templates |
| `/api/teacher/attendance` | SUBJECT_TEACHER/FACULTY_MENTOR | Attendance entry |
| `/api/teacher/ia-marks` | SUBJECT_TEACHER/FACULTY_MENTOR | IA marks entry |
| `/api/teacher/assignments` | SUBJECT_TEACHER/FACULTY_MENTOR | Assignment management |
| `/api/teacher/lms-scores` | SUBJECT_TEACHER/FACULTY_MENTOR | LMS score entry |
| `/api/teacher/flag-student` | SUBJECT_TEACHER/FACULTY_MENTOR | Flag a student |
| `/api/teacher/my-subjects` | SUBJECT_TEACHER/FACULTY_MENTOR | Get assigned subjects |
| `/api/mentor/mentees` | FACULTY_MENTOR | View mentees with risk |
| `/api/mentor/mentees/{id}/risk` | FACULTY_MENTOR | View mentee risk details |
| `/api/mentor/interventions` | FACULTY_MENTOR | Create/view interventions |
| `/api/mentor/flags` | FACULTY_MENTOR | View/resolve student flags |
| `/api/mentor/compute-risk/{id}` | FACULTY_MENTOR | Trigger risk computation |
| `/api/student/my-risk` | STUDENT | View own risk score |
| `/api/student/my-risk-trend` | STUDENT | View risk trend |
| `/api/student/my-flags` | STUDENT | View flags on self |
| `/api/notifications` | Authenticated | Notifications CRUD |

---

## Phase 6 — AlertService ✅
- Created `AlertService`: threshold crossing alerts (HIGH risk), pre-exam alerts (14-day window), data entry reminders
- Added `sendAlertEmail()` to `EmailService` with styled HTML template
- Wired alerts into `RiskScoreService.computeOverallRisk()` — automatically triggers HIGH risk notifications to mentors + coordinators
- Email alerts respect `emailAlertsEnabled` flag on users

## Phase 7 — Dashboard Services ✅
- Created `CoordinatorDashboardService`: institute dashboard (student/teacher counts, risk distribution, department summaries), student list (paged), intervention effectiveness analysis
- Created `TeacherDashboardService`: subject summaries, mentees-at-risk count, upcoming exam alerts, subject analytics (class averages, at-risk students)
- Created `StudentDashboardService`: personal dashboard (risk factors, improvement tips, mentor info, consistency streak), academic data breakdown, intervention history
- Updated `InstituteDashboardResponse` DTO with proper fields

## Phase 8 — ConsistencyStreakService ✅
- Created `ConsistencyStreakService`: weekly streak tracking based on attendance/assignment/LMS thresholds
- Thresholds configurable: attendance ≥75%, assignment ≥80%, LMS ≥50%
- Batch update all students per institute

## Phase 9 — ExamSchedule Endpoints ✅
- Added `POST /api/coordinator/exam-schedules` and `GET /api/coordinator/exam-schedules` (with date range filter)

## Phase 10 — ScheduledJobService ✅
- Created `ScheduledJobService` with 4 scheduled jobs:
  - `nightlyRiskRecompute`: 2 AM daily — recomputes risk for all students across all institutes
  - `weeklyStreakUpdate`: Monday 8 AM — updates consistency streaks
  - `dailyAlertCheck`: 9 AM daily — sends pre-exam alerts
  - `processInterventionFollowUps`: 10 AM daily — notifies mentors of due follow-ups

## Phase 11 — PdfExportService ✅
- Created `PdfExportService` using OpenPDF:
  - `generateStudentRiskReport()`: institute-wide PDF with risk scores, color-coded labels
  - `generateStudentDetailReport()`: individual student PDF with academic data, subject breakdown, interventions
- Added export endpoints: `GET /api/coordinator/export/risk-report`, `GET /api/coordinator/export/student-report/{id}`

## Phase 12 — UserManagementService ✅
- Created `UserManagementService`: manual student/teacher creation, list students/teachers (paged), reassign mentor, deactivate/activate users
- Added endpoints: `POST /api/coordinator/students/manual`, `POST /api/coordinator/teachers/manual`, `GET /api/coordinator/teachers`, `PUT /api/coordinator/students/reassign-mentor`, `PUT /api/coordinator/users/{id}/deactivate`, `PUT /api/coordinator/users/{id}/activate`
- Created request DTOs: `ManualStudentRequest`, `ManualTeacherRequest`, `ReassignMentorRequest`

## Phase 13 — Remaining DTOs + Controller Endpoints ✅
- Created response DTOs: `TeacherDashboardResponse`, `StudentDashboardResponse`, `MentorDashboardResponse`, `SubjectAnalyticsResponse`, `InterventionEffectivenessResponse`
- Added dashboard endpoints to all controllers:
  - `GET /api/coordinator/dashboard`, `GET /api/coordinator/students`, `GET /api/coordinator/intervention-effectiveness`
  - `GET /api/teacher/dashboard`, `GET /api/teacher/subject-analytics`
  - `GET /api/mentor/dashboard`
  - `GET /api/student/dashboard`, `GET /api/student/academic-data`, `POST /api/student/what-if`, `GET /api/student/interventions`, `GET /api/student/consistency-streak`
- Added `POST /api/coordinator/subjects/map-teacher`, `POST /api/coordinator/recompute-risk`
- Added `TeacherProfileRepository.findByInstituteId(UUID, Pageable)` overload

## Phase 14 — ML FastAPI Predict Endpoint ✅
- Added `PredictRequest` and `PredictResponse` Pydantic models to `ML/ml/models.py`
- Added `POST /api/predict` endpoint to `ML/ml/main.py`:
  - Weighted average: 30% attendance + 30% marks + 25% assignment + 15% LMS
  - Risk = 100 - performance
  - Labels based on `outputs/thresholds.json` (low ≤ 35, medium ≤ 55, high > 55)

---

## Complete API Endpoint Summary

| Path | Role | Description |
|------|------|-------------|
| `/api/auth/**` | Public/Auth | Login, password reset, change password |
| **Coordinator** | | |
| `GET /api/coordinator/dashboard` | ACADEMIC_COORDINATOR | Institute-wide dashboard |
| `GET /api/coordinator/students` | ACADEMIC_COORDINATOR | Paged student list with risk |
| `GET /api/coordinator/teachers` | ACADEMIC_COORDINATOR | Paged teacher list |
| `GET /api/coordinator/intervention-effectiveness` | ACADEMIC_COORDINATOR | Intervention effectiveness analysis |
| `GET /api/coordinator/departments` | ACADEMIC_COORDINATOR | List departments |
| `POST /api/coordinator/departments` | ACADEMIC_COORDINATOR | Create department |
| `GET /api/coordinator/classes` | ACADEMIC_COORDINATOR | List classes |
| `POST /api/coordinator/classes` | ACADEMIC_COORDINATOR | Create class |
| `GET /api/coordinator/subjects` | ACADEMIC_COORDINATOR | List subjects |
| `POST /api/coordinator/subjects` | ACADEMIC_COORDINATOR | Create subject |
| `POST /api/coordinator/subjects/map-to-class` | ACADEMIC_COORDINATOR | Map subject to class |
| `POST /api/coordinator/subjects/map-teacher` | ACADEMIC_COORDINATOR | Map teacher to subject-class |
| `POST /api/coordinator/upload/students` | ACADEMIC_COORDINATOR | CSV upload students |
| `POST /api/coordinator/upload/teachers` | ACADEMIC_COORDINATOR | CSV upload teachers |
| `GET /api/coordinator/csv-templates/*` | ACADEMIC_COORDINATOR | Download CSV templates |
| `POST /api/coordinator/students/manual` | ACADEMIC_COORDINATOR | Add student manually |
| `POST /api/coordinator/teachers/manual` | ACADEMIC_COORDINATOR | Add teacher manually |
| `PUT /api/coordinator/students/reassign-mentor` | ACADEMIC_COORDINATOR | Reassign student's mentor |
| `PUT /api/coordinator/users/{id}/deactivate` | ACADEMIC_COORDINATOR | Deactivate user |
| `PUT /api/coordinator/users/{id}/activate` | ACADEMIC_COORDINATOR | Activate user |
| `POST /api/coordinator/exam-schedules` | ACADEMIC_COORDINATOR | Create exam schedule |
| `GET /api/coordinator/exam-schedules` | ACADEMIC_COORDINATOR | List exam schedules (date range) |
| `GET /api/coordinator/export/risk-report` | ACADEMIC_COORDINATOR | Export risk report PDF |
| `GET /api/coordinator/export/student-report/{id}` | ACADEMIC_COORDINATOR | Export student detail PDF |
| `POST /api/coordinator/recompute-risk` | ACADEMIC_COORDINATOR | Batch recompute all risk scores |
| **Teacher** | | |
| `GET /api/teacher/dashboard` | SUBJECT_TEACHER/FACULTY_MENTOR | Teacher dashboard |
| `GET /api/teacher/subject-analytics` | SUBJECT_TEACHER/FACULTY_MENTOR | Subject analytics |
| `POST /api/teacher/attendance` | SUBJECT_TEACHER/FACULTY_MENTOR | Attendance entry |
| `GET /api/teacher/attendance` | SUBJECT_TEACHER/FACULTY_MENTOR | View attendance sessions |
| `POST /api/teacher/ia-marks` | SUBJECT_TEACHER/FACULTY_MENTOR | IA marks entry |
| `GET /api/teacher/ia-marks` | SUBJECT_TEACHER/FACULTY_MENTOR | View IA marks |
| `POST /api/teacher/assignments` | SUBJECT_TEACHER/FACULTY_MENTOR | Create assignment |
| `POST /api/teacher/assignments/{id}/submissions` | SUBJECT_TEACHER/FACULTY_MENTOR | Mark submissions |
| `GET /api/teacher/assignments` | SUBJECT_TEACHER/FACULTY_MENTOR | View assignments |
| `POST /api/teacher/lms-scores` | SUBJECT_TEACHER/FACULTY_MENTOR | LMS score entry |
| `GET /api/teacher/lms-scores` | SUBJECT_TEACHER/FACULTY_MENTOR | View LMS scores |
| `GET /api/teacher/my-subjects` | SUBJECT_TEACHER/FACULTY_MENTOR | Get assigned subjects |
| `POST /api/teacher/flag-student` | SUBJECT_TEACHER/FACULTY_MENTOR | Flag a student |
| **Mentor** | | |
| `GET /api/mentor/dashboard` | FACULTY_MENTOR | Mentor dashboard |
| `GET /api/mentor/mentees` | FACULTY_MENTOR | View mentees with risk |
| `GET /api/mentor/mentees/{id}/risk` | FACULTY_MENTOR | View mentee risk details |
| `GET /api/mentor/mentees/{id}/risk-trend` | FACULTY_MENTOR | View mentee risk trend |
| `POST /api/mentor/interventions` | FACULTY_MENTOR | Create intervention |
| `GET /api/mentor/interventions` | FACULTY_MENTOR | List interventions |
| `PUT /api/mentor/interventions/action-items/{id}/complete` | FACULTY_MENTOR | Complete action item |
| `GET /api/mentor/flags` | FACULTY_MENTOR | View unresolved flags |
| `PUT /api/mentor/flags/{id}/resolve` | FACULTY_MENTOR | Resolve flag |
| `POST /api/mentor/compute-risk/{id}` | FACULTY_MENTOR | Trigger risk computation |
| **Student** | | |
| `GET /api/student/dashboard` | STUDENT | Personal dashboard |
| `GET /api/student/my-risk` | STUDENT | View own risk score |
| `GET /api/student/my-risk-trend` | STUDENT | View risk trend |
| `GET /api/student/my-flags` | STUDENT | View flags on self |
| `GET /api/student/academic-data` | STUDENT | View academic breakdown |
| `POST /api/student/what-if` | STUDENT | What-if risk simulation |
| `GET /api/student/interventions` | STUDENT | View intervention history |
| `GET /api/student/consistency-streak` | STUDENT | View consistency streak |
| **Notifications** | | |
| `GET /api/notifications` | Authenticated | List notifications (paged) |
| `GET /api/notifications/unread-count` | Authenticated | Unread count |
| `PUT /api/notifications/mark-all-read` | Authenticated | Mark all as read |

---

## Remaining Work (Not Yet Implemented)
- **Dashboard endpoints**: Institute-level dashboard with risk distribution, dept summaries
- **What-If analysis**: Endpoint for students/mentors to simulate score changes
- **Scheduled tasks**: Periodic risk recomputation, pre-exam alerts, data entry reminders
- **PDF report generation**: Using OpenPDF for student risk reports
- **Consistency Streak**: Weekly streak calculation logic
- **ML FastAPI update**: Add `/api/predict` endpoint to ML service
- **ExamSchedule endpoints**: CRUD for exam schedules
- **Email alerts**: Risk threshold notifications via email
