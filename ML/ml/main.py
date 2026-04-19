"""
TS-12 Early Academic Risk Detection & Student Intervention Platform
FastAPI backend with Groq-powered LLM endpoints for academic suggestions and roadmaps.
"""

import json
import os
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
from groq import Groq

from models import (
    StudentRequest,
    SuggestionsResponse,
    RoadmapResponse,
    HealthResponse,
    ErrorResponse,
    PredictRequest,
    PredictResponse,
)
from prompts import (
    SUGGESTIONS_SYSTEM_PROMPT,
    ROADMAP_SYSTEM_PROMPT,
    build_suggestions_prompt,
    build_roadmap_prompt,
)

# ─── Configuration ────────────────────────────────────────────────────────────

load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ts12-api")

GROQ_API_KEY = os.getenv("GROQ_API_KEY")
MODEL_ID = "llama-3.3-70b-versatile"

if not GROQ_API_KEY:
    raise RuntimeError(
        "GROQ_API_KEY not found. Create a .env file with GROQ_API_KEY=gsk_..."
    )

# ─── Groq Client ─────────────────────────────────────────────────────────────

groq_client = Groq(api_key=GROQ_API_KEY)


# ─── App Lifecycle ────────────────────────────────────────────────────────────

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("🚀 TS-12 API starting — model: %s via Groq", MODEL_ID)
    yield
    logger.info("🛑 TS-12 API shutting down")


# ─── FastAPI App ──────────────────────────────────────────────────────────────

app = FastAPI(
    title="TS-12 Academic Risk Intervention API",
    description=(
        "LLM-powered academic intervention suggestions and improvement roadmaps "
        "for at-risk students. Uses Groq (Llama 3.3 70B) for ultra-fast inference."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

# CORS — allow any frontend origin during development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ─── Helper: Call Groq LLM ────────────────────────────────────────────────────

def call_groq_llm(system_prompt: str, user_prompt: str) -> dict:
    """
    Send a chat completion request to Groq and parse the JSON response.
    Raises HTTPException on failure.
    """
    try:
        chat_completion = groq_client.chat.completions.create(
            model=MODEL_ID,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            temperature=0.4,
            max_tokens=4096,
            response_format={"type": "json_object"},
        )

        raw_content = chat_completion.choices[0].message.content
        logger.info("Groq response received — %d chars", len(raw_content))

        # Parse JSON safely
        try:
            parsed = json.loads(raw_content)
            return parsed
        except json.JSONDecodeError as e:
            logger.error("JSON parse error: %s\nRaw content:\n%s", e, raw_content)
            raise HTTPException(
                status_code=502,
                detail=f"LLM returned invalid JSON: {str(e)}",
            )

    except HTTPException:
        raise  # Re-raise our own HTTP exceptions
    except Exception as e:
        logger.error("Groq API call failed: %s", e)
        raise HTTPException(
            status_code=503,
            detail=f"LLM service unavailable: {str(e)}",
        )


# ─── Endpoints ────────────────────────────────────────────────────────────────

@app.get("/health", response_model=HealthResponse, tags=["System"])
async def health_check():
    """Health check endpoint — verifies the API is running."""
    return HealthResponse()


@app.post(
    "/api/suggestions",
    response_model=SuggestionsResponse,
    responses={502: {"model": ErrorResponse}, 503: {"model": ErrorResponse}},
    tags=["Interventions"],
    summary="Generate academic intervention suggestions",
    description="Analyzes student data and returns prioritized, actionable suggestions.",
)
async def generate_suggestions(student: StudentRequest):
    """
    Accepts student academic data and returns LLM-generated intervention suggestions.
    Uses the student's overall and subject-wise metrics to identify weak areas
    and produce specific, actionable improvement steps.
    """
    logger.info(
        "POST /api/suggestions — student=%s risk=%s",
        student.student_id,
        student.overall.risk_label,
    )

    user_prompt = build_suggestions_prompt(student)
    result = call_groq_llm(SUGGESTIONS_SYSTEM_PROMPT, user_prompt)

    # Validate against our Pydantic model
    try:
        validated = SuggestionsResponse(**result)
        return validated
    except Exception as e:
        logger.error("Response validation failed: %s\nData: %s", e, result)
        raise HTTPException(
            status_code=502,
            detail=f"LLM response did not match expected schema: {str(e)}",
        )


@app.post(
    "/api/roadmap",
    response_model=RoadmapResponse,
    responses={502: {"model": ErrorResponse}, 503: {"model": ErrorResponse}},
    tags=["Interventions"],
    summary="Generate a 4-week improvement roadmap",
    description="Creates a personalized week-by-week academic recovery plan.",
)
async def generate_roadmap(student: StudentRequest):
    """
    Accepts student academic data and returns a 4-week improvement roadmap.
    Calibrates urgency based on risk_label and personalizes tasks per subject.
    """
    logger.info(
        "POST /api/roadmap — student=%s risk=%s",
        student.student_id,
        student.overall.risk_label,
    )

    user_prompt = build_roadmap_prompt(student)
    result = call_groq_llm(ROADMAP_SYSTEM_PROMPT, user_prompt)

    # Validate against our Pydantic model
    try:
        validated = RoadmapResponse(**result)
        return validated
    except Exception as e:
        logger.error("Response validation failed: %s\nData: %s", e, result)
        raise HTTPException(
            status_code=502,
            detail=f"LLM response did not match expected schema: {str(e)}",
        )


# ─── Risk Prediction ─────────────────────────────────────────────────────────

# Load thresholds
_thresholds_path = os.path.join(os.path.dirname(__file__), "outputs", "thresholds.json")
try:
    with open(_thresholds_path) as f:
        _thresholds = json.load(f)
    LOW_THRESHOLD = _thresholds.get("low_threshold", 35.0)
    MEDIUM_THRESHOLD = _thresholds.get("medium_threshold", 55.0)
except FileNotFoundError:
    logger.warning("thresholds.json not found — using defaults")
    LOW_THRESHOLD = 35.0
    MEDIUM_THRESHOLD = 55.0


def _classify_risk(score: float) -> str:
    if score <= LOW_THRESHOLD:
        return "LOW"
    elif score <= MEDIUM_THRESHOLD:
        return "MEDIUM"
    return "HIGH"


@app.post(
    "/api/predict",
    response_model=PredictResponse,
    tags=["Risk Prediction"],
    summary="Predict academic risk score",
    description=(
        "Accepts four feature scores (attendance, marks, assignment, lms — each 0-100) "
        "and returns a risk score + label using a weighted-average heuristic."
    ),
)
async def predict_risk(payload: PredictRequest):
    """
    Compute a risk score from academic features.
    Weights: attendance 30%, marks 30%, assignment 25%, lms 15%.
    Risk = 100 - weighted_performance.
    """
    performance = (
        0.30 * payload.attendance
        + 0.30 * payload.marks
        + 0.25 * payload.assignment
        + 0.15 * payload.lms
    )
    risk_score = round(max(0.0, min(100.0, 100.0 - performance)), 2)
    risk_label = _classify_risk(risk_score)

    logger.info(
        "POST /api/predict — att=%.1f mk=%.1f asg=%.1f lms=%.1f → risk=%.2f (%s)",
        payload.attendance, payload.marks, payload.assignment, payload.lms,
        risk_score, risk_label,
    )
    return PredictResponse(risk_score=risk_score, risk_label=risk_label)


# ─── Run with: uvicorn main:app --reload ──────────────────────────────────────

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
