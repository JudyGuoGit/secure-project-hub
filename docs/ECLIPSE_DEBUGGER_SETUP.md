# Eclipse Debugger Connection Guide

## Current Status
✅ Debug port 5005 is listening and accessible  
✅ Java Debug Wire Protocol (JDWP) is enabled  
✅ Application is running with debug agent  

```
Listening for transport dt_socket at address: 5005
```

---

## How to Connect Eclipse Debugger

### Step 1: Open Eclipse Debug Configurations

1. In Eclipse, go to: **Run → Debug Configurations**
2. Or use keyboard shortcut: **Cmd+Shift+D** (Mac) or **Ctrl+Shift+D** (Windows/Linux)

### Step 2: Create a New Debug Configuration

1. Right-click on **Remote Java Application** in the left panel
2. Select **New Configuration**
3. Name it: `secure-project-hub-debug` (or similar)

### Step 3: Configure Connection Settings

Fill in these fields:

| Field | Value |
|-------|-------|
| **Project** | secure-project-hub |
| **Host** | localhost |
| **Port** | 5005 |
| **Connection Type** | Socket Attach |
| **Filter** | (leave blank) |

Screenshot example:
```
┌─ Debug Configurations ─────────────────────┐
│ Remote Java Application                     │
│ ┌─ secure-project-hub-debug ────────────┐  │
│ │ Project:      secure-project-hub       │  │
│ │ Host:         localhost                │  │
│ │ Port:         5005                     │  │
│ │ Connection:   Socket Attach ▼          │  │
│ │                                        │  │
│ │ [Apply] [Debug] [Close]                │  │
│ └────────────────────────────────────────┘  │
└────────────────────────────────────────────┘
```

### Step 4: Click "Debug"

Once configured, click the **Debug** button to connect.

---

## Expected Behavior After Connection

### ✅ Success Indicators
1. Eclipse shows "Connected" status in Debug view
2. Debug perspective opens automatically
3. You see the running threads in the Debug pane
4. You can:
   - Set breakpoints
   - Step through code
   - Inspect variables
   - Pause/resume execution

### Example: Setting a Breakpoint

1. Open `PkiAuthenticationFilter.java`
2. Click in the left margin at line where you want to break
3. A red circle appears (breakpoint set)
4. Make a request to trigger the breakpoint
5. Execution pauses, you can inspect variables

---

## Troubleshooting Connection Issues

### Issue: "Connection refused"
**Cause:** Port 5005 not accessible  
**Solution:**
```bash
# Verify port is listening
lsof -i :5005

# Check Docker container
docker ps | grep secure-project-hub

# Verify Docker port mapping
docker port secure-project-hub-app-1 | grep 5005
```

**Output should be:**
```
0.0.0.0:5005->5005/tcp
```

### Issue: "Connection timeout"
**Cause:** Firewall or network issue  
**Solution:**
```bash
# Test connectivity
nc -zv localhost 5005

# Should show:
# Connection to localhost port 5005 succeeded!
```

### Issue: "Debug connection dropped"
**Cause:** App crashed or restarted  
**Solution:**
1. Check Docker logs: `docker logs -f secure-project-hub-app-1`
2. Reconnect the debugger
3. Or restart the container: `docker-compose restart`

### Issue: "Suspended but can't resume"
**Cause:** Suspended on startup  
**Solution:** Modify Dockerfile if needed:
```dockerfile
# Current (good for debugging):
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005

# If you want to suspend at startup (wait for debugger):
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005
```

---

## Useful Debug Configurations for Development

### Configuration 1: Standard Debug
- **Host:** localhost
- **Port:** 5005
- **Type:** Socket Attach
- **Use case:** Normal debugging session

### Configuration 2: Suspend on Startup (for early initialization)
If you want the app to pause before starting:

1. Edit Dockerfile:
```dockerfile
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", \
  "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005", \
  "-jar", "app.jar"]
```

2. Rebuild: `docker-compose build --no-cache`
3. Start: `docker-compose up -d`
4. Immediately connect debugger (app will wait)

---

## Debugging Specific Components

### Debug PKI Authentication Flow
1. Set breakpoint in `PkiAuthenticationFilter.java` line ~60
2. Make PKI request:
   ```bash
   curl -s --cert certs/client-cert.pem --key certs/client-key.pem \
     --cacert certs/ca-cert.pem https://localhost:8443/api/users
   ```
3. Debugger pauses, inspect certificate extraction

