import { riskColor, cn } from "../../lib/utils";

export default function RiskBadge({ label, className }) {
  const colors = riskColor(label);
  return (
    <span
      className={cn(
        "px-3 py-1 rounded-full text-xs font-semibold uppercase tracking-wide",
        colors.bg,
        colors.text,
        className
      )}
    >
      {label}
    </span>
  );
}
