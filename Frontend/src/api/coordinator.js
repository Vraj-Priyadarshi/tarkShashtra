import api from "./axios";

// Dashboard
export const getCoordinatorDashboard = () =>
  api.get("/coordinator/dashboard");

// Students
export const getStudents = (page = 0, size = 20) =>
  api.get(`/coordinator/students?page=${page}&size=${size}`);

export const addStudentManual = (data) =>
  api.post("/coordinator/students/manual", data);

export const reassignMentor = (studentId, newMentorId) =>
  api.put("/coordinator/students/reassign-mentor", { studentId, newMentorId });

// Teachers
export const getTeachers = (page = 0, size = 20) =>
  api.get(`/coordinator/teachers?page=${page}&size=${size}`);

export const addTeacherManual = (data) =>
  api.post("/coordinator/teachers/manual", data);

// User activation
export const deactivateUser = (userId) =>
  api.put(`/coordinator/users/${userId}/deactivate`);

export const activateUser = (userId) =>
  api.put(`/coordinator/users/${userId}/activate`);

// Departments
export const getDepartments = () =>
  api.get("/coordinator/departments");

export const createDepartment = (name, code) =>
  api.post(`/coordinator/departments?name=${encodeURIComponent(name)}&code=${encodeURIComponent(code)}`);

// Classes
export const getClasses = (departmentId) => {
  const params = departmentId ? `?departmentId=${departmentId}` : "";
  return api.get(`/coordinator/classes${params}`);
};

export const createClass = (departmentId, name, semester, academicYear) =>
  api.post(`/coordinator/classes?departmentId=${departmentId}&name=${encodeURIComponent(name)}&semester=${semester}&academicYear=${encodeURIComponent(academicYear)}`);

// Subjects
export const getSubjects = (departmentId) => {
  const params = departmentId ? `?departmentId=${departmentId}` : "";
  return api.get(`/coordinator/subjects${params}`);
};

export const createSubject = (departmentId, name, code) =>
  api.post(`/coordinator/subjects?departmentId=${departmentId}&name=${encodeURIComponent(name)}&code=${encodeURIComponent(code)}`);

// Mappings
export const mapSubjectToClass = (subjectId, classId, semester, academicYear) =>
  api.post(`/coordinator/subjects/map-to-class?subjectId=${subjectId}&classId=${classId}&semester=${semester}&academicYear=${encodeURIComponent(academicYear)}`);

export const mapTeacherToSubject = (teacherId, subjectId, classId, academicYear) =>
  api.post(`/coordinator/subjects/map-teacher?teacherId=${teacherId}&subjectId=${subjectId}&classId=${classId}&academicYear=${encodeURIComponent(academicYear)}`);

// CSV Upload
export const uploadStudentsCsv = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return api.post("/coordinator/upload/students", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const uploadTeachersCsv = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return api.post("/coordinator/upload/teachers", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const downloadStudentTemplate = () =>
  api.get("/coordinator/csv-templates/students", { responseType: "blob" });

export const downloadTeacherTemplate = () =>
  api.get("/coordinator/csv-templates/teachers", { responseType: "blob" });

// Exam Schedules
export const createExamSchedule = (data) =>
  api.post("/coordinator/exam-schedules", data);

export const getExamSchedules = (startDate, endDate) =>
  api.get(`/coordinator/exam-schedules?startDate=${startDate}&endDate=${endDate}`);

// Reports
export const getInterventionEffectiveness = () =>
  api.get("/coordinator/intervention-effectiveness");

export const exportRiskReport = () =>
  api.get("/coordinator/export/risk-report", { responseType: "blob" });

export const exportStudentReport = (studentId) =>
  api.get(`/coordinator/export/student-report/${studentId}`, { responseType: "blob" });

// Recompute risk
export const recomputeAllRisk = () =>
  api.post("/coordinator/recompute-risk");
