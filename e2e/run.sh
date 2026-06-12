#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="devices"
APP_URL="http://localhost:8080"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== E2E: Getting token from inside cluster ==="
TOKEN=$(kubectl exec -n "$NAMESPACE" deploy/devices-configuration -- bash -c \
  "curl -s -X POST http://devices-keycloak:8080/realms/iot/protocol/openid-connect/token \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d 'scope=openid' \
    -d 'username=john' \
    -d 'password=john' \
    -d 'grant_type=password' \
    -d 'client_id=iot-service' \
    -d 'client_secret=secret' | jq -r '.access_token'")

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "FAIL: Could not get token"
  exit 1
fi
echo "Token obtained: ${TOKEN:0:20}..."

ADMIN_TOKEN=$(kubectl exec -n "$NAMESPACE" deploy/devices-configuration -- bash -c \
  "curl -s -X POST http://devices-keycloak:8080/realms/iot/protocol/openid-connect/token \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d 'scope=openid' \
    -d 'username=admin' \
    -d 'password=admin' \
    -d 'grant_type=password' \
    -d 'client_id=iot-service' \
    -d 'client_secret=secret' | jq -r '.access_token'")

echo "=== E2E: Running hurl tests ==="
hurl --test --variable TOKEN="$TOKEN" --variable ADMIN_TOKEN="$ADMIN_TOKEN" "$SCRIPT_DIR"/*.hurl

echo "=== E2E: All tests passed ==="
