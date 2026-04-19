import * as RadixAvatar from "@radix-ui/react-avatar";
import { cn } from "../../lib/utils";

export default function Avatar({ src, name, size = "md", className }) {
  const sizes = {
    sm: "w-8 h-8 text-xs",
    md: "w-10 h-10 text-sm",
    lg: "w-12 h-12 text-base",
  };

  const initials = name
    ? name
        .split(" ")
        .map((n) => n[0])
        .join("")
        .toUpperCase()
        .slice(0, 2)
    : "?";

  return (
    <RadixAvatar.Root
      className={cn(
        "inline-flex items-center justify-center rounded-full bg-accent-primary/10 overflow-hidden",
        sizes[size],
        className
      )}
    >
      {src && (
        <RadixAvatar.Image
          src={src}
          alt={name}
          className="w-full h-full object-cover"
        />
      )}
      <RadixAvatar.Fallback className="flex items-center justify-center w-full h-full bg-accent-primary/10 text-accent-primary font-medium">
        {initials}
      </RadixAvatar.Fallback>
    </RadixAvatar.Root>
  );
}
