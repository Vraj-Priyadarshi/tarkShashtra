import { cn } from "../../lib/utils";
import Card from "./Card";

export default function StatCard({ title, value, icon: Icon, subtitle, className, trend }) {
  return (
    <Card className={cn("flex items-start gap-4", className)}>
      {Icon && (
        <div className="p-3 bg-accent-primary/10 rounded-xl">
          <Icon className="w-5 h-5 text-accent-primary" />
        </div>
      )}
      <div className="flex-1 min-w-0">
        <p className="text-sm text-text-secondary">{title}</p>
        <p className="text-3xl font-bold text-text-primary mt-1">{value}</p>
        {subtitle && (
          <p className="text-xs text-text-tertiary mt-1">{subtitle}</p>
        )}
        {trend !== undefined && (
          <p
            className={cn(
              "text-xs mt-1 font-medium",
              trend >= 0 ? "text-status-success" : "text-status-error"
            )}
          >
            {trend >= 0 ? "↑" : "↓"} {Math.abs(trend)}%
          </p>
        )}
      </div>
    </Card>
  );
}
