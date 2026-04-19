import {
  ResponsiveContainer,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
  Tooltip,
  Legend,
} from "recharts";

const METRICS = [
  { key: "attendance", label: "Attendance" },
  { key: "marks", label: "Marks" },
  { key: "assignment", label: "Assignments" },
  { key: "lms", label: "LMS" },
];

export default function SubjectRadar({ subjectData = {}, classAverage, height = 350 }) {
  const data = METRICS.map((m) => ({
    metric: m.label,
    Student: subjectData[m.key] ?? 0,
    ...(classAverage ? { "Class Avg": classAverage[m.key] ?? 0 } : {}),
  }));

  return (
    <ResponsiveContainer width="100%" height={height}>
      <RadarChart data={data} cx="50%" cy="50%" outerRadius="75%">
        <PolarGrid stroke="#e5e7eb" />
        <PolarAngleAxis dataKey="metric" tick={{ fontSize: 12 }} />
        <PolarRadiusAxis domain={[0, 100]} tick={{ fontSize: 10 }} />
        <Radar
          name="Student"
          dataKey="Student"
          stroke="#6366f1"
          fill="#6366f1"
          fillOpacity={0.3}
        />
        {classAverage && (
          <Radar
            name="Class Avg"
            dataKey="Class Avg"
            stroke="#f59e0b"
            fill="#f59e0b"
            fillOpacity={0.15}
          />
        )}
        <Tooltip />
        <Legend verticalAlign="bottom" iconType="circle" />
      </RadarChart>
    </ResponsiveContainer>
  );
}
