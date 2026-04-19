# TarkShastra — End-to-End Testing Guide (Frontend)

Everything here is done through the browser at **http://localhost:5173**. No curl, no terminal commands (except for starting services and DB cleanup).

---

## 0 — Clean Slate (DB Reset)

If you have stale data from a previous run, wipe the DB so the backend re-seeds institutes, departments, classes, and subjects on startup.

```bash
mysql -u root -p'Khushi79#' -e "DROP DATABASE IF EXISTS tarkshastra_db; CREATE DATABASE tarkshastra_db;"
```

Then restart the backend (Step 1).

---

## 1 — Start All Services

Open **4 separate terminals** and run one command in each:

| # | Terminal Command | What it does |
|---|------------------|--------------|
| 1 | `cd Backend/hackathon-app && mvn spring-boot:run -DskipTests` | Starts backend on :8080. Auto-seeds 3 institutes + LDCE structure |
| 2 | `cd ML/ml && python3 main.py` | Starts ML service on :8000 (risk prediction + AI suggestions) |
| 3 | `cd Frontend && npm run dev` | Starts frontend on :5173 |
| 4 | *(keep free for DB queries if needed)* | — |

Wait until backend logs say **"Started HackathonAppApplication"** before proceeding.

> **Tip:** ML service is optional — backend falls back to a local formula if ML is down. But you need it for AI suggestions and roadmap features.

---

## 2 — Coordinator: Login & Change Password

1. Open **http://localhost:5173** → you see the landing page
2. Click **"Sign In"** (top-right) → redirected to `/login`
3. Enter:
   - Email: `coordinator@ldce.ac.in`
   - Password: `LDCE@Coord2026`
4. Click **"Sign In"**
5. You're redirected to **`/change-password`** (forced on first login)
6. Enter:
   - Current Password: `LDCE@Coord2026`
   - New Password: `Admin@1234`
   - Confirm Password: `Admin@1234`
7. Click **"Change Password"**
8. You land on the **Coordinator Dashboard** (`/coordinator/dashboard`)

> **Credentials after this step:** `coordinator@ldce.ac.in` / `Admin@1234`

---

## 3 — Coordinator: Upload Teacher CSV

1. In the **sidebar**, click **"CSV Upload"** → goes to `/coordinator/csv-upload`
2. You see two cards side by side: **Student CSV** and **Teacher CSV**
3. On the **Teacher CSV** card:
   - Click **"Download Template"** to see the expected format (optional)
   - Click the upload area or drag-and-drop the file: **`Documentation/DATA/teachers.csv`**
   - Click **"Upload Teachers"**
4. You should see: **"Success: 11 created, 0 errors"**

This creates 11 teacher accounts — 9 with subject assignments, 2 mentor-only.

---

## 4 — Coordinator: Upload Student CSV

1. Still on `/coordinator/csv-upload`
2. On the **Student CSV** card:
   - Upload the file: **`Documentation/DATA/students.csv`**
   - Click **"Upload Students"**
3. You should see: **"Success: 76 created, 0 errors"**

This creates 76 students across 4 classes (CE-A, CE-B, ME-A, EC-A), each assigned to a mentor.

---

## 5 — Coordinator: Verify on Dashboard

1. Click **"Dashboard"** in the sidebar → `/coordinator/dashboard`
2. Verify the stat cards:
   - **Total Students:** 76
   - **Total Teachers:** 11
   - **Departments:** 3 (CE, ME, EC)
3. The **Risk Distribution** pie chart will be empty (no data yet)
4. Click **"Students"** in sidebar → see all 76 students in a searchable/sortable table
5. Click **"Teachers"** in sidebar → see all 11 teachers with their role badges

---

## 6 — Coordinator: Sign Out

1. At the **bottom of the sidebar**, click **"Sign Out"**
2. You're back at the login page

---

## 7 — Teacher: Login & Change Password

1. Go to `/login`
2. Enter:
   - Email: `rajesh.patel@ldce.ac.in`
   - Password: `TarkShastra@123`
3. Click **"Sign In"** → redirected to `/change-password`
4. Change password:
   - Current: `TarkShastra@123`
   - New: `Teacher@1234`
   - Confirm: `Teacher@1234`
5. Click **"Change Password"**
6. You land on the **Teacher Dashboard** (`/teacher/dashboard`)
7. You see subject cards for **CE301 (Data Structures)** assigned to classes CE-A and CE-B

