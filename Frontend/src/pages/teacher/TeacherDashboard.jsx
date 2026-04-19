import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router";
import { BookOpen, AlertTriangle, ClipboardList, Calendar } from "lucide-react";
import useUiStore from "../../stores/uiStore";
import { getTeacherDashboard } from "../../api/teacher";
import Card, { CardTitle } from "../../components/ui/Card";
import StatCard from "../../components/ui/StatCard";
import Badge from "../../components/ui/Badge";
import Button from "../../components/ui/Button";
import LoadingSpinner from "../../components/ui/LoadingSpinner";

export default function TeacherDashboard() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Teacher Dashboard"), [setPageTitle]);
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: ["teacher", "dashboard"],
    queryFn: async () => {
      const { data } = await getTeacherDashboard();
      return data;
    },
  });

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;
  if (!data) return null;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">Teacher Dashboard</h2>

      {/* Subject Cards */}
      <div>
        <h3 className="text-lg font-semibold text-text-primary mb-4">My Subjects</h3>
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {data.subjects?.map((subj) => (
            <Card
              key={`${subj.subjectId}-${subj.classId}`}
              hover
              onClick={() =>
                navigate(
                  `/teacher/subject-analytics?subjectId=${subj.subjectId}&classId=${subj.classId}`
                )
              }
            >
              <div className="flex items-start gap-3">
                <div className="p-2 bg-accent-secondary/10 rounded-lg">
                  <BookOpen className="w-5 h-5 text-accent-secondary" />
                </div>
                <div>
                  <p className="text-sm font-medium text-text-primary">
                    {subj.subjectName}
                  </p>
                  <p className="text-xs text-text-secondary">{subj.subjectCode}</p>
                  <p className="text-xs text-text-tertiary mt-1">{subj.className}</p>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>

      {/* Alerts */}
      <div className="grid md:grid-cols-3 gap-4">
        <StatCard
          title="Mentees at Risk"
          value={data.menteesAtRisk || 0}
          icon={AlertTriangle}
        />
        <StatCard
          title="Pending Data Entry"
          value={data.pendingDataEntryCount || 0}
          icon={ClipboardList}
        />
        {data.upcomingExamAlert && (
          <Card>
            <div className="flex items-start gap-3">
              <div className="p-2 bg-status-warning-bg rounded-lg">
                <Calendar className="w-5 h-5 text-status-warning" />
              </div>
              <div>
                <p className="text-xs text-text-tertiary uppercase">Upcoming Exam</p>
                <p className="text-sm font-medium text-text-primary mt-1">
                  {data.upcomingExamAlert.subjectName}
                </p>
                <p className="text-xs text-text-secondary">
                  {data.upcomingExamAlert.daysUntilExam} days away
                </p>
                <Badge variant="error" className="mt-1">
                  {data.upcomingExamAlert.highRiskMenteeCount} high-risk
                </Badge>
              </div>
            </div>
          </Card>
        )}
      </div>

      {/* Quick Actions */}
      <Card>
        <CardTitle>Quick Actions</CardTitle>
        <div className="flex flex-wrap gap-3 mt-4">
          <Button size="sm" onClick={() => navigate("/teacher/attendance")}>
            Enter Attendance
          </Button>
          <Button
            size="sm"
            variant="secondary"
            onClick={() => navigate("/teacher/ia-marks")}
          >
            Enter Marks
          </Button>
          <Button
            size="sm"
            variant="secondary"
            onClick={() => navigate("/teacher/assignments")}
          >
            Create Assignment
          </Button>
        </div>
      </Card>
    </div>
  );
}