### Debug Certificate Role Mapping
1. Set breakpoint in `CertificateRoleService.java` line ~42
2. Make request (triggers role resolution)
3. Step through the mapping logic
4. Watch variable `resolvedRole` change

### Debug OAuth2/JWT Flow
1. Set breakpoint in `JwtTokenFilter.java`
2. Make request with JWT token:
   ```bash
   curl -s http://localhost:8080/api/users \
     -H "Authorization: Bearer <JWT_TOKEN>"
   ```
3. Watch token validation

---

## Keyboard Shortcuts for Debugging

| Action | Mac | Windows/Linux |
|--------|-----|---------------|
| Resume | Cmd+⌘ | F8 |
| Step Over | F10 | F10 |
| Step Into | F11 | F11 |
| Step Out | Cmd+Shift+R | Ctrl+Shift+R |
| Toggle Breakpoint | Cmd+Shift+B | Ctrl+Shift+B |
| Inspect Variable | Hold Alt, click | Hold Ctrl, click |

---

## Pro Tips

💡 **Tip 1:** Use "Watch" expressions to monitor complex objects
```
CertificateRoleService.resolveCertificateRole(clientCert).toString()
```

💡 **Tip 2:** Set conditional breakpoints
Right-click breakpoint → Breakpoint Properties → Add condition
```
certificateCN.equals("test-client")
```

💡 **Tip 3:** Use "Debug as → Run Configurations" to debug from tests
This allows debugging unit tests with the same setup

💡 **Tip 4:** Check "Suspend VM" to pause all threads when any breakpoint hits

---

## Port Mapping Reference

| Service | Port | Type | Purpose |
|---------|------|------|---------|
| App (HTTP) | 8080 | HTTP | REST API (OAuth2) |
| App (HTTPS) | 8443 | HTTPS | REST API (PKI/mTLS) |
| Debug | 5005 | JDWP | Remote debugging |
| Database | 5432 | PostgreSQL | Data persistence |

---

## Common Debug Scenarios

### Scenario 1: "Why is my PKI request failing?"
1. Set breakpoint at `PkiAuthenticationFilter.doFilterInternal()`
2. Inspect `certificates[]` array
3. Check if certificate is extracted correctly
4. Watch role resolution step-by-step

### Scenario 2: "Certificate role mapping not working"
1. Set breakpoint in `CertificateRoleService.resolveCertificateRole()`
2. Step through DB lookup
3. Verify `Optional<CertificateRoleMapping>` is empty/present
4. Check `mapOUToRole()` logic

### Scenario 3: "Authorization failing for valid role"
1. Set breakpoint in controller method with `@PreAuthorize`
2. Inspect `Authentication` object
3. Check `getAuthorities()` collection
4. Verify role names match exactly

---

## When to Use Remote Debugging

✅ **Good use cases:**
- Debugging production-like Docker environment
- Investigating environment-specific issues
- Testing with real certificates/authentication
- Understanding multi-container interactions

❌ **Not ideal for:**
- Quick unit test debugging (use local JUnit instead)
- Initial development (use IDE's Run → Run As)
- Performance profiling (use JProfiler or YourKit)

---

## Next Steps

1. **Connect now:** Follow Steps 1-4 above
2. **Set first breakpoint:** In `PkiAuthenticationFilter.java`
3. **Trigger request:** Use `curl` with PKI cert
4. **Inspect state:** Use variables view to understand flow
5. **Explore:** Step through authentication/authorization logic

---

## Eclipse Debug Views

When connected, you'll see these views:

| View | Purpose |
|------|---------|
| **Debug** | Thread execution, suspended state |
| **Variables** | Local variables, objects, values |
| **Breakpoints** | List of all breakpoints |
| **Expressions** | Watch expressions you create |
| **Console** | Application output (System.out) |

---

## Quick Restart Cycle

If you need to make code changes:

```bash
# 1. Stop debugging (click Resume or disconnect)
# 2. Make code changes
# 3. Rebuild:
mvn clean package -DskipTests

# 4. Rebuild Docker image:
docker-compose build --no-cache

# 5. Restart container:
docker-compose down && docker-compose up -d

# 6. Wait for startup
sleep 15

# 7. Reconnect debugger in Eclipse
# Click Debug → Debug Configurations → Select config → Debug
```

---

**Status:** ✅ Ready to debug  
**Port:** 5005 (listening)  
**Connection:** Socket Attach  
**Next:** Open Eclipse and follow Step 1-4 above
