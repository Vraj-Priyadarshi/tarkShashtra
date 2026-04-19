#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════════
# TarkShastra — Bulk Data Seeder Script
# ═══════════════════════════════════════════════════════════════════════════════
# Seeds: CSV uploads → attendance → IA marks → assignments → LMS → risk scores
# Requires: backend at :8080, jq, curl. ML at :8000 optional (fallback used).
# ═══════════════════════════════════════════════════════════════════════════════

BASE_URL="http://localhost:8080/api"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

COORD_EMAIL="coordinator@ldce.ac.in"
COORD_FIRST_PASS="LDCE@Coord2026"
COORD_NEW_PASS="Admin@1234"

DEFAULT_PASS="TarkShastra@123"
TEACHER_NEW_PASS="Teacher@1234"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
ok()   { echo -e "${GREEN}[OK]${NC} $1"; }
warn() { echo -e "${YELLOW}[..]${NC} $1"; }
fail() { echo -e "${RED}[!!]${NC} $1"; }

# ─── try_login: tries password1, if 401 tries password2 ──────────────────────
# Sets global: TOKEN, MUST_CHANGE
try_login() {
    local email="$1" pass1="$2" pass2="${3:-}"
    local resp

    resp=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$email\",\"password\":\"$pass1\"}")

    TOKEN=$(echo "$resp" | jq -r '.accessToken // empty')
    MUST_CHANGE=$(echo "$resp" | jq -r '.mustChangePassword // false')

    # If first password failed, try second
    if [ -z "$TOKEN" ] && [ -n "$pass2" ]; then
        resp=$(curl -s -X POST "$BASE_URL/auth/login" \
            -H "Content-Type: application/json" \
            -d "{\"email\":\"$email\",\"password\":\"$pass2\"}")
        TOKEN=$(echo "$resp" | jq -r '.accessToken // empty')
        MUST_CHANGE=$(echo "$resp" | jq -r '.mustChangePassword // false')
    fi

    if [ -z "$TOKEN" ]; then
        fail "Login failed for $email"
        return 1
    fi
    return 0
}

# ─── change_password: changes password if mustChangePassword=true ─────────────
change_pass() {
    local old_pass="$1" new_pass="$2"
    if [ "$MUST_CHANGE" = "true" ]; then
        local resp
        resp=$(curl -s -X POST "$BASE_URL/auth/change-password" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d "{\"currentPassword\":\"$old_pass\",\"newPassword\":\"$new_pass\"}")
        TOKEN=$(echo "$resp" | jq -r '.accessToken // empty')
        if [ -z "$TOKEN" ]; then
            fail "Password change failed: $resp"
            return 1
        fi
        ok "  Password changed"
    fi
    return 0
}

# ═══════════════════════════════════════════════════════════════════════════════
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  STEP 1: Coordinator Login & CSV Uploads"
echo "═══════════════════════════════════════════════════════════════"

try_login "$COORD_EMAIL" "$COORD_FIRST_PASS" "$COORD_NEW_PASS" || exit 1
change_pass "$COORD_FIRST_PASS" "$COORD_NEW_PASS"
COORD_TOKEN="$TOKEN"
ok "Coordinator authenticated"

# Upload teachers CSV
warn "Uploading teachers CSV..."
RESP=$(curl -s -X POST "$BASE_URL/coordinator/upload/teachers" \
    -H "Authorization: Bearer $COORD_TOKEN" \
    -F "file=@${SCRIPT_DIR}/teachers.csv")
echo "  $RESP" | jq -r '"  success=\(.successCount), errors=\(.errorCount // 0)"' 2>/dev/null || echo "  $RESP"

# Upload students CSV
warn "Uploading students CSV..."
RESP=$(curl -s -X POST "$BASE_URL/coordinator/upload/students" \
    -H "Authorization: Bearer $COORD_TOKEN" \
    -F "file=@${SCRIPT_DIR}/students.csv")
echo "  $RESP" | jq -r '"  success=\(.successCount), errors=\(.errorCount // 0)"' 2>/dev/null || echo "  $RESP"

# ═══════════════════════════════════════════════════════════════════════════════
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  STEP 2: Teacher Data Entry"
echo "═══════════════════════════════════════════════════════════════"

