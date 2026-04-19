import { useState, useEffect } from "react";
import { useSearchParams, Link } from "react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { motion } from "framer-motion";
import { KeyRound, CheckCircle } from "lucide-react";
import toast from "react-hot-toast";
import { resetPassword, validateResetToken } from "../../api/auth";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import LoadingSpinner from "../../components/ui/LoadingSpinner";

const schema = z
  .object({
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

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [validating, setValidating] = useState(true);
  const [tokenValid, setTokenValid] = useState(false);
  const [success, setSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) });

  useEffect(() => {
    if (!token) {
      setValidating(false);
      return;
    }
    validateResetToken(token)
      .then(() => setTokenValid(true))
      .catch(() => setTokenValid(false))
      .finally(() => setValidating(false));
  }, [token]);

  const onSubmit = async (data) => {
    try {
      await resetPassword(token, data.newPassword);
      setSuccess(true);
      toast.success("Password reset successfully!");
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to reset password");
    }
  };

  if (validating) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-6 bg-gradient-to-b from-gradient-warm-start to-bg-primary">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md"
      >
        <div className="bg-bg-secondary rounded-2xl shadow-sm border border-border-light p-8">
          {!token || !tokenValid ? (
            <div className="text-center">
              <h1 className="text-xl font-semibold text-text-primary mb-2">
                Invalid or expired link
              </h1>
              <p className="text-sm text-text-secondary mb-6">
                This reset link is no longer valid.
              </p>
              <Link to="/forgot-password">
                <Button variant="secondary" className="w-full">
                  Request a new link
                </Button>
              </Link>
            </div>
          ) : success ? (
            <div className="text-center">
              <div className="flex justify-center mb-4">
                <CheckCircle className="w-12 h-12 text-status-success" />
              </div>
              <h1 className="text-xl font-semibold text-text-primary mb-2">
                Password reset!
              </h1>
              <p className="text-sm text-text-secondary mb-6">
                Your password has been reset successfully.
              </p>
              <Link to="/login">
                <Button className="w-full">Go to Login</Button>
              </Link>
            </div>
          ) : (
            <>
              <div className="flex justify-center mb-4">
                <div className="p-3 bg-accent-primary/10 rounded-xl">
                  <KeyRound className="w-8 h-8 text-accent-primary" />
                </div>
              </div>
              <h1 className="text-2xl font-semibold text-text-primary text-center mb-1">
                Reset your password
              </h1>
              <p className="text-sm text-text-secondary text-center mb-8">
                Enter a new password for your account.
              </p>

              <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                <Input
                  id="newPassword"
                  label="New Password"
                  type="password"
                  placeholder="Enter new password"
                  error={errors.newPassword?.message}
                  {...register("newPassword")}
                />
                <Input
                  id="confirmPassword"
                  label="Confirm Password"
                  type="password"
                  placeholder="Confirm new password"
                  error={errors.confirmPassword?.message}
                  {...register("confirmPassword")}
                />
                <Button type="submit" className="w-full" loading={isSubmitting}>
                  Reset Password
                </Button>
              </form>
            </>
          )}
        </div>
      </motion.div>
    </div>
  );
}
