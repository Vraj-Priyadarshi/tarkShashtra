import api from "./axios";

export const getMentorDashboard = () =>
  api.get("/mentor/dashboard");

export const getMentees = () =>
  api.get("/mentor/mentees");

export const getMenteeRisk = (studentId) =>
  api.get(`/mentor/mentees/${studentId}/risk`);

export const getMenteeRiskTrend = (studentId) =>
  api.get(`/mentor/mentees/${studentId}/risk-trend`);

// Interventions
export const createIntervention = (data) =>
  api.post("/mentor/interventions", data);

export const getInterventions = () =>
  api.get("/mentor/interventions");

export const completeActionItem = (actionItemId) =>
  api.put(`/mentor/interventions/action-items/${actionItemId}/complete`);

// Flags
export const getFlags = () =>
  api.get("/mentor/flags");

export const resolveFlag = (flagId) =>
  api.put(`/mentor/flags/${flagId}/resolve`);

// Risk computation
export const computeRisk = (studentId) =>
  api.post(`/mentor/compute-risk/${studentId}`);
