import api from "./axios";

export const getStudentDashboard = () =>
  api.get("/student/dashboard");

export const getMyRisk = () =>
  api.get("/student/my-risk");

export const getMyRiskTrend = () =>
  api.get("/student/my-risk-trend");

export const getMyFlags = () =>
  api.get("/student/my-flags");

export const getAcademicData = () =>
  api.get("/student/academic-data");

export const calculateWhatIf = (data) =>
  api.post("/student/what-if", data);

export const getInterventions = () =>
  api.get("/student/interventions");

export const getConsistencyStreak = () =>
  api.get("/student/consistency-streak");

export const getSuggestions = () =>
  api.get("/student/suggestions");

export const getRoadmap = () =>
  api.get("/student/roadmap");

export const regenerateRoadmap = () =>
  api.post("/student/roadmap/regenerate");
