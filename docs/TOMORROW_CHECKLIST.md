# 🎯 Tomorrow Morning Checklist - GraphQL + OAuth2

## Before You Start Tomorrow

### ✅ System Verification (5 minutes)
- [ ] App running: `docker ps | grep secure-project-hub`
- [ ] Database healthy: Check PostgreSQL on 5432
- [ ] HTTPS working: Verify port 8443 accessible
- [ ] Tests passing: Run `bash docs/tests/pki-test-suite.sh`

### 📖 Read These First (10 minutes)
1. [ ] `docs/DAY1_COMPLETE.md` - Today's summary
2. [ ] `docs/GRAPHQL_TOMORROW_PLAN.md` - Your full plan
3. [ ] `docs/README.md` - Quick reference

### 🛠️ Environment Check (5 minutes)
- [ ] Maven installed: `mvn -v`
- [ ] Java version: `java -version` (should be 21+)
- [ ] Docker working: `docker --version`
- [ ] Git status clean: `git status`

---

## Phase 1: Setup GraphQL (First 30 minutes)

### Step 1: Add GraphQL Dependency
- [ ] Open `pom.xml`
- [ ] Add `spring-boot-starter-graphql` dependency (v4.0.5)
- [ ] Run `mvn clean install`
- [ ] Verify download successful

### Step 2: Create GraphQL Directories
```bash
mkdir -p src/main/resources/graphql/
```
- [ ] Directory created

### Step 3: Configure GraphQL in application.yml
- [ ] Add graphql configuration
- [ ] Set path to `/graphql`
- [ ] Enable GraphQL playground

### Step 4: Rebuild and Test
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
sleep 15
curl http://localhost:8080/graphql
```
- [ ] GraphQL endpoint responds
- [ ] No errors in logs

---

## Phase 2: Create Schema (Next 45 minutes)

### Step 1: Create schema.graphqls
- [ ] Create `src/main/resources/graphql/schema.graphqls`
- [ ] Define User type
- [ ] Define Role type
- [ ] Define Permission type
- [ ] Define Query root with user, role, permission queries

### Step 2: Test Schema
- [ ] Rebuild: `mvn clean package`
- [ ] No schema validation errors
- [ ] GraphQL playground loads schema

---

## Phase 3: Implement Resolvers (60 minutes)

### Step 1: Create Query Resolvers
- [ ] Create `UserQueryResolver.java`
- [ ] Implement `users()` method
- [ ] Implement `user(id)` method
- [ ] Create `RoleQueryResolver.java`
- [ ] Create `PermissionQueryResolver.java`

### Step 2: Add Data Fetchers
- [ ] Lazy load User.roles
- [ ] Lazy load Role.permissions

### Step 3: Test Resolvers
- [ ] Rebuild and restart
- [ ] Test queries via GraphQL playground
- [ ] Verify data returns correctly

---

## Phase 4: OAuth2 Integration (45 minutes)

### Step 1: Update Security Config
- [ ] Add `/graphql` to authenticated endpoints
- [ ] Ensure JWT filter applies to GraphQL
- [ ] Ensure PKI filter applies to GraphQL

### Step 2: Test OAuth2 with GraphQL
```bash
# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')

# Test GraphQL query
curl -X POST http://localhost:8080/graphql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "{ users { id username } }"}'
```
- [ ] Query succeeds with valid token
- [ ] Query fails without token

### Step 3: Test PKI with GraphQL
```bash
curl -s --cacert certs/ca-cert.pem \
     --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     -X POST https://localhost:8443/graphql \
     -H "Content-Type: application/json" \
     -d '{"query": "{ users { id username } }"}'
```
- [ ] Query succeeds with PKI cert
- [ ] Both auth methods work

---

## Phase 5: Testing (30 minutes)

### Test Coverage
- [ ] GraphQL health check
- [ ] Query users with OAuth2
- [ ] Query users with PKI
- [ ] Query roles with OAuth2
- [ ] Query permissions with OAuth2
- [ ] Test authorization (admin vs user)
- [ ] Test nested queries
- [ ] Test invalid token (should fail)
- [ ] Test missing auth (should fail)

---

## Phase 6: Documentation (30 minutes)

### Create Docs
- [ ] `docs/graphql/GRAPHQL_SETUP.md` - Configuration
- [ ] `docs/graphql/GRAPHQL_QUERIES.md` - Examples
- [ ] `docs/graphql/GRAPHQL_OAUTH2.md` - Auth integration
- [ ] Create test script: `docs/tests/graphql-test.sh`

---

## Success Metrics

By end of tomorrow:
- [ ] GraphQL endpoint at `/graphql` working
- [ ] All queries returning data
- [ ] OAuth2 authentication working
- [ ] PKI authentication working
- [ ] Authorization checks enforced
- [ ] All tests passing
- [ ] Documentation complete

---

## Command Quick Reference

**Build:**
```bash
mvn clean package
docker-compose build --no-cache
```

**Run:**
```bash
docker-compose up -d
```

**Logs:**
```bash
docker logs -f secure-project-hub-app-1
```

**Test GraphQL:**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "{ users { id username } }"}'
```

---

## Troubleshooting Quick Links

**Issue:** GraphQL endpoint not found
- **Solution:** Check application.yml configuration, restart app

**Issue:** JWT token not working with GraphQL
- **Solution:** Verify SecurityConfig allows `/graphql`, check JWT filter

**Issue:** Schema validation errors
- **Solution:** Check schema.graphqls syntax, restart Maven build

**Issue:** Resolvers not called
- **Solution:** Verify @QueryResolver annotations, check class names

---

## 🏁 Sign-Off

**When you wake up tomorrow:**
1. Read: `docs/GRAPHQL_TOMORROW_PLAN.md`
2. Run: Verification checklist above
3. Start: Phase 1 with fresh energy
4. Track: Your progress in todo list

---

**Good luck tomorrow! You've got this! 🚀**

Status: Ready for GraphQL + OAuth2 Integration  
Estimated Time: 3-4 hours  
Difficulty: Intermediate  
