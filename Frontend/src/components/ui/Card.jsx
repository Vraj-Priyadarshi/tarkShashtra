import { cn } from "../../lib/utils";

export default function Card({ children, className, hover, ...props }) {
  return (
    <div
      className={cn(
        "bg-bg-secondary rounded-2xl shadow-sm border border-border-light p-6",
        hover && "hover:shadow-md transition-shadow duration-200 cursor-pointer",
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
}

export function CardHeader({ children, className }) {
  return (
    <div className={cn("mb-4", className)}>
      {children}
    </div>
  );
}

export function CardTitle({ children, className }) {
  return (
    <h3 className={cn("text-base font-medium text-text-primary", className)}>
      {children}
    </h3>
  );
}

export function CardDescription({ children, className }) {
  return (
    <p className={cn("text-sm text-text-secondary mt-1", className)}>
      {children}
    </p>
  );
}