TEACHER_EMAILS=(
    "rajesh.patel@ldce.ac.in"
    "meena.shah@ldce.ac.in"
    "vikram.desai@ldce.ac.in"
    "anita.sharma@ldce.ac.in"
    "suresh.kumar@ldce.ac.in"
    "priya.mehta@ldce.ac.in"
    "ramesh.joshi@ldce.ac.in"
    "amit.bhatt@ldce.ac.in"
    "neha.pandya@ldce.ac.in"
)

for TEACHER_EMAIL in "${TEACHER_EMAILS[@]}"; do
    echo ""
    warn "Teacher: $TEACHER_EMAIL"

    try_login "$TEACHER_EMAIL" "$DEFAULT_PASS" "$TEACHER_NEW_PASS" || continue
    change_pass "$DEFAULT_PASS" "$TEACHER_NEW_PASS"
    T_TOKEN="$TOKEN"
    ok "  Logged in"

    # Get subjects (nested entity: .subject.id, .classEntity.id)
    SUBJECTS=$(curl -s "$BASE_URL/teacher/my-subjects?academicYear=2025-26" \
        -H "Authorization: Bearer $T_TOKEN")
    N_SUBJ=$(echo "$SUBJECTS" | jq 'length' 2>/dev/null || echo 0)

    if [ "$N_SUBJ" -eq 0 ] || [ "$N_SUBJ" = "null" ]; then
        warn "  No subjects found, skipping"
        continue
    fi

    for ((i=0; i<N_SUBJ; i++)); do
        SUBJ_ID=$(echo "$SUBJECTS" | jq -r ".[$i].subject.id")
        CLS_ID=$(echo "$SUBJECTS" | jq -r ".[$i].classEntity.id")
        SUBJ_NAME=$(echo "$SUBJECTS" | jq -r ".[$i].subject.name")
        CLS_NAME=$(echo "$SUBJECTS" | jq -r ".[$i].classEntity.name")

        warn "  $SUBJ_NAME → $CLS_NAME"

        # Get students (userId is the field for API calls)
        STUDENTS=$(curl -s "$BASE_URL/teacher/students-by-class?classId=$CLS_ID" \
            -H "Authorization: Bearer $T_TOKEN")
        N_STU=$(echo "$STUDENTS" | jq 'length' 2>/dev/null || echo 0)

        if [ "$N_STU" -eq 0 ] || [ "$N_STU" = "null" ]; then
            warn "    No students, skipping"
            continue
        fi

        # ── ATTENDANCE: 12 sessions (Jan–Jun 2026) ───────────────────────
        DATES=(2026-01-06 2026-01-20 2026-02-03 2026-02-17 2026-03-03 2026-03-17
               2026-04-01 2026-04-14 2026-05-05 2026-05-19 2026-06-02 2026-06-16)

        for DATE in "${DATES[@]}"; do
            REC="["
            for ((s=0; s<N_STU; s++)); do
                SID=$(echo "$STUDENTS" | jq -r ".[$s].userId")
                # ~82% present rate, deterministic variance per student
                H=$(( (s * 7 + ${DATE//[^0-9]/}) % 100 ))
                [ "$H" -lt 18 ] && ST="ABSENT" || ST="PRESENT"
                [ $s -gt 0 ] && REC+=","
                REC+="{\"studentId\":\"$SID\",\"status\":\"$ST\"}"
            done
            REC+="]"

            curl -s -o /dev/null -X POST "$BASE_URL/teacher/attendance" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $T_TOKEN" \
                -d "{\"subjectId\":\"$SUBJ_ID\",\"classId\":\"$CLS_ID\",\"sessionDate\":\"$DATE\",\"entryMode\":\"PER_SESSION\",\"records\":$REC}"
        done
        ok "    12 attendance sessions"

        # ── IA MARKS: 3 rounds (max 30) ──────────────────────────────────
        for R in 1 2 3; do
            ENT="["
            for ((s=0; s<N_STU; s++)); do
                SID=$(echo "$STUDENTS" | jq -r ".[$s].userId")
                M=$(( (s * 3 + R * 5) % 20 + 8 + (s + R) % 5 - 2 ))
                [ "$M" -lt 5 ] && M=5; [ "$M" -gt 30 ] && M=30
                AB="false"
                [ $(( (s * 13 + R * 7) % 100 )) -lt 5 ] && { AB="true"; M=0; }
                [ $s -gt 0 ] && ENT+=","
                ENT+="{\"studentId\":\"$SID\",\"obtainedMarks\":$M,\"absent\":$AB}"
            done
            ENT+="]"

            curl -s -o /dev/null -X POST "$BASE_URL/teacher/ia-marks" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $T_TOKEN" \
                -d "{\"subjectId\":\"$SUBJ_ID\",\"classId\":\"$CLS_ID\",\"iaRound\":\"$R\",\"maxMarks\":30.0,\"entries\":$ENT}"
        done
        ok "    3 IA rounds"

        # ── ASSIGNMENTS: 6 assignments with submissions ──────────────────
        A_DATES=(2026-01-15 2026-02-10 2026-03-05 2026-03-25 2026-04-15 2026-05-10)
        A_TITLES=("Assignment 1" "Assignment 2" "Mid-Sem Project" "Assignment 3" "Assignment 4" "Final Project")

        for ((a=0; a<6; a++)); do
            CR=$(curl -s -X POST "$BASE_URL/teacher/assignments" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $T_TOKEN" \
                -d "{\"subjectId\":\"$SUBJ_ID\",\"classId\":\"$CLS_ID\",\"title\":\"${A_TITLES[$a]}\",\"dueDate\":\"${A_DATES[$a]}\"}")

            AID=$(echo "$CR" | jq -r '.id // empty')
            [ -z "$AID" ] && continue

            SUB="["
            for ((s=0; s<N_STU; s++)); do
                SID=$(echo "$STUDENTS" | jq -r ".[$s].userId")
                H=$(( (s * 11 + a * 3) % 100 ))
                if   [ "$H" -lt 80 ]; then SS="SUBMITTED"
                elif [ "$H" -lt 90 ]; then SS="LATE"
                else SS="NOT_SUBMITTED"; fi
                [ $s -gt 0 ] && SUB+=","
                SUB+="{\"studentId\":\"$SID\",\"status\":\"$SS\"}"
            done
            SUB+="]"

            curl -s -o /dev/null -X POST "$BASE_URL/teacher/assignments/$AID/submissions" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $T_TOKEN" \
                -d "{\"assignmentId\":\"$AID\",\"submissions\":$SUB}"
        done
        ok "    6 assignments"

        # ── LMS SCORES ───────────────────────────────────────────────────
        LE="["
        for ((s=0; s<N_STU; s++)); do
            SID=$(echo "$STUDENTS" | jq -r ".[$s].userId")
            SC=$(( (s * 7 + 40) % 65 + 30 ))
            D=$(( (s * 3) % 10 ))
            [ $s -gt 0 ] && LE+=","
            LE+="{\"studentId\":\"$SID\",\"score\":${SC}.${D}}"
        done
        LE+="]"

        curl -s -o /dev/null -X POST "$BASE_URL/teacher/lms-scores" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $T_TOKEN" \
            -d "{\"subjectId\":\"$SUBJ_ID\",\"classId\":\"$CLS_ID\",\"entries\":$LE}"
        ok "    LMS scores"

    done
done

# ═══════════════════════════════════════════════════════════════════════════════
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  STEP 3: Compute Risk Scores"
echo "═══════════════════════════════════════════════════════════════"

try_login "$COORD_EMAIL" "$COORD_NEW_PASS" "" || { fail "Cannot re-auth coordinator"; exit 1; }
COORD_TOKEN="$TOKEN"

RESP=$(curl -s -X POST "$BASE_URL/coordinator/recompute-risk" \
    -H "Authorization: Bearer $COORD_TOKEN")
ok "Risk computed: $RESP"

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  DONE! All data seeded."
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "  Coordinator : coordinator@ldce.ac.in / Admin@1234"
echo "  Any teacher : rajesh.patel@ldce.ac.in / Teacher@1234"
echo "  Any student : aarav.patel@ldce.ac.in / TarkShastra@123"
echo "    (students must change password on first login)"
echo ""
