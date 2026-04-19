import { cn } from "../../lib/utils";

const variants = {
  primary:
    "bg-accent-primary text-white font-medium hover:bg-accent-primary-hover",
  secondary:
    "bg-transparent border border-border-medium text-text-secondary hover:bg-bg-primary",
  dark: "bg-bg-sidebar text-text-inverse font-medium hover:bg-bg-sidebar-hover",
  danger: "bg-status-error text-white font-medium hover:bg-red-600",
  ghost: "text-text-secondary hover:bg-bg-hover hover:text-text-primary",
};

const sizes = {
  sm: "px-3 py-1.5 text-xs",
  md: "px-6 py-2.5 text-sm",
  lg: "px-8 py-3 text-base",
};

export default function Button({
  children,
  variant = "primary",
  size = "md",
  className,
  disabled,
  loading,
  ...props
}) {
  return (
    <button
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-xl transition-colors duration-200 font-medium focus:outline-none focus:ring-2 focus:ring-accent-primary/30 disabled:opacity-50 disabled:cursor-not-allowed",
        variants[variant],
        sizes[size],
        className
      )}
      disabled={disabled || loading}
      {...props}
    >
      {loading && (
        <svg
          className="animate-spin h-4 w-4"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            className="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            strokeWidth="4"
          />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
          />
        </svg>
      )}
      {children}
    </button>
  );
}
