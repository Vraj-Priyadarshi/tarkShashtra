import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { Mail, Flame, TrendingDown, Lightbulb, Map, Target, RefreshCw, ChevronDown, ChevronUp, CheckCircle } from "lucide-react";
import useUiStore from "../../stores/uiStore";
import { getStudentDashboard, getSuggestions, getRoadmap, regenerateRoadmap } from "../../api/student";
import Card, { CardTitle } from "../../components/ui/Card";
import RiskBadge from "../../components/ui/RiskBadge";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import { riskColor } from "../../lib/utils";

export default function StudentDashboard() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Dashboard"), [setPageTitle]);
  const queryClient = useQueryClient();
  const [expandedWeek, setExpandedWeek] = useState(null);

  const { data, isLoading } = useQuery({
    queryKey: ["student", "dashboard"],
    queryFn: async () => {
      const { data } = await getStudentDashboard();
      return data;
    },
  });

  const { data: suggestions, isLoading: suggestionsLoading } = useQuery({
    queryKey: ["student", "suggestions"],
    queryFn: async () => {
      const { data } = await getSuggestions();
      return data;
    },
    staleTime: 5 * 60 * 1000,
  });

  const { data: roadmap, isLoading: roadmapLoading } = useQuery({
    queryKey: ["student", "roadmap"],
    queryFn: async () => {
      const { data } = await getRoadmap();
      return data;
    },
    staleTime: 5 * 60 * 1000,
  });

  const regenMutation = useMutation({
    mutationFn: async () => {
      const { data } = await regenerateRoadmap();
      return data;
    },
    onSuccess: (data) => {
      queryClient.setQueryData(["student", "roadmap"], data);
    },
  });

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;
  if (!data) return null;

  const riskColors = riskColor(data.riskLabel);

  return (
    <div className="space-y-6">
      {/* Welcome */}
      <div>
        <h2 className="text-2xl font-semibold text-text-primary">
          Good morning, {data.fullName}
        </h2>
        <p className="text-sm text-text-secondary mt-1">
          {data.rollNumber} · Semester {data.semester} · {data.branch}
        </p>
      </div>

      {/* Risk Score + Streak */}
      <div className="grid md:grid-cols-2 gap-6">
        {/* Risk Score Card */}
        <Card className="bg-gradient-to-br from-gradient-warm-start to-gradient-cool-end">
          <div className="flex items-center gap-6">
            <div
              className={`w-24 h-24 rounded-full border-4 ${riskColors.border} flex items-center justify-center`}
            >
              <div className="text-center">
                <p className="text-2xl font-bold text-text-primary">
                  {data.riskScore?.toFixed(1)}
                </p>
                <p className="text-[10px] text-text-tertiary uppercase">Score</p>
              </div>
            </div>
            <div>
              <RiskBadge label={data.riskLabel} />
              <p className="text-sm text-text-secondary mt-2">
                Your current academic risk score
              </p>
            </div>
          </div>
        </Card>

        {/* Consistency Streak */}
        {data.consistencyStreak && (
          <Card>
            <div className="flex items-start gap-4">
              <div className="p-3 bg-accent-warm/10 rounded-xl">
                <Flame className="w-6 h-6 text-accent-warm" />
              </div>
              <div>
                <CardTitle>Consistency Streak</CardTitle>
                <p className="text-3xl font-bold text-text-primary mt-1">
                  {data.consistencyStreak.currentStreak} weeks
                </p>
                <p className="text-xs text-text-tertiary mt-1">
                  Longest: {data.consistencyStreak.longestStreak} weeks
                </p>
                {/* Visual streak dots */}
                <div className="flex gap-1.5 mt-3">
                  {Array.from({ length: Math.max(data.consistencyStreak.longestStreak, 7) }).map(
                    (_, i) => (
                      <div
                        key={i}
                        className={`w-3 h-3 rounded-full ${
                          i < data.consistencyStreak.currentStreak
                            ? "bg-accent-warm"
                            : "bg-border-light"
                        }`}
                      />
                    )
                  )}
                </div>
              </div>
            </div>
          </Card>
        )}
      </div>

      {/* Contributing Factors */}
      {data.topContributingFactors?.length > 0 && (
        <Card>
          <CardTitle className="flex items-center gap-2">
            <TrendingDown className="w-4 h-4 text-text-secondary" />
            Contributing Factors
          </CardTitle>
          <div className="mt-4 space-y-4">
            {data.topContributingFactors.map((factor) => {
              const diff = factor.classAverage - factor.value;
              return (
                <div key={factor.factor}>
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-sm font-medium text-text-primary">
                      {factor.factor}
                    </span>
                    <span className="text-xs text-text-tertiary">
                      {factor.contributionPercentage?.toFixed(0)}% contribution
                    </span>
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="flex-1 bg-bg-hover rounded-full h-2">
                      <div
                        className="h-2 rounded-full bg-accent-primary"
                        style={{ width: `${Math.min(factor.value, 100)}%` }}
                      />
                    </div>
                    <span className="text-xs text-text-secondary w-20 text-right">
                      You: {factor.value?.toFixed(1)} · Avg: {factor.classAverage?.toFixed(1)}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </Card>
      )}

      {/* AI-Powered Improvement Tips */}
      <Card>
        <CardTitle className="flex items-center gap-2">
          <Lightbulb className="w-4 h-4 text-accent-warm" />
          AI Improvement Tips
        </CardTitle>
        {suggestionsLoading ? (
          <div className="mt-3 flex items-center gap-2 text-sm text-text-tertiary">
            <LoadingSpinner size="sm" /> Generating personalized tips...
          </div>
        ) : suggestions?.suggestions?.length > 0 ? (
          <div className="mt-3 space-y-4">
            {suggestions.summary && (
              <p className="text-sm text-text-secondary italic">{suggestions.summary}</p>
            )}
            {suggestions.priorityAreas?.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {suggestions.priorityAreas.map((area, i) => (
                  <span key={i} className="text-xs px-2 py-1 rounded-full bg-accent-warm/10 text-accent-warm font-medium">
                    {area}
                  </span>
                ))}
              </div>
            )}
            <div className="space-y-3">
              {suggestions.suggestions.map((s, i) => (
                <div key={i} className="p-3 rounded-lg bg-bg-hover border border-border-light">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-sm font-medium text-text-primary">{s.area}</span>
                    <span className="text-xs text-text-tertiary">
                      {s.currentValue} → {s.targetValue}
                    </span>
                  </div>
                  <p className="text-sm text-text-secondary">{s.action}</p>
                  <p className="text-xs text-accent-primary mt-1">Impact: {s.impact}</p>
                </div>
              ))}
            </div>
            {suggestions.motivationalNote && (
              <p className="text-sm text-accent-secondary font-medium mt-2">
                💪 {suggestions.motivationalNote}
              </p>
            )}
          </div>
        ) : (
          <p className="mt-3 text-sm text-text-tertiary">No suggestions available at the moment.</p>
        )}
      </Card>

      {/* Improvement Roadmap */}
      <Card>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <Map className="w-4 h-4 text-accent-primary" />
            Improvement Roadmap
          </CardTitle>
          <button
            onClick={() => regenMutation.mutate()}
            disabled={regenMutation.isPending}
            className="flex items-center gap-1 text-xs text-accent-primary hover:text-accent-primary/80 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${regenMutation.isPending ? "animate-spin" : ""}`} />
            {regenMutation.isPending ? "Generating..." : "Regenerate"}
          </button>
        </div>
        {roadmapLoading ? (
          <div className="mt-3 flex items-center gap-2 text-sm text-text-tertiary">
            <LoadingSpinner size="sm" /> Loading roadmap...
          </div>
        ) : roadmap?.weeks?.length > 0 ? (
          <div className="mt-4 space-y-4">
            <div>
              <h3 className="text-base font-semibold text-text-primary">{roadmap.roadmapTitle}</h3>
              <p className="text-sm text-text-secondary mt-1">{roadmap.overallGoal}</p>
              <span className="text-xs text-text-tertiary">{roadmap.duration}</span>
            </div>

            {/* Weeks */}
            <div className="space-y-2">
              {roadmap.weeks.map((week) => (
                <div key={week.weekNumber} className="border border-border-light rounded-lg overflow-hidden">
                  <button
                    onClick={() => setExpandedWeek(expandedWeek === week.weekNumber ? null : week.weekNumber)}
                    className="w-full flex items-center justify-between p-3 hover:bg-bg-hover transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-accent-primary/10 flex items-center justify-center">
                        <span className="text-xs font-bold text-accent-primary">W{week.weekNumber}</span>
                      </div>
                      <div className="text-left">
                        <p className="text-sm font-medium text-text-primary">{week.theme}</p>
                        <p className="text-xs text-text-tertiary">{week.focusSubjects?.join(", ")}</p>
                      </div>
                    </div>
                    {expandedWeek === week.weekNumber ? (
                      <ChevronUp className="w-4 h-4 text-text-tertiary" />
                    ) : (
                      <ChevronDown className="w-4 h-4 text-text-tertiary" />
                    )}
                  </button>
                  {expandedWeek === week.weekNumber && (
                    <div className="px-3 pb-3 space-y-3 border-t border-border-light pt-3">
                      {/* Daily Tasks */}
                      {week.dailyTasks?.length > 0 && (
                        <div>
                          <p className="text-xs font-medium text-text-tertiary uppercase mb-1">Daily Tasks</p>
                          <ul className="space-y-1">
                            {week.dailyTasks.map((task, i) => (
                              <li key={i} className="flex items-start gap-2 text-sm text-text-secondary">
                                <CheckCircle className="w-3.5 h-3.5 text-accent-primary mt-0.5 shrink-0" />
                                {task}
                              </li>
                            ))}
                          </ul>
                        </div>
                      )}
                      {/* Weekly Targets */}
                      {week.weeklyTargets && (
                        <div>
                          <p className="text-xs font-medium text-text-tertiary uppercase mb-1">Weekly Targets</p>
                          <div className="grid grid-cols-2 gap-2">
                            <div className="p-2 bg-bg-hover rounded text-xs">
                              <span className="text-text-tertiary">Attendance:</span>{" "}
                              <span className="text-text-primary font-medium">{week.weeklyTargets.attendance}</span>
                            </div>
                            <div className="p-2 bg-bg-hover rounded text-xs">
                              <span className="text-text-tertiary">Assignments:</span>{" "}
                              <span className="text-text-primary font-medium">{week.weeklyTargets.assignmentsToComplete}</span>
                            </div>
                            <div className="p-2 bg-bg-hover rounded text-xs">
                              <span className="text-text-tertiary">LMS Sessions:</span>{" "}
                              <span className="text-text-primary font-medium">{week.weeklyTargets.lmsSessions}</span>
                            </div>
                            <div className="p-2 bg-bg-hover rounded text-xs">
                              <span className="text-text-tertiary">Study Hours:</span>{" "}
                              <span className="text-text-primary font-medium">{week.weeklyTargets.studyHours}</span>
                            </div>
                          </div>
                        </div>
                      )}
                      {/* Milestone */}
                      {week.milestone && (
                        <div className="flex items-start gap-2 p-2 bg-accent-primary/5 rounded">
                          <Target className="w-3.5 h-3.5 text-accent-primary mt-0.5 shrink-0" />
                          <p className="text-xs text-text-secondary"><span className="font-medium">Milestone:</span> {week.milestone}</p>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>

            {/* Success Metrics */}
            {roadmap.successMetrics && (
              <div>
                <p className="text-xs font-medium text-text-tertiary uppercase mb-2">Success Targets</p>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                  <div className="p-2 bg-bg-hover rounded text-center">
                    <p className="text-lg font-bold text-accent-primary">{roadmap.successMetrics.attendanceTarget}</p>
                    <p className="text-xs text-text-tertiary">Attendance</p>
                  </div>
                  <div className="p-2 bg-bg-hover rounded text-center">
                    <p className="text-lg font-bold text-accent-primary">{roadmap.successMetrics.marksTarget}</p>
                    <p className="text-xs text-text-tertiary">Marks</p>
                  </div>
                  <div className="p-2 bg-bg-hover rounded text-center">
                    <p className="text-lg font-bold text-accent-primary">{roadmap.successMetrics.assignmentTarget}</p>
                    <p className="text-xs text-text-tertiary">Assignments</p>
                  </div>
                  <div className="p-2 bg-bg-hover rounded text-center">
                    <p className="text-lg font-bold text-accent-primary">{roadmap.successMetrics.lmsTarget}</p>
                    <p className="text-xs text-text-tertiary">LMS</p>
                  </div>
                </div>
              </div>
            )}

            {roadmap.generatedAt && (
              <p className="text-xs text-text-tertiary">
                Generated: {new Date(roadmap.generatedAt).toLocaleDateString()}
              </p>
            )}
          </div>
        ) : (
          <p className="mt-3 text-sm text-text-tertiary">
            {roadmap?.overallGoal || "No roadmap available. Click Regenerate to create one."}
          </p>
        )}
      </Card>

      {/* Mentor Info */}
      {data.mentorName && (
        <Card>
          <CardTitle>Your Mentor</CardTitle>
          <div className="flex items-center gap-3 mt-3">
            <div className="w-10 h-10 rounded-full bg-accent-secondary/10 flex items-center justify-center">
              <span className="text-accent-secondary font-medium text-sm">
                {data.mentorName?.[0]}
              </span>
            </div>
            <div>
              <p className="text-sm font-medium text-text-primary">{data.mentorName}</p>
              <a
                href={`mailto:${data.mentorEmail}`}
                className="text-xs text-accent-primary hover:underline flex items-center gap-1"
              >
                <Mail className="w-3 h-3" />
                {data.mentorEmail}
              </a>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
}
