import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router";
import { Users } from "lucide-react";
import useUiStore from "../../stores/uiStore";
import { getMentees } from "../../api/mentor";
import Card from "../../components/ui/Card";
import RiskBadge from "../../components/ui/RiskBadge";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import EmptyState from "../../components/ui/EmptyState";

export default function MenteeList() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("My Mentees"), [setPageTitle]);
  const navigate = useNavigate();

  const { data: mentees = [], isLoading } = useQuery({
    queryKey: ["mentor", "mentees"],
    queryFn: async () => {
      const { data } = await getMentees();
      return data;
    },
  });

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">My Mentees</h2>

      {mentees.length === 0 ? (
        <EmptyState icon={Users} title="No mentees assigned" description="No mentees have been assigned to you yet." />
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border-light">
                <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Name</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Roll No</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Branch</th>
                <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Risk Score</th>
                <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Risk</th>
              </tr>
            </thead>
            <tbody>
              {mentees.map((m) => (
                <tr
                  key={m.id || m.userId}
                  className="border-b border-border-light last:border-0 hover:bg-bg-hover cursor-pointer transition-colors"
                  onClick={() => navigate(`/mentor/mentees/${m.id || m.userId}`)}
                >
                  <td className="px-4 py-3 font-medium text-text-primary">{m.fullName}</td>
                  <td className="px-4 py-3 text-text-secondary">{m.rollNumber}</td>
                  <td className="px-4 py-3 text-text-secondary">{m.branch}</td>
                  <td className="px-4 py-3 text-right font-medium">{m.riskScore?.toFixed(1)}</td>
                  <td className="px-4 py-3 text-right"><RiskBadge label={m.riskLabel} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
