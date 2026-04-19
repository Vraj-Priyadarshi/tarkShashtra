<p align="center">
  <h1 align="center">🎯 UniWatch</h1>
  <p align="center">
    <strong>AI-Powered Early Academic Risk Detection & Student Intervention Platform</strong>
  </p>
  <p align="center">
    <em>Detect at-risk students before they fail. Intervene early. Measure outcomes.</em>
  </p>
  <p align="center">
    <a href="#-tech-stack"><img src="https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot"/></a>
    <a href="#-tech-stack"><img src="https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black" alt="React"/></a>
    <a href="#-tech-stack"><img src="https://img.shields.io/badge/FastAPI-0.115-009688?style=flat-square&logo=fastapi&logoColor=white" alt="FastAPI"/></a>
    <a href="#-tech-stack"><img src="https://img.shields.io/badge/Llama_3.3_70B-Groq-F7931A?style=flat-square" alt="Groq LLM"/></a>
    <a href="#-tech-stack"><img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL"/></a>
    <a href="#-tech-stack"><img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21"/></a>
  </p>
</p>

---

## 📋 Table of Contents

- [The Problem](#-the-problem)
- [Our Solution](#-our-solution)
- [Key Features](#-key-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Role-Based Dashboards](#-role-based-dashboards)
- [ML & AI Pipeline](#-ml--ai-pipeline)
- [Risk Scoring System](#-risk-scoring-system)
- [API Reference](#-api-reference)
- [Data Model](#-data-model)
- [Security](#-security)
- [Scale of the Build](#-scale-of-the-build)
- [Team](#-team)

---

## 🚨 The Problem

In most Indian colleges, struggling students are identified only **after exam results** — by which time it's too late. Attendance records, internal assessment scores, assignment submissions, and LMS engagement data all exist in **silos**. Nobody connects the dots until the damage is done.

**A student silently stops attending lectures. Their assignments slow down. Their LMS engagement drops. Nobody notices until the end-semester results come out and they've failed.**

---

## 💡 Our Solution

**TarkShastra connects those dots in real-time.**

It monitors student academic performance continuously, identifies at-risk students before they fail, and generates **AI-personalised intervention plans** — so teachers, mentors, and coordinators can act early instead of reacting late.

```
📊 Data Entry → 🧮 Risk Scoring → 🤖 AI Insights → 🚩 Flagging → 🤝 Intervention → 📈 Measurable Outcomes
```

**Early detection. Targeted intervention. Measurable outcomes.**

---

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| **🔴 Real-Time Risk Scoring** | Risk updates immediately when any data changes — no batch overnight processing |
| **📊 Subject-Level Granularity** | Pinpoints _which subjects_ and _which factors_ are causing risk, not just "this student is struggling" |
| **🤖 AI-Personalised Suggestions** | LLM sees the student's actual numbers across all subjects and gives specific, achievable recommendations |
| **🗺️ 4-Week Improvement Roadmaps** | Persistent, structured week-by-week recovery plans saved to the database |
| **🔮 What-If Calculator** | Students simulate "What if I improve my attendance to 80%?" and see how risk changes |
| **🔥 Consistency Streaks** | Gamification — students see their streak of consecutive good weeks |
| **👥 Multi-Role Workflow** | Teachers flag → Mentors intervene → Coordinators oversee, each with purpose-built tools |
| **📈 Intervention Tracking** | Pre/post risk scores per intervention, enabling evidence-based evaluation |
| **📤 CSV Bulk Upload** | Instant onboarding of existing data — coordinators don't re-enter everything manually |
| **📧 Email Notifications** | Real-time alerts for risk level changes, new flags, exams, and follow-up reminders |

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                     CLIENT (Browser)                             │
│          React 19 + Vite + TailwindCSS + Recharts               │
│     Student │ Teacher │ Mentor │ Coordinator Dashboards          │
└──────────────────────┬───────────────────────────────────────────┘
                       │ REST API (Axios)
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│                 BACKEND (Spring Boot 4.0)                        │
│         Java 21 │ Spring Security (JWT) │ JPA/Hibernate          │
│         50+ REST Endpoints │ 22 Service Classes                  │
│         Role-Based Access Control │ CSV Parser │ PDF Export       │
├──────────────────────┬───────────────────────────────────────────┤
│                      │ WebClient (HTTP)                          │
│                      ▼                                           │
│  ┌─────────────────────────────────────┐                         │
│  │      ML SERVICE (FastAPI)           │                         │
│  │  Risk Prediction (Weighted Model)   │                         │
│  │  AI Suggestions (Llama 3.3 70B)     │                         │
│  │  Improvement Roadmaps (Groq API)    │                         │
│  └─────────────────────────────────────┘                         │
└──────────────────────┬───────────────────────────────────────────┘
                       │ JPA/Hibernate
                       ▼
              ┌─────────────────┐
              │   MySQL 8.0     │
              │  24 Tables      │
              │  Full Academic  │
              │  Data Lifecycle │
              └─────────────────┘
```

---

## 🛠️ Tech Stack

### Backend
| Technology | Purpose |
|-----------|---------|
| **Java 21** | Core language |
| **Spring Boot 4.0** | Application framework |
| **Spring Security** | JWT authentication & RBAC |
| **Spring Data JPA** | ORM / Database access |
| **MySQL 8.0** | Relational database |
| **Spring WebFlux (WebClient)** | HTTP client for ML service |
| **OpenCSV** | CSV parsing for bulk uploads |
| **OpenPDF** | PDF report generation |
| **SpringDoc OpenAPI** | Swagger API documentation |
| **Lombok** | Boilerplate reduction |

### Frontend
| Technology | Purpose |
|-----------|---------|
| **React 19** | UI library |
| **Vite 8** | Build tool & dev server |
| **TailwindCSS 4** | Utility-first styling |
| **React Router 7** | Client-side routing |
| **Zustand** | State management |
| **TanStack Query** | Server state & caching |
| **Recharts** | Charts & data visualisation |
| **Framer Motion** | Animations |
| **Radix UI** | Accessible UI primitives |
| **React Hook Form + Zod** | Form handling & validation |

### ML / AI Service
| Technology | Purpose |
|-----------|---------|
| **Python 3.9+** | Core language |
| **FastAPI** | API framework |
| **Groq API** | LLM inference (ultra-fast) |
| **Llama 3.3 70B** | Language model for suggestions & roadmaps |
| **Pydantic** | Data validation |
| **scikit-learn** | Model training & analysis (Jupyter notebook) |

---

## 📁 Project Structure

```
TarkShastra_Hackathon/
│
├── Backend/
│   └── hackathon-app/             # Spring Boot application
│       ├── src/main/java/com/tarkshastra/app/
│       │   ├── config/            # CORS, Security, JWT config
│       │   ├── controller/        # REST API controllers
│       │   ├── dto/               # Request/Response DTOs
│       │   ├── entity/            # JPA entities (24 tables)
│       │   ├── repository/        # Spring Data repositories
│       │   ├── service/           # Business logic (22 services)
│       │   └── util/              # Utilities (constants, token gen)
│       ├── src/main/resources/
│       │   └── application.properties
│       └── pom.xml
│
├── Frontend/
│   ├── src/
│   │   ├── api/                   # Axios API clients
│   │   ├── components/
│   │   │   ├── charts/            # Recharts visualisations (6)
│   │   │   ├── layout/            # Sidebar, TopBar, DashboardLayout
│   │   │   └── ui/                # Reusable UI components (17)
│   │   ├── hooks/                 # Custom React hooks
│   │   ├── pages/
│   │   │   ├── coordinator/       # 8 pages
│   │   │   ├── mentor/            # 6 pages
│   │   │   ├── student/           # 6 pages
│   │   │   ├── teacher/           # 8 pages
│   │   │   └── public/            # Landing, Login, Password pages
│   │   ├── routes/                # Protected & role-based routing
│   │   └── stores/                # Zustand stores
│   ├── package.json
│   └── vite.config.js
│
├── ML/
│   └── ml/
│       ├── main.py                # FastAPI app (3 endpoints)
│       ├── models.py              # Pydantic schemas
│       ├── prompts.py             # LLM prompt engineering
│       ├── risk_score_model.ipynb # Jupyter notebook (EDA & training)
│       ├── outputs/               # Model artifacts & visualisations
│       └── requirements.txt
│
├── Documentation/
│   ├── DATA/                      # Seed CSVs & testing guide
│   ├── Backend_Final_Plan.md      # Detailed backend architecture doc
│   ├── Frontend_Build_Prompt.md   # Frontend design specifications
│   └── ...                        # API reference, auth docs, etc.
│
├── Start_Project.sh               # Launch all 3 services
├── Stop_Project.sh                # Stop all services
├── Truncate_DB.sh                 # Reset database
└── SUMMARY.md                     # Detailed product overview
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|------------|---------|
| **Java** | JDK 21+ |
| **Node.js** | 18+ |
| **Python** | 3.9+ |
| **MySQL** | 8.0+ |
| **Maven** | 3.8+ |

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/Vraj-Priyadarshi/tarkShashtra.git
cd tarkShashtra
```

### 2️⃣ Database Setup

```bash
mysql -u root -p -e "CREATE DATABASE tarkshastra_db;"
```

Update `Backend/hackathon-app/src/main/resources/application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tarkshastra_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3️⃣ Backend (Spring Boot)

```bash
cd Backend/hackathon-app
mvn spring-boot:run -DskipTests
```

> The backend auto-seeds institute structure (departments, classes, subjects) on first startup.  
> Runs on **http://localhost:8080**

### 4️⃣ ML Service (FastAPI)

```bash
cd ML/ml
python -m venv venv
# Windows:
venv\Scripts\activate
# macOS/Linux:
source venv/bin/activate

pip install -r requirements.txt
```

Create a `.env` file inside `ML/ml/`:

```env
GROQ_API_KEY=gsk_your_api_key_here
```

> Get a free API key from [console.groq.com](https://console.groq.com)

```bash
python main.py
```

> Runs on **http://localhost:8000** | Swagger UI at **http://localhost:8000/docs**

### 5️⃣ Frontend (React + Vite)

```bash
cd Frontend
npm install
npm run dev
```

> Runs on **http://localhost:5173**

### 6️⃣ Initial Login

| Role | Email | Password |
|------|-------|----------|
| **Coordinator** | `coordinator@ldce.ac.in` | `LDCE@Coord2026` |

> You'll be prompted to change your password on first login.

After logging in as Coordinator, upload the seed CSVs from `Documentation/DATA/` to create teacher and student accounts:
- Upload `teachers.csv` → Creates 11 teacher accounts
- Upload `students.csv` → Creates 76 student accounts

---

## 🖥️ Role-Based Dashboards

### 👨‍🎓 Student Dashboard
- Personal risk score with colour-coded indicator (🟢 LOW, 🟡 MEDIUM, 🔴 HIGH)
- Consistency streak tracker (consecutive good weeks)
- Contributing factors breakdown with progress bars
- **AI-generated improvement tips** (LLM-powered)
- **Personalised 4-week improvement roadmap**
- What-If risk calculator with interactive sliders
- Intervention history & active flags

### 👩‍🏫 Teacher Dashboard
- Class-wise student risk overview
- **Data entry tools**: Attendance, IA Marks, Assignments, LMS Scores
- Subject-level analytics with risk distribution charts
- Flag at-risk students with notes

### 🧑‍🤝‍🧑 Mentor Dashboard
- Mentee list with risk indicators
- Detailed student profiles with full academic data
- Student flags requiring attention
- **Intervention logging** with action items & pre/post risk comparison
- Risk trend visualisation over time

### 🏛️ Coordinator Dashboard
- Institute-wide statistics & risk distribution
- Department-wise risk breakdown charts
- Student & teacher management
- **CSV bulk upload** for all academic data types
- Intervention reports & effectiveness metrics
- Exam schedule management
- Institute setup (departments, classes, subjects)

---

## 🤖 ML & AI Pipeline

### 1. Risk Prediction
A **weighted scoring model** that takes four normalised metrics (0–100 each) and produces a risk score. Designed to be **interpretable rather than black-box**.

```
Performance = 0.30 × Attendance + 0.30 × Marks + 0.25 × Assignment + 0.15 × LMS
Risk Score  = 100 − Performance
```

### 2. AI-Powered Suggestions
Uses **Groq's Llama 3.3 70B** model with carefully engineered prompts. The system prompt establishes it as an _"expert Indian academic counsellor"_ and the dynamic prompt injects the student's complete academic profile. Returns structured JSON with:
- Summary of academic standing
- Priority areas for improvement
- **Specific, actionable suggestions** with area, current value, target value, action steps, and expected impact
- Motivational note

### 3. Improvement Roadmap
Same LLM, different prompt. Generates a **4-week structured plan** calibrated to the student's risk level:
- 🔴 **HIGH** → Aggressive recovery plans
- 🟡 **MEDIUM** → Optimisation plans
- 🟢 **LOW** → Maintenance plans

Each week includes: theme, focus subjects, daily tasks, weekly targets, and milestones. **Roadmaps are saved to the database** so they persist across logins.

---

## 📊 Risk Scoring System

| Risk Level | Score Range | Colour | Meaning |
|-----------|------------|--------|---------|
| **LOW** | 0 – 35 | 🟢 Green | Student is performing well |
| **MEDIUM** | 35 – 55 | 🟡 Yellow | Needs monitoring |
| **HIGH** | 55 – 100 | 🔴 Red | Needs immediate intervention |

### Weighting Factors

| Factor | Weight | Data Source |
|--------|--------|-------------|
| Attendance | 30% | Session-by-session or bulk percentage |
| IA Marks | 30% | Internal assessment scores per round |
| Assignment Completion | 25% | Submission tracking (submitted/late/missing) |
| LMS Engagement | 15% | Learning management system activity scores |

Risk is computed **per subject and overall**, and historical scores are preserved for trend tracking.

---

## 📡 API Reference

### Backend (Spring Boot) — `http://localhost:8080`

The backend exposes **50+ REST endpoints** across these domains:

| Domain | Key Endpoints |
|--------|--------------|
| **Auth** | `POST /api/auth/login`, `POST /api/auth/change-password`, `POST /api/auth/forgot-password` |
| **Students** | `GET /api/students`, `GET /api/students/{id}/risk`, `POST /api/students/{id}/recompute-risk` |
| **Teachers** | `GET /api/teachers`, Data entry endpoints for attendance, marks, assignments, LMS |
| **Mentors** | `GET /api/mentors/mentees`, `POST /api/interventions`, `GET /api/flags` |
| **Coordinators** | `POST /api/csv/upload-students`, `POST /api/csv/upload-teachers`, `POST /api/risk/recompute-all` |
| **Risk** | `GET /api/risk/{studentId}`, `GET /api/risk/distribution`, `POST /api/risk/what-if` |

> Full Swagger docs available at `http://localhost:8080/swagger-ui.html`

### ML Service (FastAPI) — `http://localhost:8000`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check |
| `/api/predict` | POST | Risk score prediction from 4 academic metrics |
| `/api/suggestions` | POST | AI-generated improvement suggestions |
| `/api/roadmap` | POST | Personalised 4-week improvement roadmap |

> Swagger UI at `http://localhost:8000/docs`

---

## 🗃️ Data Model

**24 entities** covering the full academic lifecycle:

| Category | Entities |
|----------|----------|
| **Institutional** | Institutes → Departments → Classes → Subjects (with class/teacher mappings) |
| **People** | Users (STUDENT, TEACHER, MENTOR, COORDINATOR), Student Profiles, Teacher Profiles |
| **Academic Data** | Attendance Sessions & Records, IA Marks, Assignments & Submissions, LMS Scores |
| **Risk Engine** | Risk Scores (per student, per subject, historical), Consistency Streaks |
| **AI Outputs** | Student Roadmaps (persisted JSON) |
| **Interventions** | Student Flags, Interventions (with Action Items) |
| **Communication** | Notifications, Exam Schedules |
| **Auth** | Password Reset Tokens |

---

## 🔒 Security

- **JWT Authentication** with 6-hour token expiry
- **Role-Based Access Control** (STUDENT, TEACHER, MENTOR, COORDINATOR)
- Every endpoint is protected — students see only their own data, teachers only their subjects, mentors only their mentees
- **First-login forced password change** for seeded accounts
- Password reset via email token flow
- CORS configured for frontend origin

---

## 📏 Scale of the Build

| Layer | Count |
|-------|-------|
| Backend REST APIs | **50+** endpoints |
| Database Entities | **24** tables |
| Frontend Pages | **34** pages across 4 role dashboards |
| UI Components | **29** reusable components (charts, layout, inputs, tables) |
| Backend Services | **22** service classes |
| Chart Components | **6** (risk trends, distribution, department bars, radar, effectiveness) |

---

## 👥 Team

**Team TarkShastra** — Built with ❤️ at LD College of Engineering Hackathon

---

<p align="center">
  <strong>⭐ Star this repo if you found it useful!</strong>
</p>
