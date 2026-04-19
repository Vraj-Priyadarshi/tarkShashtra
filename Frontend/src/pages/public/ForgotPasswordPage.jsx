import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { motion } from "framer-motion";
import { Mail } from "lucide-react";
import { forgotPassword } from "../../api/auth";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";

const schema = z.object({
  email: z.string().email("Please enter a valid email"),
});

export default function ForgotPasswordPage() {
  const [sent, setSent] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) });

  const onSubmit = async (data) => {
    try {
      await forgotPassword(data.email);
    } catch {
      // Silently handle — don't reveal if account exists
    }
    setSent(true);
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
            <div className="p-3 bg-status-info-bg rounded-xl">
              <Mail className="w-8 h-8 text-status-info" />
            </div>
          </div>

          <h1 className="text-2xl font-semibold text-text-primary text-center mb-1">
            Forgot password?
          </h1>
          <p className="text-sm text-text-secondary text-center mb-8">
            {sent
              ? "If an account exists with that email, we've sent a reset link."
              : "Enter your email and we'll send you a reset link."}
          </p>

          {!sent ? (
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
              <Input
                id="email"
                label="Email"
                type="email"
                placeholder="you@university.ac.in"
                error={errors.email?.message}
                {...register("email")}
              />
              <Button type="submit" className="w-full" loading={isSubmitting}>
                Send Reset Link
              </Button>
            </form>
          ) : (
            <div className="text-center">
              <p className="text-sm text-status-success mb-4">
                Check your email for the reset link.
              </p>
              <Button
                variant="secondary"
                onClick={() => setSent(false)}
                className="w-full"
              >
                Try another email
              </Button>
            </div>
          )}
        </div>
      </motion.div>
    </div>
  );
}
