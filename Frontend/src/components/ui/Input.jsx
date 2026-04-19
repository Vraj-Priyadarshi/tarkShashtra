import { cn } from "../../lib/utils";

export default function Input({
  label,
  error,
  className,
  id,
  ...props
}) {
  return (
    <div className="space-y-1.5">
      {label && (
        <label
          htmlFor={id}
          className="block text-sm font-medium text-text-primary"
        >
          {label}
        </label>
      )}
      <input
        id={id}
        className={cn(
          "w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm text-text-primary",
          "focus:outline-none focus:ring-2 focus:ring-accent-primary/30 focus:border-accent-primary",
          "placeholder:text-text-tertiary transition-colors duration-200",
          error && "border-status-error focus:ring-status-error/30 focus:border-status-error",
          className
        )}
        {...props}
      />
      {error && (
        <p className="text-xs text-status-error mt-1">{error}</p>
      )}
    </div>
  );
}
