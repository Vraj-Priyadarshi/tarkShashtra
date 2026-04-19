import { createBrowserRouter, Navigate } from "react-router";
import PublicLayout from "../components/layout/PublicLayout";
import DashboardLayout from "../components/layout/DashboardLayout";
import ProtectedRoute from "./ProtectedRoute";
import RoleRoute from "./RoleRoute";

// Public pages
import LandingPage from "../pages/public/LandingPage";
import AboutPage from "../pages/public/AboutPage";
import LoginPage from "../pages/public/LoginPage";
import ChangePasswordPage from "../pages/public/ChangePasswordPage";
import ForgotPasswordPage from "../pages/public/ForgotPasswordPage";
import ResetPasswordPage from "../pages/public/ResetPasswordPage";

// Student pages
import StudentDashboard from "../pages/student/StudentDashboard";
import AcademicData from "../pages/student/AcademicData";
import WhatIfCalculator from "../pages/student/WhatIfCalculator";
import MyFlags from "../pages/student/MyFlags";
import InterventionHistory from "../pages/student/InterventionHistory";
import StudentNotifications from "../pages/student/NotificationsPage";

// Teacher pages
import TeacherDashboard from "../pages/teacher/TeacherDashboard";
import SubjectAnalytics from "../pages/teacher/SubjectAnalytics";
import AttendanceEntry from "../pages/teacher/AttendanceEntry";
import IAMarksEntry from "../pages/teacher/IAMarksEntry";
import AssignmentManagement from "../pages/teacher/AssignmentManagement";
import LMSScoreEntry from "../pages/teacher/LMSScoreEntry";
import FlagStudent from "../pages/teacher/FlagStudent";
import TeacherNotifications from "../pages/teacher/NotificationsPage";

// Mentor pages
import MentorDashboard from "../pages/mentor/MentorDashboard";
import MenteeList from "../pages/mentor/MenteeList";
import MenteeDetail from "../pages/mentor/MenteeDetail";
import Interventions from "../pages/mentor/Interventions";
import StudentFlags from "../pages/mentor/StudentFlags";
import MentorNotifications from "../pages/mentor/NotificationsPage";

// Coordinator pages
import CoordinatorDashboard from "../pages/coordinator/CoordinatorDashboard";
import StudentManagement from "../pages/coordinator/StudentManagement";
import TeacherManagement from "../pages/coordinator/TeacherManagement";
import InstituteSetup from "../pages/coordinator/InstituteSetup";
import CsvUpload from "../pages/coordinator/CsvUpload";
import ExamSchedules from "../pages/coordinator/ExamSchedules";
import InterventionReport from "../pages/coordinator/InterventionReport";
import CoordinatorNotifications from "../pages/coordinator/NotificationsPage";

// Dashboard redirect helper
function DashboardRedirect() {
  // This will be handled by the component itself
  return <Navigate to="/login" replace />;
}

const router = createBrowserRouter([
  // Public routes
  {
    element: <PublicLayout />,
    children: [
      { path: "/", element: <LandingPage /> },
      { path: "/about", element: <AboutPage /> },
      { path: "/login", element: <LoginPage /> },
      { path: "/change-password", element: <ChangePasswordPage /> },
      { path: "/forgot-password", element: <ForgotPasswordPage /> },
      { path: "/reset-password", element: <ResetPasswordPage /> },
    ],
  },

  // Protected routes
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          // Dashboard redirect
          { path: "/dashboard", element: <DashboardRedirect /> },

          // Student routes
          {
            element: <RoleRoute allowedRoles={["STUDENT"]} />,
            children: [
              { path: "/student/dashboard", element: <StudentDashboard /> },
              { path: "/student/academics", element: <AcademicData /> },
              { path: "/student/what-if", element: <WhatIfCalculator /> },
              { path: "/student/flags", element: <MyFlags /> },
              { path: "/student/interventions", element: <InterventionHistory /> },
              { path: "/student/notifications", element: <StudentNotifications /> },
            ],
          },

          // Teacher routes
          {
            element: <RoleRoute allowedRoles={["SUBJECT_TEACHER", "FACULTY_MENTOR"]} />,
            children: [
              { path: "/teacher/dashboard", element: <TeacherDashboard /> },
              { path: "/teacher/subject-analytics", element: <SubjectAnalytics /> },
              { path: "/teacher/attendance", element: <AttendanceEntry /> },
              { path: "/teacher/ia-marks", element: <IAMarksEntry /> },
              { path: "/teacher/assignments", element: <AssignmentManagement /> },
              { path: "/teacher/lms-scores", element: <LMSScoreEntry /> },
              { path: "/teacher/flag-student", element: <FlagStudent /> },
              { path: "/teacher/notifications", element: <TeacherNotifications /> },
            ],
          },

          // Mentor routes
          {
            element: <RoleRoute allowedRoles={["FACULTY_MENTOR"]} />,
            children: [
              { path: "/mentor/dashboard", element: <MentorDashboard /> },
              { path: "/mentor/mentees", element: <MenteeList /> },
              { path: "/mentor/mentees/:studentId", element: <MenteeDetail /> },
              { path: "/mentor/interventions", element: <Interventions /> },
              { path: "/mentor/flags", element: <StudentFlags /> },
              { path: "/mentor/notifications", element: <MentorNotifications /> },
            ],
          },

          // Coordinator routes
          {
            element: <RoleRoute allowedRoles={["ACADEMIC_COORDINATOR"]} />,
            children: [
              { path: "/coordinator/dashboard", element: <CoordinatorDashboard /> },
              { path: "/coordinator/students", element: <StudentManagement /> },
              { path: "/coordinator/teachers", element: <TeacherManagement /> },
              { path: "/coordinator/institute-setup", element: <InstituteSetup /> },
              { path: "/coordinator/csv-upload", element: <CsvUpload /> },
              { path: "/coordinator/exam-schedules", element: <ExamSchedules /> },
              { path: "/coordinator/intervention-reports", element: <InterventionReport /> },
              { path: "/coordinator/notifications", element: <CoordinatorNotifications /> },
            ],
          },
        ],
      },
    ],
  },
]);

export default router;
