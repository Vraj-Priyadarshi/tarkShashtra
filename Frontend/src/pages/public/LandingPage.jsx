import { Link } from "react-router";
import { motion } from "framer-motion";
import {
  Brain, HeartHandshake, ShieldAlert, BarChart3,
  GraduationCap, Users, BookOpen, Bell,
  TrendingUp, ArrowRight, CheckCircle,
  LineChart, PieChart, Upload,
} from "lucide-react";
import Button from "../../components/ui/Button";

// ── Animation helpers ──────────────────────────────────────────────────────
const fadeUp = {
  hidden: { opacity: 0, y: 24 },
  visible: (i = 0) => ({
    opacity: 1, y: 0,
    transition: { delay: i * 0.12, duration: 0.5, ease: "easeOut" },
  }),
};
const fadeIn = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { duration: 0.6 } },
};

// ── Data ───────────────────────────────────────────────────────────────────
const stats = [
  { value: "70+", label: "Students per institute" },
  { value: "4", label: "Roles supported" },
  { value: "ML", label: "Powered risk engine" },
  { value: "Real-time", label: "Risk detection" },
];

const roles = [
  {
    icon: GraduationCap,
    role: "Student",
    color: "text-indigo-500",
    bg: "bg-indigo-50",
    features: [
      "Personal risk score dashboard",
      "What-if academic simulator",
      "Subject-wise performance radar",
      "View active interventions",
    ],
  },
  {
    icon: BookOpen,
    role: "Subject Teacher",
    color: "text-amber-500",
    bg: "bg-amber-50",
    features: [
      "Attendance session management",
      "IA marks & assignment entry",
      "LMS score submission",
      "Flag at-risk students",
    ],
  },
  {
    icon: HeartHandshake,
    role: "Faculty Mentor",
    color: "text-emerald-500",
    bg: "bg-emerald-50",
    features: [
      "Mentee risk trend monitoring",
      "Targeted intervention creation",
      "Action-item tracking",
      "Flag resolution workflow",
    ],
  },
  {
    icon: ShieldAlert,
    role: "Coordinator",
    color: "text-rose-500",
    bg: "bg-rose-50",
    features: [
      "Institute-wide risk overview",
      "Bulk CSV student/teacher upload",
      "Department risk analytics",
      "Intervention effectiveness reports",
    ],
  },
];

const howItWorks = [
  {
    icon: Upload,
    step: "01",
    title: "Data Entry",
    desc: "Teachers log attendance, IA marks, assignments, and LMS scores for every subject.",
  },
  {
    icon: Brain,
    step: "02",
    title: "ML Risk Analysis",
    desc: "Our machine learning engine computes individual risk scores across all academic dimensions.",
  },
  {
    icon: HeartHandshake,
    step: "03",
    title: "Intervention",
    desc: "Mentors receive actionable insights and record targeted interventions for at-risk students.",
  },
];

const testimonials = [
  {
    quote: "I used to dread checking student performance data. Now I actually look forward to it.",
    name: "Prof. A", org: "PDEU",
  },
  {
    quote: "It feels like the system understands what teachers need — not more data, but better context.",
    name: "Dr. B", org: "Nirma University",
  },
];

