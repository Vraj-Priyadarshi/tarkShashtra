import { useEffect } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router";
import { Users, GraduationCap, AlertTriangle, RefreshCw, Building } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getCoordinatorDashboard, recomputeAllRisk } from "../../api/coordinator";
import Card, { CardTitle } from "../../components/ui/Card";
import StatCard from "../../components/ui/StatCard";
import Button from "../../components/ui/Button";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import RiskDistributionPie from "../../components/charts/RiskDistributionPie";
import DepartmentRiskBar from "../../components/charts/DepartmentRiskBar";

export default function CoordinatorDashboard() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Coordinator Dashboard"), [setPageTitle]);
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: ["coordinator", "dashboard"],
    queryFn: async () => {
      const { data } = await getCoordinatorDashboard();
      return data;
    },
  });

  const recomputeMutation = useMutation({
    mutationFn: recomputeAllRisk,
    onSuccess: () => toast.success("Risk recomputation triggered!"),
    onError: (err) => toast.error(err.response?.data?.message || "Failed to trigger recomputation"),
  });

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;
  if (!data) return null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-semibold text-text-primary">Coordinator Dashboard</h2>
        <Button
          variant="secondary"
          size="sm"
          onClick={() => recomputeMutation.mutate()}
          loading={recomputeMutation.isPending}
        >
          <RefreshCw className="w-4 h-4" /> Recompute All Risk
        </Button>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard title="Total Students" value={data.totalStudents || 0} icon={GraduationCap} />
        <StatCard title="Total Teachers" value={data.totalTeachers || 0} icon={Users} />
        <StatCard title="At Risk Students" value={data.atRiskCount || 0} icon={AlertTriangle} />
        <StatCard title="Departments" value={data.departmentCount || 0} icon={Building} />
      </div>

      {/* Risk Distribution */}
      <div className="grid md:grid-cols-2 gap-6">
        <Card>
          <CardTitle>Risk Distribution</CardTitle>
          <RiskDistributionPie
            donut
            data={[
              { name: "HIGH", value: data.highRiskCount || 0 },
              { name: "MEDIUM", value: data.mediumRiskCount || 0 },
              { name: "LOW", value: data.lowRiskCount || 0 },
            ]}
            height={280}
          />
        </Card>

        {/* Average Risk Score */}
        <Card className="flex flex-col items-center justify-center">
          <p className="text-sm text-text-tertiary">Average Risk Score</p>
          <p className="text-5xl font-bold text-text-primary mt-2">{data.averageRiskScore?.toFixed(1) || "—"}</p>
          <p className="text-xs text-text-tertiary mt-2">across {data.totalStudents} students</p>
        </Card>
      </div>

      {/* Department Risk Bar */}
      {data.departmentRiskSummaries?.length > 0 && (
        <Card>
          <CardTitle>Risk by Department</CardTitle>
          <DepartmentRiskBar departments={data.departmentRiskSummaries} height={350} />
        </Card>
      )}

      {/* Quick Actions */}
      <Card>
        <CardTitle>Quick Actions</CardTitle>
        <div className="flex flex-wrap gap-3 mt-4">
          <Button size="sm" onClick={() => navigate("/coordinator/students")}>
            Manage Students
          </Button>
          <Button size="sm" variant="secondary" onClick={() => navigate("/coordinator/teachers")}>
            Manage Teachers
          </Button>
          <Button size="sm" variant="secondary" onClick={() => navigate("/coordinator/institute-setup")}>
            Institute Setup
          </Button>
          <Button size="sm" variant="secondary" onClick={() => navigate("/coordinator/csv-upload")}>
            CSV Upload
          </Button>
          <Button size="sm" variant="secondary" onClick={() => navigate("/coordinator/exam-schedules")}>
            Exam Schedules
          </Button>
        </div>
      </Card>
    </div>
  );
}
