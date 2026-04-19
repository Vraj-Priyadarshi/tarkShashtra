# Backend Final Plan — TarkShastra (TS-12) Early Academic Risk Detection Platform

## Document Purpose

This is the step-by-step backend implementation plan. It covers **every entity, enum, DTO, repository, service, controller, config, and integration** needed beyond what already exists (auth layer). Each section is designed to be implemented sequentially — completing one before moving to the next. No frontend or ML model code is included.

---

## Table of Contents

1. [Current State Summary](#1-current-state-summary)
2. [Confirmed Design Decisions](#2-confirmed-design-decisions)
3. [Dependencies to Add](#3-dependencies-to-add)
4. [Enums](#4-enums)
5. [Database Schema & Entity Design](#5-database-schema--entity-design)
6. [Entity Relationship Diagram](#6-entity-relationship-diagram)
7. [DTOs — Request & Response](#7-dtos--request--response)
8. [Repositories](#8-repositories)
9. [Services — Implementation Order](#9-services--implementation-order)
10. [Controllers — API Endpoints](#10-controllers--api-endpoints)
11. [Risk Engine — Aggregation & ML Integration](#11-risk-engine--aggregation--ml-integration)
12. [Scheduled Jobs](#12-scheduled-jobs)
13. [CSV Upload & Parsing](#13-csv-upload--parsing)
14. [PDF Export](#14-pdf-export)
15. [Notification System](#15-notification-system)
16. [Security & Authorization Rules](#16-security--authorization-rules)
17. [Implementation Phases — Step-by-Step Task List](#17-implementation-phases--step-by-step-task-list)
18. [Testing Strategy](#18-testing-strategy)

---

## 1. Current State Summary

### Already Implemented (Auth Layer — COMPLETE ✅)

| Layer | What Exists |
|---|---|
| **Entities** | `User` (UUID, email, passwordHash, Set\<Role\>, Institute FK, mustChangePassword, isActive), `Institute` (UUID, aisheCode, name), `PasswordResetToken` |
| **Enums** | `Role` (STUDENT, FACULTY_MENTOR, SUBJECT_TEACHER, ACADEMIC_COORDINATOR) |
| **DTOs** | `LoginRequest`, `ChangePasswordRequest`, `ForgotPasswordRequest`, `PasswordResetRequest`, `AuthResponse`, `UserResponse`, `MessageResponse`, `ErrorResponse` |
| **Repos** | `UserRepository`, `InstituteRepository`, `PasswordResetTokenRepository` |
| **Services** | `AuthService` (login, changePassword, forgotPassword, resetPassword), `UserService` (getUserById, getCurrentProfile), `EmailService` (reset email, temp password email) |
| **Controllers** | `AuthController` (/api/auth/*), `UserController` (/api/users/me) |
| **Security** | `JwtService`, `JwtAuthenticationFilter` (blocks endpoints if mustChangePassword=true), `JwtAuthenticationEntryPoint`, `CustomUserDetailsService` |
| **Config** | `SecurityConfig`, `CorsConfig`, `AppConfig` (empty), `DataSeeder` (3 hardcoded institutes + coordinators) |
| **Exceptions** | `BadRequestException`, `ResourceNotFoundException`, `EmailAlreadyExistsException`, `UnauthorizedException`, `GlobalExceptionHandler` |
| **Utils** | `Constants`, `TokenGenerator` |
| **Stack** | Spring Boot 4.0.5, Java 21, MySQL, jjwt 0.12.6, Lombok, SpringDoc OpenAPI 3.0.2, commons-lang3 3.14.0 |

### What Needs to Be Built

Everything below the auth layer — profiles, academic data entry, risk scoring, interventions, alerts, CSV upload, PDF export, ML integration, analytics, and the what-if calculator.

---

## 2. Confirmed Design Decisions

These were clarified and locked in before writing this plan:

| # | Decision | Choice |
|---|---|---|
| 1 | Department → Class structure | Department has many Classes. Each Class belongs to exactly one Department. |
| 2 | Subject scope | Subjects are **global** — assigned to Classes via a mapping table (`subject_class_mapping`). |
| 3 | Teacher-Subject-Class | One teacher can teach the same Subject to multiple Classes. Mapping table: `subject_teacher_mapping`. |
| 4 | Mentor assignment | Both — CSV initially sets `mentor_email`, Coordinator can reassign via UI later. |
| 5 | Student per class | One student belongs to exactly one Class at a time. |
| 6 | LMS score scope | **Per student per subject** — enables subject-level risk analysis. |
| 7 | Risk score computation | Backend calls **FastAPI ML service via REST** for risk scores. |
| 8 | What-If calculator | Calls the **same ML model via FastAPI** with hypothetical input data. |
| 9 | Consistency streak | **Composite** — attendance + assignments + LMS all above threshold per week. |
| 10 | Exam dates | **Per-subject** exam dates. |
| 11 | Notifications | **Both in-app (DB) and email**. |
| 12 | PDF library | **OpenPDF** (LGPL license). |
| 13 | CSV library | **OpenCSV**. |
| 14 | Semester | Number (1–8) stored on `StudentProfile`. |
| 15 | Risk score history | Store **latest + full history** (for trend charts). |
| 16 | Dual role (teacher+mentor) | Single `User` entity with multiple roles in `user_roles` set. No separate profile entity per role — one `TeacherProfile` serves both roles. |
| 17 | Profile entities | Separate `StudentProfile` and `TeacherProfile` entities (not on User). |
| 18 | Risk score per-subject | Store **both overall and per-subject** risk scores. |
| 19 | Intervention follow-up | **Auto-compare** — snapshot risk score at log time, compare vs current on follow-up date. |
| 20 | Batch recompute | **Both** — on-demand (every data save) + nightly @Scheduled batch. |
| 21 | Assignment due dates | Yes — store due date per assignment for late submission tracking. |
| 22 | Attendance entry | Both per-session marking AND bulk % entry supported. |

---

## 3. Dependencies to Add

Add these to `pom.xml` inside `<dependencies>`:

```xml
<!-- CSV Parsing — OpenCSV -->
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.9</version>
</dependency>

<!-- PDF Generation — OpenPDF -->
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>2.0.3</version>
</dependency>

<!-- REST Client — Spring WebClient (for calling FastAPI ML service) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Scheduling support (already included via spring-boot-starter, but ensure @EnableScheduling is added) -->
```

### application.properties additions

```properties
# ML Service
ml.service.base-url=http://localhost:8000
ml.service.timeout-ms=10000

# Risk thresholds (defaults — coordinator can override per institute)
risk.threshold.low=35.0
risk.threshold.medium=55.0

# Consistency streak thresholds
streak.attendance-threshold=75.0
streak.assignment-threshold=80.0
streak.lms-threshold=50.0

# File upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Pagination defaults
pagination.default-page-size=20
pagination.max-page-size=100
```

---

## 4. Enums

### 4.1 Existing — No Changes

```
Role.java → STUDENT, FACULTY_MENTOR, SUBJECT_TEACHER, ACADEMIC_COORDINATOR
```

### 4.2 New Enums to Create

All in package `com.tarkshastra.app.enums`

#### `RiskLabel.java`
```
Values: LOW, MEDIUM, HIGH
```
Used in: RiskScore entity, risk computation responses.

#### `InterventionType.java`
```
Values: COUNSELLING_SESSION, REMEDIAL_CLASS, ASSIGNMENT_EXTENSION, PARENT_MEETING, OTHER
```
Used in: Intervention entity.

#### `SubmissionStatus.java`
```
Values: SUBMITTED, NOT_SUBMITTED, LATE
```
Used in: AssignmentSubmission entity.

#### `AttendanceStatus.java`
```
Values: PRESENT, ABSENT
```
Used in: AttendanceRecord entity.

#### `AttendanceEntryMode.java`
```
Values: PER_SESSION, BULK_PERCENTAGE
```
Used in: AttendanceSession entity — tracks how attendance was entered.

#### `NotificationType.java`
```
Values: HIGH_RISK_ALERT, RISK_THRESHOLD_CROSSED, PRE_EXAM_ALERT, DATA_ENTRY_REMINDER, 
        INTERVENTION_FOLLOW_UP, STUDENT_FLAGGED, GENERAL
```
Used in: Notification entity.

#### `ActionItemStatus.java`
```
Values: PENDING, COMPLETED
```
Used in: ActionItem entity.

---

## 5. Database Schema & Entity Design

### Naming Conventions

- Table names: `snake_case`, plural (e.g., `student_profiles`, `risk_scores`)
- Column names: `snake_case` (e.g., `full_name`, `institute_id`)
- FK columns: `<referenced_entity>_id` (e.g., `department_id`, `student_id`)
- All entities use `UUID` as primary key
- All entities have `created_at` (`@CreationTimestamp`) and `updated_at` (`@UpdateTimestamp`) unless noted otherwise
- Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` on all entities

---

### 5.1 `Department` — Table: `departments`

Represents an academic department within an institute (e.g., IT, CE, ME).

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `name` | `String` | `name` | not null, length 100 | e.g., "Information Technology" |
| `code` | `String` | `code` | not null, length 20 | e.g., "IT", "CE" |
| `institute` | `Institute` | `institute_id` | FK, not null, `@ManyToOne(LAZY)` | Scoping — each dept belongs to one institute |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:** `idx_dept_institute_code` on (`institute_id`, `code`) — UNIQUE composite.

**Relationships:**
- `@OneToMany(mappedBy = "department")` → `List<ClassEntity> classes`
- `@OneToMany(mappedBy = "department")` → `List<Subject> subjects` *(optional back-ref)*

---

### 5.2 `ClassEntity` — Table: `classes`

Represents a class/batch within a department (e.g., SE-A, TE-B). Named `ClassEntity` in Java because `Class` is a reserved word.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `name` | `String` | `name` | not null, length 50 | e.g., "SE-A", "TE-B" |
| `semester` | `Integer` | `semester` | not null | Current semester (1–8) |
| `academicYear` | `String` | `academic_year` | not null, length 20 | e.g., "2025-26" |
| `department` | `Department` | `department_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `institute` | `Institute` | `institute_id` | FK, not null, `@ManyToOne(LAZY)` | Denormalized for query performance |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:** `idx_class_dept_name_year` on (`department_id`, `name`, `academic_year`) — UNIQUE composite.

---

### 5.3 `Subject` — Table: `subjects`

A global subject that can be assigned to multiple classes via `SubjectClassMapping`.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `name` | `String` | `name` | not null, length 150 | e.g., "Data Structures" |
| `code` | `String` | `code` | not null, length 20 | e.g., "CS301" |
| `department` | `Department` | `department_id` | FK, not null, `@ManyToOne(LAZY)` | Which department owns this subject |
| `institute` | `Institute` | `institute_id` | FK, not null, `@ManyToOne(LAZY)` | Denormalized for query performance |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:** `idx_subject_institute_code` on (`institute_id`, `code`) — UNIQUE composite.

---

### 5.4 `SubjectClassMapping` — Table: `subject_class_mappings`

Maps which subjects are taught in which classes.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `subject` | `Subject` | `subject_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `classEntity` | `ClassEntity` | `class_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `semester` | `Integer` | `semester` | not null | Which semester this mapping applies to |
| `academicYear` | `String` | `academic_year` | not null, length 20 | |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |

**Indexes:** `idx_scm_subject_class` on (`subject_id`, `class_id`, `academic_year`) — UNIQUE composite.

---

### 5.5 `SubjectTeacherMapping` — Table: `subject_teacher_mappings`

Maps which teacher teaches which subject in which class.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `teacher` | `User` | `teacher_id` | FK, not null, `@ManyToOne(LAZY)` | The User with SUBJECT_TEACHER role |
| `subject` | `Subject` | `subject_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `classEntity` | `ClassEntity` | `class_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `academicYear` | `String` | `academic_year` | not null, length 20 | |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |

**Indexes:** `idx_stm_teacher_subject_class` on (`teacher_id`, `subject_id`, `class_id`, `academic_year`) — UNIQUE composite.

**Used for:** Controlling what subjects/classes a teacher sees on their dashboard. When teacher hits "Select Subject" → dropdown queries this table filtered by `teacher_id`.

---

### 5.6 `StudentProfile` — Table: `student_profiles`

Extended profile for students. Separate from User to keep auth entity clean.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `user` | `User` | `user_id` | FK, not null, UNIQUE, `@OneToOne(LAZY)` | Links to auth User entity |
| `fullName` | `String` | `full_name` | not null, length 150 | |
| `rollNumber` | `String` | `roll_number` | not null, length 30 | Institution-specific student ID |
| `department` | `Department` | `department_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `classEntity` | `ClassEntity` | `class_id` | FK, not null, `@ManyToOne(LAZY)` | Current class |
| `semester` | `Integer` | `semester` | not null | Current semester (1–8) |
| `mentor` | `User` | `mentor_id` | FK, nullable, `@ManyToOne(LAZY)` | The User with FACULTY_MENTOR role |
| `institute` | `Institute` | `institute_id` | FK, not null, `@ManyToOne(LAZY)` | Denormalized |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:**
- `idx_sp_user` on (`user_id`) — UNIQUE
- `idx_sp_roll_institute` on (`roll_number`, `institute_id`) — UNIQUE
- `idx_sp_mentor` on (`mentor_id`)
- `idx_sp_class` on (`class_id`)
- `idx_sp_department` on (`department_id`)

---

### 5.7 `TeacherProfile` — Table: `teacher_profiles`

Extended profile for teachers (both subject teachers and mentors).

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `user` | `User` | `user_id` | FK, not null, UNIQUE, `@OneToOne(LAZY)` | Links to auth User entity |
| `fullName` | `String` | `full_name` | not null, length 150 | |
| `employeeId` | `String` | `employee_id` | not null, length 30 | Institution-specific employee ID |
| `department` | `Department` | `department_id` | FK, not null, `@ManyToOne(LAZY)` | Primary department |
| `institute` | `Institute` | `institute_id` | FK, not null, `@ManyToOne(LAZY)` | Denormalized |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:**
- `idx_tp_user` on (`user_id`) — UNIQUE
- `idx_tp_emp_institute` on (`employee_id`, `institute_id`) — UNIQUE

---

### 5.8 `AttendanceSession` — Table: `attendance_sessions`

Represents a single lecture/session for which attendance is taken. Also used for bulk entries.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `subject` | `Subject` | `subject_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `classEntity` | `ClassEntity` | `class_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `teacher` | `User` | `teacher_id` | FK, not null, `@ManyToOne(LAZY)` | Who entered this |
| `sessionDate` | `LocalDate` | `session_date` | not null | Date of the lecture |
| `entryMode` | `AttendanceEntryMode` | `entry_mode` | not null, `@Enumerated(STRING)` | PER_SESSION or BULK_PERCENTAGE |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |

**Indexes:** `idx_as_subject_class_date` on (`subject_id`, `class_id`, `session_date`) — for duplicate prevention and lookups.

**Relationships:**
- `@OneToMany(mappedBy = "attendanceSession", cascade = ALL, orphanRemoval = true)` → `List<AttendanceRecord> records`

---

### 5.9 `AttendanceRecord` — Table: `attendance_records`

Individual student attendance for a session. For PER_SESSION mode, stores PRESENT/ABSENT. For BULK_PERCENTAGE mode, only a `bulkPercentage` value per student is stored (the session has one record per student with the percentage).

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `attendanceSession` | `AttendanceSession` | `attendance_session_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `student` | `User` | `student_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `status` | `AttendanceStatus` | `status` | nullable, `@Enumerated(STRING)` | Used in PER_SESSION mode (PRESENT/ABSENT) |
| `bulkPercentage` | `Double` | `bulk_percentage` | nullable | Used in BULK_PERCENTAGE mode (0.0–100.0) |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |

**Indexes:** `idx_ar_session_student` on (`attendance_session_id`, `student_id`) — UNIQUE composite.

**Note:** Exactly one of `status` or `bulkPercentage` is populated depending on `entryMode` in the parent session.

---

### 5.10 `IAMarks` — Table: `ia_marks`

Internal Assessment marks per student per subject per round.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `student` | `User` | `student_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `subject` | `Subject` | `subject_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `classEntity` | `ClassEntity` | `class_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `teacher` | `User` | `teacher_id` | FK, not null, `@ManyToOne(LAZY)` | Who entered this |
| `iaRound` | `String` | `ia_round` | not null, length 20 | e.g., "IA-1", "IA-2", "Mid-sem" |
| `maxMarks` | `Double` | `max_marks` | not null | Max obtainable (e.g., 30) |
| `obtainedMarks` | `Double` | `obtained_marks` | not null | Can be 0 for absent |
| `isAbsent` | `Boolean` | `is_absent` | not null, default false | True if student was absent |
| `normalizedScore` | `Double` | `normalized_score` | not null | (obtained / max) × 100 — computed on save |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:** `idx_ia_student_subject_round` on (`student_id`, `subject_id`, `ia_round`) — UNIQUE composite.

---

### 5.11 `Assignment` — Table: `assignments`

Represents an assignment created by a teacher for a subject+class.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `title` | `String` | `title` | not null, length 200 | Assignment name |
| `subject` | `Subject` | `subject_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `classEntity` | `ClassEntity` | `class_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `teacher` | `User` | `teacher_id` | FK, not null, `@ManyToOne(LAZY)` | Who created it |
| `dueDate` | `LocalDate` | `due_date` | nullable | For tracking late submissions |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:** `idx_assgn_subject_class` on (`subject_id`, `class_id`).

**Relationships:**
- `@OneToMany(mappedBy = "assignment", cascade = ALL, orphanRemoval = true)` → `List<AssignmentSubmission> submissions`

---

### 5.12 `AssignmentSubmission` — Table: `assignment_submissions`

Tracks whether each student submitted a specific assignment.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `assignment` | `Assignment` | `assignment_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `student` | `User` | `student_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `status` | `SubmissionStatus` | `status` | not null, `@Enumerated(STRING)` | SUBMITTED / NOT_SUBMITTED / LATE |
| `submittedAt` | `LocalDateTime` | `submitted_at` | nullable | When the status was last updated |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:** `idx_asub_assignment_student` on (`assignment_id`, `student_id`) — UNIQUE composite.

---

### 5.13 `LMSScore` — Table: `lms_scores`

LMS engagement score per student per subject (0–100).

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `student` | `User` | `student_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `subject` | `Subject` | `subject_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `classEntity` | `ClassEntity` | `class_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `teacher` | `User` | `teacher_id` | FK, not null, `@ManyToOne(LAZY)` | Who entered it |
| `score` | `Double` | `score` | not null | 0.0–100.0 |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:** `idx_lms_student_subject` on (`student_id`, `subject_id`) — UNIQUE composite. (Latest score per student per subject; update in place.)

---

### 5.14 `RiskScore` — Table: `risk_scores`

Stores computed risk scores — both overall and per-subject. Full history retained for trend charts.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `student` | `User` | `student_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `subject` | `Subject` | `subject_id` | FK, nullable, `@ManyToOne(LAZY)` | NULL = overall risk score; non-null = per-subject |
| `attendanceScore` | `Double` | `attendance_score` | not null | Aggregated attendance % used as input |
| `marksScore` | `Double` | `marks_score` | not null | Aggregated marks % used as input |
| `assignmentScore` | `Double` | `assignment_score` | not null | Aggregated assignment completion % used as input |
| `lmsScore` | `Double` | `lms_score` | not null | LMS score used as input |
| `riskScore` | `Double` | `risk_score` | not null | ML model output (0–100) |
| `riskLabel` | `RiskLabel` | `risk_label` | not null, `@Enumerated(STRING)` | LOW / MEDIUM / HIGH |
| `isLatest` | `Boolean` | `is_latest` | not null, default true | True for the most recent computation |
| `computedAt` | `LocalDateTime` | `computed_at` | not null | When this score was computed |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |

**Indexes:**
- `idx_rs_student_latest` on (`student_id`, `is_latest`) — for quickly fetching current risk
- `idx_rs_student_subject` on (`student_id`, `subject_id`, `is_latest`)
- `idx_rs_student_computed` on (`student_id`, `computed_at`) — for trend chart queries
- `idx_rs_institute_label` on (`risk_label`) — for coordinator dashboard aggregations

**Strategy for `isLatest`:**
When a new risk score is computed for a student (overall or per-subject), first update all previous records for that student+subject combo: `SET is_latest = false`, then insert the new record with `is_latest = true`. This avoids MAX(computed_at) subqueries.

---

### 5.15 `Intervention` — Table: `interventions`

Logged by mentor for their assigned mentees.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `student` | `User` | `student_id` | FK, not null, `@ManyToOne(LAZY)` | The mentee |
| `mentor` | `User` | `mentor_id` | FK, not null, `@ManyToOne(LAZY)` | The mentor who logged it |
| `interventionType` | `InterventionType` | `intervention_type` | not null, `@Enumerated(STRING)` | |
| `interventionDate` | `LocalDate` | `intervention_date` | not null | When it happened |
| `remarks` | `String` | `remarks` | nullable, `@Column(columnDefinition = "TEXT")` | What was discussed |
| `followUpDate` | `LocalDate` | `follow_up_date` | nullable | Reminder for mentor |
| `preRiskScore` | `Double` | `pre_risk_score` | nullable | Snapshot of overall risk at time of logging |
| `postRiskScore` | `Double` | `post_risk_score` | nullable | Filled in after follow-up date when auto-compared |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:**
- `idx_int_student` on (`student_id`)
- `idx_int_mentor` on (`mentor_id`)
- `idx_int_followup` on (`follow_up_date`) — for scheduled follow-up job

**Relationships:**
- `@OneToMany(mappedBy = "intervention", cascade = ALL, orphanRemoval = true)` → `List<ActionItem> actionItems`

---

### 5.16 `ActionItem` — Table: `action_items`

Action items set by mentor during an intervention.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `intervention` | `Intervention` | `intervention_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `description` | `String` | `description` | not null, length 500 | e.g., "Submit pending assignment by Friday" |
| `status` | `ActionItemStatus` | `status` | not null, default PENDING, `@Enumerated(STRING)` | PENDING / COMPLETED |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

---

### 5.17 `StudentFlag` — Table: `student_flags`

Subject teachers flag a student for their mentor's attention.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `student` | `User` | `student_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `flaggedBy` | `User` | `flagged_by_id` | FK, not null, `@ManyToOne(LAZY)` | Subject teacher who flagged |
| `subject` | `Subject` | `subject_id` | FK, not null, `@ManyToOne(LAZY)` | The subject where student is struggling |
| `note` | `String` | `note` | nullable, length 500 | e.g., "Struggling in Data Structures" |
| `isResolved` | `Boolean` | `is_resolved` | not null, default false | Mentor marks resolved after action |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:**
- `idx_sf_student` on (`student_id`)
- `idx_sf_flaggedby` on (`flagged_by_id`)

---

### 5.18 `Notification` — Table: `notifications`

In-app notifications for all user types.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `recipient` | `User` | `recipient_id` | FK, not null, `@ManyToOne(LAZY)` | Who receives this |
| `title` | `String` | `title` | not null, length 200 | Short title |
| `message` | `String` | `message` | not null, `@Column(columnDefinition = "TEXT")` | Full message body |
| `notificationType` | `NotificationType` | `notification_type` | not null, `@Enumerated(STRING)` | |
| `isRead` | `Boolean` | `is_read` | not null, default false | |
| `referenceId` | `UUID` | `reference_id` | nullable | Generic FK to related entity (student, intervention, etc.) |
| `referenceType` | `String` | `reference_type` | nullable, length 50 | e.g., "STUDENT", "INTERVENTION" — for frontend routing |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |

**Indexes:**
- `idx_notif_recipient_read` on (`recipient_id`, `is_read`) — for unread count badge
- `idx_notif_recipient_created` on (`recipient_id`, `created_at` DESC) — for listing

---

### 5.19 `ExamSchedule` — Table: `exam_schedules`

Per-subject exam dates set by the Coordinator.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `subject` | `Subject` | `subject_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `classEntity` | `ClassEntity` | `class_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `institute` | `Institute` | `institute_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `examDate` | `LocalDate` | `exam_date` | not null | Date of the exam |
| `examType` | `String` | `exam_type` | not null, length 50 | e.g., "Mid-sem", "End-sem", "IA-1" |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |

**Indexes:** `idx_es_subject_class_type` on (`subject_id`, `class_id`, `exam_type`) — UNIQUE composite.

---

### 5.20 `ConsistencyStreak` — Table: `consistency_streaks`

Tracks weekly consistency streaks per student.

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto UUID | |
| `student` | `User` | `student_id` | FK, not null, `@ManyToOne(LAZY)` | |
| `currentStreak` | `Integer` | `current_streak` | not null, default 0 | Consecutive qualifying weeks |
| `longestStreak` | `Integer` | `longest_streak` | not null, default 0 | All-time best |
| `lastQualifyingWeek` | `LocalDate` | `last_qualifying_week` | nullable | Start of the last qualifying week (Monday) |
| `updatedAt` | `LocalDateTime` | `updated_at` | `@UpdateTimestamp` | |
| `createdAt` | `LocalDateTime` | `created_at` | `@CreationTimestamp` | |

**Indexes:** `idx_cs_student` on (`student_id`) — UNIQUE.

**Qualification rule:** A week qualifies if:
- Attendance >= `streak.attendance-threshold` (default 75%) across all subjects
- Assignment completion >= `streak.assignment-threshold` (default 80%) across all subjects
- LMS score >= `streak.lms-threshold` (default 50%) across all subjects

---

### 5.21 Modifications to Existing `User` Entity

Add these fields to the existing User.java:

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `fullName` | `String` | `full_name` | nullable, length 150 | Convenience field — populated from profile on creation. Allows name display without joining profiles. |

**Why?** Avoids joining StudentProfile/TeacherProfile for every list view. Set once on CSV upload.

---

## 6. Entity Relationship Diagram

```
Institute (1) ──── (many) Department
Institute (1) ──── (many) User
Institute (1) ──── (many) ClassEntity
Institute (1) ──── (many) Subject

Department (1) ──── (many) ClassEntity
Department (1) ──── (many) Subject
Department (1) ──── (many) StudentProfile
Department (1) ──── (many) TeacherProfile

ClassEntity (1) ──── (many) StudentProfile
ClassEntity (1) ──── (many) SubjectClassMapping
ClassEntity (1) ──── (many) SubjectTeacherMapping
ClassEntity (1) ──── (many) AttendanceSession
ClassEntity (1) ──── (many) Assignment
ClassEntity (1) ──── (many) ExamSchedule

Subject (1) ──── (many) SubjectClassMapping
Subject (1) ──── (many) SubjectTeacherMapping
Subject (1) ──── (many) AttendanceSession
Subject (1) ──── (many) Assignment
Subject (1) ──── (many) LMSScore
Subject (1) ──── (many) RiskScore (per-subject)
Subject (1) ──── (many) IAMarks
Subject (1) ──── (many) StudentFlag
Subject (1) ──── (many) ExamSchedule

User (1:STUDENT) ──── (1) StudentProfile
User (1:TEACHER) ──── (1) TeacherProfile
User (1:STUDENT) ──── (many) AttendanceRecord
User (1:STUDENT) ──── (many) IAMarks
User (1:STUDENT) ──── (many) AssignmentSubmission
User (1:STUDENT) ──── (many) LMSScore
User (1:STUDENT) ──── (many) RiskScore
User (1:STUDENT) ──── (many) Intervention (as student)
User (1:STUDENT) ──── (many) StudentFlag (as flagged student)
User (1:STUDENT) ──── (1) ConsistencyStreak
User (1:STUDENT) ──── (many) Notification
User (1:MENTOR) ──── (many) StudentProfile (as mentor)
User (1:MENTOR) ──── (many) Intervention (as mentor)
User (1:TEACHER) ──── (many) SubjectTeacherMapping
User (1:TEACHER) ──── (many) AttendanceSession (as creator)
User (1:TEACHER) ──── (many) Assignment (as creator)
User (1:TEACHER) ──── (many) StudentFlag (as flagger)

AttendanceSession (1) ──── (many) AttendanceRecord
Assignment (1) ──── (many) AssignmentSubmission
Intervention (1) ──── (many) ActionItem
```

---

## 7. DTOs — Request & Response

### 7.1 Request DTOs

Package: `com.tarkshastra.app.dto.request`

#### CSV Upload

**`StudentCsvRow`** — Represents one row from student CSV
```
Fields: rollNumber, fullName, email, departmentCode, className, semester(int), mentorEmail
```

**`TeacherCsvRow`** — Represents one row from teacher CSV
```
Fields: employeeId, fullName, email, departmentCode, subjectsTaught (comma-separated subject codes), mentorTo (comma-separated student roll numbers)
```

#### Attendance

**`AttendanceSessionRequest`**
```
Fields: subjectId (UUID), classId (UUID), sessionDate (LocalDate), entryMode (AttendanceEntryMode), records (List<AttendanceRecordRequest>)
```

**`AttendanceRecordRequest`**
```
Fields: studentId (UUID), status (AttendanceStatus — for PER_SESSION), bulkPercentage (Double — for BULK)
```

#### IA Marks

**`IAMarksEntryRequest`**
```
Fields: subjectId (UUID), classId (UUID), iaRound (String), maxMarks (Double), entries (List<IAMarkEntry>)
```

**`IAMarkEntry`**
```
Fields: studentId (UUID), obtainedMarks (Double), isAbsent (boolean)
```

#### Assignments

**`CreateAssignmentRequest`**
```
Fields: subjectId (UUID), classId (UUID), title (String), dueDate (LocalDate — optional)
```

**`MarkSubmissionRequest`**
```
Fields: assignmentId (UUID), submissions (List<SubmissionEntry>)
```

**`SubmissionEntry`**
```
Fields: studentId (UUID), status (SubmissionStatus)
```

#### LMS Score

**`LMSScoreBulkRequest`**
```
Fields: subjectId (UUID), classId (UUID), entries (List<LMSScoreEntry>)
```

**`LMSScoreEntry`**
```
Fields: studentId (UUID), score (Double — 0 to 100)
```

#### Intervention

**`CreateInterventionRequest`**
```
Fields: studentId (UUID), interventionType (InterventionType), interventionDate (LocalDate), remarks (String), followUpDate (LocalDate — optional), actionItems (List<String> — descriptions)
```

#### Student Flag

**`FlagStudentRequest`**
```
Fields: studentId (UUID), subjectId (UUID), note (String — optional)
```

#### What-If Calculator

**`WhatIfRequest`**
```
Fields: studentId (UUID), hypotheticalSubjects (List<WhatIfSubjectEntry>)
```

**`WhatIfSubjectEntry`**
```
Fields: subjectId (UUID), attendance (Double), marks (Double), assignment (Double), lms (Double)
```

#### Exam Schedule

**`CreateExamScheduleRequest`**
```
Fields: subjectId (UUID), classId (UUID), examDate (LocalDate), examType (String)
```

#### Notification Preferences

**`NotificationPreferenceRequest`**
```
Fields: emailAlertsEnabled (boolean)
```

#### Mentor Reassignment

**`ReassignMentorRequest`**
```
Fields: studentId (UUID), newMentorId (UUID)
```

#### Filters (reusable)

**`StudentFilterRequest`**
```
Fields: departmentId (UUID — optional), classId (UUID — optional), riskLabel (RiskLabel — optional), search (String — optional, name/roll search), page (int), size (int), sortBy (String), sortDir (String)
```

---

### 7.2 Response DTOs

Package: `com.tarkshastra.app.dto.response`

#### Profiles

**`StudentProfileResponse`**
```
Fields: id, userId, fullName, email, rollNumber, departmentName, className, semester, mentorName, mentorEmail, riskScore (Double), riskLabel (RiskLabel), attendancePercentage, isActive
```

**`TeacherProfileResponse`**
```
Fields: id, userId, fullName, email, employeeId, departmentName, roles (Set<Role>), isActive
```

#### Dashboard — Coordinator

**`InstituteDashboardResponse`**
```
Fields: totalStudents (long), totalTeachers (long), riskDistribution (Map<RiskLabel, Long>), avgRiskScore (Double), interventionCount (long), departmentRiskSummary (List<DepartmentRiskSummary>)
```

**`DepartmentRiskSummary`**
```
Fields: departmentName, departmentId, totalStudents, highRiskCount, mediumRiskCount, lowRiskCount, avgRiskScore
```

#### Dashboard — Teacher

**`TeacherDashboardResponse`**
```
Fields: subjects (List<SubjectSummary>), menteesAtRisk (int), pendingDataEntryCount (int), upcomingExamAlert (ExamAlertInfo — nullable)
```

**`SubjectSummary`**
```
Fields: subjectId, subjectName, subjectCode, className, classId
```

**`ExamAlertInfo`**
```
Fields: subjectName, examDate, daysUntilExam, highRiskMenteeCount
```

#### Dashboard — Student

**`StudentDashboardResponse`**
```
Fields: fullName, rollNumber, semester, branch, riskScore, riskLabel, topContributingFactors (List<RiskFactor>), improvementTips (List<String>), mentorName, mentorEmail, consistencyStreak (ConsistencyStreakResponse)
```

**`RiskFactor`**
```
Fields: factor (String — e.g., "Attendance"), value (Double), classAverage (Double), contributionPercentage (Double)
```

**`ConsistencyStreakResponse`**
```
Fields: currentStreak, longestStreak, lastQualifyingWeek
```

#### Academic Data

**`SubjectAcademicDataResponse`**
```
Fields: subjectName, subjectCode, attendancePercentage, iaMarksNormalized, assignmentCompletionPercentage, lmsScore, isAboveThreshold (boolean per field)
```

**`StudentAcademicDetailResponse`**
```
Fields: studentId, fullName, rollNumber, overallAttendance, overallMarks, overallAssignment, overallLms, overallRiskScore, overallRiskLabel, subjects (List<SubjectAcademicDataResponse>)
```

#### Risk Score

**`RiskScoreResponse`**
```
Fields: studentId, fullName, riskScore, riskLabel, attendanceScore, marksScore, assignmentScore, lmsScore, computedAt, subjectRisks (List<SubjectRiskResponse>)
```

**`SubjectRiskResponse`**
```
Fields: subjectId, subjectName, riskScore, riskLabel, attendance, marks, assignment, lms
```

**`RiskTrendResponse`**
```
Fields: studentId, dataPoints (List<RiskTrendPoint>)
```

**`RiskTrendPoint`**
```
Fields: date (LocalDate), riskScore (Double), riskLabel (RiskLabel)
```

#### Intervention

**`InterventionResponse`**
```
Fields: id, studentName, studentId, mentorName, interventionType, interventionDate, remarks, followUpDate, preRiskScore, postRiskScore, scoreChange (Double — computed), actionItems (List<ActionItemResponse>), createdAt
```

**`ActionItemResponse`**
```
Fields: id, description, status
```

#### Student Flag

**`StudentFlagResponse`**
```
Fields: id, studentName, studentId, flaggedByName, subjectName, note, isResolved, createdAt
```

#### Notification

**`NotificationResponse`**
```
Fields: id, title, message, notificationType, isRead, referenceId, referenceType, createdAt
```

**`UnreadCountResponse`**
```
Fields: unreadCount (long)
```

#### CSV Upload

**`CsvUploadResponse`**
```
Fields: totalRows (int), successCount (int), errorCount (int), errors (List<CsvRowError>)
```

**`CsvRowError`**
```
Fields: rowNumber (int), field (String), message (String)
```

#### What-If

**`WhatIfResponse`**
```
Fields: currentRiskScore, currentRiskLabel, predictedRiskScore, predictedRiskLabel, subjectPredictions (List<SubjectRiskResponse>)
```

#### Analytics

**`InterventionEffectivenessResponse`**
```
Fields: interventionType, count, avgPreScore, avgPostScore, avgImprovement
```

#### Pagination Wrapper (generic)

**`PagedResponse<T>`**
```
Fields: content (List<T>), page (int), size (int), totalElements (long), totalPages (int), isLast (boolean)
```

---

## 8. Repositories

Package: `com.tarkshastra.app.repository`

All extend `JpaRepository<Entity, UUID>`. Only custom query methods listed below.

### Existing (no changes needed)
- `UserRepository` — `findByEmail`, `existsByEmail`
- `InstituteRepository` — `findByAisheCode`, `existsByAisheCode`
- `PasswordResetTokenRepository` — `findByToken`, `findByUserAndIsUsedFalse`, `deleteByExpiryDateLessThan`, `deleteByUser`

### New Repositories

**`DepartmentRepository`**
```
- Optional<Department> findByInstituteIdAndCode(UUID instituteId, String code)
- List<Department> findByInstituteId(UUID instituteId)
- boolean existsByInstituteIdAndCode(UUID instituteId, String code)
```

**`ClassEntityRepository`**
```
- Optional<ClassEntity> findByDepartmentIdAndNameAndAcademicYear(UUID deptId, String name, String year)
- List<ClassEntity> findByDepartmentId(UUID deptId)
- List<ClassEntity> findByInstituteId(UUID instituteId)
- boolean existsByDepartmentIdAndNameAndAcademicYear(UUID deptId, String name, String year)
```

**`SubjectRepository`**
```
- Optional<Subject> findByInstituteIdAndCode(UUID instituteId, String code)
- List<Subject> findByInstituteId(UUID instituteId)
- List<Subject> findByDepartmentId(UUID deptId)
- boolean existsByInstituteIdAndCode(UUID instituteId, String code)
```

**`SubjectClassMappingRepository`**
```
- List<SubjectClassMapping> findByClassEntityId(UUID classId)
- List<SubjectClassMapping> findBySubjectId(UUID subjectId)
- boolean existsBySubjectIdAndClassEntityIdAndAcademicYear(UUID subjectId, UUID classId, String year)
```

**`SubjectTeacherMappingRepository`**
```
- List<SubjectTeacherMapping> findByTeacherId(UUID teacherId)
- List<SubjectTeacherMapping> findByTeacherIdAndAcademicYear(UUID teacherId, String year)
- List<SubjectTeacherMapping> findBySubjectIdAndClassEntityId(UUID subjectId, UUID classId)
- boolean existsByTeacherIdAndSubjectIdAndClassEntityIdAndAcademicYear(UUID tId, UUID sId, UUID cId, String year)
```

**`StudentProfileRepository`**
```
- Optional<StudentProfile> findByUserId(UUID userId)
- Optional<StudentProfile> findByRollNumberAndInstituteId(String rollNumber, UUID instituteId)
- List<StudentProfile> findByMentorId(UUID mentorId)
- List<StudentProfile> findByClassEntityId(UUID classId)
- List<StudentProfile> findByDepartmentId(UUID deptId)
- Page<StudentProfile> findByInstituteId(UUID instituteId, Pageable pageable)
- boolean existsByRollNumberAndInstituteId(String rollNumber, UUID instituteId)
- long countByInstituteId(UUID instituteId)
```

**`TeacherProfileRepository`**
```
- Optional<TeacherProfile> findByUserId(UUID userId)
- Optional<TeacherProfile> findByEmployeeIdAndInstituteId(String empId, UUID instituteId)
- List<TeacherProfile> findByInstituteId(UUID instituteId)
- boolean existsByEmployeeIdAndInstituteId(String empId, UUID instituteId)
- long countByInstituteId(UUID instituteId)
```

**`AttendanceSessionRepository`**
```
- List<AttendanceSession> findBySubjectIdAndClassEntityId(UUID subjectId, UUID classId)
- Optional<AttendanceSession> findBySubjectIdAndClassEntityIdAndSessionDate(UUID subjectId, UUID classId, LocalDate date)
```

**`AttendanceRecordRepository`**
```
- List<AttendanceRecord> findByAttendanceSessionId(UUID sessionId)
- List<AttendanceRecord> findByStudentIdAndAttendanceSession_SubjectId(UUID studentId, UUID subjectId)
- @Query: countByStudentIdAndAttendanceSession_SubjectIdAndStatus(UUID studentId, UUID subjectId, AttendanceStatus status)
- @Query: countByStudentIdAndAttendanceSession_SubjectId(UUID studentId, UUID subjectId)
```

**`IAMarksRepository`**
```
- List<IAMarks> findByStudentIdAndSubjectId(UUID studentId, UUID subjectId)
- List<IAMarks> findBySubjectIdAndClassEntityIdAndIaRound(UUID subjectId, UUID classId, String round)
- Optional<IAMarks> findByStudentIdAndSubjectIdAndIaRound(UUID studentId, UUID subjectId, String round)
- @Query: avgNormalizedScoreByStudentIdAndSubjectId(UUID studentId, UUID subjectId) → Double
```

**`AssignmentRepository`**
```
- List<Assignment> findBySubjectIdAndClassEntityId(UUID subjectId, UUID classId)
- long countBySubjectIdAndClassEntityId(UUID subjectId, UUID classId)
```

**`AssignmentSubmissionRepository`**
```
- List<AssignmentSubmission> findByStudentIdAndAssignment_SubjectId(UUID studentId, UUID subjectId)
- Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId)
- @Query: countByStudentIdAndAssignment_SubjectIdAndStatus(UUID studentId, UUID subjectId, SubmissionStatus status)
- @Query: countByStudentIdAndAssignment_SubjectId(UUID studentId, UUID subjectId)
```

**`LMSScoreRepository`**
```
- Optional<LMSScore> findByStudentIdAndSubjectId(UUID studentId, UUID subjectId)
- List<LMSScore> findByStudentId(UUID studentId)
- List<LMSScore> findBySubjectIdAndClassEntityId(UUID subjectId, UUID classId)
```

**`RiskScoreRepository`**
```
- Optional<RiskScore> findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(UUID studentId) → latest overall
- Optional<RiskScore> findByStudentIdAndSubjectIdAndIsLatestTrue(UUID studentId, UUID subjectId) → latest per-subject
- List<RiskScore> findByStudentIdAndSubjectIdIsNullOrderByComputedAtAsc(UUID studentId) → trend data (overall)
- List<RiskScore> findByStudentIdAndIsLatestTrue(UUID studentId) → all latest (overall + per-subject)
- @Query: countByRiskLabelAndStudent_Institute_Id(RiskLabel label, UUID instituteId)
- @Query: avgRiskScoreByInstituteId(UUID instituteId) → Double
- @Modifying @Query: markPreviousAsNotLatest(UUID studentId, UUID subjectId) → UPDATE risk_scores SET is_latest=false WHERE student_id=? AND subject_id=? AND is_latest=true
- @Modifying @Query: markPreviousOverallAsNotLatest(UUID studentId) → UPDATE risk_scores SET is_latest=false WHERE student_id=? AND subject_id IS NULL AND is_latest=true
- Page<RiskScore> findByStudent_Institute_IdAndSubjectIdIsNullAndIsLatestTrue(UUID instituteId, Pageable pageable) → institution-wide student list with risk
```

**`InterventionRepository`**
```
- List<Intervention> findByStudentId(UUID studentId)
- List<Intervention> findByMentorId(UUID mentorId)
- Page<Intervention> findByStudent_Institute_Id(UUID instituteId, Pageable pageable)
- List<Intervention> findByFollowUpDateLessThanEqualAndPostRiskScoreIsNull(LocalDate date)
- long countByStudent_Institute_Id(UUID instituteId)
```

**`ActionItemRepository`**
```
- List<ActionItem> findByInterventionId(UUID interventionId)
```

**`StudentFlagRepository`**
```
- List<StudentFlag> findByStudentIdAndIsResolvedFalse(UUID studentId)
- List<StudentFlag> findByStudent_StudentProfile_MentorId(UUID mentorId) → unresolved flags for mentor's mentees
- Page<StudentFlag> findByFlaggedById(UUID teacherId, Pageable pageable)
```

**`NotificationRepository`**
```
- Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID userId, Pageable pageable)
- long countByRecipientIdAndIsReadFalse(UUID userId)
- @Modifying @Query: markAllAsRead(UUID userId)
```

**`ExamScheduleRepository`**
```
- List<ExamSchedule> findByInstituteIdAndExamDateBetween(UUID instituteId, LocalDate start, LocalDate end)
- List<ExamSchedule> findByClassEntityIdAndExamDateAfter(UUID classId, LocalDate today)
- Optional<ExamSchedule> findBySubjectIdAndClassEntityIdAndExamType(UUID subjectId, UUID classId, String type)
```

**`ConsistencyStreakRepository`**
```
- Optional<ConsistencyStreak> findByStudentId(UUID studentId)
```

### Modifications to Existing Repositories

**`UserRepository`** — add:
```
- Page<User> findByInstitute_IdAndRolesContaining(UUID instituteId, Role role, Pageable pageable)
- List<User> findByInstitute_IdAndRolesContaining(UUID instituteId, Role role)
- long countByInstitute_IdAndRolesContaining(UUID instituteId, Role role)
- Optional<User> findByEmailAndInstitute_Id(String email, UUID instituteId)
```

---

## 9. Services — Implementation Order

Build these in sequence. Each service depends only on previously built ones.

### Phase 1 — Structure Setup (no academic data yet)

#### 9.1 `DepartmentService`
```
Methods:
- Department createDepartment(UUID instituteId, String name, String code) 
- List<Department> getDepartmentsByInstitute(UUID instituteId)
- Department getDepartmentById(UUID id)
- Department getDepartmentByCode(UUID instituteId, String code)
```

#### 9.2 `ClassService`
```
Methods:
- ClassEntity createClass(UUID departmentId, String name, int semester, String academicYear)
- List<ClassEntity> getClassesByDepartment(UUID departmentId)
- List<ClassEntity> getClassesByInstitute(UUID instituteId)
- ClassEntity getClassById(UUID id)
```

#### 9.3 `SubjectService`
```
Methods:
- Subject createSubject(UUID departmentId, UUID instituteId, String name, String code)
- List<Subject> getSubjectsByInstitute(UUID instituteId)
- List<Subject> getSubjectsByDepartment(UUID departmentId)
- Subject getSubjectById(UUID id)
- void mapSubjectToClass(UUID subjectId, UUID classId, int semester, String academicYear)
- void mapTeacherToSubjectClass(UUID teacherId, UUID subjectId, UUID classId, String academicYear)
- List<SubjectTeacherMapping> getTeacherSubjects(UUID teacherId, String academicYear)
```

### Phase 2 — CSV Upload & User Creation

#### 9.4 `CsvUploadService`
```
Dependencies: UserRepository, StudentProfileRepository, TeacherProfileRepository, DepartmentService, ClassService, SubjectService, PasswordEncoder, TokenGenerator, EmailService

Methods:
- CsvUploadResponse uploadStudentCsv(MultipartFile file, UUID instituteId)
  Logic:
  1. Parse CSV with OpenCSV (CsvToBeanBuilder)
  2. Validate each row: email format, required fields, dept exists, class exists
  3. For each valid row:
     a. Create User (email, generated temp password, role=STUDENT, institute, isActive=true, mustChangePassword=true, fullName)
     b. Create StudentProfile (rollNumber, fullName, department, class, semester, mentor by email lookup)
     c. Queue email with temp credentials
  4. Return CsvUploadResponse with success/error counts

- CsvUploadResponse uploadTeacherCsv(MultipartFile file, UUID instituteId)
  Logic:
  1. Parse CSV
  2. Validate each row
  3. For each valid row:
     a. Create User (email, temp password, roles=determine from CSV columns, institute, fullName)
        - If subjectsTaught is non-empty → add SUBJECT_TEACHER role
        - If mentorTo is non-empty → add FACULTY_MENTOR role
     b. Create TeacherProfile (employeeId, fullName, department)
     c. Create SubjectTeacherMapping entries for each subject+class
     d. Update StudentProfile.mentor for each student in mentorTo
     e. Queue email
  4. Return CsvUploadResponse

Helper methods:
- List<StudentCsvRow> parseStudentCsv(MultipartFile file)
- List<TeacherCsvRow> parseTeacherCsv(MultipartFile file)
- void validateStudentRow(StudentCsvRow row, int rowNum, UUID instituteId, List<CsvRowError> errors)
- void validateTeacherRow(TeacherCsvRow row, int rowNum, UUID instituteId, List<CsvRowError> errors)
```

#### 9.5 `UserManagementService`
```
Methods:
- StudentProfileResponse addStudentManually(ManualStudentRequest req, UUID instituteId)
- TeacherProfileResponse addTeacherManually(ManualTeacherRequest req, UUID instituteId)
- Page<StudentProfileResponse> listStudents(UUID instituteId, StudentFilterRequest filter)
- Page<TeacherProfileResponse> listTeachers(UUID instituteId, Pageable pageable)
- void reassignMentor(UUID studentId, UUID newMentorId, UUID instituteId)
- void deactivateUser(UUID userId)
- void activateUser(UUID userId)
```

### Phase 3 — Academic Data Entry

#### 9.6 `AttendanceService`
```
Methods:
- void saveAttendanceSession(AttendanceSessionRequest request, UUID teacherId)
  Logic:
  - Verify teacher is mapped to this subject+class
  - If PER_SESSION: create AttendanceSession + AttendanceRecords with PRESENT/ABSENT
  - If BULK_PERCENTAGE: create AttendanceSession + records with bulkPercentage
  - Trigger risk recompute for affected students (async)

- Double computeAttendancePercentage(UUID studentId, UUID subjectId)
  Logic:
  - If only PER_SESSION records: (count PRESENT / total sessions) × 100
  - If only BULK records: take the latest bulk percentage
  - If mixed: prioritize per-session where available, fill gaps with bulk
  
- Map<UUID, Double> computeAttendanceForAllSubjects(UUID studentId)
  Returns subjectId → attendance% for every subject the student is enrolled in
```

#### 9.7 `IAMarksService`
```
Methods:
- void saveIAMarks(IAMarksEntryRequest request, UUID teacherId)
  Logic:
  - Verify teacher is mapped to this subject+class
  - For each entry: compute normalizedScore = (obtained / max) × 100
  - Upsert (if existing entry for student+subject+round, update it)
  - Trigger risk recompute for affected students

- Double computeAverageNormalizedMarks(UUID studentId, UUID subjectId)
  Returns average of all normalizedScore values across IA rounds

- Map<UUID, Double> computeMarksForAllSubjects(UUID studentId)
```

#### 9.8 `AssignmentService`
```
Methods:
- Assignment createAssignment(CreateAssignmentRequest request, UUID teacherId)
- void markSubmissions(MarkSubmissionRequest request, UUID teacherId)
  - Trigger risk recompute for affected students

- Double computeAssignmentCompletion(UUID studentId, UUID subjectId)
  Logic: (count SUBMITTED + count LATE) / total assignments × 100

- Map<UUID, Double> computeAssignmentForAllSubjects(UUID studentId)
- List<AssignmentSubmission> getStudentAssignments(UUID studentId, UUID subjectId)
```

#### 9.9 `LMSScoreService`
```
Methods:
- void saveLMSScores(LMSScoreBulkRequest request, UUID teacherId)
  - Upsert per student+subject
  - Trigger risk recompute

- Double getLMSScore(UUID studentId, UUID subjectId)
- Map<UUID, Double> getLMSForAllSubjects(UUID studentId)
```

### Phase 4 — Risk Engine

#### 9.10 `AggregationService`
```
Purpose: Computes the 4-feature vector per student (overall and per-subject) by pulling from attendance, marks, assignment, and LMS services.

Methods:
- StudentFeatureVector computePerSubjectFeatures(UUID studentId, UUID subjectId)
  Returns: { attendance%, marks%, assignment%, lms% } for one subject

- StudentFeatureVector computeOverallFeatures(UUID studentId)
  Logic:
  1. Get list of all subjects the student is enrolled in (via StudentProfile.class → SubjectClassMapping)
  2. For each subject: call computePerSubjectFeatures()
  3. Average across all subjects → { avg_attendance, avg_marks, avg_assignment, avg_lms }
  Returns: the averaged 4-feature vector

- record StudentFeatureVector(double attendance, double marks, double assignment, double lms) {}
```

#### 9.11 `MLServiceClient`
```
Purpose: REST client calling the FastAPI ML service.

Dependencies: WebClient (Spring WebFlux)

Configuration:
- Base URL from `ml.service.base-url`
- Timeout from `ml.service.timeout-ms`

Methods:
- RiskPredictionResult predictRisk(StudentFeatureVector features)
  Calls: POST {ml-base-url}/api/predict (if exposed by FastAPI — OR use the threshold logic locally)
  Returns: { riskScore, riskLabel }

- SuggestionsResponse getSuggestions(StudentMLPayload payload)
  Calls: POST {ml-base-url}/api/suggestions
  Maps StudentMLPayload to the StudentRequest format expected by FastAPI

- RoadmapResponse getRoadmap(StudentMLPayload payload)
  Calls: POST {ml-base-url}/api/roadmap
  Maps payload similarly

Inner class / record:
- StudentMLPayload: Maps to the FastAPI's StudentRequest schema
  { student_id, student_name, semester, branch, overall: {...}, subject_wise: [...] }
```

**IMPORTANT:** The ML FastAPI currently has `/api/suggestions` and `/api/roadmap` endpoints but NO `/api/predict` endpoint for risk score computation. Two options:
1. **Add a `/api/predict` endpoint to FastAPI** (preferred — keeps ML model centralized)
2. **Implement threshold-based scoring in Java** as fallback (using thresholds.json: low < 35, medium < 55)

For the What-If calculator, use the same ML predict endpoint with hypothetical data.

#### 9.12 `RiskScoreService`
```
Methods:
- void computeAndSaveRiskScore(UUID studentId)
  Logic:
  1. Compute overall features via AggregationService
  2. Compute per-subject features for each enrolled subject
  3. Call MLServiceClient.predictRisk() for overall + each subject
  4. Mark previous risk scores as not-latest
  5. Save new RiskScore records (overall + per-subject)
  6. Check if risk label changed → if crossed HIGH threshold, trigger alert

- void batchRecomputeAllStudents(UUID instituteId)
  Logic: Fetch all student IDs in institute → call computeAndSaveRiskScore for each

- RiskScoreResponse getCurrentRiskScore(UUID studentId)
- RiskTrendResponse getRiskTrend(UUID studentId)
- WhatIfResponse computeWhatIf(WhatIfRequest request)
  Logic:
  1. Build hypothetical feature vectors from request
  2. Call ML model with hypothetical data
  3. Return predicted vs current comparison
```

### Phase 5 — Interventions & Flags

#### 9.13 `InterventionService`
```
Methods:
- InterventionResponse createIntervention(CreateInterventionRequest request, UUID mentorId)
  Logic:
  1. Verify mentor is assigned to this student
  2. Snapshot current overall risk score as preRiskScore
  3. Create Intervention + ActionItem records
  4. Return response

- List<InterventionResponse> getInterventionsByStudent(UUID studentId)
- List<InterventionResponse> getInterventionsByMentor(UUID mentorId)
- Page<InterventionResponse> getInterventionsByInstitute(UUID instituteId, Pageable pageable)
- void processFollowUps()
  Logic (called by scheduler):
  1. Find interventions where followUpDate <= today AND postRiskScore is null
  2. For each: fetch current risk score, set postRiskScore, compute change
  3. Notify mentor about follow-up result

- List<InterventionEffectivenessResponse> getInterventionEffectiveness(UUID instituteId)
  Logic: Group by interventionType, compute avg pre/post/improvement
```

#### 9.14 `StudentFlagService`
```
Methods:
- StudentFlagResponse flagStudent(FlagStudentRequest request, UUID teacherId)
  Logic:
  1. Create StudentFlag
  2. Notify mentor of the flagged student

- List<StudentFlagResponse> getFlagsForMentee(UUID studentId)
- List<StudentFlagResponse> getUnresolvedFlagsForMentor(UUID mentorId)
- void resolveFlag(UUID flagId, UUID mentorId)
```

### Phase 6 — Notifications & Alerts

#### 9.15 `NotificationService`
```
Methods:
- void createNotification(UUID recipientId, String title, String message, NotificationType type, UUID referenceId, String referenceType)
  Logic:
  1. Save Notification to DB (in-app)
  2. If user has email alerts enabled → send email via EmailService

- Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable)
- long getUnreadCount(UUID userId)
- void markAsRead(UUID notificationId, UUID userId)
- void markAllAsRead(UUID userId)
```

#### 9.16 `AlertService`
```
Methods:
- void checkAndSendHighRiskAlert(UUID studentId, RiskLabel previousLabel, RiskLabel newLabel)
  Logic: If crossed to HIGH → notify mentor + coordinator

- void sendPreExamAlerts(UUID instituteId)
  Logic:
  1. Find exams within 14 days
  2. For each exam's class → find HIGH risk students
  3. Notify their mentors

- void sendDataEntryReminders(UUID instituteId)
  Logic: Find teachers who haven't entered attendance/marks this week → notify
```

### Phase 7 — Analytics & Dashboards

#### 9.17 `CoordinatorDashboardService`
```
Methods:
- InstituteDashboardResponse getDashboard(UUID instituteId)
- Page<StudentProfileResponse> getStudentList(UUID instituteId, StudentFilterRequest filter)
- List<DepartmentRiskSummary> getDepartmentRiskHeatmap(UUID instituteId)
- List<InterventionEffectivenessResponse> getInterventionEffectiveness(UUID instituteId)
- byte[] exportStudentListPdf(UUID instituteId, StudentFilterRequest filter)
- byte[] exportStudentListCsv(UUID instituteId, StudentFilterRequest filter)
```

#### 9.18 `TeacherDashboardService`
```
Methods:
- TeacherDashboardResponse getDashboard(UUID teacherId)
- List<StudentAcademicDetailResponse> getSubjectAtRiskStudents(UUID teacherId, UUID subjectId, UUID classId)
- SubjectAnalyticsResponse getSubjectAnalytics(UUID subjectId, UUID classId)
  Returns: classAvgAttendance, classAvgMarks, classAvgAssignment, atRiskStudentList
```

#### 9.19 `StudentDashboardService`
```
Methods:
- StudentDashboardResponse getDashboard(UUID studentUserId)
- StudentAcademicDetailResponse getAcademicData(UUID studentUserId)
- RiskTrendResponse getProgressOverTime(UUID studentUserId)
- List<InterventionResponse> getInterventionHistory(UUID studentUserId)
- ConsistencyStreakResponse getConsistencyStreak(UUID studentUserId)
```

### Phase 8 — Consistency Streak & Scheduling

#### 9.20 `ConsistencyStreakService`
```
Methods:
- void updateStreak(UUID studentId)
  Logic:
  1. Check if current week qualifies (attendance >= 75%, assignment >= 80%, LMS >= 50%)
  2. If yes and last qualifying week was the previous week → increment currentStreak
  3. If yes but gap → reset currentStreak to 1
  4. Update longestStreak if needed
  5. Save

- ConsistencyStreakResponse getStreak(UUID studentId)

- void weeklyBatchUpdateAllStreaks(UUID instituteId)
  Called by scheduler every Monday
```

#### 9.21 `ScheduledJobService`
```
@EnableScheduling on main application class

Methods:
- @Scheduled(cron = "0 0 2 * * ?")  // 2 AM daily
  void nightlyRiskRecompute()
  Logic: For each institute → batchRecomputeAllStudents

- @Scheduled(cron = "0 0 8 * * MON")  // 8 AM every Monday
  void weeklyStreakUpdate()
  Logic: For each institute → weeklyBatchUpdateAllStreaks

- @Scheduled(cron = "0 0 9 * * ?")  // 9 AM daily
  void dailyAlertCheck()
  Logic:
  - For each institute: sendPreExamAlerts, sendDataEntryReminders

- @Scheduled(cron = "0 0 10 * * ?")  // 10 AM daily
  void processInterventionFollowUps()
  Logic: interventionService.processFollowUps()
```

### Phase 9 — PDF Export

#### 9.22 `PdfExportService`
```
Dependencies: OpenPDF library

Methods:
- byte[] generateStudentRiskReport(UUID instituteId, StudentFilterRequest filter)
  Logic:
  1. Fetch filtered students with risk data
  2. Build PDF with OpenPDF:
     - Header: institute name, report date, filter summary
     - Table: student name, roll, department, class, risk score, risk label, attendance%, marks%, assignment%, lms%
     - Footer: page numbers
  3. Return byte array

- byte[] generateStudentDetailReport(UUID studentId)
  Logic:
  1. Fetch full student data including subject-wise breakdown
  2. Build detailed PDF per student
```

---

## 10. Controllers — API Endpoints

### 10.1 `CoordinatorController` — `/api/coordinator`
**Requires:** `ROLE_ACADEMIC_COORDINATOR`

| Method | Path | Request | Response | Description |
|---|---|---|---|---|
| GET | `/dashboard` | - | `InstituteDashboardResponse` | Main dashboard metrics |
| POST | `/upload/students` | `MultipartFile` | `CsvUploadResponse` | Upload student CSV |
| POST | `/upload/teachers` | `MultipartFile` | `CsvUploadResponse` | Upload teacher CSV |
| GET | `/students` | `StudentFilterRequest` (query params) | `PagedResponse<StudentProfileResponse>` | Paginated, filterable student list |
| GET | `/students/{id}` | - | `StudentAcademicDetailResponse` | Full student detail |
| GET | `/teachers` | `Pageable` | `PagedResponse<TeacherProfileResponse>` | Teacher list |
| POST | `/students/add` | `ManualStudentRequest` | `StudentProfileResponse` | Add single student |
| POST | `/teachers/add` | `ManualTeacherRequest` | `TeacherProfileResponse` | Add single teacher |
| PUT | `/students/{id}/reassign-mentor` | `ReassignMentorRequest` | `MessageResponse` | Reassign mentor |
| PUT | `/users/{id}/deactivate` | - | `MessageResponse` | Deactivate user |
| PUT | `/users/{id}/activate` | - | `MessageResponse` | Activate user |
| GET | `/analytics/risk-heatmap` | - | `List<DepartmentRiskSummary>` | Department-wise risk |
| GET | `/analytics/interventions` | `Pageable` | `PagedResponse<InterventionResponse>` | All interventions |
| GET | `/analytics/intervention-effectiveness` | - | `List<InterventionEffectivenessResponse>` | Effectiveness stats |
| GET | `/export/students/pdf` | `StudentFilterRequest` | `byte[]` (application/pdf) | PDF export |
| GET | `/export/students/csv` | `StudentFilterRequest` | `byte[]` (text/csv) | CSV export |
| POST | `/exam-schedules` | `CreateExamScheduleRequest` | `MessageResponse` | Set exam date |
| GET | `/exam-schedules` | - | `List<ExamSchedule>` | List exam dates |
| GET | `/departments` | - | `List<Department>` | List departments |
| POST | `/departments` | name, code | `Department` | Create department |
| GET | `/classes` | `departmentId` (optional) | `List<ClassEntity>` | List classes |
| POST | `/classes` | departmentId, name, semester, year | `ClassEntity` | Create class |
| GET | `/subjects` | `departmentId` (optional) | `List<Subject>` | List subjects |
| POST | `/subjects` | departmentId, name, code | `Subject` | Create subject |
| POST | `/subjects/map-to-class` | subjectId, classId, semester, year | `MessageResponse` | Map subject to class |
| GET | `/csv-templates/students` | - | `byte[]` (text/csv) | Download sample student CSV |
| GET | `/csv-templates/teachers` | - | `byte[]` (text/csv) | Download sample teacher CSV |

### 10.2 `TeacherController` — `/api/teacher`
**Requires:** `ROLE_SUBJECT_TEACHER` or `ROLE_FACULTY_MENTOR`

| Method | Path | Request | Response | Description |
|---|---|---|---|---|
| GET | `/dashboard` | - | `TeacherDashboardResponse` | Teacher home dashboard |
| GET | `/subjects` | - | `List<SubjectSummary>` | Subjects assigned to this teacher |
| POST | `/attendance` | `AttendanceSessionRequest` | `MessageResponse` | Save attendance |
| POST | `/ia-marks` | `IAMarksEntryRequest` | `MessageResponse` | Save IA marks |
| POST | `/assignments` | `CreateAssignmentRequest` | `Assignment` | Create assignment |
| POST | `/assignments/submissions` | `MarkSubmissionRequest` | `MessageResponse` | Mark submissions |
| POST | `/lms-scores` | `LMSScoreBulkRequest` | `MessageResponse` | Save LMS scores |
| GET | `/subject-analysis/{subjectId}/{classId}` | - | `SubjectAnalyticsResponse` | Subject-level analytics |
| GET | `/subject-analysis/{subjectId}/{classId}/students` | - | `List<StudentAcademicDetailResponse>` | At-risk students in subject |
| POST | `/flag-student` | `FlagStudentRequest` | `StudentFlagResponse` | Flag a student |
| GET | `/flags` | `Pageable` | `PagedResponse<StudentFlagResponse>` | Flags raised by this teacher |

### 10.3 `MentorController` — `/api/mentor`
**Requires:** `ROLE_FACULTY_MENTOR`

| Method | Path | Request | Response | Description |
|---|---|---|---|---|
| GET | `/dashboard` | - | `MentorDashboardResponse` | Mentor section of dashboard |
| GET | `/mentees` | - | `List<StudentProfileResponse>` | Assigned mentees |
| GET | `/mentees/{studentId}` | - | `StudentAcademicDetailResponse` | Full mentee detail |
| GET | `/mentees/{studentId}/risk` | - | `RiskScoreResponse` | Mentee risk breakdown |
| GET | `/mentees/{studentId}/trend` | - | `RiskTrendResponse` | Risk score over time |
| GET | `/flags` | - | `List<StudentFlagResponse>` | Flags from subject teachers |
| PUT | `/flags/{flagId}/resolve` | - | `MessageResponse` | Resolve a flag |
| POST | `/interventions` | `CreateInterventionRequest` | `InterventionResponse` | Log intervention |
| GET | `/interventions` | - | `List<InterventionResponse>` | My interventions |
| PUT | `/action-items/{itemId}/complete` | - | `MessageResponse` | Mark action item done |

### 10.4 `StudentController` — `/api/student`
**Requires:** `ROLE_STUDENT`

| Method | Path | Request | Response | Description |
|---|---|---|---|---|
| GET | `/dashboard` | - | `StudentDashboardResponse` | Home dashboard |
| GET | `/academic-data` | - | `StudentAcademicDetailResponse` | Subject-wise breakdown |
| GET | `/risk/current` | - | `RiskScoreResponse` | Current risk score + breakdown |
| GET | `/risk/trend` | - | `RiskTrendResponse` | Score over time |
| POST | `/risk/what-if` | `WhatIfRequest` | `WhatIfResponse` | What-if calculator |
| GET | `/interventions` | - | `List<InterventionResponse>` | Intervention history |
| GET | `/consistency-streak` | - | `ConsistencyStreakResponse` | Streak data |
| GET | `/suggestions` | - | `SuggestionsResponse` | AI-generated suggestions (calls ML) |
| GET | `/roadmap` | - | `RoadmapResponse` | AI-generated roadmap (calls ML) |

### 10.5 `NotificationController` — `/api/notifications`
**Requires:** Authenticated

| Method | Path | Request | Response | Description |
|---|---|---|---|---|
| GET | `/` | `Pageable` | `PagedResponse<NotificationResponse>` | List notifications |
| GET | `/unread-count` | - | `UnreadCountResponse` | Unread badge count |
| PUT | `/{id}/read` | - | `MessageResponse` | Mark one as read |
| PUT | `/read-all` | - | `MessageResponse` | Mark all as read |

### 10.6 Update `SecurityConfig`

Add these role-based access rules:

```
/api/coordinator/** → ROLE_ACADEMIC_COORDINATOR
/api/teacher/**     → ROLE_SUBJECT_TEACHER or ROLE_FACULTY_MENTOR
/api/mentor/**      → ROLE_FACULTY_MENTOR
/api/student/**     → ROLE_STUDENT
/api/notifications/** → authenticated (any role)
```

---

## 11. Risk Engine — Aggregation & ML Integration

### Flow Diagram

```
Teacher saves data (attendance/marks/assignment/LMS)
    │
    ▼
Service layer triggers risk recompute for affected students
    │
    ▼
AggregationService.computePerSubjectFeatures(studentId, subjectId)
    │ Returns: { attendance%, marks%, assignment%, lms% } per subject
    ▼
AggregationService.computeOverallFeatures(studentId)
    │ Returns: averaged { attendance, marks, assignment, lms } across subjects
    ▼
MLServiceClient.predictRisk(featureVector)
    │ Calls FastAPI: POST /api/predict
    │ Input: { attendance, marks, assignment, lms }
    │ Output: { risk_score: 0-100, risk_label: "Low"/"Medium"/"High" }
    ▼
RiskScoreService saves new RiskScore records (overall + per-subject)
    │ Previous records marked is_latest = false
    ▼
AlertService checks for threshold crossing
    │ If crossed to HIGH → notify mentor + coordinator
    ▼
Done
```

### ML FastAPI Endpoint to Add

The current ML service has `/api/suggestions` and `/api/roadmap` but is missing a predict endpoint. A new endpoint should be added to the ML service:

**POST `/api/predict`**
```json
// Request
{
  "attendance": 42.0,
  "marks": 35.0,
  "assignment": 30.0,
  "lms": 18.0
}

// Response
{
  "risk_score": 65.8,
  "risk_label": "High"
}
```

This uses the same thresholds from `outputs/thresholds.json` (low < 35, medium < 55).

### Fallback: Local Threshold Computation

If ML service is unavailable, use this fallback in Java:
```
risk_score = 100 - (0.30 * attendance + 0.30 * marks + 0.20 * assignment + 0.20 * lms)
if risk_score < 35.0 → LOW
else if risk_score < 55.0 → MEDIUM
else → HIGH
```

---

## 12. Scheduled Jobs

| Job | Cron | Description |
|---|---|---|
| Nightly risk recompute | `0 0 2 * * ?` (2 AM) | Recompute all student risk scores |
| Weekly streak update | `0 0 8 * * MON` (Mon 8 AM) | Update consistency streaks |
| Daily alert check | `0 0 9 * * ?` (9 AM) | Pre-exam alerts + data entry reminders |
| Intervention follow-up | `0 0 10 * * ?` (10 AM) | Process due follow-ups, fill post-scores |

Add `@EnableScheduling` to `HackathonAppApplication.java`.

---

## 13. CSV Upload & Parsing

### Student CSV Format

```csv
roll_number,full_name,email,department_code,class_name,semester,mentor_email
STU001,Raj Mehta,raj@pdeu.ac.in,IT,SE-A,4,prof.shah@pdeu.ac.in
STU002,Priya Sharma,priya@pdeu.ac.in,CE,TE-B,5,prof.patel@pdeu.ac.in
```

### Teacher CSV Format

```csv
employee_id,full_name,email,department_code,subjects_taught,mentor_to
EMP001,Prof. Shah,prof.shah@pdeu.ac.in,IT,"CS301:SE-A,CS302:SE-B","STU001,STU003"
EMP002,Prof. Patel,prof.patel@pdeu.ac.in,CE,"CE201:TE-B",STU002
```

**`subjects_taught` format:** `subject_code:class_name` (comma-separated within quotes)
**`mentor_to` format:** Student roll numbers (comma-separated within quotes)

### Parsing Logic (OpenCSV)

```
1. Read file → CSVReader with CSVParserBuilder (separator=',', quoteChar='"')
2. Skip header row
3. For each row:
   a. Trim all values
   b. Validate required fields non-empty
   c. Validate email format
   d. Check department exists → if not, add error
   e. Check class exists → if not, add error  
   f. Check email not already registered → if yes, add error
4. Return validated list + error list
5. Only process valid rows
```

### Department, Class, Subject Pre-creation

The CSV upload assumes departments, classes, and subjects already exist. The Coordinator must:
1. Create departments first (via `/api/coordinator/departments`)
2. Create classes (via `/api/coordinator/classes`)
3. Create subjects and map them to classes
4. Then upload CSVs

Alternatively: auto-create departments/classes/subjects from CSV if they don't exist (configurable via a `createMissing` query param on the upload endpoint).

---

## 14. PDF Export

### Library: OpenPDF 2.0.3

### Report Types

1. **Student Risk Report (Filtered List)**
   - Table format: Name, Roll, Dept, Class, Risk Score, Label, Attendance%, Marks%, Assignment%, LMS%
   - Sorted by risk score descending
   - Header: Institute name, date, filter summary
   - Color-coded risk labels

2. **Individual Student Report**
   - Student details
   - Overall risk score and breakdown
   - Subject-wise table
   - Risk trend chart data (text-based list, not actual chart)
   - Intervention history

### Implementation

```
PdfExportService:
  - Uses Document, PdfWriter, PdfPTable from OpenPDF
  - Creates table with headers, iterates rows, sets colors for risk labels
  - Returns byte[] from ByteArrayOutputStream
  - Controller returns ResponseEntity<byte[]> with Content-Type: application/pdf
```

---

## 15. Notification System

### When Notifications Are Created

| Trigger | Recipient | Type | Email Too? |
|---|---|---|---|
| Student crosses to HIGH risk | Mentor + Coordinator | `HIGH_RISK_ALERT` | Yes |
| Risk threshold crossed (any direction) | Mentor | `RISK_THRESHOLD_CROSSED` | No |
| Exam within 14 days + high-risk students exist | Mentor | `PRE_EXAM_ALERT` | Yes |
| Teacher hasn't entered data this week | Teacher | `DATA_ENTRY_REMINDER` | Yes |
| Intervention follow-up due | Mentor | `INTERVENTION_FOLLOW_UP` | Yes |
| Subject teacher flags a student | Mentor | `STUDENT_FLAGGED` | No |

### User Preference

Add `emailAlertsEnabled` (boolean, default true) to User entity. Notifications are always stored in DB. Email is sent only if `emailAlertsEnabled = true`.

---

## 16. Security & Authorization Rules

### Data Scoping

**All queries must be scoped by `institute_id`** extracted from the JWT token. This ensures Institute A cannot see Institute B's data.

### Role-Based Access

| Role | Can Access |
|---|---|
| `ACADEMIC_COORDINATOR` | All data in their institute. Full CRUD on users, analytics, interventions. |
| `SUBJECT_TEACHER` | Only subjects+classes they are mapped to. Can enter data, view subject-level analytics, flag students. |
| `FACULTY_MENTOR` | Only their assigned mentees. Can view full risk breakdown, log interventions, resolve flags. |
| `STUDENT` | Only their own data. Read-only. |

### Service-Level Authorization Checks

Every service method must verify:
1. The requesting user's `institute_id` matches the target data's institute
2. Teachers can only access their mapped subjects/classes
3. Mentors can only access their assigned mentees
4. Students can only access their own records

Use `@AuthenticationPrincipal User currentUser` in controllers to get the logged-in user, then pass to service for verification.

---

## 17. Implementation Phases — Step-by-Step Task List

### Phase 0: Setup (Before any feature code)
- [ ] Add dependencies to pom.xml (OpenCSV, OpenPDF, WebFlux)
- [ ] Add new application.properties entries
- [ ] Add `@EnableScheduling` to main application class
- [ ] Add `fullName` field to User entity
- [ ] Add `emailAlertsEnabled` field to User entity
- [ ] Create all new Enum files (7 enums)

### Phase 1: Core Entities & Repositories
- [ ] Create `Department` entity + repository
- [ ] Create `ClassEntity` entity + repository
- [ ] Create `Subject` entity + repository
- [ ] Create `SubjectClassMapping` entity + repository
- [ ] Create `SubjectTeacherMapping` entity + repository
- [ ] Create `StudentProfile` entity + repository
- [ ] Create `TeacherProfile` entity + repository
- [ ] Run `mvn clean compile` — verify all entities are valid
- [ ] Start app to verify Hibernate creates all tables

### Phase 2: Structure Services & Coordinator Setup APIs
- [ ] Create `DepartmentService` + tests
- [ ] Create `ClassService` + tests
- [ ] Create `SubjectService` (including mapping methods) + tests
- [ ] Create `CoordinatorController` — structure endpoints only (departments, classes, subjects)
- [ ] Update `SecurityConfig` with new role-based rules
- [ ] Test via Postman: create dept, class, subject, map subject to class

### Phase 3: CSV Upload
- [ ] Create CSV DTOs (`StudentCsvRow`, `TeacherCsvRow`)
- [ ] Create `CsvUploadService` — student CSV parsing + validation + user creation
- [ ] Create `CsvUploadService` — teacher CSV parsing + user creation + role assignment + mappings
- [ ] Create `CsvUploadResponse`, `CsvRowError` DTOs
- [ ] Add CSV upload endpoints to `CoordinatorController`
- [ ] Add CSV template download endpoints
- [ ] Update `EmailService` with temp password email for CSV-created users
- [ ] Test: upload student CSV → verify users created, profiles linked, temp passwords emailed
- [ ] Test: upload teacher CSV → verify users created, subject mappings, mentor assignments

### Phase 4: Academic Data Entry Entities & Services
- [ ] Create `AttendanceSession` + `AttendanceRecord` entities + repositories
- [ ] Create `IAMarks` entity + repository
- [ ] Create `Assignment` + `AssignmentSubmission` entities + repositories
- [ ] Create `LMSScore` entity + repository
- [ ] Create `AttendanceService`
- [ ] Create `IAMarksService`
- [ ] Create `AssignmentService`
- [ ] Create `LMSScoreService`
- [ ] Create `TeacherController` — data entry endpoints
- [ ] Test: enter attendance (both modes), IA marks, create assignment + mark submissions, LMS scores

### Phase 5: Risk Engine
- [ ] Create `RiskScore` entity + repository
- [ ] Create `AggregationService` — per-subject + overall feature computation
- [ ] Configure WebClient bean in `AppConfig`
- [ ] Create `MLServiceClient` — REST client to FastAPI
- [ ] Create `RiskScoreService` — compute, save, trend, what-if
- [ ] Add risk score trigger calls in AttendanceService, IAMarksService, AssignmentService, LMSScoreService
- [ ] Test: enter data → verify risk score computed → verify history stored

### Phase 6: Interventions & Flags
- [ ] Create `Intervention` + `ActionItem` entities + repositories
- [ ] Create `StudentFlag` entity + repository
- [ ] Create `InterventionService`
- [ ] Create `StudentFlagService`
- [ ] Create `MentorController` — all endpoints
- [ ] Add flag endpoints to `TeacherController`
- [ ] Test: flag student → mentor sees flag → log intervention → verify pre-score snapshot

### Phase 7: Notifications & Alerts
- [ ] Create `Notification` entity + repository
- [ ] Create `NotificationService`
- [ ] Create `AlertService`
- [ ] Create `NotificationController`
- [ ] Wire alerts into risk score service (threshold crossing)
- [ ] Test: risk crosses HIGH → notification created + email sent

### Phase 8: Dashboards & Analytics
- [ ] Create `CoordinatorDashboardService` — dashboard, analytics, heatmap
- [ ] Create `TeacherDashboardService` — teacher home, subject analysis
- [ ] Create `StudentDashboardService` — student home, academic data, suggestions
- [ ] Wire dashboard endpoints in all controllers
- [ ] Test: all dashboard endpoints return correct data

### Phase 9: Consistency Streak
- [ ] Create `ConsistencyStreak` entity + repository
- [ ] Create `ConsistencyStreakService`
- [ ] Wire into StudentController
- [ ] Test: verify streak computation logic

### Phase 10: Exam Schedules
- [ ] Create `ExamSchedule` entity + repository
- [ ] Add exam schedule endpoints to CoordinatorController
- [ ] Wire into AlertService for pre-exam alerts
- [ ] Test: create exam → verify alerts fire 14 days before

### Phase 11: Scheduled Jobs
- [ ] Create `ScheduledJobService` with all @Scheduled methods
- [ ] Test: verify nightly recompute, weekly streak update, daily alerts, follow-up processing

### Phase 12: PDF & CSV Export
- [ ] Create `PdfExportService`
- [ ] Add export endpoints to CoordinatorController
- [ ] Create CSV export utility
- [ ] Test: export PDF and CSV, verify content

### Phase 13: User Management Extras
- [ ] Create `UserManagementService` — manual add, list, deactivate, mentor reassignment
- [ ] Wire remaining CoordinatorController endpoints
- [ ] Test: add user manually, deactivate, reassign mentor

### Phase 14: Final Integration Testing
- [ ] End-to-end: Coordinator creates structure → uploads CSVs → Teacher enters data → Risk computed → Student sees dashboard → Mentor logs intervention → Follow-up auto-compared
- [ ] Verify institute isolation (multi-tenant)
- [ ] Verify all pagination, sorting, filtering works
- [ ] Load test risk batch recompute

---

## 18. Testing Strategy

### Unit Tests
- Service layer tests with mocked repositories
- AggregationService: verify correct computation of attendance%, marks%, assignment%
- CSV parsing: test with valid CSV, invalid rows, edge cases

### Integration Tests
- Repository tests with `@DataJpaTest` — verify custom queries
- Controller tests with `@WebMvcTest` + `@WithMockUser` for role-based access
- Full flow test with `@SpringBootTest` + test MySQL (or H2 in MySQL mode)

### Key Edge Cases to Test
1. Student enrolled in no subjects yet → risk score = 0 (or N/A)
2. Teacher uploads CSV with duplicate emails → graceful error per row
3. Mixed attendance entry modes (some per-session, some bulk) for same student
4. IA marks with absent students (0 marks, isAbsent=true)
5. Risk score recompute when no ML service available → fallback formula
6. Intervention follow-up where student no longer exists (deactivated)
7. Concurrent risk score updates (same student, two teachers save at once)
8. Streak computation with incomplete week data

---

## Appendix A: Full Entity List (Quick Reference)

| # | Entity | Table | New/Existing |
|---|---|---|---|
| 1 | Institute | institutes | Existing ✅ |
| 2 | User | users | Existing ✅ (minor additions: fullName, emailAlertsEnabled) |
| 3 | PasswordResetToken | password_reset_tokens | Existing ✅ |
| 4 | Department | departments | **New** |
| 5 | ClassEntity | classes | **New** |
| 6 | Subject | subjects | **New** |
| 7 | SubjectClassMapping | subject_class_mappings | **New** |
| 8 | SubjectTeacherMapping | subject_teacher_mappings | **New** |
| 9 | StudentProfile | student_profiles | **New** |
| 10 | TeacherProfile | teacher_profiles | **New** |
| 11 | AttendanceSession | attendance_sessions | **New** |
| 12 | AttendanceRecord | attendance_records | **New** |
| 13 | IAMarks | ia_marks | **New** |
| 14 | Assignment | assignments | **New** |
| 15 | AssignmentSubmission | assignment_submissions | **New** |
| 16 | LMSScore | lms_scores | **New** |
| 17 | RiskScore | risk_scores | **New** |
| 18 | Intervention | interventions | **New** |
| 19 | ActionItem | action_items | **New** |
| 20 | StudentFlag | student_flags | **New** |
| 21 | Notification | notifications | **New** |
| 22 | ExamSchedule | exam_schedules | **New** |
| 23 | ConsistencyStreak | consistency_streaks | **New** |

**Total: 23 entities (3 existing + 20 new)**

## Appendix B: Full Enum List

| # | Enum | Values |
|---|---|---|
| 1 | Role | STUDENT, FACULTY_MENTOR, SUBJECT_TEACHER, ACADEMIC_COORDINATOR (existing) |
| 2 | RiskLabel | LOW, MEDIUM, HIGH |
| 3 | InterventionType | COUNSELLING_SESSION, REMEDIAL_CLASS, ASSIGNMENT_EXTENSION, PARENT_MEETING, OTHER |
| 4 | SubmissionStatus | SUBMITTED, NOT_SUBMITTED, LATE |
| 5 | AttendanceStatus | PRESENT, ABSENT |
| 6 | AttendanceEntryMode | PER_SESSION, BULK_PERCENTAGE |
| 7 | NotificationType | HIGH_RISK_ALERT, RISK_THRESHOLD_CROSSED, PRE_EXAM_ALERT, DATA_ENTRY_REMINDER, INTERVENTION_FOLLOW_UP, STUDENT_FLAGGED, GENERAL |
| 8 | ActionItemStatus | PENDING, COMPLETED |

**Total: 8 enums (1 existing + 7 new)**

## Appendix C: Dependency Versions Summary

| Dependency | Version | Purpose |
|---|---|---|
| Spring Boot | 4.0.5 | Framework |
| Java | 21 | Runtime |
| MySQL Connector | runtime (managed by Spring) | Database |
| jjwt | 0.12.6 | JWT auth |
| SpringDoc OpenAPI | 3.0.2 | Swagger docs |
| commons-lang3 | 3.14.0 | String utils |
| OpenCSV | 5.9 | CSV parsing |
| OpenPDF | 2.0.3 | PDF generation |
| Spring WebFlux | managed by Spring Boot | WebClient for ML REST calls |
| Lombok | managed by Spring Boot | Boilerplate reduction |

---

*End of Backend Final Plan*
