#!/usr/bin/env bash
# =============================================================================
# TarkShastra — Stop All Services
# Kills Backend (Spring Boot :8080), ML API (FastAPI :8000), and
# Frontend (Vite :5173) by port.
# Usage: ./Stop_Project.sh
# =============================================================================

set -uo pipefail

# ── Colour helpers ─────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()  { echo -e "${CYAN}[TarkShastra]${NC} $*"; }
ok()    { echo -e "${GREEN}[OK]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }

# ── Kill by port ───────────────────────────────────────────────────────────
kill_port() {
  local port=$1
  local name=$2
  local pids
  pids=$(lsof -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null || true)
  if [ -z "$pids" ]; then
    warn "Nothing running on port $port ($name)"
  else
    echo "$pids" | xargs kill -SIGTERM 2>/dev/null || true
    # Give processes a moment to shut down gracefully, then force if needed
    sleep 1
    local remaining
    remaining=$(lsof -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null || true)
    if [ -n "$remaining" ]; then
      echo "$remaining" | xargs kill -SIGKILL 2>/dev/null || true
    fi
    ok "Stopped $name (port $port) — PIDs: $pids"
  fi
}

echo ""
echo "============================================================"
info "Stopping TarkShastra services..."
echo "============================================================"
echo ""

kill_port 8080 "Spring Boot Backend"
kill_port 8000 "ML FastAPI"
kill_port 5173 "Vite Frontend"

echo ""
echo "============================================================"
ok "All services stopped."
echo "============================================================"
echo ""
