# TarkShastra – Complete Backend API Reference for Frontend Development

> Generated for frontend integration. Contains every endpoint, DTO shape, enum, and security rule.

---

## Table of Contents
1. [Authentication & Security](#authentication--security)
2. [Roles & Route Guards](#roles--route-guards)
3. [Enums](#enums)
4. [Auth Endpoints](#auth-endpoints)
5. [User Endpoints](#user-endpoints)
6. [Notification Endpoints](#notification-endpoints)
7. [Student Endpoints](#student-endpoints)
8. [Teacher Endpoints](#teacher-endpoints)
9. [Mentor Endpoints](#mentor-endpoints)
10. [Coordinator Endpoints](#coordinator-endpoints)
11. [Request DTOs](#request-dtos)
12. [Response DTOs](#response-dtos)
13. [Configuration](#configuration)

---

## Authentication & Security

- **JWT-based** stateless authentication
- Token type: `Bearer`
- Token expiration: **6 hours** (21600000 ms)
- Header: `Authorization: Bearer <token>`
- On first login, `mustChangePassword: true` → user MUST change password before proceeding
- Password requirements: min 8 chars, max 100, must contain special characters (regex validated)

### Login Flow
1. POST `/api/auth/login` → returns `AuthResponse` with `accessToken`, `roles`, `mustChangePassword`
2. If `mustChangePassword === true` → redirect to change-password page
3. POST `/api/auth/change-password` → returns new `AuthResponse` with `mustChangePassword: false`
4. Redirect to role-based dashboard

---

## Roles & Route Guards

| Role | Enum Value | Spring hasRole | API Prefix |
|------|-----------|----------------|------------|
| Student | `STUDENT` | `ROLE_STUDENT` | `/api/student/**` |
| Faculty Mentor | `FACULTY_MENTOR` | `ROLE_FACULTY_MENTOR` | `/api/mentor/**` |
| Subject Teacher | `SUBJECT_TEACHER` | `ROLE_SUBJECT_TEACHER` | `/api/teacher/**` |
| Academic Coordinator | `ACADEMIC_COORDINATOR` | `ROLE_ACADEMIC_COORDINATOR` | `/api/coordinator/**` |

**Important:** A user can have MULTIPLE roles (e.g., a teacher can be both `SUBJECT_TEACHER` and `FACULTY_MENTOR`). The `roles` field is `Set<Role>`.

### Access Rules (SecurityConfig)
- **Public** (no auth): `/api/auth/login`, `/api/auth/forgot-password`, `/api/auth/reset-password`, `/api/auth/validate-reset-token`, `/api/auth/health`
- **Authenticated** (any role): `/api/auth/change-password`, `/api/notifications/**`, `/api/users/me`
- **ACADEMIC_COORDINATOR only**: `/api/coordinator/**`
- **SUBJECT_TEACHER or FACULTY_MENTOR**: `/api/teacher/**`
- **FACULTY_MENTOR only**: `/api/mentor/**`
- **STUDENT only**: `/api/student/**`

---

## Enums

```
Role:               STUDENT | FACULTY_MENTOR | SUBJECT_TEACHER | ACADEMIC_COORDINATOR
RiskLabel:          LOW | MEDIUM | HIGH
AttendanceStatus:   PRESENT | ABSENT
AttendanceEntryMode: PER_SESSION | BULK_PERCENTAGE
SubmissionStatus:   SUBMITTED | NOT_SUBMITTED | LATE
InterventionType:   COUNSELLING_SESSION | REMEDIAL_CLASS | ASSIGNMENT_EXTENSION | PARENT_MEETING | OTHER
ActionItemStatus:   PENDING | COMPLETED
NotificationType:   HIGH_RISK_ALERT | RISK_THRESHOLD_CROSSED | PRE_EXAM_ALERT | DATA_ENTRY_REMINDER | INTERVENTION_FOLLOW_UP | STUDENT_FLAGGED | GENERAL
```

---

## Auth Endpoints

### POST `/api/auth/login` (Public)
**Request:** `{ email: string, password: string }`
**Response:** `AuthResponse`
```json
{
  "accessToken": "eyJhbG...",
  "tokenType": "Bearer",
  "userId": "uuid",
  "email": "user@example.com",
  "roles": ["ACADEMIC_COORDINATOR"],
  "instituteId": "uuid",
  "mustChangePassword": true
}
```

### POST `/api/auth/change-password` (Authenticated)
**Request:** `{ currentPassword: string, newPassword: string }`
**Response:** `AuthResponse` (new token, mustChangePassword: false)

### POST `/api/auth/forgot-password` (Public)
**Request:** `{ email: string }`
**Response:** `{ message: string, success: boolean }`

### POST `/api/auth/reset-password` (Public)
**Request:** `{ token: string, newPassword: string }`
**Response:** `{ message: string, success: boolean }`

### GET `/api/auth/validate-reset-token?token=xxx` (Public)
**Response:** `{ message: string, success: boolean }`

### GET `/api/auth/health` (Public)
**Response:** `{ message: string }`

---

## User Endpoints

### GET `/api/users/me` (Authenticated)
**Response:** `UserResponse`
```json
{
  "id": "uuid",
  "email": "string",
  "roles": ["STUDENT"],
  "instituteId": "uuid",
  "instituteName": "string",
  "mustChangePassword": false,
  "isActive": true,
  "createdAt": "2026-04-19T10:30:00"
}
```

---

## Notification Endpoints (Authenticated – any role)

### GET `/api/notifications?page=0&size=20`
**Response:** `PagedResponse<NotificationResponse>`
```json
{
  "content": [{
    "id": "uuid",
    "title": "string",
    "message": "string",
    "notificationType": "HIGH_RISK_ALERT",
    "read": false,
    "referenceId": "uuid",
    "referenceType": "string",
    "createdAt": "2026-04-19T10:30:00"
  }],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3,
  "last": false
}
```

### GET `/api/notifications/unread-count`
**Response:** `{ "count": 5 }`

### PUT `/api/notifications/mark-all-read`
**Response:** `{ message: string }`

---

## Student Endpoints (Role: STUDENT)

### GET `/api/student/dashboard`
**Response:** `StudentDashboardResponse`
```json
{
  "fullName": "string",
  "rollNumber": "string",
  "semester": 4,
  "branch": "string",
  "riskScore": 42.5,
  "riskLabel": "MEDIUM",
  "topContributingFactors": [{
    "factor": "Attendance",
    "value": 55.0,
    "classAverage": 78.0,
    "contributionPercentage": 35.0
  }],
  "improvementTips": ["Attend more classes", "Submit assignments on time"],
  "mentorName": "string",
  "mentorEmail": "string",
  "consistencyStreak": {
    "currentStreak": 3,
    "longestStreak": 5,
    "lastQualifyingWeek": "2026-04-14"
  }
}
```

### GET `/api/student/my-risk`
**Response:** `RiskScoreResponse`
```json
{
  "studentId": "uuid",
  "fullName": "string",
  "riskScore": 42.5,
  "riskLabel": "MEDIUM",
  "attendanceScore": 55.0,
  "marksScore": 40.0,
  "assignmentScore": 35.0,
  "lmsScore": 50.0,
  "computedAt": "2026-04-19T10:30:00",
  "subjectRisks": [{
    "subjectId": "uuid",
    "subjectName": "Data Structures",
    "riskScore": 45.0,
    "riskLabel": "MEDIUM",
    "attendance": 60.0,
    "marks": 50.0,
    "assignment": 40.0,
    "lms": 55.0
  }]
}
```

### GET `/api/student/my-risk-trend`
**Response:** `RiskTrendResponse`
```json
{
  "studentId": "uuid",
  "dataPoints": [{
    "date": "2026-04-01",
    "riskScore": 42.5,
    "riskLabel": "MEDIUM"
  }]
}
```

### GET `/api/student/my-flags`
**Response:** `List<StudentFlagResponse>`
```json
[{
  "id": "uuid",
  "studentName": "string",
  "studentId": "uuid",
  "flaggedByName": "Prof. Shah",
  "subjectName": "Data Structures",
  "note": "string",
  "resolved": false,
  "createdAt": "2026-04-19T10:30:00"
}]
```

### GET `/api/student/academic-data`
**Response:** `StudentAcademicDetailResponse`
```json
{
  "studentId": "uuid",
  "fullName": "string",
  "rollNumber": "string",
  "overallAttendance": 72.5,
  "overallMarks": 65.0,
  "overallAssignment": 80.0,
  "overallLms": 55.0,
  "overallRiskScore": 42.5,
  "overallRiskLabel": "MEDIUM",
  "subjects": [{
    "subjectId": "uuid",
    "subjectName": "Data Structures",
    "subjectCode": "CS301",
    "attendancePercentage": 75.0,
    "iaMarksNormalized": 60.0,
    "assignmentCompletionPercentage": 90.0,
    "lmsScore": 55.0
  }]
}
```

### POST `/api/student/what-if`
**Request:** `WhatIfRequest`
```json
{
  "studentId": "uuid (auto-set from token)",
  "hypotheticalSubjects": [{
    "subjectId": "uuid",
    "attendance": 85.0,
    "marks": 75.0,
    "assignment": 90.0,
    "lms": 70.0
  }]
}
```
**Response:** `WhatIfResponse`
```json
{
  "currentRiskScore": 42.5,
  "currentRiskLabel": "MEDIUM",
  "predictedRiskScore": 25.0,
  "predictedRiskLabel": "LOW",
  "subjectPredictions": [/* SubjectRiskResponse items */]
}
```

### GET `/api/student/interventions`
**Response:** `List<InterventionResponse>`

### GET `/api/student/consistency-streak`
**Response:** `ConsistencyStreakResponse`

---

## Teacher Endpoints (Role: SUBJECT_TEACHER or FACULTY_MENTOR)

### GET `/api/teacher/dashboard`
**Response:** `TeacherDashboardResponse`
```json
{
  "subjects": [{
    "subjectId": "uuid",
    "subjectName": "string",
    "subjectCode": "CS301",
    "className": "SE-A",
    "classId": "uuid"
  }],
  "menteesAtRisk": 5,
  "pendingDataEntryCount": 3,
  "upcomingExamAlert": {
    "subjectName": "string",
    "examDate": "2026-05-01",
    "daysUntilExam": 12,
    "highRiskMenteeCount": 3
  }
}
```

### GET `/api/teacher/subject-analytics?subjectId=uuid&classId=uuid`
**Response:** `SubjectAnalyticsResponse`
```json
{
  "subjectName": "string",
  "subjectCode": "CS301",
  "className": "SE-A",
  "classAvgAttendance": 78.0,
  "classAvgMarks": 65.0,
  "classAvgAssignment": 80.0,
  "classAvgLms": 55.0,
  "totalStudents": 60,
  "atRiskCount": 12,
  "atRiskStudents": [/* StudentProfileResponse items */]
}
```

### POST `/api/teacher/attendance`
**Request:** `AttendanceSessionRequest`
```json
{
  "subjectId": "uuid",
  "classId": "uuid",
  "sessionDate": "2026-04-19",
  "entryMode": "PER_SESSION",
  "records": [{
    "studentId": "uuid",
    "status": "PRESENT"
  }]
}
```
**Response:** `AttendanceSession` (entity)

### GET `/api/teacher/attendance?subjectId=uuid&classId=uuid`
**Response:** `List<AttendanceSession>`

### POST `/api/teacher/ia-marks`
**Request:** `IAMarksEntryRequest`
```json
{
  "subjectId": "uuid",
  "classId": "uuid",
  "iaRound": "IA-1",
  "maxMarks": 30.0,
  "entries": [{
    "studentId": "uuid",
    "obtainedMarks": 25.0,
    "absent": false
  }]
}
```
**Response:** `List<IAMarks>`

### GET `/api/teacher/ia-marks?subjectId=uuid&classId=uuid&iaRound=1`
**Response:** `List<IAMarks>`

### POST `/api/teacher/assignments`
**Request:** `CreateAssignmentRequest`
```json
{
  "subjectId": "uuid",
  "classId": "uuid",
  "title": "Assignment 1",
  "dueDate": "2026-04-30"
}
```
**Response:** `Assignment` (entity)

### POST `/api/teacher/assignments/{assignmentId}/submissions`
**Request:** `MarkSubmissionRequest`
```json
{
  "assignmentId": "uuid",
  "submissions": [{
    "studentId": "uuid",
    "status": "SUBMITTED"
  }]
}
```
**Response:** `List<AssignmentSubmission>`

### GET `/api/teacher/assignments?subjectId=uuid&classId=uuid`
**Response:** `List<Assignment>`

### POST `/api/teacher/lms-scores`
**Request:** `LMSScoreBulkRequest`
```json
{
  "subjectId": "uuid",
  "classId": "uuid",
  "entries": [{
    "studentId": "uuid",
    "score": 75.0
  }]
}
```
**Response:** `List<LMSScore>`

### GET `/api/teacher/lms-scores?subjectId=uuid&classId=uuid`
**Response:** `List<LMSScore>`

### GET `/api/teacher/my-subjects?academicYear=2025-26`
**Response:** `List<SubjectTeacherMapping>` (entity with nested subject, classEntity, teacher)

### POST `/api/teacher/flag-student`
**Request:** `FlagStudentRequest`
```json
{
  "studentId": "uuid",
  "subjectId": "uuid",
  "note": "string (optional)"
}
```
**Response:** `{ message: string }`

---

## Mentor Endpoints (Role: FACULTY_MENTOR)

### GET `/api/mentor/dashboard`
**Response:** `MentorDashboardResponse`
```json
{
  "totalMentees": 15,
  "highRiskMentees": 3,
  "mediumRiskMentees": 5,
  "lowRiskMentees": 7,
  "unresolvedFlags": 2,
  "pendingFollowUps": 1,
  "menteeSummary": [/* StudentProfileResponse items */]
}
```

### GET `/api/mentor/mentees`
**Response:** `List<StudentProfileResponse>`
```json
[{
  "id": "uuid",
  "userId": "uuid",
  "fullName": "string",
  "email": "string",
  "rollNumber": "string",
  "departmentName": "string",
  "className": "string",
  "semester": 4,
  "mentorName": "string",
  "mentorEmail": "string",
  "riskScore": 42.5,
  "riskLabel": "MEDIUM",
  "attendancePercentage": 72.0,
  "active": true
}]
```

### GET `/api/mentor/mentees/{studentId}/risk`
**Response:** `RiskScoreResponse`

### GET `/api/mentor/mentees/{studentId}/risk-trend`
**Response:** `RiskTrendResponse`

### POST `/api/mentor/interventions`
**Request:** `CreateInterventionRequest`
```json
{
  "studentId": "uuid",
  "interventionType": "COUNSELLING_SESSION",
  "interventionDate": "2026-04-19",
  "remarks": "string (optional)",
  "followUpDate": "2026-04-26 (optional)",
  "actionItems": ["Attend extra classes", "Submit pending assignments"]
}
```
**Response:** `InterventionResponse`

### GET `/api/mentor/interventions`
**Response:** `List<InterventionResponse>`
```json
[{
  "id": "uuid",
  "studentName": "string",
  "studentId": "uuid",
  "mentorName": "string",
  "interventionType": "COUNSELLING_SESSION",
  "interventionDate": "2026-04-19",
  "remarks": "string",
  "followUpDate": "2026-04-26",
  "preRiskScore": 55.0,
  "postRiskScore": 40.0,
  "scoreChange": -15.0,
  "actionItems": [{
    "id": "uuid",
    "description": "Attend extra classes",
    "status": "PENDING"
  }],
  "createdAt": "2026-04-19T10:30:00"
}]
```

### PUT `/api/mentor/interventions/action-items/{actionItemId}/complete`
**Response:** `{ message: string }`

### GET `/api/mentor/flags`
**Response:** `List<StudentFlagResponse>`

### PUT `/api/mentor/flags/{flagId}/resolve`
**Response:** `{ message: string }`

### POST `/api/mentor/compute-risk/{studentId}`
**Response:** `RiskScoreResponse`

---

## Coordinator Endpoints (Role: ACADEMIC_COORDINATOR)

### GET `/api/coordinator/dashboard`
**Response:** `InstituteDashboardResponse`
```json
{
  "totalStudents": 500,
  "totalTeachers": 50,
  "highRiskCount": 45,
  "mediumRiskCount": 120,
  "lowRiskCount": 335,
  "averageRiskScore": 32.5,
  "totalInterventions": 80,
  "departmentRiskSummaries": [{
    "departmentName": "Computer Science",
    "departmentId": "uuid",
    "highRiskCount": 15,
    "mediumRiskCount": 40,
    "lowRiskCount": 100
  }]
}
```

### GET `/api/coordinator/students?page=0&size=20`
**Response:** `Page<StudentProfileResponse>` (Spring Page format with content, totalElements, totalPages, etc.)

### GET `/api/coordinator/intervention-effectiveness`
**Response:** `List<InterventionEffectivenessResponse>`
```json
[{
  "interventionType": "COUNSELLING_SESSION",
  "count": 25,
  "avgPreScore": 55.0,
  "avgPostScore": 40.0,
  "avgImprovement": 15.0
}]
```

### GET `/api/coordinator/departments`
**Response:** `List<Department>` (entity)

### POST `/api/coordinator/departments?name=xxx&code=xxx`
**Response:** `Department` (entity)

### GET `/api/coordinator/classes?departmentId=uuid` (optional)
**Response:** `List<ClassEntity>` (entity)

### POST `/api/coordinator/classes?departmentId=uuid&name=SE-A&semester=4&academicYear=2025-26`
**Response:** `ClassEntity` (entity)

### GET `/api/coordinator/subjects?departmentId=uuid` (optional)
**Response:** `List<Subject>` (entity)

### POST `/api/coordinator/subjects?departmentId=uuid&name=xxx&code=xxx`
**Response:** `Subject` (entity)

### POST `/api/coordinator/subjects/map-to-class?subjectId=uuid&classId=uuid&semester=4&academicYear=2025-26`
**Response:** `{ message: string }`

### POST `/api/coordinator/subjects/map-teacher?teacherId=uuid&subjectId=uuid&classId=uuid&academicYear=2025-26`
**Response:** `{ message: string }`

### POST `/api/coordinator/upload/students` (multipart file)
**Response:** `CsvUploadResponse`
```json
{
  "totalRows": 50,
  "successCount": 48,
  "errorCount": 2,
  "errors": [{
    "rowNumber": 5,
    "field": "email",
    "message": "Invalid email format"
  }]
}
```

### POST `/api/coordinator/upload/teachers` (multipart file)
**Response:** `CsvUploadResponse`

### GET `/api/coordinator/csv-templates/students`
**Response:** CSV file download

### GET `/api/coordinator/csv-templates/teachers`
**Response:** CSV file download

### POST `/api/coordinator/students/manual`
**Request:** `ManualStudentRequest`
```json
{
  "rollNumber": "STU001",
  "fullName": "Raj Mehta",
  "email": "raj@pdeu.ac.in",
  "departmentId": "uuid",
  "classId": "uuid",
  "semester": 4,
  "mentorId": "uuid (optional)"
}
```
**Response:** `StudentProfileResponse`

### POST `/api/coordinator/teachers/manual`
**Request:** `ManualTeacherRequest`
```json
{
  "employeeId": "EMP001",
  "fullName": "Prof. Shah",
  "email": "prof.shah@pdeu.ac.in",
  "departmentId": "uuid",
  "isSubjectTeacher": true,
  "isFacultyMentor": true
}
```
**Response:** `TeacherProfileResponse`

### GET `/api/coordinator/teachers?page=0&size=20`
**Response:** `Page<TeacherProfileResponse>`

### PUT `/api/coordinator/students/reassign-mentor`
**Request:** `{ studentId: "uuid", newMentorId: "uuid" }`
**Response:** `{ message: string }`

### PUT `/api/coordinator/users/{userId}/deactivate`
**Response:** `{ message: string }`

### PUT `/api/coordinator/users/{userId}/activate`
**Response:** `{ message: string }`

### POST `/api/coordinator/exam-schedules`
**Request:** `CreateExamScheduleRequest`
```json
{
  "subjectId": "uuid",
  "classId": "uuid",
  "examDate": "2026-05-01",
  "examType": "Mid-Semester"
}
```
**Response:** `ExamSchedule` (entity)

### GET `/api/coordinator/exam-schedules?startDate=2026-04-01&endDate=2026-06-30`
**Response:** `List<ExamSchedule>`

### GET `/api/coordinator/export/risk-report`
**Response:** PDF file download

### GET `/api/coordinator/export/student-report/{studentId}`
**Response:** PDF file download

### POST `/api/coordinator/recompute-risk`
**Response:** `{ message: string }`

---

## Configuration

| Property | Value |
|----------|-------|
| Backend Port | 8080 |
| Frontend Port (CORS) | 5173 |
| JWT Expiry | 6 hours |
| ML Service | http://localhost:8000 |
| Risk Threshold LOW | ≤ 35.0 |
| Risk Threshold MEDIUM | ≤ 55.0 |
| Risk Threshold HIGH | > 55.0 |
| Max File Upload | 10MB |
| Default Page Size | 20 |
| Max Page Size | 100 |

---

## Seeded Test Accounts

| Email | Password | Institute |
|-------|----------|-----------|
| coordinator@pdeu.ac.in | PDEU@Coord2026 | PDEU |
| coordinator@nirmauni.ac.in | Nirma@Coord2026 | Nirma University |
| coordinator@ldeuni.ac.in | LDE@Coord2026 | LDE University |

> All accounts have `mustChangePassword: true` on first login.
