import { motion } from "framer-motion";
import { Database, Brain, HeartHandshake } from "lucide-react";

const team = [
  { name: "Hriday Mulchandani", role: "Full Stack & ML" },
  { name: "Tanish Jha", role: "Backend & Architecture" },
  { name: "Vraj Priyadarshi", role: "Frontend & UI/UX" },
  { name: "Jil Patel", role: "ML & Data Engineering" },
];

const steps = [
  {
    icon: Database,
    title: "Data Entry",
    desc: "Teachers input attendance, marks, assignments, and LMS scores for their subjects.",
  },
  {
    icon: Brain,
    title: "ML Risk Analysis",
    desc: "Our machine learning model computes risk scores for every student based on academic data.",
  },
  {
    icon: HeartHandshake,
    title: "Intervention",
    desc: "Faculty mentors support at-risk students through targeted, personalized interventions.",
  },
];

export default function AboutPage() {
  return (
    <div className="pt-24 pb-16 px-6">
      {/* Hero */}
      <section className="max-w-4xl mx-auto text-center mb-20">
        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-4xl md:text-5xl font-display text-text-primary mb-6"
        >
          About TarkShastra
        </motion.h1>
        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="text-lg text-text-secondary max-w-2xl mx-auto leading-relaxed"
        >
          TarkShastra is an academic risk management platform designed to identify
          at-risk students early and facilitate meaningful interventions. Built by
          educators, for educators.
        </motion.p>
        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="text-base text-text-secondary mt-4 max-w-2xl mx-auto leading-relaxed"
        >
          We believe that every student deserves timely support. Our platform
          combines real-time academic data with machine learning to surface risk
          signals that might otherwise go unnoticed — enabling mentors and
          coordinators to act before it's too late.
        </motion.p>
      </section>

      {/* How it works */}
      <section className="max-w-5xl mx-auto mb-20">
        <h2 className="text-2xl font-semibold text-text-primary text-center mb-12">
          How it works
        </h2>
        <div className="grid md:grid-cols-3 gap-8">
          {steps.map((step, i) => (
            <motion.div
              key={step.title}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.15 }}
              className="text-center"
            >
              <div className="w-16 h-16 rounded-2xl bg-accent-primary/10 flex items-center justify-center mx-auto mb-4">
                <step.icon className="w-7 h-7 text-accent-primary" />
              </div>
              <div className="text-xs font-semibold text-accent-primary uppercase tracking-wider mb-2">
                Step {i + 1}
              </div>
              <h3 className="text-lg font-semibold text-text-primary mb-2">
                {step.title}
              </h3>
              <p className="text-sm text-text-secondary leading-relaxed">
                {step.desc}
              </p>
            </motion.div>
          ))}
        </div>
      </section>

      {/* Team placeholder */}
      <section className="max-w-4xl mx-auto text-center">
        <h2 className="text-2xl font-semibold text-text-primary mb-4">
          Built with care
        </h2>
        <p className="text-text-secondary max-w-lg mx-auto mb-12">
          TarkShastra was designed and built during a hackathon by a passionate
          team of developers and educators who believe technology should serve
          the student community.
        </p>

        {/* Team grid */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          {team.map((member, i) => (
            <motion.div
              key={member.name}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.1 }}
              className="bg-bg-secondary rounded-2xl p-6 border border-border-light hover:shadow-md transition-shadow duration-300"
            >
              {/* Avatar placeholder */}
              <div className="w-14 h-14 rounded-full bg-accent-primary/10 flex items-center justify-center mx-auto mb-3">
                <span className="text-lg font-bold text-accent-primary">
                  {member.name.charAt(0)}
                </span>
              </div>
              <h3 className="text-sm font-semibold text-text-primary">{member.name}</h3>
              <p className="text-xs text-text-tertiary mt-1">{member.role}</p>
            </motion.div>
          ))}
        </div>
      </section>
    </div>
  );
}
