import * as RadixSelect from "@radix-ui/react-select";
import { ChevronDown, Check } from "lucide-react";
import { cn } from "../../lib/utils";

export default function Select({ label, placeholder = "Select...", value, onValueChange, options = [], error, className }) {
  return (
    <div className="space-y-1.5">
      {label && (
        <label className="block text-sm font-medium text-text-primary">{label}</label>
      )}
      <RadixSelect.Root value={value} onValueChange={onValueChange}>
        <RadixSelect.Trigger
          className={cn(
            "inline-flex items-center justify-between w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm",
            "focus:outline-none focus:ring-2 focus:ring-accent-primary/30 focus:border-accent-primary",
            "data-[placeholder]:text-text-tertiary",
            error && "border-status-error",
            className
          )}
        >
          <RadixSelect.Value placeholder={placeholder} />
          <RadixSelect.Icon>
            <ChevronDown className="w-4 h-4 text-text-tertiary" />
          </RadixSelect.Icon>
        </RadixSelect.Trigger>

        <RadixSelect.Portal>
          <RadixSelect.Content
            className="bg-bg-secondary border border-border-light rounded-xl shadow-lg overflow-hidden z-50"
            position="popper"
            sideOffset={4}
          >
            <RadixSelect.Viewport className="p-1 max-h-60">
              {options.map((option) => (
                <RadixSelect.Item
                  key={option.value}
                  value={option.value}
                  className="flex items-center gap-2 px-3 py-2 text-sm text-text-primary rounded-lg cursor-pointer outline-none data-[highlighted]:bg-bg-hover"
                >
                  <RadixSelect.ItemText>{option.label}</RadixSelect.ItemText>
                  <RadixSelect.ItemIndicator className="ml-auto">
                    <Check className="w-4 h-4 text-accent-primary" />
                  </RadixSelect.ItemIndicator>
                </RadixSelect.Item>
              ))}
            </RadixSelect.Viewport>
          </RadixSelect.Content>
        </RadixSelect.Portal>
      </RadixSelect.Root>
      {error && <p className="text-xs text-status-error mt-1">{error}</p>}
    </div>
  );
}
