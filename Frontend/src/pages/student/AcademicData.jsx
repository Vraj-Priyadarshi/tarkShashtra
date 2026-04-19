import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { ClipboardCheck, FileText, Monitor, BookOpen } from "lucide-react";
import useUiStore from "../../stores/uiStore";
import { getAcademicData } from "../../api/student";
import Card, { CardTitle } from "../../components/ui/Card";
import StatCard from "../../components/ui/StatCard";
import RiskBadge from "../../components/ui/RiskBadge";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import EmptyState from "../../components/ui/EmptyState";
import AcademicMetricsBar from "../../components/charts/AcademicMetricsBar";

export default function AcademicData() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Academic Data"), [setPageTitle]);

  const { data, isLoading } = useQuery({
    queryKey: ["student", "academic-data"],
    queryFn: async () => {
      const { data } = await getAcademicData();
      return data;
    },
  });

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;
  if (!data) return null;

  const getColorClass = (value) => {
    if (value >= 75) return "text-status-success";
    if (value >= 50) return "text-status-warning";
    return "text-status-error";
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold text-text-primary">Academic Data</h2>
        <p className="text-sm text-text-secondary mt-1">
          {data.fullName} · {data.rollNumber}
        </p>
      </div>

      {/* Overall Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard
          title="Attendance"
          value={`${data.overallAttendance?.toFixed(1)}%`}
          icon={ClipboardCheck}
        />
        <StatCard
          title="Marks"
          value={`${data.overallMarks?.toFixed(1)}%`}
          icon={BookOpen}
        />
        <StatCard
          title="Assignments"
          value={`${data.overallAssignment?.toFixed(1)}%`}
          icon={FileText}
        />
        <StatCard
          title="LMS Score"
          value={`${data.overallLms?.toFixed(1)}%`}
          icon={Monitor}
        />
      </div>

      {/* Overall Risk */}
      <Card className="flex items-center gap-4">
        <div>
          <p className="text-sm text-text-secondary">Overall Risk</p>
          <p className="text-2xl font-bold text-text-primary">
            {data.overallRiskScore?.toFixed(1)}
          </p>
        </div>
        <RiskBadge label={data.overallRiskLabel} />
      </Card>

      {/* Subject-wise breakdown */}
      <Card>
        <CardTitle>Subject-wise Breakdown</CardTitle>
        {data.subjects?.length > 0 ? (
          <>
            <div className="mt-4">
              <AcademicMetricsBar
                subjects={data.subjects.map((s) => ({
                  subjectName: s.subjectName,
                  attendance: s.attendancePercentage,
                  marks: s.iaMarksNormalized,
                  assignment: s.assignmentCompletionPercentage,
                  lms: s.lmsScore,
                }))}
                height={300}
              />
            </div>
            <div className="overflow-x-auto mt-4">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border-light">
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">
                    Subject
                  </th>
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">
                    Code
                  </th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">
                    Attendance
                  </th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">
                    IA Marks
                  </th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">
                    Assignments
                  </th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">
                    LMS
                  </th>
                </tr>
              </thead>
              <tbody>
                {data.subjects.map((subj) => (
                  <tr key={subj.subjectId} className="border-b border-border-light last:border-0">
                    <td className="px-4 py-3 font-medium text-text-primary">
                      {subj.subjectName}
                    </td>
                    <td className="px-4 py-3 text-text-secondary">{subj.subjectCode}</td>
                    <td className={`px-4 py-3 text-right font-medium ${getColorClass(subj.attendancePercentage)}`}>
                      {subj.attendancePercentage?.toFixed(1)}%
                    </td>
                    <td className={`px-4 py-3 text-right font-medium ${getColorClass(subj.iaMarksNormalized)}`}>
                      {subj.iaMarksNormalized?.toFixed(1)}%
                    </td>
                    <td className={`px-4 py-3 text-right font-medium ${getColorClass(subj.assignmentCompletionPercentage)}`}>
                      {subj.assignmentCompletionPercentage?.toFixed(1)}%
                    </td>
                    <td className={`px-4 py-3 text-right font-medium ${getColorClass(subj.lmsScore)}`}>
                      {subj.lmsScore?.toFixed(1)}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          </>
        ) : (
          <EmptyState title="No subjects" description="No academic data available yet." />
        )}
      </Card>
    </div>
  );
}
