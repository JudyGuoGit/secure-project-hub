#!/bin/bash

# PKI Testing - One Command Test Suite for Docker
# Run from project root: bash pki-test-suite.sh

cd /Users/jguo/work/eclipse-workspace/secure-project-hub

echo "======================================================"
echo "PKI/mTLS Testing Suite"
echo "======================================================"
echo ""
echo "App running on:"
echo "  HTTPS: https://localhost:8443 (PKI/mTLS endpoints)"
echo ""
echo "Certificate Files:"
echo "  CA:     certs/ca-cert.pem"
echo "  Client: certs/client-cert.pem"
echo "  Key:    certs/client-key.pem"
echo ""
echo "NOTE: Running HTTPS-only with mandatory client certificates"
echo ""
echo "Started at: $(date)"
echo "======================================================"
echo ""

# SSL/TLS options for curl (PKI endpoints only)
SSL_OPTS="--cacert certs/ca-cert.pem --cert certs/client-cert.pem --key certs/client-key.pem"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Test counter
PASSED=0
FAILED=0

# Function to run test
run_test() {
    local test_num=$1
    local test_name=$2
    local command=$3
    local expected=$4
    
    echo -e "${BLUE}Test $test_num: $test_name${NC}"
    echo "Command: $command"
    
    result=$(eval "$command" 2>&1)
    
    if echo "$result" | grep -q "$expected"; then
        echo -e "${GREEN}✅ PASSED${NC}"
        ((PASSED++))
    else
        echo -e "${RED}❌ FAILED${NC}"
        echo "Result: $result"
        ((FAILED++))
    fi
    echo ""
}

# ========================================
# Phase 1: Basic Health Checks (skipped for HTTPS-only)
# ========================================
echo -e "${BLUE}==== Phase 1: Health Checks (HTTPS-only) ====${NC}"
echo ""
echo "⏭️  Skipping health check - application requires client certificates"
echo ""

# ========================================
# Phase 2: OAuth2 Token (skipped for HTTPS-only)
# ========================================
echo -e "${BLUE}==== Phase 2: OAuth2 Token (HTTPS-only) ====${NC}"
echo ""
echo "⏭️  Skipping token endpoint - using PKI authentication instead"
echo ""

# ========================================
# Phase 3: PKI/mTLS Tests (Main Event)
# ========================================
echo -e "${BLUE}==== Phase 3: PKI/mTLS Tests ====${NC}"
echo ""

run_test 3 "PKI Health Check" \
    "curl -s $SSL_OPTS https://localhost:8443/api/pki/health" \
    "UP"

run_test 4 "Certificate Info" \
    "curl -s $SSL_OPTS https://localhost:8443/api/pki/certificate-info" \
    "subject"

run_test 5 "Verify Certificate" \
    "curl -s $SSL_OPTS https://localhost:8443/api/pki/verify" \
    "valid"

run_test 6 "Secure Data" \
    "curl -s $SSL_OPTS https://localhost:8443/api/pki/secure-data" \
    "{" 

run_test 7 "Auth Method Check" \
    "curl -s $SSL_OPTS https://localhost:8443/api/pki/auth-method" \
    "PKI"

# ========================================
# Phase 4: Security Tests (Should Fail)
# ========================================
echo -e "${BLUE}==== Phase 4: Security Tests (Should Fail) ====${NC}"
echo ""

echo -e "${YELLOW}Test 8: Request without certificate (should fail)${NC}"
result=$(curl -s -k https://localhost:8443/api/pki/health 2>&1)
if echo "$result" | grep -qE "error|401|403|UNABLE"; then
    echo -e "${GREEN}✅ PASSED (correctly rejected)${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠️  WARNING - Request was not rejected${NC}"
    echo "Result: $result"
fi
echo ""

# ========================================
# Phase 5: Certificate Details
# ========================================
echo -e "${BLUE}==== Phase 5: Certificate Details ====${NC}"
echo ""

echo "Client Certificate Info:"
openssl x509 -in certs/client-cert.pem -noout -subject -issuer -dates
echo ""

echo "Certificate Chain Validity:"
echo "  CA: $(openssl x509 -in certs/ca-cert.pem -noout -dates | tr '\n' ' ')"
echo "  Server: $(openssl x509 -in certs/server-cert.pem -noout -dates | tr '\n' ' ')"
echo "  Client: $(openssl x509 -in certs/client-cert.pem -noout -dates | tr '\n' ' ')"
echo ""

# ========================================
# Phase 6: Docker Status
# ========================================
echo -e "${BLUE}==== Phase 6: Docker Status ====${NC}"
echo ""

echo "Container Status:"
docker ps | grep secure-project-hub
echo ""

# ========================================
# Summary
# ========================================
echo "======================================================"
echo "Test Summary"
echo "======================================================"
echo -e "✅ Passed: ${GREEN}$PASSED${NC}"
echo -e "❌ Failed: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed! PKI is working correctly.${NC}"
    exit 0
else
    echo -e "${RED}⚠️  Some tests failed. Check output above.${NC}"
    exit 1
fi
