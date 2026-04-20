#!/bin/bash

set -u

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
CERT_DIR="$PROJECT_ROOT/certs"

# Modern version (8.x) → no known OOM bug like 7.82.0
CURL_BIN="${CURL_BIN:-curl}"

echo "Using curl: $CURL_BIN"
"$CURL_BIN" --version | head -n 1
echo ""

BASE_URL="https://localhost:8443"

SSL_ARGS=(
  --cacert "$CERT_DIR/ca-cert.pem"
  --cert "$CERT_DIR/client-cert.pem"
  --key "$CERT_DIR/client-key.pem"
)

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PASSED=0
FAILED=0

BODY_FILE="/tmp/pki_test_body.$$"
HDR_FILE="/tmp/pki_test_headers.$$"

cleanup() {
    rm -f "$BODY_FILE" "$HDR_FILE"
}
trap cleanup EXIT

check_file() {
    local file_path=$1
    if [ ! -f "$file_path" ]; then
        echo -e "${RED}Missing file: $file_path${NC}"
        exit 1
    fi
}

check_file "$CERT_DIR/ca-cert.pem"
check_file "$CERT_DIR/client-cert.pem"
check_file "$CERT_DIR/client-key.pem"
check_file "$CERT_DIR/server-cert.pem"

run_success_test() {
    local test_num=$1
    local test_name=$2
    local url=$3
    local expected_text=$4

    echo -e "${BLUE}Test $test_num: $test_name${NC}"
    echo "URL: $url"

    : > "$BODY_FILE"
    : > "$HDR_FILE"

    local status
    status=$("$CURL_BIN" -sS \
        "${SSL_ARGS[@]}" \
        -o "$BODY_FILE" \
        -D "$HDR_FILE" \
        -w "%{http_code}" \
        "$url" 2>&1)

    local curl_exit=$?
    local body
    body=$(cat "$BODY_FILE" 2>/dev/null || true)

    if [ $curl_exit -ne 0 ]; then
        echo -e "${RED}❌ FAILED${NC}"
        echo "curl exit code: $curl_exit"
        echo "curl output:"
        echo "$status"
        ((FAILED++))
        echo ""
        return
    fi

    if [ "$status" = "200" ] && echo "$body" | grep -qi "$expected_text"; then
        echo -e "${GREEN}✅ PASSED${NC}"
        echo "HTTP Status: $status"
        ((PASSED++))
    else
        echo -e "${RED}❌ FAILED${NC}"
        echo "HTTP Status: $status"
        echo "Expected body to contain: $expected_text"
        echo "Response body:"
        echo "$body"
        echo ""
        echo "Response headers:"
        cat "$HDR_FILE"
        ((FAILED++))
    fi

    echo ""
}

run_failure_test() {
    local test_num=$1
    local test_name=$2
    local url=$3

    echo -e "${YELLOW}Test $test_num: $test_name${NC}"
    echo "URL: $url"

    : > "$BODY_FILE"
    : > "$HDR_FILE"

    local status
    status=$("$CURL_BIN" -sS -k \
        -o "$BODY_FILE" \
        -D "$HDR_FILE" \
        -w "%{http_code}" \
        "$url" 2>&1)

    local curl_exit=$?
    local body
    body=$(cat "$BODY_FILE" 2>/dev/null || true)

    if [ $curl_exit -ne 0 ]; then
        echo -e "${GREEN}✅ PASSED (request rejected before normal response)${NC}"
        echo "curl exit code: $curl_exit"
        echo "curl output:"
        echo "$status"
        ((PASSED++))
    elif [ "$status" = "400" ] || [ "$status" = "401" ] || [ "$status" = "403" ] || [ "$status" = "495" ] || [ "$status" = "496" ]; then
        echo -e "${GREEN}✅ PASSED (correctly rejected)${NC}"
        echo "HTTP Status: $status"
        echo "Response body:"
        echo "$body"
        ((PASSED++))
    else
        echo -e "${RED}❌ FAILED${NC}"
        echo "Expected request without certificate to be rejected"
        echo "HTTP Status: $status"
        echo "Response body:"
        echo "$body"
        echo ""
        echo "Response headers:"
        cat "$HDR_FILE"
        ((FAILED++))
    fi

    echo ""
}

run_success_test 1 "PKI Health Check" \
    "$BASE_URL/api/pki/health" \
    "UP"

run_success_test 2 "Certificate Info" \
    "$BASE_URL/api/pki/certificate-info" \
    "subject"

run_success_test 3 "Verify Certificate" \
    "$BASE_URL/api/pki/verify" \
    "valid"

run_success_test 4 "Secure Data" \
    "$BASE_URL/api/pki/secure-data" \
    "PKI authentication"

run_success_test 5 "Auth Method Check" \
    "$BASE_URL/api/pki/auth-method" \
    "PKI"

run_failure_test 6 "Request without certificate should fail" \
    "$BASE_URL/api/pki/health"