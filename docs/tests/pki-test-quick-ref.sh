#!/bin/bash
# PKI Testing Commands - Quick Script
# Run this script from the project root directory

PROJECT_DIR="/Users/jguo/work/eclipse-workspace/secure-project-hub"
cd "$PROJECT_DIR"

echo "=========================================="
echo "PKI Testing - Quick Command Reference"
echo "=========================================="
echo ""
echo "App is running on:"
echo "  - HTTP:  http://localhost:8080"
echo "  - HTTPS: https://localhost:8443 (PKI/mTLS)"
echo ""

# Color codes
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ========== Phase 1: Health Checks ==========
echo -e "${BLUE}=== Phase 1: Health Checks ===${NC}"
echo ""

echo -e "${YELLOW}1.1 HTTP Health Check${NC}"
echo "Command:"
echo "curl -s http://localhost:8080/actuator/health | jq ."
echo ""
echo "Expected: {\"status\": \"UP\"}"
echo ""

# ========== Phase 2: OAuth2 Token ==========
echo -e "${BLUE}=== Phase 2: Get OAuth2 Token ===${NC}"
echo ""

echo -e "${YELLOW}2.1 Request OAuth2 Token${NC}"
echo "Command:"
echo "TOKEN=\$(curl -s -X POST http://localhost:8080/api/token \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{\"username\": \"admin\", \"password\": \"password\"}' | jq -r '.token // .access_token')"
echo "echo \$TOKEN"
echo ""
echo "Expected: JWT token (starts with eyJ...)"
echo ""

# ========== Phase 3: PKI Tests ==========
echo -e "${BLUE}=== Phase 3: PKI/mTLS Tests ===${NC}"
echo ""

echo -e "${YELLOW}3.1 PKI Health Check (mTLS)${NC}"
echo "Command:"
echo "curl -s --cert certs/client-cert.pem \\"
echo "     --key certs/client-key.pem \\"
echo "     --cacert certs/ca-cert.pem \\"
echo "     https://localhost:8443/api/pki/health | jq ."
echo ""
echo "Expected: {\"status\": \"UP\", \"description\": \"PKI Authentication successful\"}"
echo ""

echo -e "${YELLOW}3.2 Get Certificate Information${NC}"
echo "Command:"
echo "curl -s --cert certs/client-cert.pem \\"
echo "     --key certs/client-key.pem \\"
echo "     --cacert certs/ca-cert.pem \\"
echo "     https://localhost:8443/api/pki/certificate-info | jq ."
echo ""
echo "Expected: Certificate details (subject, issuer, validity)"
echo ""

echo -e "${YELLOW}3.3 Verify Certificate${NC}"
echo "Command:"
echo "curl -s --cert certs/client-cert.pem \\"
echo "     --key certs/client-key.pem \\"
echo "     --cacert certs/ca-cert.pem \\"
echo "     https://localhost:8443/api/pki/verify | jq ."
echo ""
echo "Expected: {\"valid\": true, \"reason\": \"Certificate is valid and trusted\"}"
echo ""

echo -e "${YELLOW}3.4 Access Secure Data (PKI)${NC}"
echo "Command:"
echo "curl -s --cert certs/client-cert.pem \\"
echo "     --key certs/client-key.pem \\"
echo "     --cacert certs/ca-cert.pem \\"
echo "     https://localhost:8443/api/pki/secure-data | jq ."
echo ""
echo "Expected: Secure data payload"
echo ""

echo -e "${YELLOW}3.5 Check Authentication Method${NC}"
echo "Command:"
echo "curl -s --cert certs/client-cert.pem \\"
echo "     --key certs/client-key.pem \\"
echo "     --cacert certs/ca-cert.pem \\"
echo "     https://localhost:8443/api/pki/auth-method | jq ."
echo ""
echo "Expected: {\"method\": \"PKI\", \"details\": \"...\"}"
echo ""

# ========== Phase 4: Security Tests ==========
echo -e "${BLUE}=== Phase 4: Security Tests (Should Fail) ===${NC}"
echo ""

echo -e "${YELLOW}4.1 Request WITHOUT Certificate (Should Fail)${NC}"
echo "Command:"
echo "curl -s -k https://localhost:8443/api/pki/health 2>&1"
echo ""
echo "Expected: Connection failure or 401/403 error"
echo ""

echo -e "${YELLOW}4.2 Request with Wrong Certificate (Should Fail)${NC}"
echo "Command:"
echo "openssl req -x509 -newkey rsa:4096 -nodes \\"
echo "  -keyout /tmp/fake-key.pem -out /tmp/fake-cert.pem \\"
echo "  -days 1 -subj \"/CN=fake\""
echo ""
echo "curl -s --cert /tmp/fake-cert.pem \\"
echo "     --key /tmp/fake-key.pem \\"
echo "     --cacert certs/ca-cert.pem \\"
echo "     https://localhost:8443/api/pki/health 2>&1"
echo ""
echo "Expected: SSL certificate verification failure"
echo ""

# ========== Phase 5: Certificate Inspection ==========
echo -e "${BLUE}=== Phase 5: Certificate Inspection ===${NC}"
echo ""

echo -e "${YELLOW}5.1 View Client Certificate Details${NC}"
echo "Command:"
echo "openssl x509 -in certs/client-cert.pem -text -noout | head -30"
echo ""

echo -e "${YELLOW}5.2 Check Certificate Expiration Dates${NC}"
echo "Command:"
echo "echo '=== CA Certificate ===' && openssl x509 -in certs/ca-cert.pem -noout -dates"
echo "echo '=== Server Certificate ===' && openssl x509 -in certs/server-cert.pem -noout -dates"
echo "echo '=== Client Certificate ===' && openssl x509 -in certs/client-cert.pem -noout -dates"
echo ""

# ========== Phase 6: Docker Management ==========
echo -e "${BLUE}=== Phase 6: Docker Container Management ===${NC}"
echo ""

echo -e "${YELLOW}6.1 Check Docker Containers${NC}"
echo "Command:"
echo "docker ps | grep secure-project-hub"
echo ""
echo "Expected: Both app and postgres containers running"
echo ""

echo -e "${YELLOW}6.2 View Application Logs${NC}"
echo "Command:"
echo "docker logs -f secure-project-hub-app-1"
echo ""
echo "Note: Press Ctrl+C to exit"
echo ""

echo -e "${YELLOW}6.3 Restart Application${NC}"
echo "Command:"
echo "docker restart secure-project-hub-app-1"
echo ""

# ========== Run All Tests Script ==========
echo -e "${BLUE}=== Quick Test: Run All PKI Tests ===${NC}"
echo ""
echo "Copy and paste this to run all tests at once:"
echo ""
cat << 'EOF'
#!/bin/bash
cd /Users/jguo/work/eclipse-workspace/secure-project-hub

echo "1. Health Check"
curl -s http://localhost:8080/actuator/health | jq .

echo -e "\n2. Get OAuth2 Token"
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')
echo "Token: $TOKEN"

echo -e "\n3. PKI Health Check"
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health | jq .

echo -e "\n4. Certificate Info"
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/certificate-info | jq .

echo -e "\n5. Verify Certificate"
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/verify | jq .

echo -e "\n6. Secure Data"
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/secure-data | jq .

echo -e "\n7. Auth Method"
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/auth-method | jq .

echo -e "\n✅ All PKI tests completed!"
EOF
echo ""

echo "=========================================="
echo "Documentation Files:"
echo "- PKI_TESTING_GUIDE_DOCKER.md (detailed guide)"
echo "- PKI_TESTING_GUIDE.md (general guide)"
echo "=========================================="
echo ""
