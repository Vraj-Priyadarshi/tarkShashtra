import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { BarChart3, Download } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getInterventionEffectiveness, exportRiskReport } from "../../api/coordinator";
import Card, { CardTitle } from "../../components/ui/Card";
import StatCard from "../../components/ui/StatCard";
import Button from "../../components/ui/Button";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import EmptyState from "../../components/ui/EmptyState";
import InterventionEffectivenessChart from "../../components/charts/InterventionEffectivenessChart";

export default function InterventionReport() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Intervention Reports"), [setPageTitle]);

  const { data, isLoading } = useQuery({
    queryKey: ["coordinator", "intervention-effectiveness"],
    queryFn: async () => {
      const { data } = await getInterventionEffectiveness();
      return data;
    },
  });

  const handleExportRisk = async () => {
    try {
      const { data: blob } = await exportRiskReport();
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", "risk_report.csv");
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error("Failed to export report");
    }
  };

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-semibold text-text-primary">Intervention Reports</h2>
        <Button variant="secondary" size="sm" onClick={handleExportRisk}>
          <Download className="w-4 h-4" /> Export Risk Report
        </Button>
      </div>

      {!data ? (
        <EmptyState icon={BarChart3} title="No data" description="No intervention data available." />
      ) : (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard title="Total Interventions" value={data.totalInterventions || 0} />
            <StatCard title="Avg Risk Reduction" value={`${data.avgRiskReduction?.toFixed(1) || 0}`} />
            <StatCard title="Success Rate" value={`${data.successRate?.toFixed(0) || 0}%`} />
            <StatCard title="Avg Follow-up Days" value={data.avgFollowUpDays?.toFixed(0) || 0} />
          </div>

          {data.byType?.length > 0 && (
            <Card>
              <CardTitle>Effectiveness by Type</CardTitle>
              <InterventionEffectivenessChart
                data={data.byType.map((t) => ({
                  interventionType: t.type,
                  count: t.count,
                  avgPreScore: t.avgPreScore || 0,
                  avgPostScore: t.avgPostScore || 0,
                  avgImprovement: t.avgReduction || 0,
                }))}
                height={300}
              />
            </Card>
          )}

          {data.byType?.length > 0 && (
            <Card>
              <CardTitle>By Intervention Type</CardTitle>
              <div className="overflow-x-auto mt-4">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-border-light">
                      <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Type</th>
                      <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Count</th>
                      <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Avg Reduction</th>
                      <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Success Rate</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.byType.map((t) => (
                      <tr key={t.type} className="border-b border-border-light last:border-0">
                        <td className="px-4 py-3 text-text-primary">{t.type?.replace(/_/g, " ")}</td>
                        <td className="px-4 py-3 text-right text-text-secondary">{t.count}</td>
                        <td className="px-4 py-3 text-right text-text-secondary">{t.avgReduction?.toFixed(1)}</td>
                        <td className="px-4 py-3 text-right text-text-secondary">{t.successRate?.toFixed(0)}%</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          )}
        </>
      )}
    </div>
  );
}
