import { cn } from "../../lib/utils";
import { FileQuestion } from "lucide-react";

export default function EmptyState({
  icon: Icon = FileQuestion,
  title = "No data found",
  description = "There's nothing to show here yet.",
  action,
  className,
}) {
  return (
    <div className={cn("flex flex-col items-center justify-center py-16 text-center", className)}>
      <div className="p-4 bg-bg-hover rounded-full mb-4">
        <Icon className="w-8 h-8 text-text-tertiary" />
      </div>
      <h3 className="text-base font-medium text-text-primary mb-1">{title}</h3>
      <p className="text-sm text-text-secondary max-w-sm">{description}</p>
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