> **Credentials after this step:** `rajesh.patel@ldce.ac.in` / `Teacher@1234`

---

## 8 — Teacher: Enter Attendance

1. Click **"Attendance"** in sidebar → `/teacher/attendance`
2. **Select Subject:** Pick `CE301 - Data Structures` from the dropdown
3. **Select Class:** Pick `CE-A`
4. **Select Date:** Pick any date (e.g. `2026-01-06`)
5. **Entry Mode:** Keep as `Per Session`
6. A table appears with all 20 CE-A students, each with **Present / Absent** radio buttons
7. Click **"All Present"** button to mark everyone present (or set individually)
8. Change a few students to **Absent** for realistic data
9. Click **"Submit Attendance"**
10. Toast: **"Attendance submitted successfully"**
11. Repeat for a few more dates to build up data (the history table below shows past sessions)

> **Do this for at least 3-4 sessions** so attendance percentages are meaningful.

---

## 9 — Teacher: Enter IA Marks

1. Click **"IA Marks"** in sidebar → `/teacher/ia-marks`
2. **Select Subject:** `CE301 - Data Structures`
3. **Select Class:** `CE-A`
4. **IA Round:** Select `IA-1`
5. **Max Marks:** Enter `30`
6. A table appears with all students — enter marks for each (e.g., 8-28 range, check "Absent" for 1-2 students)
7. Click **"Submit Marks"**
8. Toast: **"IA marks submitted successfully"**
9. Repeat for **IA-2** and **IA-3**

---

## 10 — Teacher: Create Assignments & Mark Submissions

1. Click **"Assignments"** in sidebar → `/teacher/assignments`
2. **Select Subject:** `CE301 - Data Structures`
3. **Select Class:** `CE-A`
4. Click **"Create Assignment"** button → a modal opens
5. Enter:
   - Title: `Assignment 1 - Linked Lists`
   - Due Date: `2026-02-01`
6. Click **"Create"**
7. The assignment card appears. Click the **submissions icon** on it
8. A table shows all students with a **status dropdown** per student: `Submitted / Not Submitted / Late`
9. Click **"Mark all Submitted"** then change a few to `Not Submitted` or `Late`
10. Click **"Save Submissions"**
11. Repeat: create 2-3 more assignments with varying submission patterns

---

## 11 — Teacher: Enter LMS Scores

1. Click **"LMS Scores"** in sidebar → `/teacher/lms-scores`
2. **Select Subject:** `CE301 - Data Structures`
3. **Select Class:** `CE-A`
4. A table shows all students with a **score input** (0-100) per student
5. Enter scores (mix of high and low, e.g., 30-95)
6. Click **"Submit Scores"**
7. Toast: **"LMS scores submitted successfully"**

---

## 12 — Teacher: Flag a Student

1. Click **"Flag Student"** in sidebar → `/teacher/flag-student`
2. **Select Subject:** `CE301 - Data Structures`
3. **Select Class:** `CE-A`
4. **Select Student:** Start typing a name/roll number in the searchable dropdown — pick a student who has been absent a lot
5. **Note:** Type something like `"Frequently absent, missed IA-2, not submitting assignments"`
6. Click **"Flag Student"**
7. Toast: **"Student flagged successfully"**

---

## 13 — Teacher: View Subject Analytics

1. Click **"Subject Analytics"** in sidebar → `/teacher/subject-analytics`
2. Select your subject and class from the dropdown
3. You see:
   - **4 stat cards:** Average Attendance, Marks, Assignment completion, LMS score
   - **Total students** and **At-risk count**
   - **Risk Distribution pie chart** (if risk has been computed)
   - **At-risk students table** at the bottom

> Risk scores will be null here until the coordinator triggers recomputation (Step 16).

---

## 14 — (Optional) Enter Data for More Subjects / Teachers

To get better visualization data, **repeat Steps 7-12** for more teachers:

| Teacher | Email | Password | Subjects |
|---------|-------|----------|----------|
| Prof. Meena Shah | meena.shah@ldce.ac.in | TarkShastra@123 → Teacher@1234 | CE302 (OOP) |
| Dr. Vikram Desai | vikram.desai@ldce.ac.in | TarkShastra@123 → Teacher@1234 | CE303 (DBMS) |
| Dr. Priya Mehta | priya.mehta@ldce.ac.in | TarkShastra@123 → Teacher@1234 | ME301, ME302 |
| Dr. Amit Bhatt | amit.bhatt@ldce.ac.in | TarkShastra@123 → Teacher@1234 | EC301, EC302 |

