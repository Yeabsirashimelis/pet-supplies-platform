#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "[API] Checking health endpoint..."
HEALTH_JSON="$(curl -sS "${BASE_URL}/actuator/health")"
if [[ "${HEALTH_JSON}" != *"\"status\":\"UP\""* ]]; then
  echo "[API][FAIL] Health endpoint is not UP"
  echo "Response: ${HEALTH_JSON}"
  exit 1
fi
echo "[API][PASS] Health endpoint is UP"

echo "[API] Logging in with seeded admin account..."
LOGIN_JSON="$(curl -sS -X POST "${BASE_URL}/api/v1/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"admin1234"}')"
if [[ "${LOGIN_JSON}" != *"sessionToken"* ]]; then
  echo "[API][FAIL] Login did not return session token"
  echo "Response: ${LOGIN_JSON}"
  exit 1
fi

TOKEN="$(printf "%s" "${LOGIN_JSON}" | sed -n 's/.*"sessionToken":"\([^"]*\)".*/\1/p')"
if [[ -z "${TOKEN}" ]]; then
  echo "[API][FAIL] Unable to parse session token"
  echo "Response: ${LOGIN_JSON}"
  exit 1
fi
echo "[API][PASS] Login succeeded"

echo "[API] Calling protected endpoint /api/v1/auth/sessions..."
SESSIONS_JSON="$(curl -sS "${BASE_URL}/api/v1/auth/sessions" -H "Authorization: Bearer ${TOKEN}")"
if [[ "${SESSIONS_JSON}" == *"AUTH_SESSION_EXPIRED"* ]] || [[ "${SESSIONS_JSON}" == *"PERMISSION_DENIED"* ]]; then
  echo "[API][FAIL] Protected endpoint rejected valid token"
  echo "Response: ${SESSIONS_JSON}"
  exit 1
fi
echo "[API][PASS] Protected endpoint accessible"

echo "[API] Creating category depth chain and validating max depth rule..."

create_category() {
  local code="$1"
  local name="$2"
  local parent_json="$3"
  curl -sS -X POST "${BASE_URL}/api/v1/categories" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{\"categoryCode\":\"${code}\",\"categoryName\":\"${name}\",${parent_json}}"
}

C1="$(create_category "CAT_L1_$(date +%s)" "L1" "\"parentId\":null")"
C1_ID="$(printf "%s" "${C1}" | sed -n 's/.*"id":\([0-9][0-9]*\).*/\1/p')"
[[ -n "${C1_ID}" ]] || { echo "[API][FAIL] Unable to create level 1 category"; echo "${C1}"; exit 1; }

C2="$(create_category "CAT_L2_$(date +%s)" "L2" "\"parentId\":${C1_ID}")"
C2_ID="$(printf "%s" "${C2}" | sed -n 's/.*"id":\([0-9][0-9]*\).*/\1/p')"
[[ -n "${C2_ID}" ]] || { echo "[API][FAIL] Unable to create level 2 category"; echo "${C2}"; exit 1; }

C3="$(create_category "CAT_L3_$(date +%s)" "L3" "\"parentId\":${C2_ID}")"
C3_ID="$(printf "%s" "${C3}" | sed -n 's/.*"id":\([0-9][0-9]*\).*/\1/p')"
[[ -n "${C3_ID}" ]] || { echo "[API][FAIL] Unable to create level 3 category"; echo "${C3}"; exit 1; }

C4="$(create_category "CAT_L4_$(date +%s)" "L4" "\"parentId\":${C3_ID}")"
C4_ID="$(printf "%s" "${C4}" | sed -n 's/.*"id":\([0-9][0-9]*\).*/\1/p')"
[[ -n "${C4_ID}" ]] || { echo "[API][FAIL] Unable to create level 4 category"; echo "${C4}"; exit 1; }

C5="$(create_category "CAT_L5_$(date +%s)" "L5" "\"parentId\":${C4_ID}")"
if [[ "${C5}" != *"VALIDATION_ERROR"* ]] && [[ "${C5}" != *"exceeds max 4"* ]]; then
  echo "[API][FAIL] Depth >4 category creation was not rejected"
  echo "Response: ${C5}"
  exit 1
fi
echo "[API][PASS] Category depth constraint enforced"

echo "[API] API tests passed"