// ── Dashboard mockup component ─────────────────────────────────────────────
function DashboardMockup() {
  return (
    <div className="rounded-2xl border border-border-light bg-bg-secondary shadow-xl overflow-hidden select-none">
      {/* Top bar */}
      <div className="flex items-center gap-2 px-4 py-3 border-b border-border-light bg-bg-primary">
        <div className="w-3 h-3 rounded-full bg-red-400" />
        <div className="w-3 h-3 rounded-full bg-amber-400" />
        <div className="w-3 h-3 rounded-full bg-green-400" />
        <span className="ml-3 text-xs text-text-tertiary">TarkShastra — Coordinator Dashboard</span>
      </div>
      <div className="flex">
        {/* Sidebar */}
        <div className="w-36 border-r border-border-light p-3 space-y-1 bg-bg-primary">
          {["Dashboard", "Students", "Teachers", "Institute Setup", "CSV Upload", "Reports"].map((item, i) => (
            <div key={item} className={`text-xs px-3 py-2 rounded-lg ${i === 0 ? "bg-accent-primary/10 text-accent-primary font-medium" : "text-text-tertiary"}`}>
              {item}
            </div>
          ))}
        </div>
        {/* Main content */}
        <div className="flex-1 p-4 space-y-3">
          {/* Stat cards */}
          <div className="grid grid-cols-4 gap-2">
            {[
              { label: "Students", value: "70", color: "text-indigo-500" },
              { label: "High Risk", value: "12", color: "text-red-500" },
              { label: "Teachers", value: "8", color: "text-emerald-500" },
              { label: "Avg Risk", value: "38.2", color: "text-amber-500" },
            ].map((s) => (
              <div key={s.label} className="bg-bg-primary rounded-xl p-3 border border-border-light">
                <p className={`text-lg font-bold ${s.color}`}>{s.value}</p>
                <p className="text-[10px] text-text-tertiary mt-0.5">{s.label}</p>
              </div>
            ))}
          </div>
          {/* Charts row */}
          <div className="grid grid-cols-2 gap-2">
            {/* Fake pie chart */}
            <div className="bg-bg-primary rounded-xl p-3 border border-border-light">
              <p className="text-xs font-medium text-text-primary mb-2">Risk Distribution</p>
              <div className="flex items-center gap-3">
                <div className="relative w-16 h-16">
                  <svg viewBox="0 0 32 32" className="w-full h-full -rotate-90">
                    <circle r="14" cx="16" cy="16" fill="none" stroke="#fef2f2" strokeWidth="4" />
                    <circle r="14" cx="16" cy="16" fill="none" stroke="#ef4444" strokeWidth="4"
                      strokeDasharray="22 66" strokeDashoffset="0" />
                    <circle r="14" cx="16" cy="16" fill="none" stroke="#f59e0b" strokeWidth="4"
                      strokeDasharray="30 58" strokeDashoffset="-22" />
                    <circle r="14" cx="16" cy="16" fill="none" stroke="#22c55e" strokeWidth="4"
                      strokeDasharray="36 52" strokeDashoffset="-52" />
                  </svg>
                </div>
                <div className="space-y-1 text-[10px]">
                  <div className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-red-500" /> High 17%</div>
                  <div className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-amber-500" /> Medium 43%</div>
                  <div className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-green-500" /> Low 40%</div>
                </div>
              </div>
            </div>
            {/* Fake bar chart */}
            <div className="bg-bg-primary rounded-xl p-3 border border-border-light">
              <p className="text-xs font-medium text-text-primary mb-2">By Department</p>
              <div className="space-y-1.5">
                {[{ dept: "CE", high: 8, med: 18, low: 14 }, { dept: "ME", high: 3, med: 7, low: 5 }, { dept: "EC", high: 1, med: 5, low: 9 }].map((d) => {
                  const total = d.high + d.med + d.low;
                  return (
                    <div key={d.dept} className="flex items-center gap-2">
                      <span className="text-[10px] w-4 text-text-tertiary">{d.dept}</span>
                      <div className="flex-1 flex h-3 rounded overflow-hidden gap-px">
                        <div className="bg-red-400" style={{ width: `${(d.high / total) * 100}%` }} />
                        <div className="bg-amber-400" style={{ width: `${(d.med / total) * 100}%` }} />
                        <div className="bg-green-400" style={{ width: `${(d.low / total) * 100}%` }} />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function StudentMockup() {
  return (
    <div className="rounded-2xl border border-border-light bg-bg-secondary shadow-xl overflow-hidden select-none">
      <div className="flex items-center gap-2 px-4 py-3 border-b border-border-light bg-bg-primary">
        <div className="w-3 h-3 rounded-full bg-red-400" />
        <div className="w-3 h-3 rounded-full bg-amber-400" />
        <div className="w-3 h-3 rounded-full bg-green-400" />
        <span className="ml-3 text-xs text-text-tertiary">TarkShastra — Student Dashboard</span>
      </div>
      <div className="p-4 space-y-3">
        <div className="grid grid-cols-2 gap-3">
          {/* Risk score circle */}
          <div className="bg-gradient-to-br from-amber-50 to-orange-50 rounded-xl p-4 border border-amber-100 flex items-center gap-4">
            <div className="w-16 h-16 rounded-full border-4 border-amber-400 flex items-center justify-center">
              <div className="text-center">
                <p className="text-lg font-bold text-text-primary">42.5</p>
                <p className="text-[9px] text-text-tertiary">SCORE</p>
              </div>
            </div>
            <div>
              <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-amber-100 text-amber-700">MEDIUM</span>
              <p className="text-[10px] text-text-tertiary mt-1">Academic risk</p>
            </div>
          </div>
          {/* Streak */}
          <div className="bg-bg-primary rounded-xl p-4 border border-border-light">
            <p className="text-xs font-medium text-text-primary mb-2">Streak 🔥</p>
            <div className="flex gap-1">
              {Array.from({ length: 7 }).map((_, i) => (
                <div key={i} className={`w-4 h-4 rounded-full ${i < 5 ? "bg-accent-warm" : "bg-bg-hover"}`} />
              ))}
            </div>
            <p className="text-[10px] text-text-tertiary mt-1">5 weeks consistent</p>
          </div>
        </div>
        {/* Radar placeholder */}
        <div className="bg-bg-primary rounded-xl p-3 border border-border-light">
          <p className="text-xs font-medium text-text-primary mb-2">Subject Performance</p>
          <div className="flex gap-2">
            {[["Attendance", 72, "indigo"], ["Marks", 58, "amber"], ["Assignments", 85, "emerald"], ["LMS", 45, "rose"]].map(([label, val, c]) => (
              <div key={label} className="flex-1 text-center">
                <div className="relative mx-auto w-8">
                  <div className="h-12 bg-bg-hover rounded overflow-hidden flex flex-col-reverse">
                    <div className={`bg-${c}-400`} style={{ height: `${val}%` }} />
                  </div>
                </div>
                <p className="text-[9px] text-text-tertiary mt-1">{label}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Page ───────────────────────────────────────────────────────────────────
export default function LandingPage() {
  return (
    <div className="overflow-hidden">

      {/* ── Hero ─────────────────────────────────────────────────────────── */}
      <section className="relative min-h-screen flex flex-col items-center justify-center text-center px-6 bg-gradient-to-b from-gradient-warm-start via-[#fef6ee] to-bg-primary overflow-hidden">
        {/* Background orbs */}
        <div className="absolute top-20 left-1/4 w-96 h-96 bg-accent-primary/5 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute bottom-32 right-1/4 w-64 h-64 bg-accent-warm/8 rounded-full blur-3xl pointer-events-none" />

        <motion.div variants={fadeUp} custom={0} initial="hidden" animate="visible" className="mb-5">
          <span className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-risk-low-bg text-risk-low text-xs font-semibold ring-1 ring-risk-low/20">
            <span className="w-2 h-2 rounded-full bg-risk-low animate-pulse" />
            Early Access Available
          </span>
        </motion.div>

        <motion.h1
          variants={fadeUp} custom={1} initial="hidden" animate="visible"
          className="text-5xl md:text-7xl font-display text-text-primary max-w-4xl leading-[1.1] tracking-tight"
        >
          Academic risk,
          <br />
          <span className="text-accent-primary">detected early.</span>
        </motion.h1>

        <motion.p
          variants={fadeUp} custom={2} initial="hidden" animate="visible"
          className="mt-6 text-lg md:text-xl text-text-secondary max-w-2xl leading-relaxed"
        >
          TarkShastra connects students, teachers, mentors, and coordinators on one
          platform — powered by ML, guided by empathy.
        </motion.p>

        <motion.div
          variants={fadeUp} custom={3} initial="hidden" animate="visible"
          className="mt-10 flex flex-wrap items-center justify-center gap-4"
        >
          <Link to="/login">
            <Button size="lg" className="gap-2">
              Get Started <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
          <a href="#how-it-works">
            <Button variant="secondary" size="lg">Learn How It Works</Button>
          </a>
        </motion.div>

        {/* Stats bar */}
        <motion.div
          variants={fadeIn} initial="hidden" animate="visible"
          className="mt-20 grid grid-cols-2 md:grid-cols-4 gap-8 w-full max-w-3xl"
        >
          {stats.map((s, i) => (
            <motion.div key={s.label} variants={fadeUp} custom={4 + i} initial="hidden" animate="visible" className="text-center">
              <p className="text-2xl md:text-3xl font-bold text-text-primary">{s.value}</p>
              <p className="text-sm text-text-tertiary mt-1">{s.label}</p>
            </motion.div>
          ))}
        </motion.div>
      </section>

      {/* ── Dashboard Preview ─────────────────────────────────────────────── */}
      <section className="py-24 px-6 bg-bg-secondary">
        <div className="max-w-6xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }} className="text-center mb-16"
          >
            <h2 className="text-3xl md:text-4xl font-semibold text-text-primary mb-4">
              See it in action
            </h2>
            <p className="text-text-secondary max-w-xl mx-auto">
              Purpose-built dashboards for every stakeholder — calm, clear, and actionable.
            </p>
          </motion.div>

          <div className="grid md:grid-cols-2 gap-8 items-start">
            <motion.div
              initial={{ opacity: 0, x: -30 }} whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }} transition={{ duration: 0.6 }}
            >
              <p className="text-xs font-semibold text-accent-primary uppercase tracking-widest mb-3">Coordinator View</p>
              <DashboardMockup />
            </motion.div>
            <motion.div
              initial={{ opacity: 0, x: 30 }} whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }} transition={{ duration: 0.6, delay: 0.15 }}
            >
              <p className="text-xs font-semibold text-emerald-500 uppercase tracking-widest mb-3">Student View</p>
              <StudentMockup />
            </motion.div>
          </div>
        </div>
      </section>

      {/* ── Features by role ─────────────────────────────────────────────── */}
      <section id="features" className="py-24 px-6">
        <div className="max-w-6xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }} className="text-center mb-16"
          >
            <h2 className="text-3xl md:text-4xl font-semibold text-text-primary mb-4">
              Built for every role
            </h2>
            <p className="text-text-secondary max-w-xl mx-auto">
              Each role gets a focused experience with exactly the tools they need.
            </p>
          </motion.div>

          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {roles.map((r, i) => (
              <motion.div
                key={r.role}
                custom={i} variants={fadeUp} initial="hidden"
                whileInView="visible" viewport={{ once: true }}
                className="bg-bg-secondary rounded-2xl p-6 border border-border-light hover:shadow-lg transition-all duration-300 hover:-translate-y-1"
              >
                <div className={`w-12 h-12 rounded-xl ${r.bg} flex items-center justify-center mb-4`}>
                  <r.icon className={`w-6 h-6 ${r.color}`} />
                </div>
                <h3 className="text-base font-semibold text-text-primary mb-3">{r.role}</h3>
                <ul className="space-y-2">
                  {r.features.map((f) => (
                    <li key={f} className="flex items-start gap-2 text-sm text-text-secondary">
                      <CheckCircle className="w-3.5 h-3.5 text-status-success mt-0.5 flex-shrink-0" />
                      {f}
                    </li>
                  ))}
                </ul>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Philosophy split ─────────────────────────────────────────────── */}
      <section className="py-24 px-6 bg-bg-secondary">
        <div className="max-w-6xl mx-auto grid md:grid-cols-2 gap-12 items-center">
          <motion.div
            initial={{ opacity: 0, x: -30 }} whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }} transition={{ duration: 0.6 }}
          >
            <h2 className="text-3xl md:text-4xl font-semibold text-text-primary leading-snug">
              It's not about catching failures.
            </h2>
            <p className="text-2xl md:text-3xl text-accent-primary font-medium mt-2">
              It's about catching them before they happen.
            </p>
            <p className="text-text-secondary mt-6 leading-relaxed">
              TarkShastra analyzes attendance, marks, assignment completion, and LMS
              engagement to surface early warning signals — giving educators the
              context to act before it's too late.
            </p>
            <div className="mt-8 grid grid-cols-3 gap-4">
              {[
                { icon: LineChart, label: "Trend Analysis" },
                { icon: PieChart, label: "Risk Breakdown" },
                { icon: BarChart3, label: "Department View" },
              ].map((item) => (
                <div key={item.label} className="flex flex-col items-center gap-2 p-4 bg-bg-primary rounded-xl border border-border-light text-center">
                  <item.icon className="w-5 h-5 text-accent-primary" />
                  <p className="text-xs text-text-secondary">{item.label}</p>
                </div>
              ))}
            </div>
          </motion.div>

          {/* Risk score visual */}
          <motion.div
            initial={{ opacity: 0, x: 30 }} whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }} transition={{ duration: 0.6, delay: 0.2 }}
            className="bg-bg-primary rounded-2xl p-8 border border-border-light shadow-sm"
          >
            <p className="text-sm font-medium text-text-tertiary mb-6 text-center">Risk Score Zones</p>
            <div className="relative h-4 rounded-full overflow-hidden mb-6">
              <div className="absolute inset-0 flex">
                <div className="flex-1 bg-gradient-to-r from-green-200 to-green-400" />
                <div className="flex-1 bg-gradient-to-r from-amber-300 to-amber-500" />
                <div className="flex-1 bg-gradient-to-r from-red-400 to-red-600" />
              </div>
            </div>
            <div className="flex justify-between text-xs text-text-tertiary mb-8">
              <span>0 — Low Risk</span><span>35 — Medium</span><span>55 — High Risk — 100</span>
            </div>
            <div className="space-y-4">
              {[
                { name: "Aarav P.", score: 28, label: "LOW", color: "bg-green-400" },
                { name: "Priya J.", score: 47, label: "MEDIUM", color: "bg-amber-400" },
                { name: "Rohan T.", score: 71, label: "HIGH", color: "bg-red-400" },
              ].map((s) => (
                <div key={s.name} className="flex items-center gap-4">
                  <div className="w-20 text-xs text-text-secondary">{s.name}</div>
                  <div className="flex-1 h-2 bg-bg-hover rounded-full overflow-hidden">
                    <div className={`h-full ${s.color} rounded-full`} style={{ width: `${s.score}%` }} />
                  </div>
                  <span className={`text-xs font-semibold w-14 text-right ${s.label === "HIGH" ? "text-red-500" : s.label === "MEDIUM" ? "text-amber-500" : "text-green-500"}`}>
                    {s.score} · {s.label}
                  </span>
                </div>
              ))}
            </div>
          </motion.div>
        </div>
      </section>

      {/* ── How it works ─────────────────────────────────────────────────── */}
      <section id="how-it-works" className="py-24 px-6">
        <div className="max-w-5xl mx-auto">
          <motion.h2
            initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-3xl md:text-4xl font-semibold text-text-primary text-center mb-16"
          >
            Three steps to insight
          </motion.h2>
          <div className="grid md:grid-cols-3 gap-10 relative">
            {/* Connector line */}
            <div className="hidden md:block absolute top-8 left-1/6 right-1/6 h-px bg-gradient-to-r from-transparent via-border-medium to-transparent" />
            {howItWorks.map((step, i) => (
              <motion.div
                key={step.title}
                custom={i} variants={fadeUp} initial="hidden"
                whileInView="visible" viewport={{ once: true }}
                className="text-center relative"
              >
                <div className="w-16 h-16 rounded-2xl bg-accent-primary/10 flex items-center justify-center mx-auto mb-4 relative z-10">
                  <step.icon className="w-7 h-7 text-accent-primary" />
                </div>
                <div className="text-xs font-bold text-accent-primary tracking-widest mb-2">{step.step}</div>
                <h3 className="text-lg font-semibold text-text-primary mb-2">{step.title}</h3>
                <p className="text-sm text-text-secondary leading-relaxed">{step.desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Testimonials ─────────────────────────────────────────────────── */}
      <section className="py-24 px-6 bg-bg-secondary">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-3xl font-semibold text-text-primary text-center mb-12">
            Notes from the community
          </h2>
          <div className="grid md:grid-cols-2 gap-6">
            {testimonials.map((t, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }} transition={{ delay: i * 0.15 }}
                className="bg-bg-primary rounded-2xl p-8 border border-border-light"
              >
                <p className="text-base text-text-primary italic leading-relaxed mb-6">"{t.quote}"</p>
                <p className="text-sm font-medium text-text-primary">
                  — {t.name}, <span className="text-text-secondary">{t.org}</span>
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* ── CTA ──────────────────────────────────────────────────────────── */}
      <section className="py-24 px-6">
        <motion.div
          initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          className="max-w-3xl mx-auto text-center bg-gradient-to-br from-accent-primary/8 to-accent-warm/8 rounded-3xl border border-accent-primary/20 p-16"
        >
          <TrendingUp className="w-10 h-10 text-accent-primary mx-auto mb-4" />
          <h2 className="text-3xl md:text-4xl font-semibold text-text-primary mb-4">
            Ready to support your students?
          </h2>
          <p className="text-text-secondary mb-8 max-w-lg mx-auto">
            Log in with your institute credentials and start identifying at-risk
            students before it's too late.
          </p>
          <Link to="/login">
            <Button size="lg" className="gap-2">
              Sign In to TarkShastra <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
        </motion.div>
      </section>
    </div>
  );
}
