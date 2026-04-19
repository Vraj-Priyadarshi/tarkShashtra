import * as RadixTabs from "@radix-ui/react-tabs";
import { cn } from "../../lib/utils";

export function Tabs({ defaultValue, value, onValueChange, children, className }) {
  return (
    <RadixTabs.Root
      defaultValue={defaultValue}
      value={value}
      onValueChange={onValueChange}
      className={className}
    >
      {children}
    </RadixTabs.Root>
  );
}

export function TabsList({ children, className }) {
  return (
    <RadixTabs.List
      className={cn(
        "flex border-b border-border-light gap-1",
        className
      )}
    >
      {children}
    </RadixTabs.List>
  );
}

export function TabsTrigger({ value, children, className }) {
  return (
    <RadixTabs.Trigger
      value={value}
      className={cn(
        "px-4 py-2.5 text-sm font-medium text-text-secondary",
        "border-b-2 border-transparent -mb-px",
        "hover:text-text-primary transition-colors duration-200",
        "data-[state=active]:text-accent-primary data-[state=active]:border-accent-primary",
        className
      )}
    >
      {children}
    </RadixTabs.Trigger>
  );
}

export function TabsContent({ value, children, className }) {
  return (
    <RadixTabs.Content value={value} className={cn("pt-6", className)}>
      {children}
    </RadixTabs.Content>
  );
}
