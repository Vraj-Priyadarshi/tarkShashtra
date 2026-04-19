import * as RadixSeparator from "@radix-ui/react-separator";
import { cn } from "../../lib/utils";

export default function Separator({ orientation = "horizontal", className }) {
  return (
    <RadixSeparator.Root
      orientation={orientation}
      className={cn(
        "bg-border-light",
        orientation === "horizontal" ? "h-px w-full" : "w-px h-full",
        className
      )}
    />
  );
}
