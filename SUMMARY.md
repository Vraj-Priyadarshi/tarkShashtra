# TarkShastra — Early Academic Risk Detection & Student Intervention Platform

## What is TarkShastra?

TarkShastra is an **AI-powered academic early warning system** built for Indian universities. It monitors student academic performance in real-time, identifies at-risk students before they fail, and generates personalised intervention plans — so teachers, mentors, and coordinators can act early instead of reacting late.

---

## The Problem

In most Indian colleges, struggling students are identified only after exam results — by which time it's too late. Attendance records, internal assessment scores, assignment submissions, and LMS engagement data all exist in silos. Nobody connects the dots until the damage is done.

**TarkShastra connects those dots in real-time.**

---

## How It Works

### 1. Data Flows In Continuously

Teachers enter academic data through intuitive dashboards:
- **Attendance** — Session-by-session or bulk percentage entry for classes
- **IA Marks** — Internal assessment scores per round (IA-1, IA-2, etc.)
- **Assignment Submissions** — Track what's been submitted, late, or missing
- **LMS Engagement Scores** — How actively students interact with learning materials

Coordinators can also **bulk-upload CSV files** for attendance, marks, assignments, and LMS scores — enabling easy onboarding of historical data.

### 2. ML-Powered Risk Scoring

Every student gets a **Risk Score (0–100)** computed from a weighted formula:

| Factor | Weight |
|---|---|
| Attendance | 30% |
| IA Marks | 30% |
| Assignment Completion | 25% |
| LMS Engagement | 15% |

The score is computed **per subject and overall**, with labels:
- **LOW** (0–35) — Student is doing well
- **MEDIUM** (35–55) — Needs monitoring
- **HIGH** (55–100) — Needs immediate intervention

Risk scores are recomputed automatically when new data is entered. Historical scores are preserved so trends can be tracked over time.

### 3. LLM-Powered Personalised Suggestions & Roadmaps

When a student opens their dashboard, the platform calls an **AI model (Llama 3.3 70B via Groq)** with their complete academic profile — overall metrics plus subject-wise breakdowns. The AI returns:

- **Personalised Improvement Tips** — Specific, actionable suggestions like _"Your Data Structures attendance is 40%. Attend the next 10 consecutive lectures to reach 65% before IA-2."_ Each tip includes the area, current value, target value, action to take, and expected impact.

- **4-Week Improvement Roadmap** — A structured week-by-week plan with daily tasks, weekly targets (attendance hours, assignments to complete, LMS sessions, study hours), focus subjects, and milestones. This roadmap is **saved to the database** so it persists across logins and doesn't need to be regenerated every time. Students can also regenerate it when they want a fresh plan.

### 4. Flagging & Intervention Workflow

- **Teachers flag at-risk students** with a note (e.g., _"Hasn't attended last 3 weeks of Data Structures"_)
- **Mentors see all flags** for their assigned mentees and can log formal **interventions** — counselling, parent meetings, academic support — with pre/post risk scores and action items
- **Coordinators get the big picture** — institute-wide risk distribution, department comparisons, and intervention effectiveness reports

### 5. Notifications & Alerts

The platform generates real-time notifications for:
- Risk level changes (e.g., student moved from MEDIUM to HIGH)
- New flags raised by teachers
- Upcoming exam schedules
- Intervention follow-up reminders

Users can opt into **email alerts** for critical notifications.

### 6. What-If Calculator

Students can explore _"What if I improve my attendance to 80%?"_ scenarios. The What-If Calculator lets them adjust individual metric sliders and see how their risk score would change — encouraging proactive behaviour rather than reactive panic.

---

## Four Dashboards, Four Roles

### Student Dashboard
- Personal risk score with visual indicator
- Consistency streak tracker (weeks of sustained attendance)
- Contributing factors breakdown with progress bars
- AI-generated improvement tips (LLM-powered)
- Personalised 4-week improvement roadmap
- Intervention history from mentor meetings
- Detailed academic data per subject
- What-If risk calculator
- Active flags from teachers

### Teacher Dashboard
- Class-wise student risk overview
- Attendance entry (per-session or bulk)
- IA Marks entry per subject/round
- Assignment creation and submission tracking
- LMS score entry
- Subject-level analytics and risk distribution
- Ability to flag at-risk students

### Mentor Dashboard
- Mentee list with risk indicators
- Detailed mentee profiles with full academic data
- Student flags requiring attention
- Intervention logging with action items
- Pre/post intervention risk comparison

