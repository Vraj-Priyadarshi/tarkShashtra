import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
  ReferenceArea,
} from "recharts";

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  const score = payload[0].value;
  const riskLabel =
    score >= 55 ? "HIGH" : score >= 35 ? "MEDIUM" : "LOW";
  const color =
    score >= 55 ? "#ef4444" : score >= 35 ? "#f59e0b" : "#22c55e";
  return (
    <div className="rounded-lg border bg-white px-3 py-2 shadow-md text-sm">
      <p className="text-gray-500">{label}</p>
      <p className="font-semibold" style={{ color }}>
        {score.toFixed(1)} — {riskLabel}
      </p>
    </div>
  );
};

export default function RiskTrendChart({ dataPoints = [], height = 300 }) {
  const data = dataPoints.map((dp) => ({
    date: dp.date,
    score: dp.riskScore,
  }));

  return (
    <ResponsiveContainer width="100%" height={height}>
      <LineChart data={data} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
        <ReferenceArea y1={0} y2={35} fill="#22c55e" fillOpacity={0.07} />
        <ReferenceArea y1={35} y2={55} fill="#f59e0b" fillOpacity={0.07} />
        <ReferenceArea y1={55} y2={100} fill="#ef4444" fillOpacity={0.07} />
        <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
        <XAxis dataKey="date" tick={{ fontSize: 12 }} stroke="#9ca3af" />
        <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} stroke="#9ca3af" />
        <Tooltip content={<CustomTooltip />} />
        <ReferenceLine y={35} stroke="#f59e0b" strokeDasharray="4 4" />
        <ReferenceLine y={55} stroke="#ef4444" strokeDasharray="4 4" />
        <Line
          type="monotone"
          dataKey="score"
          stroke="#6366f1"
          strokeWidth={2}
          dot={{ r: 4, fill: "#6366f1" }}
          activeDot={{ r: 6 }}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
