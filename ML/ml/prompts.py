"""
Prompt engineering module for the TS-12 Academic Risk Intervention Platform.
Contains system prompts and dynamic prompt builders for the /api/suggestions and /api/roadmap endpoints.
All prompts are designed for structured JSON output via Groq (Llama 3.3 70B).
"""

import json
from models import StudentRequest


# ═══════════════════════════════════════════════════════════════════════════════
# SYSTEM PROMPTS
# ═══════════════════════════════════════════════════════════════════════════════

SUGGESTIONS_SYSTEM_PROMPT = """You are an expert academic counselor at an engineering college in India. Your role is to analyze student academic data and provide concise, actionable, and encouraging intervention suggestions.

STRICT OUTPUT RULES:
- Return only valid JSON. No markdown. No explanation. No code fences. No extra text.
- The JSON must exactly follow the schema provided in the user message.
- Every suggestion must reference specific subjects and exact metric values from the student data.
- Prioritize the most critical issues first (lowest metrics = highest priority).
- Provide 3 to 5 suggestions maximum — keep it focused so the student can read it in 30 seconds.
- Be encouraging in tone — never blame or discourage the student.
- Use Indian academic context (semesters, LMS portals, attendance policies, internal marks)."""

ROADMAP_SYSTEM_PROMPT = """You are an expert academic counselor at an engineering college in India. Your role is to create detailed, realistic, week-by-week improvement roadmaps for students based on their academic performance data.

STRICT OUTPUT RULES:
- Return only valid JSON. No markdown. No explanation. No code fences. No extra text.
- The JSON must exactly follow the schema provided in the user message.
- Calibrate urgency based on the risk_label: High = aggressive recovery plan, Medium = moderate improvement, Low = maintenance and excellence.
- Each week must have specific, achievable daily tasks — not vague platitudes.
- Reference specific subjects by name throughout the roadmap.
- Week 1 should focus on foundation/basics, Week 4 on consolidation/review.
- The roadmap must progress logically with increasing complexity.
- All targets should be realistic for a real engineering student in India.
- Use Indian academic context (semesters, LMS portals, attendance policies, internal marks)."""


# ═══════════════════════════════════════════════════════════════════════════════
# DYNAMIC PROMPT BUILDERS
# ═══════════════════════════════════════════════════════════════════════════════

def build_suggestions_prompt(student: StudentRequest) -> str:
    """
    Build the user prompt for the /api/suggestions endpoint.
    Dynamically injects all student metrics into the prompt.
    """

    # Build subject-wise breakdown string
    subject_lines = []
    for s in student.subject_wise:
        subject_lines.append(
            f"  - {s.subject}: Attendance={s.attendance}%, Marks={s.marks}/100, "
            f"Assignment={s.assignment}%, LMS={s.lms}/100, "
            f"Risk Score={s.risk_score}, Risk Label={s.risk_label}"
        )
    subject_breakdown = "\n".join(subject_lines)

    prompt = f"""Analyze the following student's academic data and generate actionable intervention suggestions.

STUDENT PROFILE:
- Name: {student.student_name}
- ID: {student.student_id}
- Semester: {student.semester}
- Branch: {student.branch}

OVERALL PERFORMANCE:
- Attendance: {student.overall.attendance}%
- Marks: {student.overall.marks}/100
- Assignment Completion: {student.overall.assignment}%
- LMS Engagement: {student.overall.lms}/100
- Overall Risk Score: {student.overall.risk_score}
- Overall Risk Label: {student.overall.risk_label}

SUBJECT-WISE BREAKDOWN:
{subject_breakdown}

TASK:
1. Identify the top 3-5 weakest areas across all subjects and metrics (e.g., "Data Structures attendance is critically low at 40%").
2. For each weak area, provide a specific, actionable suggestion with a realistic target value.
3. Prioritize from most critical to least critical.
4. Include an encouraging motivational note.

REQUIRED JSON OUTPUT SCHEMA:
{{
  "summary": "one sentence overall summary of the student's situation",
  "priority_areas": ["area1", "area2", "area3"],
  "suggestions": [
    {{
      "area": "Subject + Metric (e.g., Data Structures Attendance)",
      "current_value": "current value with unit (e.g., 40%)",
      "target_value": "realistic target value (e.g., 75%)",
      "action": "specific actionable step the student should take",
      "impact": "expected positive outcome of this action"
    }}
  ],
  "motivational_note": "one encouraging closing sentence"
}}

Return ONLY the JSON object. No markdown. No explanation."""

    return prompt


