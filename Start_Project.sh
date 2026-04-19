#!/usr/bin/env bash
# =============================================================================
# TarkShastra — Start All Services
# Launches Backend (Spring Boot), ML API (FastAPI), and Frontend (Vite)
# each in a separate macOS Terminal window.
# Usage: ./Start_Project.sh
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

BACKEND_JAR="$SCRIPT_DIR/Backend/hackathon-app/target/hackathon-app-0.0.1-SNAPSHOT.jar"
ML_DIR="$SCRIPT_DIR/ML/ml"
FRONTEND_DIR="$SCRIPT_DIR/Frontend"

# ── Colour helpers ─────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()  { echo -e "${CYAN}[TarkShastra]${NC} $*"; }
ok()    { echo -e "${GREEN}[OK]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; }

# ── Prerequisite checks ────────────────────────────────────────────────────
check_command() {
  if ! command -v "$1" &>/dev/null; then
    error "'$1' not found. $2"
    exit 1
  fi
}

info "Checking prerequisites..."
check_command java   "Install JDK 17+ from https://adoptium.net"
check_command node   "Install Node.js 18+ from https://nodejs.org"
check_command python3 "Install Python 3.9+ from https://python.org"

if [ ! -f "$BACKEND_JAR" ]; then
  error "Backend JAR not found: $BACKEND_JAR"
  error "Build it first:  cd Backend/hackathon-app && mvn package -DskipTests"
  exit 1
fi

if [ ! -f "$ML_DIR/main.py" ]; then
  error "ML main.py not found at: $ML_DIR/main.py"
  exit 1
fi

if [ ! -f "$FRONTEND_DIR/package.json" ]; then
  error "Frontend package.json not found at: $FRONTEND_DIR/package.json"
  exit 1
fi

ok "All prerequisites satisfied."
echo ""

# ── Port availability warnings ────────────────────────────────────────────
check_port() {
  local port=$1 name=$2
  if lsof -iTCP:"$port" -sTCP:LISTEN -t &>/dev/null; then
    warn "Port $port ($name) is already in use — the service may already be running."
  fi
}

check_port 8080 "Spring Boot Backend"
check_port 8000 "ML FastAPI"
check_port 5173 "Vite Frontend"
echo ""

# ── Determine python command (prefer venv if it exists) ───────────────────
if [ -f "$ML_DIR/.venv/bin/python3" ]; then
  PYTHON_CMD="$ML_DIR/.venv/bin/python3"
elif [ -f "$ML_DIR/venv/bin/python3" ]; then
  PYTHON_CMD="$ML_DIR/venv/bin/python3"
else
  PYTHON_CMD="python3"
fi

# ── Open each service in its own macOS Terminal window ────────────────────
open_terminal() {
  local title="$1"
  local cmd="$2"
  osascript \
    -e "tell application \"Terminal\"" \
    -e "  set newTab to do script \"printf '\\\\033]0;${title}\\\\007' && ${cmd}\"" \
    -e "  set custom title of front window to \"${title}\"" \
    -e "end tell" \
    &>/dev/null || true
}

info "Opening Backend terminal..."
open_terminal \
  "TarkShastra — Backend :8080" \
  "java -jar '${BACKEND_JAR}'"

sleep 1

info "Opening ML API terminal..."
open_terminal \
  "TarkShastra — ML API :8000" \
  "cd '${ML_DIR}' && '${PYTHON_CMD}' main.py"

sleep 1

info "Opening Frontend terminal..."
open_terminal \
  "TarkShastra — Frontend :5173" \
  "cd '${FRONTEND_DIR}' && npm run dev"

echo ""
echo "============================================================"
ok "All 3 services are launching in separate Terminal windows."
echo "============================================================"
echo ""
echo "  Backend   → http://localhost:8080"
echo "  ML API    → http://localhost:8000/docs"
echo "  Frontend  → http://localhost:5173"
echo ""
echo "  Coordinator login:"
echo "    Email   : coordinator@pdeu.ac.in"
echo "    Password: PDEU@Coord2026"
echo ""
echo "  Seed data & setup guide:"
echo "    Documentation/Seed_Data/README.md"
echo ""
echo "  Backend logs: watch the Spring Boot terminal window"
echo "  Temp passwords for CSV-uploaded users are printed in backend logs"
echo "============================================================"
