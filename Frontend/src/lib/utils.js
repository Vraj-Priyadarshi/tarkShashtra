import { clsx } from "clsx";
import { twMerge } from "tailwind-merge";
import { format, parseISO } from "date-fns";

export function cn(...inputs) {
  return twMerge(clsx(inputs));
}

export function formatDate(dateString) {
  if (!dateString) return "";
  try {
    const date = typeof dateString === "string" ? parseISO(dateString) : dateString;
    return format(date, "MMM d, yyyy");
  } catch {
    return dateString;
  }
}

export function formatDateTime(dateString) {
  if (!dateString) return "";
  try {
    const date = typeof dateString === "string" ? parseISO(dateString) : dateString;
    return format(date, "MMM d, yyyy 'at' h:mm a");
  } catch {
    return dateString;
  }
}

export function riskColor(label) {
  switch (label?.toUpperCase()) {
    case "HIGH":
      return { bg: "bg-risk-high-bg", text: "text-risk-high", border: "border-risk-high" };
    case "MEDIUM":
      return { bg: "bg-risk-medium-bg", text: "text-risk-medium", border: "border-risk-medium" };
    case "LOW":
      return { bg: "bg-risk-low-bg", text: "text-risk-low", border: "border-risk-low" };
    default:
      return { bg: "bg-bg-hover", text: "text-text-secondary", border: "border-border-light" };
  }
}

export function getRiskScore(score) {
  if (score > 55) return "HIGH";
  if (score > 35) return "MEDIUM";
  return "LOW";
}

export function truncate(str, maxLength = 50) {
  if (!str) return "";
  return str.length > maxLength ? str.slice(0, maxLength) + "..." : str;
}
