import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from "recharts";

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded-lg border bg-white px-3 py-2 shadow-md text-sm">
      <p className="font-medium mb-1">{label}</p>
      {payload.map((p) => (
        <p key={p.dataKey} style={{ color: p.color }}>
          {p.name}: {p.value?.toFixed(1)}%
        </p>
      ))}
    </div>
  );
};

export default function AcademicMetricsBar({ subjects = [], height = 350 }) {
  const data = subjects.map((s) => ({
    name: s.subjectName,
    Attendance: s.attendance,
    Marks: s.marks,
    Assignments: s.assignment,
    LMS: s.lms,
  }));

  return (
    <ResponsiveContainer width="100%" height={height}>
      <BarChart data={data} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
        <XAxis dataKey="name" tick={{ fontSize: 11 }} stroke="#9ca3af" />
        <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} stroke="#9ca3af" />
        <Tooltip content={<CustomTooltip />} />
        <Legend verticalAlign="bottom" iconType="circle" />
        <Bar dataKey="Attendance" fill="#6366f1" radius={[4, 4, 0, 0]} />
        <Bar dataKey="Marks" fill="#f59e0b" radius={[4, 4, 0, 0]} />
        <Bar dataKey="Assignments" fill="#22c55e" radius={[4, 4, 0, 0]} />
        <Bar dataKey="LMS" fill="#ec4899" radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}
