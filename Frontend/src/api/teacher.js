import api from "./axios";

export const getTeacherDashboard = () =>
  api.get("/teacher/dashboard");

export const getStudentsByClass = (classId) =>
  api.get(`/teacher/students-by-class?classId=${classId}`);

export const getSubjectAnalytics = (subjectId, classId) =>
  api.get(`/teacher/subject-analytics?subjectId=${subjectId}&classId=${classId}`);

export const getMySubjects = (academicYear) =>
  api.get(`/teacher/my-subjects?academicYear=${encodeURIComponent(academicYear)}`);

// Attendance
export const createAttendanceSession = (data) =>
  api.post("/teacher/attendance", data);

export const getAttendanceHistory = (subjectId, classId) =>
  api.get(`/teacher/attendance?subjectId=${subjectId}&classId=${classId}`);

// IA Marks
export const submitIAMarks = (data) =>
  api.post("/teacher/ia-marks", data);

export const getIAMarks = (subjectId, classId, iaRound) =>
  api.get(`/teacher/ia-marks?subjectId=${subjectId}&classId=${classId}&iaRound=${iaRound}`);

// Assignments
export const createAssignment = (data) =>
  api.post("/teacher/assignments", data);

export const getAssignments = (subjectId, classId) =>
  api.get(`/teacher/assignments?subjectId=${subjectId}&classId=${classId}`);

export const markSubmissions = (assignmentId, data) =>
  api.post(`/teacher/assignments/${assignmentId}/submissions`, data);

// LMS Scores
export const submitLMSScores = (data) =>
  api.post("/teacher/lms-scores", data);

export const getLMSScores = (subjectId, classId) =>
  api.get(`/teacher/lms-scores?subjectId=${subjectId}&classId=${classId}`);

// Flag Student
export const flagStudent = (data) =>
  api.post("/teacher/flag-student", data);