> **Or use the automated seed script** to bulk-enter data for all 9 teachers:
> ```bash
> cd Documentation/DATA && ./seed_all_data.sh
> ```
> This enters 12 attendance sessions, 3 IA rounds, 6 assignments, and LMS scores per subject automatically.

---

## 15 — Teacher (as Mentor): View Mentees

Some teachers are also mentors. If the logged-in teacher has the `FACULTY_MENTOR` role, you'll see a **toggle** at the top of the sidebar to switch between **Teacher** and **Mentor** views.

1. Log in as `rajesh.patel@ldce.ac.in` / `Teacher@1234`
2. Switch to **Mentor** view using the toggle
3. Click **"Dashboard"** → `/mentor/dashboard`
   - See: Total mentees, at-risk count, active interventions, open flags
   - Risk distribution pie chart
   - At-risk mentees table
4. Click **"Mentees"** → `/mentor/mentees`
   - Table of all assigned mentees with risk scores and labels
5. Click on a **student row** → `/mentor/mentees/:studentId`
   - Student info card, risk score circle, contributing factors
   - **"Recompute"** button — triggers ML risk calculation for this student
   - **"Create Intervention"** button → modal with type, remarks, follow-up date, action items
   - Risk trend line chart (shows score over time)
6. Click **"Student Flags"** → `/mentor/flags`
   - See flags you or other teachers created
   - Click **"Resolve"** on any flag to mark it resolved
7. Click **"Interventions"** → `/mentor/interventions`
   - See intervention cards with action items
   - Click **"Complete"** on individual action items

---

## 16 — Coordinator: Trigger Risk Recomputation

1. Log out of teacher, log back in as **coordinator**:
   - Email: `coordinator@ldce.ac.in`
   - Password: `Admin@1234`
2. On the **Coordinator Dashboard**, click the **"Recompute All Risk"** button
3. This calls the ML model for every student (or fallback formula if ML is down)
4. Wait for the toast confirmation
5. The dashboard now shows:
   - **Risk Distribution pie chart** with LOW / MEDIUM / HIGH segments
   - **Average Risk Score** number
   - **Department-wise stacked bar chart** showing risk breakdown per department
   - **At-risk count** in the stat cards

---

## 17 — Coordinator: View Intervention Reports

1. Click **"Intervention Reports"** in sidebar → `/coordinator/intervention-reports`
2. See:
   - Total interventions, avg risk reduction, success rate, avg follow-up days
   - Effectiveness bar chart (pre vs post risk by type)
   - Breakdown table by intervention type
3. Click **"Export Risk Report"** → downloads a PDF with all student risk data

---

## 18 — Student: Login & View Dashboard

1. Sign out of coordinator
2. Log in as a student:
   - Email: `aarav.patel@ldce.ac.in`
   - Password: `TarkShastra@123`
3. Redirected to `/change-password` → change to `Student@1234`
4. You land on **Student Dashboard** (`/student/dashboard`):
   - **Risk Score circle** with color-coded label (LOW = green, MEDIUM = yellow, HIGH = red)
   - **Consistency streak** (flame dots)
   - **Contributing factors** (progress bars for attendance, marks, etc.)
   - **Improvement tips**
   - **Mentor info card** (name, email of assigned mentor)

---

## 19 — Student: View Academic Data

1. Click **"Academics"** in sidebar → `/student/academics`
2. See:
   - **4 stat cards:** Overall Attendance %, Marks %, Assignment %, LMS %
   - **Overall risk score** with label
   - **Subject-wise bar chart** — grouped bars for attendance/marks/assignment/LMS per subject
   - **Subject table** with color-coded percentages (green >70, yellow 40-70, red <40)

---

## 20 — Student: What-If Calculator

1. Click **"What-If Calculator"** in sidebar → `/student/what-if`
2. You see per-subject sliders for attendance, marks, assignment, and LMS (0-100)
3. Adjust the sliders to simulate different scenarios
4. Click **"Calculate"**
5. See: **Current risk score** vs **Predicted risk score** with delta
6. This helps students understand what they need to improve

---

## 21 — Student: View Flags & Interventions

