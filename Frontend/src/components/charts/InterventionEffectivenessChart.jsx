import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  Cell,
} from "recharts";

const formatType = (s) =>
  s
    .replace(/_/g, " ")
    .toLowerCase()
    .replace(/\b\w/g, (c) => c.toUpperCase());

const CustomTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  const d = payload[0].payload;
  return (
    <div className="rounded-lg border bg-white px-3 py-2 shadow-md text-sm">
      <p className="font-medium mb-1">{formatType(d.type)}</p>
      <p>Count: {d.count}</p>
      <p>Avg Pre: {d.avgPreScore?.toFixed(1)}</p>
      <p>Avg Post: {d.avgPostScore?.toFixed(1)}</p>
      <p className="font-semibold text-green-600">Improvement: {d.avgImprovement?.toFixed(1)}</p>
    </div>
  );
};

export default function InterventionEffectivenessChart({ data = [], height = 350 }) {
  const chartData = data.map((d) => ({
    type: d.interventionType,
    label: formatType(d.interventionType),
    count: d.count,
    avgPreScore: d.avgPreScore,
    avgPostScore: d.avgPostScore,
    avgImprovement: d.avgImprovement,
  }));

  return (
    <ResponsiveContainer width="100%" height={height}>
      <BarChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
        <XAxis dataKey="label" tick={{ fontSize: 11 }} stroke="#9ca3af" />
        <YAxis tick={{ fontSize: 12 }} stroke="#9ca3af" />
        <Tooltip content={<CustomTooltip />} />
        <Legend verticalAlign="bottom" iconType="circle" />
        <Bar dataKey="avgPreScore" name="Pre Score" fill="#f87171" radius={[4, 4, 0, 0]} />
        <Bar dataKey="avgPostScore" name="Post Score" fill="#34d399" radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}
