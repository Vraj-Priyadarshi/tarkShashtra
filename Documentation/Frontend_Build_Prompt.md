# TarkShastra Frontend – Comprehensive Build Prompt

> **Stack:** React 19 + Vite 8 + Tailwind CSS v4 + Axios + React Router v7 + Zustand + React Query v5 + Recharts v3 + Radix UI + React Hook Form + Zod v4 + Framer Motion + Lucide Icons
> **Backend:** Spring Boot on `http://localhost:8080` | ML FastAPI on `http://localhost:8000`
> **Frontend Dev Server:** `http://localhost:5173`

---

## 0. IMPORTANT VERSION NOTES

All dependencies are already installed. Use ONLY the syntax supported by these specific versions:

- **React 19.2**: No `defaultProps` on function components. Use default params. `ref` is a regular prop (no `forwardRef` needed).
- **React Router v7.14**: Use `createBrowserRouter`, `RouterProvider`, `<Outlet />`, `loader`, `action`. NO `<Switch>`, NO `<Redirect>`, NO v5/v6-only patterns. Use `useNavigate()`, `useParams()`, `useSearchParams()`.
- **Tailwind CSS v4.2**: Import via `@import "tailwindcss"` in CSS. Config goes in CSS using `@theme {}` blocks, NOT in `tailwind.config.js` (which doesn't exist). Use `@theme` to define custom colors, fonts, etc.
- **Zustand v5**: Use `create` from `zustand`. No class syntax. Middleware via `persist`, `devtools`.
- **React Query v5**: Use `@tanstack/react-query`. `useQuery({ queryKey, queryFn })` object syntax only. No positional arguments.
- **Zod v4**: Import from `zod`. Use `z.object()`, `z.string()`, etc.
- **React Hook Form v7.72**: Use `useForm`, `register`, `handleSubmit`, `formState`.
- **@hookform/resolvers v5.2**: Use `zodResolver` from `@hookform/resolvers/zod`.
- **Framer Motion v12**: Import from `framer-motion`. Use `motion.div`, `AnimatePresence`.
- **Recharts v3.8**: Use `ResponsiveContainer`, `LineChart`, `BarChart`, `PieChart`, `AreaChart`, etc.
- **Lucide React v1.8**: Import icons directly: `import { Home, Users } from "lucide-react"`.
- **date-fns v4**: Import functions individually: `import { format, parseISO } from "date-fns"`.
- **jwt-decode v4**: Use `jwtDecode` (named export), NOT default `jwt_decode`.
- **react-hot-toast v2.6**: Use `toast.success()`, `toast.error()`, `<Toaster />`.
- **axios v1.15**: Standard axios API. Use interceptors for JWT.
- **Radix UI**: All `@radix-ui/react-*` packages at latest. Use their compound component pattern.
- **clsx + tailwind-merge**: Combine via `cn()` utility function.

---

## 1. DESIGN SYSTEM & VISUAL IDENTITY

### 1.1 Design Philosophy
Inspired by the "Softly." reference UI but adapted for a **professional academic dashboard**. The design should feel:
- **Warm but professional** – not a toy, not corporate cold
- **Clean and breathable** – generous whitespace, no visual clutter
- **Softly rounded** – rounded corners (rounded-xl to rounded-2xl on cards)
- **Gentle depth** – subtle shadows, no harsh borders
- **Calm data visualization** – charts should feel approachable, not intimidating

### 1.2 Color Palette

```css
/* index.css – Inside @theme {} block */
@theme {
  --color-bg-primary: #FAF8F5;          /* Warm off-white (page background) */
  --color-bg-secondary: #FFFFFF;         /* Pure white (cards, modals) */
  --color-bg-sidebar: #1A1A1A;          /* Near-black sidebar */
  --color-bg-sidebar-hover: #2A2A2A;    /* Sidebar hover */
  --color-bg-sidebar-active: #333333;   /* Sidebar active item */
  
  --color-accent-primary: #FFB4A2;       /* Soft coral/peach (primary CTA, highlights) */
  --color-accent-primary-hover: #FF9B85; /* Coral hover */
  --color-accent-secondary: #D4C5F9;     /* Soft lavender (secondary accents) */
  --color-accent-tertiary: #B8E0D2;      /* Soft mint (success states) */
  
  --color-text-primary: #1A1A1A;         /* Near-black for headings */
  --color-text-secondary: #6B6B6B;       /* Muted gray for body text */
  --color-text-tertiary: #9B9B9B;        /* Light gray for captions */
  --color-text-inverse: #FFFFFF;         /* White text on dark backgrounds */
  --color-text-sidebar: #E0E0E0;        /* Light text for sidebar */
  --color-text-sidebar-muted: #888888;  /* Muted sidebar text */
  
  --color-risk-high: #E57373;            /* Soft red (not aggressive) */
  --color-risk-high-bg: #FDECEA;         /* Light red background */
  --color-risk-medium: #FFB74D;          /* Warm amber */
  --color-risk-medium-bg: #FFF3E0;       /* Light amber background */
  --color-risk-low: #81C784;             /* Soft green */
  --color-risk-low-bg: #E8F5E9;          /* Light green background */
  
  --color-border-light: #F0EDE8;         /* Very subtle warm border */
  --color-border-medium: #E0DCD5;        /* Slightly more visible */
  
  --color-gradient-start: #FFECD2;       /* Warm gradient start */
  --color-gradient-end: #FCB69F;         /* Warm gradient end */
  --color-gradient-cool-start: #E8F0FE; /* Cool blue-white */
  --color-gradient-cool-end: #F5E6FF;   /* Lavender tint */
}
```

### 1.3 Typography

```css
@theme {
  --font-sans: 'Inter', system-ui, -apple-system, sans-serif;
  --font-display: 'DM Serif Display', Georgia, serif;  /* For large headings on landing page */
  --font-mono: 'JetBrains Mono', 'Fira Code', monospace;
}
```

Load fonts in `index.html`:
```html
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=DM+Serif+Display&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
```

Typography scale:
- Page titles: `text-2xl font-semibold` (Inter)
- Landing page hero: `text-5xl md:text-6xl` (DM Serif Display)
- Section headings: `text-lg font-semibold`
- Card titles: `text-base font-medium`
- Body text: `text-sm text-text-secondary`
- Captions: `text-xs text-text-tertiary`
- Stat numbers: `text-3xl font-bold`

### 1.4 Component Styling Conventions

**Cards:**
```
bg-bg-secondary rounded-2xl shadow-sm border border-border-light p-6
hover:shadow-md transition-shadow duration-200
```

**Buttons (Primary):**
```
bg-accent-primary text-text-primary font-medium rounded-xl px-6 py-2.5
hover:bg-accent-primary-hover transition-colors duration-200
```

**Buttons (Secondary/Outline):**
```
bg-transparent border border-border-medium text-text-secondary rounded-xl px-6 py-2.5
hover:bg-bg-primary transition-colors duration-200
```

**Buttons (Dark – for landing CTA "Get early access" style):**
```
bg-bg-sidebar text-text-inverse font-medium rounded-xl px-6 py-2.5
hover:bg-bg-sidebar-hover transition-colors duration-200
```

**Input fields:**
```
bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm
focus:outline-none focus:ring-2 focus:ring-accent-primary/30 focus:border-accent-primary
placeholder:text-text-tertiary
```

**Risk Label Badges:**
```jsx
// HIGH → bg-risk-high-bg text-risk-high
// MEDIUM → bg-risk-medium-bg text-risk-medium
// LOW → bg-risk-low-bg text-risk-low
// All: px-3 py-1 rounded-full text-xs font-semibold uppercase tracking-wide
```

**Sidebar:**
```
w-64 bg-bg-sidebar min-h-screen fixed left-0 top-0
```

**Main content area:**
```
ml-64 bg-bg-primary min-h-screen p-8
```

### 1.5 Animations (Framer Motion)

- Page transitions: fade + slight upward slide (duration 0.3s)
- Card hover: subtle scale(1.01) + shadow increase
- Stat counters: animate on mount with `countUp` effect
- Sidebar nav items: hover scale + background transition
- Toast notifications: slide in from top-right
- Modals: fade backdrop + scale dialog

---

## 2. PROJECT STRUCTURE

```
Frontend/src/
├── main.jsx                          # Entry point with providers
├── App.jsx                           # Router setup
├── index.css                         # Tailwind imports + @theme
│
├── api/
│   ├── axios.js                      # Axios instance with JWT interceptor
│   ├── auth.js                       # Auth API calls
│   ├── student.js                    # Student API calls
│   ├── teacher.js                    # Teacher API calls
│   ├── mentor.js                     # Mentor API calls
│   ├── coordinator.js                # Coordinator API calls
│   ├── notifications.js              # Notification API calls
│   └── user.js                       # User API calls
│
├── stores/
│   ├── authStore.js                  # Zustand: auth state, token, user, roles
│   └── uiStore.js                    # Zustand: sidebar state, theme
│
├── hooks/
│   ├── useAuth.js                    # Auth helpers (login, logout, isRole)
│   └── useNotifications.js           # Notification polling/count
│
├── lib/
│   └── utils.js                      # cn() utility, formatDate, riskColor helpers
│
├── components/
│   ├── ui/                           # Reusable primitives
│   │   ├── Button.jsx
│   │   ├── Input.jsx
│   │   ├── Card.jsx
│   │   ├── Badge.jsx
│   │   ├── Modal.jsx                 # Radix Dialog wrapper
│   │   ├── Select.jsx                # Radix Select wrapper
│   │   ├── Tabs.jsx                  # Radix Tabs wrapper
│   │   ├── Tooltip.jsx               # Radix Tooltip wrapper
│   │   ├── DropdownMenu.jsx          # Radix Dropdown wrapper
│   │   ├── Avatar.jsx                # Radix Avatar wrapper
│   │   ├── Progress.jsx              # Radix Progress wrapper
│   │   ├── Separator.jsx             # Radix Separator wrapper
│   │   ├── DataTable.jsx             # Reusable table with sorting/pagination
│   │   ├── FileUpload.jsx            # react-dropzone wrapper
│   │   ├── RiskBadge.jsx             # Risk label badge (HIGH/MEDIUM/LOW)
│   │   ├── StatCard.jsx              # Dashboard stat card with icon + number
│   │   ├── EmptyState.jsx            # Empty state illustration
│   │   └── LoadingSpinner.jsx
│   │
│   ├── layout/
│   │   ├── PublicLayout.jsx          # Layout for landing/about/login (no sidebar)
│   │   ├── DashboardLayout.jsx       # Layout with sidebar + main content
│   │   ├── Sidebar.jsx               # Vertical navigation sidebar
│   │   ├── SidebarNavItem.jsx        # Individual nav link
│   │   └── TopBar.jsx                # Top bar with notifications bell + user avatar
│   │
│   └── charts/
│       ├── RiskTrendChart.jsx        # Line chart for risk score over time
│       ├── RiskDistributionPie.jsx   # Pie chart (HIGH/MEDIUM/LOW breakdown)
│       ├── DepartmentRiskBar.jsx     # Bar chart by department
│       ├── SubjectRadar.jsx          # Radar chart for subject metrics
│       ├── InterventionEffectivenessChart.jsx
│       └── AcademicMetricsBar.jsx    # Grouped bar chart for attendance/marks/etc.
│
├── pages/
│   ├── public/
│   │   ├── LandingPage.jsx           # Marketing/info landing page
│   │   ├── AboutPage.jsx             # About the platform
│   │   ├── LoginPage.jsx             # Login form (email + password only)
│   │   ├── ChangePasswordPage.jsx    # Forced password change on first login
│   │   ├── ForgotPasswordPage.jsx    # Request reset link
│   │   └── ResetPasswordPage.jsx     # Reset with token from URL
│   │
│   ├── student/
│   │   ├── StudentDashboard.jsx      # Main dashboard with risk overview
│   │   ├── AcademicData.jsx          # Subject-wise academic breakdown
│   │   ├── WhatIfCalculator.jsx      # Hypothetical scenario calculator
│   │   ├── MyFlags.jsx               # Flags raised by teachers
│   │   ├── InterventionHistory.jsx   # Past interventions
│   │   └── NotificationsPage.jsx     # Full notification list
│   │
│   ├── teacher/
│   │   ├── TeacherDashboard.jsx      # Subject list + alerts overview
│   │   ├── SubjectAnalytics.jsx      # Per-subject analytics page
│   │   ├── AttendanceEntry.jsx       # Attendance session form (table-style)
│   │   ├── IAMarksEntry.jsx          # IA marks entry (table-style)
│   │   ├── AssignmentManagement.jsx  # Create assignments + mark submissions
│   │   ├── LMSScoreEntry.jsx         # LMS score bulk entry
│   │   ├── FlagStudent.jsx           # Flag a student form
│   │   └── NotificationsPage.jsx
│   │
│   ├── mentor/
│   │   ├── MentorDashboard.jsx       # Mentee overview + risk breakdown
│   │   ├── MenteeList.jsx            # All mentees with risk indicators
│   │   ├── MenteeDetail.jsx          # Individual mentee risk + trend
│   │   ├── Interventions.jsx         # Create + manage interventions
│   │   ├── StudentFlags.jsx          # Unresolved flags to review
│   │   └── NotificationsPage.jsx
│   │
│   └── coordinator/
│       ├── CoordinatorDashboard.jsx  # Institute-wide stats + department breakdown
│       ├── StudentManagement.jsx     # Student list with search/filter
│       ├── TeacherManagement.jsx     # Teacher list + add manually
│       ├── InstituteSetup.jsx        # Tabbed: Departments / Classes / Subjects / Mappings
│       ├── CsvUpload.jsx             # Upload students/teachers via CSV
│       ├── ExamSchedules.jsx         # Manage exam schedules
│       ├── InterventionReport.jsx    # Intervention effectiveness analytics
│       └── NotificationsPage.jsx
│
└── routes/
    ├── index.jsx                     # Main router definition
    ├── ProtectedRoute.jsx            # Auth guard (redirects to /login if no token)
    └── RoleRoute.jsx                 # Role guard (checks role, shows 403 if wrong)
```

---

## 3. CORE INFRASTRUCTURE

### 3.1 Axios Instance with JWT Interceptor (`api/axios.js`)

```javascript
import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// Request interceptor – attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor – handle 401 (expired token)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;
```

### 3.2 Auth Store (`stores/authStore.js`)

Zustand store managing:
- `accessToken` (persisted to localStorage)
- `user` object: `{ id, email, roles, instituteId, instituteName, mustChangePassword, isActive }`
- `isAuthenticated` derived state
- `login(authResponse)` – saves token + user
- `logout()` – clears everything + redirects
- `hasRole(role)` – checks if user has a specific role
- `getPrimaryRole()` – returns the "highest priority" role for dashboard routing:
  - Priority: `ACADEMIC_COORDINATOR` > `FACULTY_MENTOR` > `SUBJECT_TEACHER` > `STUDENT`
- `activeView` – for dual-role teachers: `"teacher"` or `"mentor"` (toggled by role switcher)
- `setActiveView(view)` – switch between teacher/mentor views

### 3.3 React Query Setup

In `main.jsx`, wrap app in `QueryClientProvider` with:
- `staleTime: 5 * 60 * 1000` (5 minutes)
- `retry: 1`
- `refetchOnWindowFocus: false`

### 3.4 Router Setup (`routes/index.jsx`)

Use `createBrowserRouter` with:

**Public routes** (wrapped in `PublicLayout`):
- `/` → LandingPage
- `/about` → AboutPage
- `/login` → LoginPage
- `/change-password` → ChangePasswordPage
- `/forgot-password` → ForgotPasswordPage
- `/reset-password` → ResetPasswordPage

**Protected routes** (wrapped in `ProtectedRoute` → `DashboardLayout`):
- `/dashboard` → redirects to role-specific dashboard
- `/student/*` → (wrapped in `RoleRoute` requiring `STUDENT`)
  - `/student/dashboard`
  - `/student/academics`
  - `/student/what-if`
  - `/student/flags`
  - `/student/interventions`
  - `/student/notifications`
- `/teacher/*` → (wrapped in `RoleRoute` requiring `SUBJECT_TEACHER` or `FACULTY_MENTOR`)
  - `/teacher/dashboard`
  - `/teacher/subject-analytics`
  - `/teacher/attendance`
  - `/teacher/ia-marks`
  - `/teacher/assignments`
  - `/teacher/lms-scores`
  - `/teacher/flag-student`
  - `/teacher/notifications`
- `/mentor/*` → (wrapped in `RoleRoute` requiring `FACULTY_MENTOR`)
  - `/mentor/dashboard`
  - `/mentor/mentees`
  - `/mentor/mentees/:studentId`
  - `/mentor/interventions`
  - `/mentor/flags`
  - `/mentor/notifications`
- `/coordinator/*` → (wrapped in `RoleRoute` requiring `ACADEMIC_COORDINATOR`)
  - `/coordinator/dashboard`
  - `/coordinator/students`
  - `/coordinator/teachers`
  - `/coordinator/institute-setup`
  - `/coordinator/csv-upload`
  - `/coordinator/exam-schedules`
  - `/coordinator/interventions`
  - `/coordinator/notifications`

### 3.5 Protected Route Logic

```
ProtectedRoute:
  1. Check if accessToken exists in authStore
  2. If not → redirect to /login
  3. If user.mustChangePassword === true → redirect to /change-password
  4. Otherwise → render <Outlet />

RoleRoute({ allowedRoles }):
  1. Check if user has at least one of allowedRoles
  2. If not → show 403 page or redirect to correct dashboard
  3. Otherwise → render <Outlet />
```

### 3.6 Post-Login Redirect Logic

After successful login:
1. If `mustChangePassword` → navigate to `/change-password`
2. Else, based on roles (check in priority order):
   - `ACADEMIC_COORDINATOR` → `/coordinator/dashboard`
   - `FACULTY_MENTOR` → `/mentor/dashboard`
   - `SUBJECT_TEACHER` → `/teacher/dashboard`
   - `STUDENT` → `/student/dashboard`

---

## 4. LAYOUTS

### 4.1 PublicLayout
- NO sidebar
- Sticky top navbar (transparent → white on scroll):
  - Left: Logo placeholder (empty circle + "ProductName" text placeholder)
  - Center: Nav links (Features, About, etc.)
  - Right: "Login" button (dark style)
- Content renders below
- Footer with placeholder company name, links (Privacy, Terms, Contact)

### 4.2 DashboardLayout
- **Left Sidebar** (fixed, 64 width, dark background `bg-bg-sidebar`):
  - Top: Logo placeholder + product name
  - Navigation items with Lucide icons
  - **Role Switcher** (only visible if user has both SUBJECT_TEACHER and FACULTY_MENTOR):
    - Two tabs/pills at top of nav: "Teacher" | "Mentor"
    - Switching changes nav items and redirects to that role's dashboard
  - Bottom: User avatar + name + "Sign out" button
- **Main Area** (right of sidebar):
  - **TopBar**: Page title (dynamic) + Notification bell (with unread count badge) + User dropdown
  - **Content**: `<Outlet />` with page-level animations

### 4.3 Sidebar Navigation Items by Role

**STUDENT:**
- Dashboard (LayoutDashboard icon)
- Academics (BookOpen icon)
- What-If Calculator (Calculator icon)
- My Flags (Flag icon)
- Interventions (HeartHandshake icon)
- Notifications (Bell icon)

**TEACHER (activeView === "teacher"):**
- Dashboard (LayoutDashboard)
- Subject Analytics (BarChart3)
- Attendance (ClipboardList)
- IA Marks (FileSpreadsheet)
- Assignments (FileText)
- LMS Scores (Monitor)
- Flag Student (Flag)
- Notifications (Bell)

**MENTOR (activeView === "mentor"):**
- Dashboard (LayoutDashboard)
- Mentees (Users)
- Interventions (HeartHandshake)
- Student Flags (AlertTriangle)
- Notifications (Bell)

**COORDINATOR:**
- Dashboard (LayoutDashboard)
- Students (GraduationCap)
- Teachers (UserCog)
- Institute Setup (Building2)
- CSV Upload (Upload)
- Exam Schedules (Calendar)
- Intervention Reports (TrendingUp)
- Notifications (Bell)

---

## 5. PUBLIC PAGES

### 5.1 Landing Page

**Hero Section** (full viewport height, gradient background from warm peach to soft cream):
- Small pill badge at top: "● Early Access Available" (green dot + text)
- Large heading (DM Serif Display): "[Product Name] for the academic community." (placeholder product name)
- Subheading (Inter, muted): "A gentle space to track student wellness, identify risks early, and support meaningful academic interventions."
- Two CTA buttons:
  - Primary (coral): "Get Started" → links to /login
  - Secondary (outlined): "Learn More" → scrolls to features section

**Philosophy Section** (two-column layout):
- Left: Bold text "It's not about catching failures. It's about catching them before they happen." with lighter second line in accent color
- Right: Soft gradient card with handwritten-style quote: "Finally, a system that cares."

**Features Section** ("Quiet by design." heading):
- Three phone-style mockup cards showing:
  - "Good Morning" – Dashboard preview
  - "Risk Score" – A circular risk indicator
  - "Your Insights" – Academic data grid
- Below: Three feature cards in a row:
  - **Risk Detection** (cloud icon): "Track academic risk in real-time. Powered by ML, guided by empathy."
  - **Smart Interventions** (leaf/sprout icon): "Log and track interventions. See what actually moves the needle."
  - **Gentle Alerts** (moon icon): "No red dots. No panic. Just timely nudges when students need support."

**Community/Testimonials Section:**
- Heading: "Notes from the community"
- Staggered testimonial cards with quotes and names (use placeholder data):
  - "I used to dread checking my students' performance data. Now I actually look forward to it." — Prof. A, PDEU
  - "It feels like the system understands what teachers need. Not more data, but better context." — Dr. B, Nirma

**CTA Section** (centered, gradient background):
- Logo placeholder (circle + dot like Softly.)
- "Ready to get started?"
- "Join the platform that puts student wellness first."
- Email input + "Join" button (or just "Login" button linking to /login)
- Small text: "No spam, just meaningful updates."

**Footer:**
- Left: Logo placeholder + "© 2026 [Product Name]. All rights reserved."
- Center: Privacy | Terms | Contact links
- Right: Social icons (placeholder)

### 5.2 About Page
- Hero: "About [Product Name]" heading
- Mission statement (2–3 paragraphs about the platform's purpose)
- How it works (3-step visual):
  1. Data Entry – Teachers input academic data
  2. Risk Analysis – ML engine computes risk scores
  3. Intervention – Mentors support at-risk students
- Team section (placeholder)

### 5.3 Login Page
- Centered card on warm gradient background
- Logo placeholder at top
- "Welcome back" heading
- Email input field
- Password input field (with show/hide toggle)
- "Sign in" button (coral/primary)
- "Forgot password?" link below
- **NO registration** – accounts are created by coordinators only

### 5.4 Change Password Page
- Shown when `mustChangePassword === true`
- Card with message: "For security, please change your password before continuing."
- Current password field
- New password field (with strength indicator)
- Confirm new password field
- Password requirements displayed: min 8 chars, special character, etc.
- Submit button → on success, saves new token and redirects to role dashboard

### 5.5 Forgot Password Page
- Email input
- "Send Reset Link" button
- Success message: "If an account exists with that email, we've sent a reset link."

### 5.6 Reset Password Page
- Token read from URL query param
- New password + confirm password fields
- Submit → success message → link to login

---

## 6. STUDENT PAGES

### 6.1 Student Dashboard
**API:** `GET /api/student/dashboard` → `StudentDashboardResponse`

Layout:
- **Welcome header**: "Good morning, {fullName}" with roll number, semester, branch below
- **Risk Score Card** (prominent, gradient background):
  - Large circular indicator showing risk score (0-100)
  - Risk label badge (HIGH/MEDIUM/LOW)
  - "Last computed: {date}"
- **Consistency Streak** card:
  - 🔥 Current streak: X weeks
  - Best: Y weeks
  - Visual streak indicator (dots/circles for weeks)
- **Contributing Factors** (horizontal bar cards):
  - Each factor: name, your value vs. class average, contribution %
  - Color-coded by how far below average
- **Improvement Tips** card:
  - List of tips as styled bullet points
- **Mentor Info** card:
  - Mentor name + email
  - "Reach out" link (mailto)

### 6.2 Academic Data
**API:** `GET /api/student/academic-data` → `StudentAcademicDetailResponse`

- **Overall Stats Row**: 4 stat cards (Attendance %, Marks %, Assignment %, LMS %)
- **Subject-wise Table/Cards**:
  - Each subject: name, code, attendance %, IA marks normalized, assignment completion %, LMS score
  - Color-coded cells based on performance thresholds
- **Radar Chart** (Recharts): Spider chart showing all metrics for a selected subject vs. class average

### 6.3 What-If Calculator
**API:** `POST /api/student/what-if` → `WhatIfResponse`

- Instructions card: "See how improving your metrics could change your risk score"
- For each subject the student has:
  - Sliders or number inputs for: Attendance, Marks, Assignment, LMS (0-100)
  - Pre-filled with current values
- "Calculate" button
- Results panel:
  - Side-by-side comparison: Current Risk → Predicted Risk
  - Visual arrow/animation showing change
  - Per-subject breakdown of predictions
  - Color transition (e.g., red → green if improvement)

### 6.4 My Flags
**API:** `GET /api/student/my-flags` → `List<StudentFlagResponse>`

- List of flag cards:
  - Subject name
  - Flagged by (teacher name)
  - Note
  - Date
  - Resolved/Unresolved badge
- Empty state if no flags: "No flags! Keep it up 🎉"

### 6.5 Intervention History
**API:** `GET /api/student/interventions` → `List<InterventionResponse>`

- Timeline-style list of interventions:
  - Type badge (COUNSELLING_SESSION, REMEDIAL_CLASS, etc.)
  - Date
  - Mentor name
  - Remarks
  - Pre/post risk score comparison (if available)
  - Action items with status checkmarks

---

## 7. TEACHER PAGES

### 7.1 Teacher Dashboard
**API:** `GET /api/teacher/dashboard` → `TeacherDashboardResponse`

- **Subject Cards** row: List of subjects teacher is assigned to (clickable → subject analytics)
  - Each card: Subject name, code, class name
- **Alerts Section**:
  - Mentees at risk count (if also a mentor)
  - Pending data entry count
  - Upcoming exam alert (subject, date, days until, high-risk mentees)
- Quick action buttons: "Enter Attendance", "Enter Marks", "Create Assignment"

### 7.2 Subject Analytics
**API:** `GET /api/teacher/subject-analytics?subjectId=uuid&classId=uuid`

- Subject selector (dropdown if teacher has multiple subjects)
- **Stat Cards**: Average attendance, marks, assignment, LMS for the class
- **Risk Distribution** mini pie chart (at-risk vs. safe students)
- **At-Risk Students** table:
  - Name, roll number, risk score, risk label, attendance %
  - Click to flag student

### 7.3 Attendance Entry
**APIs:**
- `GET /api/teacher/my-subjects?academicYear=2025-26` → subject list
- `POST /api/teacher/attendance` → create session
- `GET /api/teacher/attendance?subjectId=x&classId=x` → history

**Page Layout:**
1. Top: Subject selector + Class (auto-filled) + Date picker + Entry mode selector (PER_SESSION / BULK_PERCENTAGE)
2. **Table-style form** (spreadsheet-like):
   - Columns: Student Name | Roll Number | Status (Present/Absent radio) OR Percentage (number input if bulk mode)
   - All students in the class listed
   - Select all "Present" / "Absent" buttons at top
3. Submit button
4. Below: Attendance session history table (past sessions for selected subject+class)

### 7.4 IA Marks Entry
**API:** `POST /api/teacher/ia-marks`

- Subject + Class selector + IA Round selector (IA-1, IA-2, IA-3) + Max Marks input
- **Table form**:
  - Columns: Student Name | Roll Number | Obtained Marks (number input) | Absent (checkbox)
  - If absent checked, marks auto-set to 0
- Submit button

### 7.5 Assignment Management
**APIs:**
- `POST /api/teacher/assignments` → create
- `GET /api/teacher/assignments?subjectId=x&classId=x` → list
- `POST /api/teacher/assignments/{id}/submissions` → mark submissions

- **Create Assignment** card: Subject + Class + Title + Due Date → Create
- **Assignment List**: Cards with title, due date, submission count
- Click assignment → **Mark Submissions** table:
  - Columns: Student Name | Roll Number | Status dropdown (SUBMITTED / NOT_SUBMITTED / LATE)
  - Bulk "Mark all as Submitted" button

### 7.6 LMS Score Entry
**API:** `POST /api/teacher/lms-scores`

- Subject + Class selector
- **Table form**:
  - Columns: Student Name | Roll Number | LMS Score (number input, 0-100)
- Submit button

### 7.7 Flag Student
**API:** `POST /api/teacher/flag-student`

- Student selector (search by name/roll number within teacher's classes)
- Subject selector
- Note (textarea, optional)
- Submit button
- List of previously flagged students (if any)

---

## 8. MENTOR PAGES

### 8.1 Mentor Dashboard
**API:** `GET /api/mentor/dashboard` → `MentorDashboardResponse`

- **Stat Cards Row**:
  - Total Mentees
  - High Risk (red accent)
  - Medium Risk (amber accent)
  - Low Risk (green accent)
  - Unresolved Flags
  - Pending Follow-ups
- **Mentee Risk Distribution** pie chart
- **Mentee Summary Table**: Quick view of all mentees with name, roll, risk score, risk label
  - Click row → navigates to mentee detail

### 8.2 Mentee List
**API:** `GET /api/mentor/mentees` → `List<StudentProfileResponse>`

- Filterable/sortable table:
  - Name, Roll Number, Department, Class, Semester, Risk Score, Risk Label, Attendance %
  - Risk label column has colored badges
  - Click row → `/mentor/mentees/{userId}`

### 8.3 Mentee Detail
**APIs:**
- `GET /api/mentor/mentees/{studentId}/risk` → `RiskScoreResponse`
- `GET /api/mentor/mentees/{studentId}/risk-trend` → `RiskTrendResponse`

- **Student Info Header**: Name, roll, department, class, email
- **Risk Score Card** (same style as student dashboard)
- **Risk Trend Chart** (Recharts LineChart):
  - X-axis: dates, Y-axis: risk score (0-100)
  - Colored zones: green (0-35), amber (35-55), red (55-100)
- **Subject-wise Risk Breakdown** (from subjectRisks):
  - Table or cards showing per-subject scores
- **Quick Actions**: "Create Intervention" button, "Recompute Risk" button

### 8.4 Interventions
**APIs:**
- `POST /api/mentor/interventions` → create
- `GET /api/mentor/interventions` → list
- `PUT /api/mentor/interventions/action-items/{id}/complete`

- **Create Intervention** form (modal or inline):
  - Student selector (from mentees)
  - Intervention type dropdown (COUNSELLING_SESSION, REMEDIAL_CLASS, ASSIGNMENT_EXTENSION, PARENT_MEETING, OTHER)
  - Date picker
  - Remarks textarea
  - Follow-up date (optional)
  - Action items (dynamic list – add/remove text fields)
- **Interventions List**:
  - Cards with: student name, type badge, date, remarks preview
  - Expandable to show full details + action items
  - Action items: each with checkbox to mark complete
  - Pre/Post risk score comparison (if available)

### 8.5 Student Flags
**APIs:**
- `GET /api/mentor/flags` → `List<StudentFlagResponse>`
- `PUT /api/mentor/flags/{flagId}/resolve`

- List of unresolved flags:
  - Student name, subject, flagged-by teacher, note, date
  - "Resolve" button on each
  - Confirmation dialog before resolving

---

## 9. COORDINATOR PAGES

### 9.1 Coordinator Dashboard
**API:** `GET /api/coordinator/dashboard` → `InstituteDashboardResponse`

- **Stat Cards Row**:
  - Total Students
  - Total Teachers
  - High Risk Students (with % of total)
  - Medium Risk Students
  - Low Risk Students
  - Average Risk Score (gauge-style)
  - Total Interventions
- **Department Risk Breakdown** (Recharts BarChart):
  - Grouped bars: High/Medium/Low per department
- **Risk Distribution** pie/donut chart
- **Quick Actions**: "Export Risk Report" (PDF download), "Recompute All Risks"

### 9.2 Student Management
**APIs:**
- `GET /api/coordinator/students?page=0&size=20` → `Page<StudentProfileResponse>`
- `POST /api/coordinator/students/manual` → add student
- `PUT /api/coordinator/students/reassign-mentor`
- `PUT /api/coordinator/users/{userId}/deactivate`
- `PUT /api/coordinator/users/{userId}/activate`
- `GET /api/coordinator/export/student-report/{studentId}` → PDF

- **Search/Filter bar**: Search by name, filter by department, class, risk label
- **Paginated Table**:
  - Columns: Name, Roll, Email, Department, Class, Semester, Mentor, Risk Score, Risk Label, Status (Active/Inactive)
  - Actions dropdown per row: View Report (PDF), Reassign Mentor, Deactivate/Activate
- **Add Student** button → Modal with ManualStudentRequest form
- Pagination controls at bottom

### 9.3 Teacher Management
**APIs:**
- `GET /api/coordinator/teachers?page=0&size=20`
- `POST /api/coordinator/teachers/manual`
- `PUT /api/coordinator/users/{userId}/deactivate`
- `PUT /api/coordinator/users/{userId}/activate`

- Similar to student management but for teachers
- Table columns: Name, Employee ID, Email, Department, Roles, Status
- Add Teacher button → modal with ManualTeacherRequest form (including checkboxes for isSubjectTeacher and isFacultyMentor)

### 9.4 Institute Setup (Tabbed Page)
**APIs:**
- Departments: `GET/POST /api/coordinator/departments`
- Classes: `GET/POST /api/coordinator/classes`
- Subjects: `GET/POST /api/coordinator/subjects`
- Mappings: `POST /api/coordinator/subjects/map-to-class`, `POST /api/coordinator/subjects/map-teacher`

**Tab 1 – Departments:**
- List of departments (name, code)
- "Add Department" form (name + code)

**Tab 2 – Classes:**
- Filter by department
- List of classes (name, semester, academic year, department)
- "Add Class" form (department selector, name, semester, academic year)

**Tab 3 – Subjects:**
- Filter by department
- List of subjects (name, code, department)
- "Add Subject" form (department selector, name, code)

**Tab 4 – Mappings:**
- **Map Subject to Class**: Subject selector + Class selector + Semester + Academic Year → Submit
- **Map Teacher to Subject-Class**: Teacher selector + Subject selector + Class selector + Academic Year → Submit
- List of existing mappings (if backend provides – otherwise show success messages)

### 9.5 CSV Upload
**APIs:**
- `POST /api/coordinator/upload/students`
- `POST /api/coordinator/upload/teachers`
- `GET /api/coordinator/csv-templates/students`
- `GET /api/coordinator/csv-templates/teachers`

- **Two sections**: Upload Students / Upload Teachers
- Each section:
  - "Download Template" link (CSV)
  - Drag-and-drop zone (react-dropzone) for file upload
  - Upload button
  - Results panel after upload:
    - Success count (green)
    - Error count (red)
    - Error details table: Row #, Field, Error Message

### 9.6 Exam Schedules
**APIs:**
- `POST /api/coordinator/exam-schedules`
- `GET /api/coordinator/exam-schedules?startDate=x&endDate=x`

- Date range selector (start + end date)
- Calendar-style or table view of exam schedules
- "Add Exam" form: Subject selector, Class selector, Date, Exam Type (text input)
- List/table of scheduled exams: Subject, Class, Date, Type

### 9.7 Intervention Reports
**API:** `GET /api/coordinator/intervention-effectiveness`

- **Bar/grouped chart**: Intervention types vs. effectiveness
- **Table**: Intervention type, count, avg pre-score, avg post-score, avg improvement
- Visual indication of most effective intervention type

---

## 10. API SERVICE MODULES

Each API module in `api/` should export functions that return the axios promise. Use React Query in components for caching and loading states.

### Example Pattern:

```javascript
// api/student.js
import api from "./axios";

export const getStudentDashboard = () => api.get("/student/dashboard");
export const getMyRisk = () => api.get("/student/my-risk");
export const getMyRiskTrend = () => api.get("/student/my-risk-trend");
export const getMyFlags = () => api.get("/student/my-flags");
export const getAcademicData = () => api.get("/student/academic-data");
export const computeWhatIf = (data) => api.post("/student/what-if", data);
export const getInterventionHistory = () => api.get("/student/interventions");
export const getConsistencyStreak = () => api.get("/student/consistency-streak");
```

```javascript
// In component:
import { useQuery } from "@tanstack/react-query";
import { getStudentDashboard } from "../../api/student";

function StudentDashboard() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["student", "dashboard"],
    queryFn: () => getStudentDashboard().then((res) => res.data),
  });
  // ...
}
```

### Coordinator APIs – Note on Parameter Types:

Many coordinator endpoints use **query parameters** (not JSON body) for POST requests:
```javascript
// api/coordinator.js
export const createDepartment = (name, code) =>
  api.post(`/coordinator/departments?name=${encodeURIComponent(name)}&code=${encodeURIComponent(code)}`);

export const createClass = (departmentId, name, semester, academicYear) =>
  api.post(`/coordinator/classes?departmentId=${departmentId}&name=${encodeURIComponent(name)}&semester=${semester}&academicYear=${encodeURIComponent(academicYear)}`);
```

### File Upload APIs:
```javascript
export const uploadStudentCsv = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return api.post("/coordinator/upload/students", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};
```

### PDF Download APIs:
```javascript
export const downloadRiskReport = () =>
  api.get("/coordinator/export/risk-report", { responseType: "blob" });

export const downloadStudentReport = (studentId) =>
  api.get(`/coordinator/export/student-report/${studentId}`, { responseType: "blob" });
```

---

## 11. UTILITY FUNCTIONS (`lib/utils.js`)

```javascript
import { clsx } from "clsx";
import { twMerge } from "tailwind-merge";
import { format, parseISO } from "date-fns";

// Merge Tailwind classes (clsx + tailwind-merge)
export function cn(...inputs) {
  return twMerge(clsx(inputs));
}

// Format date strings from backend
export function formatDate(dateString) {
  if (!dateString) return "—";
  return format(parseISO(dateString), "MMM d, yyyy");
}

export function formatDateTime(dateString) {
  if (!dateString) return "—";
  return format(parseISO(dateString), "MMM d, yyyy 'at' h:mm a");
}

// Risk label → Tailwind classes
export function riskColor(label) {
  switch (label) {
    case "HIGH": return { bg: "bg-risk-high-bg", text: "text-risk-high", border: "border-risk-high" };
    case "MEDIUM": return { bg: "bg-risk-medium-bg", text: "text-risk-medium", border: "border-risk-medium" };
    case "LOW": return { bg: "bg-risk-low-bg", text: "text-risk-low", border: "border-risk-low" };
    default: return { bg: "bg-gray-100", text: "text-gray-500", border: "border-gray-300" };
  }
}

// Download blob as file
export function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  window.URL.revokeObjectURL(url);
}
```

---

## 12. NOTIFICATION SYSTEM

### Bell Icon in TopBar:
- Shows unread count as a red badge on bell icon
- Clicking opens a dropdown (Radix DropdownMenu) with latest 5 notifications
- "View All" link at bottom → navigates to full notifications page
- Poll `GET /api/notifications/unread-count` every 30 seconds using React Query's `refetchInterval`

### Notifications Page (shared across all roles):
- Paginated list of notifications
- Each notification card: icon (based on notificationType), title, message, time ago
- Unread notifications have a subtle left border accent
- "Mark All Read" button at top
- Click notification → navigate to relevant page (if referenceType/referenceId provided)

### Notification Type → Icon Mapping:
- `HIGH_RISK_ALERT` → AlertTriangle (red)
- `RISK_THRESHOLD_CROSSED` → TrendingUp (amber)
- `PRE_EXAM_ALERT` → Calendar (blue)
- `DATA_ENTRY_REMINDER` → ClipboardList (orange)
- `INTERVENTION_FOLLOW_UP` → HeartHandshake (purple)
- `STUDENT_FLAGGED` → Flag (red)
- `GENERAL` → Info (gray)

---

## 13. FORM VALIDATION SCHEMAS (Zod)

Define Zod schemas matching backend validation. Examples:

```javascript
import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(1, "Password is required"),
});

export const changePasswordSchema = z.object({
  currentPassword: z.string().min(1, "Current password is required"),
  newPassword: z.string()
    .min(8, "Minimum 8 characters")
    .max(100)
    .regex(/[!@#$%^&*(),.?":{}|<>]/, "Must contain a special character"),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

export const manualStudentSchema = z.object({
  rollNumber: z.string().min(1, "Required"),
  fullName: z.string().min(1, "Required"),
  email: z.string().email("Invalid email"),
  departmentId: z.string().uuid("Select a department"),
  classId: z.string().uuid("Select a class"),
  semester: z.coerce.number().int().min(1).max(8),
  mentorId: z.string().uuid().optional().or(z.literal("")),
});
```

---

## 14. ERROR HANDLING

### Global Error Response Shape (from backend):
```json
{
  "timestamp": "2026-04-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/login",
  "details": ["Email is required", "Password is required"]
}
```

### In components, display errors via `react-hot-toast`:
- Success: `toast.success("Password changed successfully")`
- Error: `toast.error(error.response?.data?.message || "Something went wrong")`
- Validation errors: Show inline under form fields

---

## 15. IMPORTANT BACKEND QUIRKS TO HANDLE

1. **Spring Page format** (`Page<T>`) returns: `{ content: [...], pageable: {...}, totalElements: N, totalPages: N, last: bool, ... }`. The `content` array has the actual data. The custom `PagedResponse<T>` returns: `{ content, page, size, totalElements, totalPages, last }`.

2. **Coordinator POST endpoints for departments/classes/subjects** use **query parameters**, not JSON body. Construct URLs with `?name=X&code=Y`.

3. **SubjectTeacherMapping** entity returned from `GET /api/teacher/my-subjects` contains nested objects (`subject`, `classEntity`, `teacher`). Access via `mapping.subject.name`, `mapping.classEntity.name`, etc.

4. **Entity responses** (AttendanceSession, IAMarks, Assignment, etc.) from teacher endpoints return raw entities with nested relationships. Handle potential lazy-loading nulls – use optional chaining.

5. **UUID format**: All IDs are UUID strings (`"550e8400-e29b-41d4-a716-446655440000"`).

6. **Date formats**: Backend sends `LocalDate` as `"2026-04-19"` and `LocalDateTime` as `"2026-04-19T10:30:00"`.

7. **Roles in JWT**: The `roles` field in AuthResponse is a Set (array in JSON) of strings: `["ACADEMIC_COORDINATOR"]`, `["SUBJECT_TEACHER", "FACULTY_MENTOR"]`, etc.

8. **mustChangePassword flow**: After login, if `true`, ALL API calls except `/api/auth/change-password` will still work (backend doesn't block), but the frontend MUST enforce the redirect.

9. **ExamSchedule GET** requires both `startDate` and `endDate` as query params in `YYYY-MM-DD` format.

10. **Risk thresholds**: LOW ≤ 35, MEDIUM ≤ 55, HIGH > 55. These are the risk SCORE values (not labels). The backend returns `riskLabel` already computed.

---

## 16. DEVELOPMENT NOTES

- **Do NOT use `tailwind.config.js`** – Tailwind v4 uses CSS-based config via `@theme {}` in `index.css`
- **Do NOT use `React.forwardRef`** – React 19 passes ref as a regular prop
- **Do NOT use `defaultProps`** – Use default function parameters instead
- **Do NOT use `<Switch>` or `<Redirect>`** from react-router – These don't exist in v7
- **Do NOT use positional arguments in `useQuery`** – Use the object syntax `{ queryKey, queryFn }`
- **Use `import { jwtDecode } from "jwt-decode"`** – NOT default import
- **All Radix components** use the `@radix-ui/react-*` compound component pattern. They are already installed.
- **Framer Motion** v12: Use `motion` components and `AnimatePresence` for page transitions
- **react-hot-toast**: Place `<Toaster />` in `main.jsx` inside the providers tree
- Backend CORS is configured for `http://localhost:5173` – ensure Vite runs on this port (default)

---

## 17. PRIORITY BUILD ORDER

1. **Infrastructure**: index.css theme, axios instance, auth store, router, layouts
2. **Auth flow**: Login, change password, forgot/reset password
3. **Shared components**: UI primitives, RiskBadge, StatCard, DataTable, charts
4. **Student pages**: Dashboard, Academic Data, What-If, Flags, Interventions
5. **Teacher pages**: Dashboard, Attendance Entry, IA Marks, Assignments, LMS, Flag Student, Subject Analytics
6. **Mentor pages**: Dashboard, Mentee List, Mentee Detail, Interventions, Flags
7. **Coordinator pages**: Dashboard, Student/Teacher Management, Institute Setup, CSV Upload, Exam Schedules, Intervention Reports
8. **Public pages**: Landing Page, About Page
9. **Notifications**: Bell dropdown, notification page
10. **Polish**: Animations, loading states, empty states, error boundaries
