import * as RadixTooltip from "@radix-ui/react-tooltip";
import { cn } from "../../lib/utils";

export default function Tooltip({ children, content, side = "top", className }) {
  return (
    <RadixTooltip.Provider delayDuration={200}>
      <RadixTooltip.Root>
        <RadixTooltip.Trigger asChild>{children}</RadixTooltip.Trigger>
        <RadixTooltip.Portal>
          <RadixTooltip.Content
            side={side}
            sideOffset={4}
            className={cn(
              "bg-bg-sidebar text-text-inverse text-xs px-3 py-1.5 rounded-lg shadow-lg z-50",
              "animate-in fade-in-0 zoom-in-95",
              className
            )}
          >
            {content}
            <RadixTooltip.Arrow className="fill-bg-sidebar" />
          </RadixTooltip.Content>
        </RadixTooltip.Portal>
      </RadixTooltip.Root>
    </RadixTooltip.Provider>
  );
}
