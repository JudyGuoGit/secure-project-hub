#!/bin/bash

# Certificate Generation Script for Secure Project Hub
# This script generates all certificates needed for mTLS/PKI support

set -e  # Exit on error

echo "=========================================="
echo "Certificate Generation for Secure Project Hub"
echo "=========================================="
echo ""

# Create certificates directory
CERT_DIR="./certs"
mkdir -p "$CERT_DIR"
cd "$CERT_DIR"

echo "📁 Creating certificates in: $CERT_DIR"
echo ""

# Configuration
DAYS_CA=3650          # 10 years for CA
DAYS_SERVER=365       # 1 year for server
DAYS_CLIENT=365       # 1 year for client
KEY_SIZE=2048

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ============================================
# Step 1: Generate Root CA
# ============================================
echo -e "${YELLOW}[1/8] Generating Root CA Private Key...${NC}"
openssl genrsa -out ca-key.pem $KEY_SIZE 2>/dev/null
echo -e "${GREEN}✅ Created: ca-key.pem${NC}"

echo -e "${YELLOW}[2/8] Generating Root CA Certificate...${NC}"
openssl req -new -x509 -days $DAYS_CA -key ca-key.pem -out ca-cert.pem \
  -subj "/CN=Secure-Project-Hub-CA/O=Judy-Inc/C=US" 2>/dev/null
echo -e "${GREEN}✅ Created: ca-cert.pem${NC}"

# ============================================
# Step 2: Generate Server Certificate
# ============================================
echo ""
echo -e "${YELLOW}[3/8] Generating Server Private Key...${NC}"
openssl genrsa -out server-key.pem $KEY_SIZE 2>/dev/null
echo -e "${GREEN}✅ Created: server-key.pem${NC}"

echo -e "${YELLOW}[4/8] Generating Server Certificate Signing Request...${NC}"
openssl req -new -key server-key.pem -out server.csr \
  -subj "/CN=localhost/O=Secure-Project-Hub/C=US" 2>/dev/null
echo -e "${GREEN}✅ Created: server.csr${NC}"

echo -e "${YELLOW}[5/8] Signing Server Certificate with CA...${NC}"
openssl x509 -req -in server.csr -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out server-cert.pem -days $DAYS_SERVER -sha256 2>/dev/null
echo -e "${GREEN}✅ Created: server-cert.pem${NC}"

# ============================================
# Step 3: Generate Client Certificate (for testing)
# ============================================
echo ""
echo -e "${YELLOW}[6/8] Generating Client Private Key...${NC}"
openssl genrsa -out client-key.pem $KEY_SIZE 2>/dev/null
echo -e "${GREEN}✅ Created: client-key.pem${NC}"

echo -e "${YELLOW}[7/8] Generating Client Certificate Signing Request...${NC}"
openssl req -new -key client-key.pem -out client.csr \
  -subj "/CN=test-client/O=Secure-Project-Hub/C=US" 2>/dev/null
echo -e "${GREEN}✅ Created: client.csr${NC}"

echo -e "${YELLOW}[8/8] Signing Client Certificate with CA...${NC}"
openssl x509 -req -in client.csr -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out client-cert.pem -days $DAYS_CLIENT -sha256 2>/dev/null
echo -e "${GREEN}✅ Created: client-cert.pem${NC}"

# ============================================
# Step 4: Create PKCS12 Keystores
# ============================================
echo ""
echo -e "${YELLOW}Creating Keystores...${NC}"

# Server keystore
openssl pkcs12 -export -in server-cert.pem -inkey server-key.pem \
  -out keystore.p12 -name localhost -passout pass:changeit 2>/dev/null
echo -e "${GREEN}✅ Created: keystore.p12 (password: changeit)${NC}"

# Client keystore (for testing)
openssl pkcs12 -export -in client-cert.pem -inkey client-key.pem \
  -out client-keystore.p12 -name test-client -passout pass:changeit 2>/dev/null
echo -e "${GREEN}✅ Created: client-keystore.p12 (password: changeit)${NC}"

# Truststore with CA certificate
keytool -import -alias ca -file ca-cert.pem -into truststore.p12 \
  -storepass changeit -noprompt -storetype PKCS12 2>/dev/null
echo -e "${GREEN}✅ Created: truststore.p12 (password: changeit)${NC}"

# ============================================
# Step 5: Verify Certificates
# ============================================
echo ""
echo -e "${YELLOW}Verifying Certificates...${NC}"

echo ""
echo "📋 CA Certificate Info:"
openssl x509 -in ca-cert.pem -text -noout | grep -E "Subject:|Issuer:|Not Before|Not After"

echo ""
echo "📋 Server Certificate Info:"
openssl x509 -in server-cert.pem -text -noout | grep -E "Subject:|Issuer:|Not Before|Not After"

echo ""
echo "📋 Client Certificate Info:"
openssl x509 -in client-cert.pem -text -noout | grep -E "Subject:|Issuer:|Not Before|Not After"

# ============================================
# Step 6: Security Setup
# ============================================
echo ""
echo -e "${YELLOW}Setting File Permissions...${NC}"
chmod 600 ca-key.pem
chmod 600 server-key.pem
chmod 600 client-key.pem
chmod 600 keystore.p12
chmod 600 client-keystore.p12
echo -e "${GREEN}✅ Private keys set to readable by owner only (0600)${NC}"

# ============================================
# Step 7: Display Summary
# ============================================
echo ""
echo "=========================================="
echo -e "${GREEN}✅ Certificate Generation Complete!${NC}"
echo "=========================================="
echo ""
echo "📂 Files Created:"
ls -lh | grep -E "\.(pem|p12|csr)$" | awk '{print "   " $9 " (" $5 ")"}'
echo ""
echo "🔐 Important Notes:"
echo "   1. Keep ca-key.pem in secure vault (never commit to git)"
echo "   2. ca-cert.pem is public (safe to commit to git)"
echo "   3. Keystore password: changeit"
echo "   4. Truststore password: changeit"
echo ""
echo "📝 Next Steps:"
echo "   1. Copy keystore.p12 to src/main/resources/"
echo "   2. Copy truststore.p12 to src/main/resources/"
echo "   3. Copy ca-cert.pem to src/main/resources/"
echo "   4. Configure application.yml for mTLS"
echo "   5. Run tests with curl or client certificate"
echo ""
echo "🧪 Testing with curl:"
echo "   curl --cert certs/client-cert.pem --key certs/client-key.pem \\"
echo "        --cacert certs/ca-cert.pem \\"
echo "        https://localhost:8443/api/health"
echo ""
