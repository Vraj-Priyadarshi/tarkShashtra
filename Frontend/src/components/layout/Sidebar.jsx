import { useState } from "react";
import { useLocation } from "react-router";
import { useQuery } from "@tanstack/react-query";
import {
  LayoutDashboard,
  BookOpen,
  Calculator,
  Flag,
  HeartHandshake,
  Bell,
  BarChart3,
  ClipboardList,
  FileSpreadsheet,
  FileText,
  Monitor,
  Users,
  AlertTriangle,
  GraduationCap,
  UserCog,
  Building2,
  Upload,
  Calendar,
  TrendingUp,
  LogOut,
  ArrowLeftRight,
  KeyRound,
  User,
  X,
} from "lucide-react";
import useAuthStore from "../../stores/authStore";
import { changePassword as changePasswordApi } from "../../api/auth";
import { getMe } from "../../api/user";
import toast from "react-hot-toast";
import SidebarNavItem from "./SidebarNavItem";
import Avatar from "../ui/Avatar";
import Separator from "../ui/Separator";

const studentNav = [
  { to: "/student/dashboard", icon: LayoutDashboard, label: "Dashboard" },
  { to: "/student/academics", icon: BookOpen, label: "Academics" },
  { to: "/student/what-if", icon: Calculator, label: "What-If Calculator" },
  { to: "/student/flags", icon: Flag, label: "My Flags" },
  { to: "/student/interventions", icon: HeartHandshake, label: "Interventions" },
  { to: "/student/notifications", icon: Bell, label: "Notifications" },
];

const teacherNav = [
  { to: "/teacher/dashboard", icon: LayoutDashboard, label: "Dashboard" },
  { to: "/teacher/subject-analytics", icon: BarChart3, label: "Subject Analytics" },
  { to: "/teacher/attendance", icon: ClipboardList, label: "Attendance" },
  { to: "/teacher/ia-marks", icon: FileSpreadsheet, label: "IA Marks" },
  { to: "/teacher/assignments", icon: FileText, label: "Assignments" },
  { to: "/teacher/lms-scores", icon: Monitor, label: "LMS Scores" },
  { to: "/teacher/flag-student", icon: Flag, label: "Flag Student" },
  { to: "/teacher/notifications", icon: Bell, label: "Notifications" },
];

const mentorNav = [
  { to: "/mentor/dashboard", icon: LayoutDashboard, label: "Dashboard" },
  { to: "/mentor/mentees", icon: Users, label: "Mentees" },
  { to: "/mentor/interventions", icon: HeartHandshake, label: "Interventions" },
  { to: "/mentor/flags", icon: AlertTriangle, label: "Student Flags" },
  { to: "/mentor/notifications", icon: Bell, label: "Notifications" },
];

const coordinatorNav = [
  { to: "/coordinator/dashboard", icon: LayoutDashboard, label: "Dashboard" },
  { to: "/coordinator/students", icon: GraduationCap, label: "Students" },
  { to: "/coordinator/teachers", icon: UserCog, label: "Teachers" },
  { to: "/coordinator/institute-setup", icon: Building2, label: "Institute Setup" },
  { to: "/coordinator/csv-upload", icon: Upload, label: "CSV Upload" },
  { to: "/coordinator/exam-schedules", icon: Calendar, label: "Exam Schedules" },
  { to: "/coordinator/intervention-reports", icon: TrendingUp, label: "Intervention Reports" },
  { to: "/coordinator/notifications", icon: Bell, label: "Notifications" },
];

