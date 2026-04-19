import * as RadixDropdown from "@radix-ui/react-dropdown-menu";
import { cn } from "../../lib/utils";

export function DropdownMenu({ children }) {
  return <RadixDropdown.Root>{children}</RadixDropdown.Root>;
}

export function DropdownMenuTrigger({ children, className }) {
  return (
    <RadixDropdown.Trigger asChild className={className}>
      {children}
    </RadixDropdown.Trigger>
  );
}

export function DropdownMenuContent({ children, className, align = "end" }) {
  return (
    <RadixDropdown.Portal>
      <RadixDropdown.Content
        align={align}
        sideOffset={4}
        className={cn(
          "bg-bg-secondary border border-border-light rounded-xl shadow-lg p-1 min-w-[180px] z-50",
          "animate-in fade-in-0 zoom-in-95",
          className
        )}
      >
        {children}
      </RadixDropdown.Content>
    </RadixDropdown.Portal>
  );
}

export function DropdownMenuItem({ children, className, ...props }) {
  return (
    <RadixDropdown.Item
      className={cn(
        "flex items-center gap-2 px-3 py-2 text-sm text-text-primary rounded-lg cursor-pointer outline-none",
        "data-[highlighted]:bg-bg-hover transition-colors",
        className
      )}
      {...props}
    >
      {children}
    </RadixDropdown.Item>
  );
}

export function DropdownMenuSeparator({ className }) {
  return (
    <RadixDropdown.Separator
      className={cn("h-px bg-border-light my-1", className)}
    />
  );
}
