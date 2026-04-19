import { useEffect } from "react";
import { useSearchParams } from "react-router";
import { useQuery } from "@tanstack/react-query";
import { Users, AlertTriangle } from "lucide-react";
import useUiStore from "../../stores/uiStore";
import { getSubjectAnalytics, getMySubjects } from "../../api/teacher";
import Card, { CardTitle } from "../../components/ui/Card";
import StatCard from "../../components/ui/StatCard";
import Select from "../../components/ui/Select";
import RiskBadge from "../../components/ui/RiskBadge";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import EmptyState from "../../components/ui/EmptyState";
import RiskDistributionPie from "../../components/charts/RiskDistributionPie";

export default function SubjectAnalytics() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Subject Analytics"), [setPageTitle]);

  const [searchParams, setSearchParams] = useSearchParams();
  const subjectId = searchParams.get("subjectId") || "";
  const classId = searchParams.get("classId") || "";

  const { data: subjects = [] } = useQuery({
    queryKey: ["teacher", "my-subjects"],
    queryFn: async () => {
      const { data } = await getMySubjects("2025-26");
      return data;
    },
  });

  const subjectOptions = subjects.map((s) => ({
    value: `${s.subject?.id || s.subjectId}__${s.classEntity?.id || s.classId}`,
    label: `${s.subject?.name || s.subjectName} - ${s.classEntity?.name || s.className}`,
  }));

  const { data: analytics, isLoading } = useQuery({
    queryKey: ["teacher", "subject-analytics", subjectId, classId],
    queryFn: async () => {
      const { data } = await getSubjectAnalytics(subjectId, classId);
      return data;
    },
    enabled: !!subjectId && !!classId,
  });

  const handleSubjectChange = (val) => {
    const [sId, cId] = val.split("__");
    setSearchParams({ subjectId: sId, classId: cId });
  };

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">Subject Analytics</h2>

      <Select
        label="Select Subject"
        placeholder="Choose a subject..."
        options={subjectOptions}
        value={subjectId && classId ? `${subjectId}__${classId}` : ""}
        onValueChange={handleSubjectChange}
      />

      {!subjectId || !classId ? (
        <EmptyState title="Select a subject" description="Choose a subject above to view analytics." />
      ) : isLoading ? (
        <LoadingSpinner size="lg" className="mt-16" />
      ) : analytics ? (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard title="Avg Attendance" value={`${analytics.classAvgAttendance?.toFixed(1)}%`} />
            <StatCard title="Avg Marks" value={`${analytics.classAvgMarks?.toFixed(1)}%`} />
            <StatCard title="Avg Assignments" value={`${analytics.classAvgAssignment?.toFixed(1)}%`} />
            <StatCard title="Avg LMS" value={`${analytics.classAvgLms?.toFixed(1)}%`} />
          </div>

          <div className="grid md:grid-cols-2 gap-4">
            <StatCard
              title="Total Students"
              value={analytics.totalStudents}
              icon={Users}
            />
            <StatCard
              title="At Risk Students"
              value={analytics.atRiskCount}
              icon={AlertTriangle}
            />
          </div>

          {/* Risk Distribution Pie */}
          {analytics.totalStudents > 0 && (
            <Card>
              <CardTitle>Risk Distribution</CardTitle>
              <RiskDistributionPie
                data={[
                  { name: "At Risk", value: analytics.atRiskCount || 0 },
                  { name: "Safe", value: (analytics.totalStudents || 0) - (analytics.atRiskCount || 0) },
                ]}
                height={260}
              />
            </Card>
          )}

          {analytics.atRiskStudents?.length > 0 && (
            <Card>
              <CardTitle>At-Risk Students</CardTitle>
              <div className="overflow-x-auto mt-4">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-border-light">
                      <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Name</th>
                      <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Roll No</th>
                      <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Risk Score</th>
                      <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Risk</th>
                      <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Attendance</th>
                    </tr>
                  </thead>
                  <tbody>
                    {analytics.atRiskStudents.map((s) => (
                      <tr key={s.id || s.userId} className="border-b border-border-light last:border-0">
                        <td className="px-4 py-3 font-medium text-text-primary">{s.fullName}</td>
                        <td className="px-4 py-3 text-text-secondary">{s.rollNumber}</td>
                        <td className="px-4 py-3 text-right font-medium">{s.riskScore?.toFixed(1)}</td>
                        <td className="px-4 py-3 text-right"><RiskBadge label={s.riskLabel} /></td>
                        <td className="px-4 py-3 text-right text-text-secondary">{s.attendancePercentage?.toFixed(1)}%</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          )}
        </>
      ) : null}
    </div>
  );
}
