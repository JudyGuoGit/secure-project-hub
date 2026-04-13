# Tomorrow's Plan: GraphQL + OAuth2 Integration

## 🎯 Objective
Expose REST API endpoints as GraphQL queries with OAuth2/JWT authentication support.

---

## 📋 Step-by-Step Plan for Tomorrow

### Phase 1: Setup GraphQL (30 minutes)
**Goal:** Get GraphQL endpoint running at `/graphql`

**Tasks:**
1. [ ] Add dependency to pom.xml: `spring-boot-starter-graphql`
2. [ ] Create directory: `src/main/resources/graphql/`
3. [ ] Create GraphQL schema file: `schema.graphqls`
4. [ ] Configure GraphQL properties in `application.yml`
5. [ ] Test endpoint responds at `http://localhost:8080/graphql`

**Expected:**
```
POST http://localhost:8080/graphql
Content-Type: application/json
{ "query": "{ users { id username } }" }
```

---

### Phase 2: Create GraphQL Schema (45 minutes)
**Goal:** Define User, Role, Permission types and root queries

**Tasks:**
1. [ ] Define `User` type (id, username, email, enabled, roles)
2. [ ] Define `Role` type (id, name, permissions)
3. [ ] Define `Permission` type (id, name, description)
4. [ ] Create root Query type with:
   - `users: [User!]!`
   - `user(id: ID!): User`
   - `roles: [Role!]!`
   - `role(id: ID!): Role`
   - `permissions: [Permission!]!`

**Schema Structure:**
```graphql
type Query {
  users: [User!]!
  user(id: ID!): User
  roles: [Role!]!
  role(id: ID!): Role
  permissions: [Permission!]!
}

type User {
  id: ID!
  username: String!
  email: String!
  fullName: String
  enabled: Boolean!
  roles: [Role!]!
}

type Role {
  id: ID!
  name: String!
  permissions: [Permission!]!
}

type Permission {
  id: ID!
  name: String!
  description: String
}
```

---

### Phase 3: Implement GraphQL Resolvers (60 minutes)
**Goal:** Connect schema to existing repositories

**Tasks:**
1. [ ] Create `UserQueryResolver` with `@QueryResolver` methods:
   - `users()` → calls UserRepository.findAll()
   - `user(id)` → calls UserRepository.findById(id)

2. [ ] Create `RoleQueryResolver`:
   - `roles()` → calls RoleRepository.findAll()
   - `role(id)` → calls RoleRepository.findById(id)

3. [ ] Create `PermissionQueryResolver`:
   - `permissions()` → calls PermissionRepository.findAll()

4. [ ] Create data fetchers for nested fields:
   - User → roles (lazy load)
   - Role → permissions (lazy load)

**Implementation Pattern:**
```java
@Component
public class UserQueryResolver implements GraphQLQueryResolver {
    @Autowired
    private UserRepository userRepository;
    
    public List<User> users() {
        return userRepository.findAll();
    }
    
    public Optional<User> user(Long id) {
        return userRepository.findById(id);
    }
}
```

---

### Phase 4: Integrate OAuth2 Authentication (45 minutes)
**Goal:** Make JWT tokens work with GraphQL queries

**Tasks:**
1. [ ] Update SecurityConfig to allow `/graphql` endpoint:
   - Add `.requestMatchers("/graphql").authenticated()`
   - Ensure JWT filter applies to GraphQL

2. [ ] Create GraphQL context to extract authentication:
   - Extract JWT from Authorization header
   - Set Spring Security context

3. [ ] Add authorization to resolvers:
   - Use `@PreAuthorize("hasAnyRole('ADMIN', 'USER')")`
   - Restrict specific queries by role

4. [ ] Verify both auth methods work:
   - OAuth2/JWT token ✅
   - PKI client certificate ✅

**Test Flow:**
```bash
# Get token
TOKEN=$(curl -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')

# Use token with GraphQL
curl -X POST http://localhost:8080/graphql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "{ users { id username } }"}'
```

---

### Phase 5: Testing (30 minutes)
**Goal:** Verify all queries work with authentication

**Tests:**
1. [ ] GraphQL health check (no auth required)
2. [ ] Query users with OAuth2 token ✅
3. [ ] Query users with PKI certificate ✅
4. [ ] Query with invalid token ❌ (should fail)
5. [ ] Query with missing auth ❌ (should fail)
6. [ ] Test role-based access (admin vs user)
7. [ ] Test nested queries (users → roles → permissions)

---

### Phase 6: Documentation (30 minutes)
**Goal:** Document GraphQL endpoint and usage

**Create:**
1. [ ] `docs/graphql/GRAPHQL_SETUP.md` - Configuration guide
2. [ ] `docs/graphql/GRAPHQL_QUERIES.md` - Query examples
3. [ ] `docs/graphql/GRAPHQL_OAUTH2.md` - Auth integration
4. [ ] `docs/tests/graphql-test.sh` - Test script

---

## 📊 Dependencies to Add

```xml
<!-- GraphQL -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
    <version>4.0.5</version>
</dependency>

<!-- GraphQL Java Tools (if needed for schema generation) -->
<dependency>
    <groupId>com.graphql-java</groupId>
    <artifactId>graphql-java</artifactId>
    <version>21.0</version>
</dependency>
```

---

## ⏰ Estimated Timeline

| Phase | Task | Time |
|-------|------|------|
| 1 | Setup GraphQL | 30 min |
| 2 | Create Schema | 45 min |
| 3 | Implement Resolvers | 60 min |
| 4 | OAuth2 Integration | 45 min |
| 5 | Testing | 30 min |
| 6 | Documentation | 30 min |
| **Total** | | **3.5 hours** |

---

## 🔍 Key Files to Work With Tomorrow

| File | Purpose |
|------|---------|
| pom.xml | Add spring-graphql dependency |
| application.yml | GraphQL configuration |
| SecurityConfig.java | Allow /graphql endpoint, ensure JWT/PKI work |
| src/main/resources/graphql/schema.graphqls | GraphQL schema definition |
| New: UserQueryResolver.java | User query resolvers |
| New: RoleQueryResolver.java | Role query resolvers |
| New: PermissionQueryResolver.java | Permission query resolvers |

---

## 🎯 Success Criteria for Tomorrow

✅ GraphQL endpoint responds at `/graphql`  
✅ All queries return correct data  
✅ OAuth2 tokens work with GraphQL  
✅ PKI certificates work with GraphQL  
✅ Authorization checks enforced  
✅ Tests passing (GraphQL + OAuth2)  
✅ Documentation complete  

---

## 💡 Important Notes

1. **Consistency:** GraphQL schema should mirror REST API structure
2. **Auth:** Both OAuth2 and PKI should work automatically (already in SecurityConfig)
3. **Performance:** Use DataFetcher for nested fields to avoid N+1 queries
4. **Error Handling:** GraphQL errors should include authorization info
5. **Testing:** Test with both OAuth2 tokens AND PKI certificates

---

## 🚀 Start Tomorrow With

1. Update pom.xml with GraphQL dependency
2. Run `mvn clean install` to download deps
3. Create `src/main/resources/graphql/schema.graphqls`
4. Start fresh build: `docker-compose down && docker-compose build --no-cache`
5. Begin Phase 1 setup

**Entry point doc:** This file (`docs/GRAPHQL_TOMORROW_PLAN.md`)

---

**Status:** Ready for tomorrow! All groundwork done. 🎉
