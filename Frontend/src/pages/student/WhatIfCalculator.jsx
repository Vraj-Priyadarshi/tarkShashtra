import { useEffect, useState } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { Calculator, ArrowRight } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import useAuthStore from "../../stores/authStore";
import { getAcademicData, calculateWhatIf } from "../../api/student";
import Card, { CardTitle } from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import RiskBadge from "../../components/ui/RiskBadge";
import LoadingSpinner from "../../components/ui/LoadingSpinner";

export default function WhatIfCalculator() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("What-If Calculator"), [setPageTitle]);

  const user = useAuthStore((s) => s.user);
  const [hypotheticals, setHypotheticals] = useState({});
  const [result, setResult] = useState(null);

  const { data: academic, isLoading } = useQuery({
    queryKey: ["student", "academic-data"],
    queryFn: async () => {
      const { data } = await getAcademicData();
      return data;
    },
  });

  // Initialize hypotheticals from current data
  useEffect(() => {
    if (academic?.subjects) {
      const init = {};
      academic.subjects.forEach((s) => {
        init[s.subjectId] = {
          attendance: s.attendancePercentage || 0,
          marks: s.iaMarksNormalized || 0,
          assignment: s.assignmentCompletionPercentage || 0,
          lms: s.lmsScore || 0,
        };
      });
      setHypotheticals(init);
    }
  }, [academic]);

  const mutation = useMutation({
    mutationFn: async (payload) => {
      const { data } = await calculateWhatIf(payload);
      return data;
    },
    onSuccess: (data) => setResult(data),
    onError: (err) => toast.error(err.response?.data?.message || "Calculation failed"),
  });

  const handleCalculate = () => {
    const hypotheticalSubjects = Object.entries(hypotheticals).map(
      ([subjectId, values]) => ({
        subjectId,
        attendance: parseFloat(values.attendance),
        marks: parseFloat(values.marks),
        assignment: parseFloat(values.assignment),
        lms: parseFloat(values.lms),
      })
    );
    mutation.mutate({ studentId: user?.id, hypotheticalSubjects });
  };

  const updateValue = (subjectId, field, value) => {
    const num = Math.min(100, Math.max(0, parseFloat(value) || 0));
    setHypotheticals((prev) => ({
      ...prev,
      [subjectId]: { ...prev[subjectId], [field]: num },
    }));
  };

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold text-text-primary">What-If Calculator</h2>
        <p className="text-sm text-text-secondary mt-1">
          See how improving your metrics could change your risk score
        </p>
      </div>

      {/* Input sliders */}
      <Card>
        <CardTitle className="flex items-center gap-2">
          <Calculator className="w-4 h-4" />
          Adjust Your Metrics
        </CardTitle>
        <div className="mt-4 space-y-6">
          {academic?.subjects?.map((subj) => (
            <div key={subj.subjectId} className="space-y-3">
              <h4 className="text-sm font-medium text-text-primary">
                {subj.subjectName}{" "}
                <span className="text-text-tertiary">({subj.subjectCode})</span>
              </h4>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {["attendance", "marks", "assignment", "lms"].map((field) => (
                  <div key={field}>
                    <label className="text-xs text-text-tertiary capitalize">{field}</label>
                    <input
                      type="number"
                      min="0"
                      max="100"
                      value={hypotheticals[subj.subjectId]?.[field] || 0}
                      onChange={(e) =>
                        updateValue(subj.subjectId, field, e.target.value)
                      }
                      className="w-full mt-1 bg-bg-primary border border-border-light rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30"
                    />
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
        <div className="mt-6">
          <Button onClick={handleCalculate} loading={mutation.isPending}>
            Calculate
          </Button>
        </div>
      </Card>

      {/* Results */}
      {result && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
        >
          <Card>
            <CardTitle>Results</CardTitle>
            <div className="mt-4 flex items-center gap-8">
              <div className="text-center">
                <p className="text-xs text-text-tertiary uppercase mb-1">Current</p>
                <p className="text-3xl font-bold text-text-primary">
                  {result.currentRiskScore?.toFixed(1)}
                </p>
                <RiskBadge label={result.currentRiskLabel} className="mt-1" />
              </div>
              <ArrowRight className="w-6 h-6 text-text-tertiary" />
              <div className="text-center">
                <p className="text-xs text-text-tertiary uppercase mb-1">Predicted</p>
                <p className="text-3xl font-bold text-text-primary">
                  {result.predictedRiskScore?.toFixed(1)}
                </p>
                <RiskBadge label={result.predictedRiskLabel} className="mt-1" />
              </div>
              <div className="ml-4">
                {result.predictedRiskScore < result.currentRiskScore ? (
                  <p className="text-sm text-status-success font-medium">
                    ↓ {(result.currentRiskScore - result.predictedRiskScore).toFixed(1)} points improvement
                  </p>
                ) : (
                  <p className="text-sm text-status-error font-medium">
                    ↑ {(result.predictedRiskScore - result.currentRiskScore).toFixed(1)} points increase
                  </p>
                )}
              </div>
            </div>
          </Card>
        </motion.div>
      )}
    </div>
  );
}