### Coordinator Dashboard
- Institute-wide statistics (total students, risk distribution, avg risk score)
- Department-wise risk breakdown charts
- Student management (search, filter, view profiles)
- Teacher management
- CSV bulk data upload (attendance, marks, assignments, LMS)
- Intervention reports and effectiveness metrics
- Exam schedule management
- Institute setup (departments, classes, subjects, mappings)

---

## Key Product Differentiators

| Feature | What Makes It Special |
|---|---|
| **Real-time risk scoring** | Risk updates immediately when any data changes — no batch overnight processing |
| **Subject-level granularity** | Risk isn't just "this student is struggling" — it pinpoints _which subjects_ and _which factors_ |
| **AI-personalised suggestions** | Not generic advice — the LLM sees the student's actual numbers across all subjects and gives specific, achievable recommendations |
| **Persistent roadmaps** | The improvement roadmap is saved to the database, so students see it every time they log in — not just once |
| **What-If scenarios** | Students can self-motivate by seeing how small improvements compound into big risk reductions |
| **Consistency streaks** | Gamification element — students see their "streak" of consecutive good weeks, encouraging sustained effort |
| **Multi-role workflow** | Teachers flag → Mentors intervene → Coordinators oversee. Each role has exactly the tools they need |
| **Intervention tracking** | Captures pre and post risk scores per intervention, enabling evidence-based evaluation of what works |
| **CSV bulk upload** | Instant onboarding of existing data — coordinators don't have to manually re-enter everything |

---

## ML Model Details

The platform's ML layer serves three purposes:

1. **Risk Prediction** — A weighted scoring model that takes four normalised metrics (0–100 each) and produces a risk score. Designed to be interpretable rather than black-box.

2. **Personalised Suggestions** — Uses Groq's Llama 3.3 70B model with carefully engineered prompts. The system prompt establishes it as an _"expert Indian academic counsellor"_ and the dynamic prompt injects the student's complete academic profile. Returns structured JSON with summary, priority areas, individual suggestions (with area/current/target/action/impact), and a motivational note.

3. **Improvement Roadmap** — Same LLM, different prompt. Generates a 4-week structured plan calibrated to the student's risk level (HIGH gets aggressive recovery plans, MEDIUM gets optimisation plans, LOW gets maintenance plans). Each week has a theme, focus subjects, daily tasks, weekly targets, and milestones.

Risk thresholds were derived from analysis of real academic data (documented in the Jupyter notebook). The LLM outputs are constrained to structured JSON schemas to ensure reliable parsing.

---

## Data Model

24 entities covering the full academic lifecycle:

- **Institutional hierarchy**: Institutes → Departments → Classes → Subjects (with class/teacher mappings)
- **People**: Users (with roles: STUDENT, TEACHER, MENTOR, COORDINATOR), Student Profiles, Teacher Profiles
- **Academic data**: Attendance Sessions & Records, IA Marks, Assignments & Submissions, LMS Scores
- **Risk engine**: Risk Scores (per student, per subject, historical), Consistency Streaks
- **AI outputs**: Student Roadmaps (persisted JSON)
- **Intervention pipeline**: Student Flags, Interventions (with Action Items)
- **Communication**: Notifications, Exam Schedules
- **Auth**: Password Reset Tokens

---

## Scale of the Build

| Layer | Count |
|---|---|
| Backend endpoints | 50+ REST APIs |
| Database entities | 24 tables |
| Frontend pages | 34 pages across 4 role dashboards |
| UI components | 29 reusable components (charts, layout, inputs, data tables) |
| Backend services | 22 service classes |
| Chart components | 6 (risk trends, distribution pies, department bars, radar, intervention effectiveness) |

---

## Security

- JWT-based authentication with 6-hour token expiry
- Role-based access control (STUDENT, TEACHER, MENTOR, COORDINATOR)
- Every API endpoint is protected — students can only see their own data, teachers only their subjects, mentors only their mentees
- Password reset via email token flow
- First-login forced password change for seeded accounts
- CORS configured for frontend origin

---

## What This Solves

**Before TarkShastra**: A student silently stops attending Data Structures lectures. Their assignment submissions slow down. Their LMS engagement drops. Nobody notices until the end-semester results come out and they've failed.

**With TarkShastra**: Within days of the pattern emerging, their risk score spikes. Their Data Structures teacher flags them. Their mentor gets notified, logs an intervention, and creates an action plan. The student opens their dashboard and sees exactly what's happening, personalised AI suggestions on how to recover, and a week-by-week roadmap to get back on track. The coordinator tracks whether the intervention worked by comparing pre/post risk scores.

**Early detection. Targeted intervention. Measurable outcomes.**
