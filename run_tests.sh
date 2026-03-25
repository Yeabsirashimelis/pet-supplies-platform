#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

OS_NAME="$(uname -s || true)"
if [[ "${OS_NAME}" == MINGW* || "${OS_NAME}" == MSYS* || "${OS_NAME}" == CYGWIN* ]]; then
  ROOT_DIR_MOUNT="$(cd "${ROOT_DIR}" && pwd -W)"
  export MSYS_NO_PATHCONV=1
  export MSYS2_ARG_CONV_EXCL="*"
else
  ROOT_DIR_MOUNT="${ROOT_DIR}"
fi

echo "=============================="
echo "Running unit tests"
echo "=============================="

if ! docker info >/dev/null 2>&1; then
  echo "[FAIL] Docker daemon is not running or not reachable"
  exit 1
fi

UNIT_STATUS=0
API_STATUS=0

run_unit_tests() {
  docker run --rm \
    -v "${ROOT_DIR_MOUNT}:/app" \
    -v m2_cache_pet_platform:/root/.m2 \
    -w /app \
    maven:3.9.9-eclipse-temurin-17 \
    mvn -B -DskipITs test
}

for attempt in 1 2 3; do
  if run_unit_tests; then
    UNIT_STATUS=0
    break
  else
    UNIT_STATUS=$?
    echo "[UNIT] Attempt ${attempt} failed"
    if [[ "${attempt}" -lt 3 ]]; then
      echo "[UNIT] Retrying in 5s..."
      sleep 5
    fi
  fi
done

if [[ "$UNIT_STATUS" -ne 0 ]]; then
  echo "[UNIT][FAIL] Unit tests failed"
  exit "$UNIT_STATUS"
fi

echo "[UNIT][PASS] Unit tests completed"

echo "=============================="
echo "Starting services"
echo "=============================="

COMPOSE_STATUS=0
for attempt in 1 2 3; do
  if docker compose up --build -d; then
    COMPOSE_STATUS=0
    break
  else
    COMPOSE_STATUS=$?
    echo "[COMPOSE] Attempt ${attempt} failed"
    if [[ "${attempt}" -lt 3 ]]; then
      echo "[COMPOSE] Retrying in 10s..."
      sleep 10
    fi
  fi
done

if [[ "$COMPOSE_STATUS" -ne 0 ]]; then
  echo "[FAIL] docker compose up --build failed"
  exit "$COMPOSE_STATUS"
fi

echo "Waiting for app readiness..."
for i in {1..90}; do
  if curl -sS http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then
    echo "App is UP"
    break
  fi
  sleep 2
  if [[ "$i" -eq 90 ]]; then
    echo "[FAIL] App did not become healthy in time"
    docker compose logs app
    docker compose down
    exit 1
  fi
done

echo "=============================="
echo "Running API tests"
echo "=============================="

bash "${ROOT_DIR}/api_tests.sh" || API_STATUS=$?

if [[ "$API_STATUS" -ne 0 ]]; then
  echo "[API][FAIL] API tests failed"
  docker compose logs app || true
  docker compose down || true
  exit "$API_STATUS"
fi

echo "[API][PASS] API tests completed"

echo "=============================="
echo "run_tests.sh output showing unit tests + API tests passing"
echo "=============================="

docker compose down
