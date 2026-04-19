import * as RadixProgress from "@radix-ui/react-progress";
import { cn } from "../../lib/utils";

export default function Progress({ value = 0, max = 100, className, indicatorClassName }) {
  const percentage = Math.min((value / max) * 100, 100);

  return (
    <RadixProgress.Root
      value={value}
      max={max}
      className={cn(
        "relative overflow-hidden bg-bg-hover rounded-full h-2",
        className
      )}
    >
      <RadixProgress.Indicator
        className={cn(
          "h-full bg-accent-primary rounded-full transition-all duration-500 ease-out",
          indicatorClassName
        )}
        style={{ width: `${percentage}%` }}
      />
    </RadixProgress.Root>
  );
}
