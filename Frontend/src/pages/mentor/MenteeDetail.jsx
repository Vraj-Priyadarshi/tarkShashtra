import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, RefreshCw } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getMenteeRisk, getMenteeRiskTrend, computeRisk, createIntervention } from "../../api/mentor";
import Card, { CardTitle } from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import RiskBadge from "../../components/ui/RiskBadge";
import Modal from "../../components/ui/Modal";
import Input from "../../components/ui/Input";
import Select from "../../components/ui/Select";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import { formatDate, riskColor } from "../../lib/utils";
import RiskTrendChart from "../../components/charts/RiskTrendChart";

export default function MenteeDetail() {
  const { studentId } = useParams();
  const navigate = useNavigate();
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Mentee Detail"), [setPageTitle]);
  const queryClient = useQueryClient();

  const [showIntervention, setShowIntervention] = useState(false);
  const [interventionType, setInterventionType] = useState("COUNSELLING_SESSION");
  const [remarks, setRemarks] = useState("");
  const [followUpDate, setFollowUpDate] = useState("");
  const [actionItems, setActionItems] = useState("");

  const { data: risk, isLoading } = useQuery({
    queryKey: ["mentor", "mentee-risk", studentId],
    queryFn: async () => {
      const { data } = await getMenteeRisk(studentId);
      return data;
    },
  });

  const { data: trend = [] } = useQuery({
    queryKey: ["mentor", "mentee-risk-trend", studentId],
    queryFn: async () => {
      const { data } = await getMenteeRiskTrend(studentId);
      return data;
    },
  });

  const recomputeMutation = useMutation({
    mutationFn: () => computeRisk(studentId),
    onSuccess: () => {
      toast.success("Risk score recomputed!");
      queryClient.invalidateQueries({ queryKey: ["mentor", "mentee-risk", studentId] });
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to recompute"),
  });

  const interventionMutation = useMutation({
    mutationFn: async (data) => {
      const response = await createIntervention(data);
      return response.data;
    },
    onSuccess: () => {
      toast.success("Intervention recorded!");
      setShowIntervention(false);
      setRemarks("");
      setFollowUpDate("");
      setActionItems("");
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to create intervention"),
  });

  const handleCreateIntervention = () => {
    interventionMutation.mutate({
      studentId,
      interventionType,
      remarks,
      followUpDate: followUpDate || undefined,
      actionItems: actionItems
        ? actionItems.split("\n").filter(Boolean).map((desc) => ({ description: desc.trim() }))
        : [],
    });
  };

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;
  if (!risk) return null;

  const colors = riskColor(risk.riskLabel);

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate("/mentor/mentees")}>
        <ArrowLeft className="w-4 h-4" /> Back to Mentees
      </Button>

      {/* Student Info + Risk */}
      <div className="grid md:grid-cols-2 gap-6">
        <Card>
          <CardTitle>Student Info</CardTitle>
          <div className="mt-3 space-y-2 text-sm">
            <p><span className="text-text-tertiary">Name:</span> <span className="font-medium text-text-primary">{risk.fullName}</span></p>
            <p><span className="text-text-tertiary">Roll No:</span> <span className="text-text-primary">{risk.rollNumber}</span></p>
            <p><span className="text-text-tertiary">Branch:</span> <span className="text-text-primary">{risk.branch}</span></p>
            <p><span className="text-text-tertiary">Semester:</span> <span className="text-text-primary">{risk.semester}</span></p>
          </div>
        </Card>

        <Card className={`border-l-4 ${colors.border}`}>
          <CardTitle>Risk Score</CardTitle>
          <div className="mt-3 flex items-center gap-6">
            <div className={`w-20 h-20 rounded-full border-4 ${colors.border} flex items-center justify-center`}>
              <p className="text-xl font-bold text-text-primary">{risk.riskScore?.toFixed(1)}</p>
            </div>
            <div>
              <RiskBadge label={risk.riskLabel} />
              <p className="text-xs text-text-tertiary mt-1">
                Last computed: {risk.lastComputedAt ? formatDate(risk.lastComputedAt) : "N/A"}
              </p>
              <Button
                variant="ghost"
                size="sm"
                className="mt-2"
                onClick={() => recomputeMutation.mutate()}
                loading={recomputeMutation.isPending}
              >
                <RefreshCw className="w-3 h-3" /> Recompute
              </Button>
            </div>
          </div>
        </Card>
      </div>

      {/* Contributing Factors */}
      {risk.topContributingFactors?.length > 0 && (
        <Card>
          <CardTitle>Contributing Factors</CardTitle>
          <div className="mt-4 space-y-3">
            {risk.topContributingFactors.map((f) => (
              <div key={f.factor} className="flex items-center justify-between text-sm">
                <span className="text-text-primary">{f.factor}</span>
                <span className="text-text-secondary">
                  Value: {f.value?.toFixed(1)} · Avg: {f.classAverage?.toFixed(1)} · {f.contributionPercentage?.toFixed(0)}%
                </span>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Risk Trend */}
      {trend.length > 0 && (
        <Card>
          <CardTitle>Risk Trend</CardTitle>
          <div className="mt-4">
            <RiskTrendChart
              dataPoints={trend.map((t) => ({ date: formatDate(t.computedAt), riskScore: t.riskScore, riskLabel: t.riskLabel }))}
              height={280}
            />
          </div>
        </Card>
      )}

      {/* Create Intervention */}
      <Button onClick={() => setShowIntervention(true)}>
        Create Intervention
      </Button>

      <Modal open={showIntervention} onOpenChange={setShowIntervention} title="Create Intervention">
        <div className="space-y-4">
          <Select
            label="Type"
            options={[
              { value: "COUNSELLING_SESSION", label: "Counselling Session" },
              { value: "REMEDIAL_CLASS", label: "Remedial Class" },
              { value: "ASSIGNMENT_EXTENSION", label: "Assignment Extension" },
              { value: "PARENT_MEETING", label: "Parent Meeting" },
              { value: "OTHER", label: "Other" },
            ]}
            value={interventionType}
            onValueChange={setInterventionType}
          />
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">Remarks</label>
            <textarea
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              rows={3}
              className="w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30 resize-none"
            />
          </div>
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">Follow-up Date</label>
            <input
              type="date"
              value={followUpDate}
              onChange={(e) => setFollowUpDate(e.target.value)}
              className="w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30"
            />
          </div>
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">Action Items (one per line)</label>
            <textarea
              value={actionItems}
              onChange={(e) => setActionItems(e.target.value)}
              rows={3}
              placeholder="Attend extra tutoring&#10;Submit pending assignments&#10;..."
              className="w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30 resize-none placeholder:text-text-tertiary"
            />
          </div>
          <Button onClick={handleCreateIntervention} loading={interventionMutation.isPending} className="w-full">
            Create Intervention
          </Button>
        </div>
      </Modal>
    </div>
  );
}
