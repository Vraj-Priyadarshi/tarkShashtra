"""
Pydantic models for the Early Academic Risk Detection & Student Intervention Platform.
Defines all request/response schemas for the /api/suggestions and /api/roadmap endpoints.
"""

from pydantic import BaseModel, Field
from typing import List, Optional


# ─── Request Models ───────────────────────────────────────────────────────────

class OverallMetrics(BaseModel):
    """Overall academic metrics for a student."""
    attendance: float = Field(..., description="Attendance percentage (0-100)")
    marks: float = Field(..., description="Marks obtained (0-100)")
    assignment: float = Field(..., description="Assignment completion percentage (0-100)")
    lms: float = Field(..., description="LMS engagement score (0-100)")
    risk_score: float = Field(..., description="ML-predicted risk score")
    risk_label: str = Field(..., description="Risk category: Low, Medium, or High")


class SubjectMetrics(BaseModel):
    """Per-subject academic metrics."""
    subject: str = Field(..., description="Subject name")
    attendance: float = Field(..., description="Attendance percentage (0-100)")
    marks: float = Field(..., description="Marks obtained (0-100)")
    assignment: float = Field(..., description="Assignment completion percentage (0-100)")
    lms: float = Field(..., description="LMS engagement score (0-100)")
    risk_score: float = Field(..., description="ML-predicted risk score for this subject")
    risk_label: str = Field(..., description="Risk category: Low, Medium, or High")


class StudentRequest(BaseModel):
    """Full student data payload sent from the frontend / ML pipeline."""
    student_id: str = Field(..., description="Unique student identifier")
    student_name: str = Field(..., description="Student's full name")
    semester: int = Field(..., description="Current semester number")
    branch: str = Field(..., description="Academic branch/department")
    overall: OverallMetrics
    subject_wise: List[SubjectMetrics]


# ─── Response Models — Suggestions ────────────────────────────────────────────

class SuggestionItem(BaseModel):
    """A single actionable suggestion for the student."""
    area: str = Field(..., description="Specific area of concern, e.g. 'Data Structures Attendance'")
    current_value: str = Field(..., description="Current metric value, e.g. '40%'")
    target_value: str = Field(..., description="Recommended target value, e.g. '75%'")
    action: str = Field(..., description="Concrete action the student should take")
    impact: str = Field(..., description="Expected improvement from this action")


class SuggestionsResponse(BaseModel):
    """Structured suggestions response from the LLM."""
    summary: str = Field(..., description="One-sentence overall summary")
    priority_areas: List[str] = Field(..., description="Top 3-5 priority areas to address")
    suggestions: List[SuggestionItem] = Field(..., description="Ordered list of actionable suggestions")
    motivational_note: str = Field(..., description="Encouraging closing message")


# ─── Response Models — Roadmap ────────────────────────────────────────────────

class WeeklyTargets(BaseModel):
    """Quantitative targets for a single week."""
    attendance: str = Field(..., description="Attendance target for the week")
    assignments_to_complete: str = Field(..., description="Number of assignments to finish")
    lms_sessions: str = Field(..., description="Number of LMS sessions to complete")
    study_hours: str = Field(..., description="Total study hours for the week")


class WeekPlan(BaseModel):
    """Plan for a single week of the improvement roadmap."""
    week_number: int = Field(..., description="Week number (1-4)")
    theme: str = Field(..., description="Theme for the week, e.g. 'Attendance Recovery'")
    focus_subjects: List[str] = Field(..., description="Subjects to focus on this week")
    daily_tasks: List[str] = Field(..., description="Daily tasks to perform")
    weekly_targets: WeeklyTargets
    milestone: str = Field(..., description="What success looks like by end of this week")


class SuccessMetrics(BaseModel):
    """Final target metrics after completing the 4-week roadmap."""
    attendance_target: str = Field(..., description="Target attendance percentage")
    marks_target: str = Field(..., description="Target marks score")
    assignment_target: str = Field(..., description="Target assignment completion rate")
    lms_target: str = Field(..., description="Target LMS engagement score")


class RoadmapResponse(BaseModel):
    """Structured 4-week improvement roadmap from the LLM."""
    roadmap_title: str = Field(..., description="Title of the roadmap")
    duration: str = Field(default="4 weeks", description="Duration of the roadmap")
    overall_goal: str = Field(..., description="High-level goal of the roadmap")
    weeks: List[WeekPlan] = Field(..., description="Week-by-week plan")
    success_metrics: SuccessMetrics


# ─── Error / Health Models ────────────────────────────────────────────────────

class HealthResponse(BaseModel):
    """Health check response."""
    status: str = "ok"
    service: str = "TS-12 Academic Risk Intervention API"
    model: str = "llama-3.3-70b-versatile"
    provider: str = "Groq"


class ErrorResponse(BaseModel):
    """Standard error response."""
    error: str
    detail: Optional[str] = None


# ─── Risk Prediction Models ──────────────────────────────────────────────────

class PredictRequest(BaseModel):
    """Request body for /api/predict — features for risk scoring."""
    attendance: float = Field(..., description="Attendance percentage (0-100)")
    marks: float = Field(..., description="Normalized marks score (0-100)")
    assignment: float = Field(..., description="Assignment completion percentage (0-100)")
    lms: float = Field(..., description="LMS engagement score (0-100)")


class PredictResponse(BaseModel):
    """Response body for /api/predict — predicted risk score."""
    risk_score: float = Field(..., description="Predicted risk score (0-100, higher = riskier)")
    risk_label: str = Field(..., description="Risk label: LOW, MEDIUM, or HIGH")