def build_roadmap_prompt(student: StudentRequest) -> str:
    """
    Build the user prompt for the /api/roadmap endpoint.
    Dynamically injects all student metrics and calibrates urgency by risk label.
    """

    # Build subject-wise breakdown string
    subject_lines = []
    for s in student.subject_wise:
        subject_lines.append(
            f"  - {s.subject}: Attendance={s.attendance}%, Marks={s.marks}/100, "
            f"Assignment={s.assignment}%, LMS={s.lms}/100, "
            f"Risk Score={s.risk_score}, Risk Label={s.risk_label}"
        )
    subject_breakdown = "\n".join(subject_lines)

    # Determine urgency calibration
    risk = student.overall.risk_label.lower()
    if risk == "high":
        urgency = "URGENT — This student needs an aggressive recovery plan. Prioritize the weakest subjects heavily in Weeks 1–2."
    elif risk == "medium":
        urgency = "MODERATE — This student needs a balanced improvement plan. Focus on steady, sustainable progress each week."
    else:
        urgency = "MAINTENANCE — This student is doing reasonably well. Focus on pushing towards excellence and maintaining good habits."

    prompt = f"""Create a detailed 4-week improvement roadmap for the following student.

STUDENT PROFILE:
- Name: {student.student_name}
- ID: {student.student_id}
- Semester: {student.semester}
- Branch: {student.branch}

OVERALL PERFORMANCE:
- Attendance: {student.overall.attendance}%
- Marks: {student.overall.marks}/100
- Assignment Completion: {student.overall.assignment}%
- LMS Engagement: {student.overall.lms}/100
- Overall Risk Score: {student.overall.risk_score}
- Overall Risk Label: {student.overall.risk_label}

SUBJECT-WISE BREAKDOWN:
{subject_breakdown}

URGENCY CALIBRATION:
{urgency}

ROADMAP REQUIREMENTS:
1. Create a 4-week plan that progresses logically:
   - Week 1: Foundation & Habit Building (address attendance, basic study routines)
   - Week 2: Active Engagement (assignments, LMS, deeper study)
   - Week 3: Intensive Practice (marks improvement, revision, problem solving)
   - Week 4: Consolidation & Review (exam prep, maintaining gains)
2. Each week must specify which subjects to focus on (prioritize weakest first).
3. Daily tasks must be specific and achievable (e.g., "Attend all Data Structures lectures", not "Study more").
4. Weekly targets must include concrete numbers for attendance, assignments, LMS sessions, and study hours.
5. Each week must have a clear milestone defining what success looks like.

REQUIRED JSON OUTPUT SCHEMA:
{{
  "roadmap_title": "descriptive title for this roadmap",
  "duration": "4 weeks",
  "overall_goal": "one sentence describing the end goal",
  "weeks": [
    {{
      "week_number": 1,
      "theme": "theme for this week (e.g., Attendance Recovery & Habit Building)",
      "focus_subjects": ["subject1", "subject2"],
      "daily_tasks": ["task1", "task2", "task3"],
      "weekly_targets": {{
        "attendance": "target percentage for this week",
        "assignments_to_complete": "number of pending assignments to finish",
        "lms_sessions": "number of LMS sessions this week",
        "study_hours": "total study hours this week"
      }},
      "milestone": "what success looks like by end of this week"
    }}
  ],
  "success_metrics": {{
    "attendance_target": "final target attendance %",
    "marks_target": "final target marks",
    "assignment_target": "final target assignment completion %",
    "lms_target": "final target LMS score"
  }}
}}

Return ONLY the JSON object. No markdown. No explanation."""

    return prompt