1. Click **"My Flags"** → see any flags raised by teachers (from Step 12)
2. Click **"Interventions"** → see any interventions created by mentor (from Step 15)
   - Intervention details: type, mentor name, remarks, pre→post risk, action items, follow-up date

---

## 22 — ML Endpoints (Direct — Optional)

If you want to test the ML service directly, open **http://localhost:8000/docs** for the Swagger UI.

Key endpoints:
- **POST /api/predict** — Risk score prediction
- **POST /api/suggestions** — AI-generated improvement suggestions (uses Groq LLM)
- **POST /api/roadmap** — Personalized improvement roadmap (uses Groq LLM)

---

## Quick Reference

### All Credentials

| Role | Email | Initial Password | After Change |
|------|-------|------------------|--------------|
| **Coordinator** | coordinator@ldce.ac.in | LDCE@Coord2026 | Admin@1234 |
| **Teacher** | rajesh.patel@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Teacher** | meena.shah@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Teacher** | vikram.desai@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Teacher** | anita.sharma@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Teacher** | suresh.kumar@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Teacher** | priya.mehta@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Teacher** | ramesh.joshi@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Teacher** | kavita.trivedi@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Teacher** | amit.bhatt@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Teacher** | neha.pandya@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Teacher** | arun.kumar@ldce.ac.in | TarkShastra@123 | Teacher@1234 |
| **Student** | aarav.patel@ldce.ac.in | TarkShastra@123 | Student@1234 |
| **Student** | priya.sharma@ldce.ac.in | TarkShastra@123 | (your choice) |
| ... | *(76 total students)* | TarkShastra@123 | (your choice) |

### CSV File Locations

| File | Path | Records |
|------|------|---------|
| Teachers | `Documentation/DATA/teachers.csv` | 11 teachers |
| Students | `Documentation/DATA/students.csv` | 76 students |

### LDCE Academic Structure (auto-seeded)

| Department | Code | Classes | Subjects |
|-----------|------|---------|----------|
| Computer Engineering | CE | CE-A, CE-B (Sem 3) | CE301 Data Structures, CE302 OOP, CE303 DBMS, CE304 OS, CE305 Discrete Math |
| Mechanical Engineering | ME | ME-A (Sem 3) | ME301 Fluid Mechanics, ME302 Thermo, ME303 Strength of Materials, ME304 Manufacturing |
| Electronics & Communication | EC | EC-A (Sem 3) | EC301 Signals, EC302 Digital Electronics, EC303 Analog Circuits, EC304 EM Theory |

### Risk Score System

- **Formula:** `performance = 0.30×attendance + 0.30×marks + 0.25×assignment + 0.15×lms` → `risk = 100 − performance`
- **Labels:** LOW (0-35) 🟢, MEDIUM (35-55) 🟡, HIGH (55-100) 🔴

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Login says "Invalid credentials" | Check email/password. If password was already changed, use the new one. |
| CSV upload shows errors | Read the error messages — usually duplicate emails or wrong department codes. Reset DB and try again. |
| No subjects shown for teacher | That teacher has no subject assignments in the CSV (T008, T011 are mentor-only). |
| Risk scores show as "N/A" | Coordinator needs to click "Recompute All Risk" on their dashboard first. |
| Risk pie chart is empty | No data entered yet, or risk hasn't been recomputed. |
| ML suggestions/roadmap fails | Check that ML service is running (`python3 main.py` in `ML/ml/`). Also needs valid Groq API key in ML code. |
| Frontend shows blank page | Check browser console for errors. Make sure backend is running on :8080. |
| "Access Denied" / 403 | You're accessing a page for a different role. Check the sidebar matches your role. |
| Password change screen loops | Clear localStorage in browser DevTools → Application → Local Storage → clear all → refresh → login again. |

---

## Automated Bulk Data Entry (Optional)

If you don't want to enter attendance/marks/assignments/LMS manually for every teacher and subject, run the seed script **after** doing Steps 2-4 (coordinator CSV uploads):

```bash
cd Documentation/DATA && ./seed_all_data.sh
```

This enters data for all 9 teachers across all their subjects:
- 12 attendance sessions per subject
- 3 IA rounds per subject
- 6 assignments with submissions per subject
- LMS scores per subject
- Triggers risk recomputation at the end

Then continue from **Step 18** (Student Login) to see the results.
