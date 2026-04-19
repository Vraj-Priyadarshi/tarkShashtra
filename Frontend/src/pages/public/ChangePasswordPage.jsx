import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { motion } from "framer-motion";
import { Eye, EyeOff, ShieldCheck } from "lucide-react";
import toast from "react-hot-toast";
import useAuth from "../../hooks/useAuth";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";

const schema = z
  .object({
    currentPassword: z.string().min(1, "Current password is required"),
    newPassword: z
      .string()
      .min(8, "Min 8 characters")
      .regex(/[!@#$%^&*(),.?":{}|<>]/, "Must contain a special character"),
    confirmPassword: z.string().min(1, "Please confirm your password"),
  })
  .refine((d) => d.newPassword === d.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });

const calcStrength = (pwd) => {
  if (!pwd) return { score: 0, label: "", color: "" };
  let score = 0;
  if (pwd.length >= 8) score++;
  if (pwd.length >= 12) score++;
  if (/[A-Z]/.test(pwd)) score++;
  if (/[0-9]/.test(pwd)) score++;
  if (/[!@#$%^&*(),.?":{}|<>]/.test(pwd)) score++;
  const labels = ["", "Weak", "Fair", "Good", "Strong", "Very Strong"];
  const colors = ["", "#ef4444", "#f97316", "#f59e0b", "#22c55e", "#10b981"];
  return { score, label: labels[score] || "Very Strong", color: colors[score] || "#10b981" };
};

export default function ChangePasswordPage() {
  const [showNew, setShowNew] = useState(false);
  const { changePassword } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    watch,
  } = useForm({ resolver: zodResolver(schema) });

  const newPassword = watch("newPassword", "");
  const strength = calcStrength(newPassword);
  const strengthPct = newPassword ? Math.max(10, (strength.score / 5) * 100) : 0;

  const onSubmit = async (data) => {
    try {
      await changePassword(data.currentPassword, data.newPassword);
      toast.success("Password changed successfully!");
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to change password");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-6 bg-gradient-to-b from-gradient-warm-start to-bg-primary">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md"
      >
        <div className="bg-bg-secondary rounded-2xl shadow-sm border border-border-light p-8">
          <div className="flex justify-center mb-4">
            <div className="p-3 bg-accent-warm/10 rounded-xl">
              <ShieldCheck className="w-8 h-8 text-accent-warm" />
            </div>
          </div>

          <h1 className="text-2xl font-semibold text-text-primary text-center mb-1">
            Change your password
          </h1>
          <p className="text-sm text-text-secondary text-center mb-8">
            For security, please change your password before continuing.
          </p>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <Input
              id="currentPassword"
              label="Current Password"
              type="password"
              placeholder="Enter current password"
              error={errors.currentPassword?.message}
              {...register("currentPassword")}
            />

            <div className="space-y-1.5">
              <label className="block text-sm font-medium text-text-primary">
                New Password
              </label>
              <div className="relative">
                <input
                  type={showNew ? "text" : "password"}
                  placeholder="Enter new password"
                  className="w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-accent-primary/30 focus:border-accent-primary placeholder:text-text-tertiary pr-10"
                  {...register("newPassword")}
                />
                <button
                  type="button"
                  onClick={() => setShowNew(!showNew)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-text-tertiary hover:text-text-secondary"
                >
                  {showNew ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {errors.newPassword && (
                <p className="text-xs text-status-error">{errors.newPassword.message}</p>
              )}

              {/* Password strength bar */}
              {newPassword.length > 0 && (
                <div className="mt-2 space-y-1.5">
                  <div className="h-1.5 w-full bg-bg-hover rounded-full overflow-hidden">
                    <motion.div
                      className="h-full rounded-full"
                      style={{ backgroundColor: strength.color }}
                      initial={{ width: 0 }}
                      animate={{ width: `${strengthPct}%` }}
                      transition={{ duration: 0.3 }}
                    />
                  </div>
                  <div className="flex items-center justify-between">
                    <p className="text-xs font-medium" style={{ color: strength.color }}>
                      {strength.label}
                    </p>
                    <div className="flex gap-3 text-xs text-text-tertiary">
                      <span className={/[A-Z]/.test(newPassword) ? "text-status-success" : ""}>A–Z</span>
                      <span className={/[0-9]/.test(newPassword) ? "text-status-success" : ""}>0–9</span>
                      <span className={/[!@#$%^&*(),.?":{}|<>]/.test(newPassword) ? "text-status-success" : ""}>!@#</span>
                      <span className={newPassword.length >= 8 ? "text-status-success" : ""}>8+</span>
                    </div>
                  </div>
                </div>
              )}
            </div>

            <Input
              id="confirmPassword"
              label="Confirm New Password"
              type="password"
              placeholder="Confirm new password"
              error={errors.confirmPassword?.message}
              {...register("confirmPassword")}
            />

            <Button type="submit" className="w-full" loading={isSubmitting}>
              Change Password
            </Button>
          </form>
        </div>
      </motion.div>
    </div>
  );
}