export default function Sidebar() {
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const storeLogin = useAuthStore((s) => s.login);
  const activeView = useAuthStore((s) => s.activeView);
  const setActiveView = useAuthStore((s) => s.setActiveView);
  const hasRole = useAuthStore((s) => s.hasRole);

  const [showChangePw, setShowChangePw] = useState(false);
  const [showProfile, setShowProfile] = useState(false);
  const [pwForm, setPwForm] = useState({ current: "", next: "", confirm: "" });
  const [pwLoading, setPwLoading] = useState(false);

  const { data: profile } = useQuery({
    queryKey: ["user-profile"],
    queryFn: () => getMe().then((r) => r.data),
    staleTime: 5 * 60 * 1000,
  });

  const roles = user?.roles || [];
  const isCoordinator = roles.includes("ACADEMIC_COORDINATOR");
  const isTeacher = roles.includes("SUBJECT_TEACHER");
  const isMentor = roles.includes("FACULTY_MENTOR");
  const isStudent = roles.includes("STUDENT");
  const isDualRole = isTeacher && isMentor;

  let navItems = [];
  if (isCoordinator) {
    navItems = coordinatorNav;
  } else if (isDualRole) {
    navItems = activeView === "mentor" ? mentorNav : teacherNav;
  } else if (isMentor) {
    navItems = mentorNav;
  } else if (isTeacher) {
    navItems = teacherNav;
  } else if (isStudent) {
    navItems = studentNav;
  }

  return (
    <aside className="w-64 bg-bg-sidebar min-h-screen fixed left-0 top-0 flex flex-col z-40">
      {/* Logo */}
      <div className="px-6 py-5 flex items-center gap-3">
        <div className="w-8 h-8 rounded-full bg-accent-primary flex items-center justify-center">
          <span className="text-white font-bold text-sm">T</span>
        </div>
        <span className="text-white font-semibold text-lg">TarkShastra</span>
      </div>

      <Separator className="bg-white/10" />

      {/* Role Switcher for dual-role */}
      {isDualRole && (
        <div className="px-4 pt-4 pb-2">
          <div className="flex rounded-xl bg-white/5 p-1">
            <button
              onClick={() => setActiveView("teacher")}
              className={`flex-1 flex items-center justify-center gap-1.5 px-3 py-2 rounded-lg text-xs font-medium transition-all ${
                activeView === "teacher"
                  ? "bg-accent-primary text-white shadow-sm"
                  : "text-white/50 hover:text-white/80"
              }`}
            >
              <BookOpen className="w-3.5 h-3.5" />
              Teacher
            </button>
            <button
              onClick={() => setActiveView("mentor")}
              className={`flex-1 flex items-center justify-center gap-1.5 px-3 py-2 rounded-lg text-xs font-medium transition-all ${
                activeView === "mentor"
                  ? "bg-accent-primary text-white shadow-sm"
                  : "text-white/50 hover:text-white/80"
              }`}
            >
              <HeartHandshake className="w-3.5 h-3.5" />
              Mentor
            </button>
          </div>
        </div>
      )}

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {navItems.map((item) => (
          <SidebarNavItem key={item.to} {...item} />
        ))}
      </nav>

      <Separator className="bg-white/10" />

      {/* User section */}
      <div className="p-4 space-y-1">
        <button
          onClick={() => setShowProfile(true)}
          className="flex items-center gap-3 px-1 mb-2 w-full rounded-xl hover:bg-white/5 py-1 transition-colors cursor-pointer"
        >
          <Avatar name={profile?.fullName || user?.email} size="sm" className="bg-white/10" />
          <div className="flex-1 min-w-0 text-left">
            <p className="text-sm text-white font-medium truncate">
              {profile?.fullName || user?.email?.split("@")[0]}
            </p>
            <p className="text-xs text-white/40 truncate">{user?.email}</p>
          </div>
        </button>
        <button
          onClick={() => { setPwForm({ current: "", next: "", confirm: "" }); setShowChangePw(true); }}
          className="flex items-center gap-2 w-full px-3 py-2 rounded-xl text-white/60 hover:text-white hover:bg-white/5 text-sm transition-colors"
        >
          <KeyRound className="w-4 h-4" />
          Change Password
        </button>
        <button
          onClick={logout}
          className="flex items-center gap-2 w-full px-3 py-2 rounded-xl text-white/60 hover:text-white hover:bg-white/5 text-sm transition-colors"
        >
          <LogOut className="w-4 h-4" />
          Sign out
        </button>
      </div>

      {/* Change Password inline panel */}
      {showChangePw && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" onClick={() => setShowChangePw(false)}>
          <div className="bg-bg-primary rounded-2xl shadow-2xl p-6 w-full max-w-sm mx-4" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-base font-semibold text-text-primary mb-4">Change Password</h3>
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                if (pwForm.next !== pwForm.confirm) {
                  toast.error("New passwords do not match");
                  return;
                }
                if (pwForm.next.length < 8) {
                  toast.error("New password must be at least 8 characters");
                  return;
                }
                setPwLoading(true);
                try {
                  const { data } = await changePasswordApi(pwForm.current, pwForm.next);
                  storeLogin(data);
                  toast.success("Password updated successfully");
                  setShowChangePw(false);
                } catch (err) {
                  toast.error(err.response?.data?.message || "Failed to change password");
                } finally {
                  setPwLoading(false);
                }
              }}
              className="space-y-3"
            >
              <div className="space-y-1">
                <label className="text-xs font-medium text-text-secondary">Current Password</label>
                <input
                  type="password"
                  value={pwForm.current}
                  onChange={(e) => setPwForm((f) => ({ ...f, current: e.target.value }))}
                  required
                  className="w-full px-3 py-2.5 text-sm bg-bg-secondary border border-border-light rounded-xl text-text-primary focus:outline-none focus:ring-2 focus:ring-accent-primary/30 focus:border-accent-primary"
                  placeholder="Enter current password"
                />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-text-secondary">New Password</label>
                <input
                  type="password"
                  value={pwForm.next}
                  onChange={(e) => setPwForm((f) => ({ ...f, next: e.target.value }))}
                  required
                  className="w-full px-3 py-2.5 text-sm bg-bg-secondary border border-border-light rounded-xl text-text-primary focus:outline-none focus:ring-2 focus:ring-accent-primary/30 focus:border-accent-primary"
                  placeholder="Min 8 characters"
                />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-text-secondary">Confirm New Password</label>
                <input
                  type="password"
                  value={pwForm.confirm}
                  onChange={(e) => setPwForm((f) => ({ ...f, confirm: e.target.value }))}
                  required
                  className="w-full px-3 py-2.5 text-sm bg-bg-secondary border border-border-light rounded-xl text-text-primary focus:outline-none focus:ring-2 focus:ring-accent-primary/30 focus:border-accent-primary"
                  placeholder="Repeat new password"
                />
              </div>
              <div className="flex gap-2 pt-1">
                <button
                  type="button"
                  onClick={() => setShowChangePw(false)}
                  className="flex-1 px-4 py-2.5 text-sm rounded-xl border border-border-light text-text-secondary hover:bg-bg-hover transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={pwLoading}
                  className="flex-1 px-4 py-2.5 text-sm rounded-xl bg-accent-primary text-white hover:bg-accent-primary/90 transition-colors disabled:opacity-60"
                >
                  {pwLoading ? "Saving…" : "Save"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Profile Modal */}
      {showProfile && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" onClick={() => setShowProfile(false)}>
          <div className="bg-bg-primary rounded-2xl shadow-2xl w-full max-w-sm mx-4" onClick={(e) => e.stopPropagation()}>
            {/* Header */}
            <div className="flex items-center justify-between px-6 pt-5 pb-3">
              <h3 className="text-base font-semibold text-text-primary">My Profile</h3>
              <button onClick={() => setShowProfile(false)} className="text-text-muted hover:text-text-primary transition-colors">
                <X className="w-4 h-4" />
              </button>
            </div>
            {/* Avatar + Name */}
            <div className="flex flex-col items-center pb-4">
              <Avatar name={profile?.fullName || user?.email} size="lg" className="bg-accent-primary/10 text-accent-primary w-16 h-16 text-xl mb-3" />
              <p className="text-lg font-semibold text-text-primary">{profile?.fullName || "—"}</p>
              <p className="text-sm text-text-muted">{user?.email}</p>
              <div className="flex gap-1.5 mt-2">
                {roles.map((r) => (
                  <span key={r} className="px-2 py-0.5 text-[10px] font-medium rounded-full bg-accent-primary/10 text-accent-primary">
                    {r.replace(/_/g, " ")}
                  </span>
                ))}
              </div>
            </div>
            <Separator />
            {/* Details */}
            <div className="px-6 py-4 space-y-3 text-sm">
              <ProfileRow label="Institute" value={profile?.instituteName} />
              {profile?.departmentName && <ProfileRow label="Department" value={profile.departmentName} />}
              {profile?.rollNumber && <ProfileRow label="Roll Number" value={profile.rollNumber} />}
              {profile?.className && <ProfileRow label="Class" value={profile.className} />}
              {profile?.semester && <ProfileRow label="Semester" value={profile.semester} />}
              {profile?.employeeId && <ProfileRow label="Employee ID" value={profile.employeeId} />}
              {profile?.mentorName && <ProfileRow label="Faculty Mentor" value={profile.mentorName} />}
            </div>
          </div>
        </div>
      )}
    </aside>
  );
}

function ProfileRow({ label, value }) {
  return (
    <div className="flex justify-between">
      <span className="text-text-muted">{label}</span>
      <span className="text-text-primary font-medium">{value || "—"}</span>
    </div>
  );
}
