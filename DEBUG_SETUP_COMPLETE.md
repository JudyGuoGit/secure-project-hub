# Eclipse Remote Debugger Connection - Final Troubleshooting Guide

## Status: ✅ READY TO DEBUG

**Container Status**: App running with debugger listening on port 5005  
**Debugger Mode**: `suspend=n` (app starts immediately, debugger can attach anytime)  
**Port**: Confirmed open and accessible on localhost:5005

---

## CRITICAL STEPS IN ECLIPSE

### 1. **Refresh Eclipse Project**
Before attempting to debug, you MUST refresh the project:
- Right-click `secure-project-hub` project
- Click `Refresh` (or press `F5`)
- This ensures Eclipse reloads the launch configuration

### 2. **Clear Eclipse Cache** (if still having issues)
- Close Eclipse completely
- Delete: `~/.eclipse/org.eclipse.platform_<version>/configuration/.settings`
- Restart Eclipse

### 3. **Launch Configuration Verification**
- Go to `Run` → `Debug Configurations...`
- Look for **"Docker Debug 5005"** in the left panel
- If not visible, click `New` → `Remote Java Application`
- Fill in:
  - **Name**: Docker Debug 5005
  - **Host**: localhost
  - **Port**: 5005
  - **Project**: secure-project-hub (from dropdown)
- Click **Apply** then **Debug**

### 4. **Source Path Configuration**
This is CRITICAL for breakpoints to work:
- In Debug Configurations window, go to **Source** tab
- Click **Add...**
- Select **Java Project**
- Choose **secure-project-hub**
- Click **Finish**

### 5. **Set a Breakpoint**
- Open any Java file (e.g., `SecureProjectHubApplication.java`)
- Find the `main()` method or a simple method
- Double-click the line number to set a breakpoint
- You should see a blue dot appear

### 6. **Start Debugging**
- Click **Debug** button in Debug Configurations
- Watch the Eclipse Console tab
- Expected messages:
  - "Connecting to localhost:5005..."
  - "Execution suspended"
  - Debugger should pause at your breakpoint

---

## If "Connection Reset" Still Occurs

### Diagnosis Steps:
```bash
# Check container is healthy
docker ps | grep secure-project-hub-app

# Check debugger is listening
docker logs secure-project-hub-app-1 | grep "Listening"

# Test network connectivity
nc -zv localhost 5005
```

### Solution: Restart Container
```bash
cd /Users/jguo/work/eclipse-workspace/secure-project-hub
docker-compose restart app

# Wait 10 seconds for app to start
sleep 10

# Verify it started
docker logs secure-project-hub-app-1 | tail -20
```

---

## Understanding suspend=n vs suspend=y

**Current Setting**: `suspend=n`

| Setting | Behavior | Pro | Con |
|---------|----------|-----|-----|
| **suspend=n** | App starts immediately, debugger attaches on demand | App runs without debugger; easier connection | Can't break at startup |
| **suspend=y** | App waits for debugger to connect before starting | Can debug from startup | Connection issues if Eclipse doesn't attach quickly |

**For your case**: `suspend=n` is better because:
- App can start and respond to health checks
- Eclipse can attach at any time without timing issues
- You can still set breakpoints and pause execution

---

## Using the Debugger: Common Operations

| Action | Shortcut | Description |
|--------|----------|-------------|
| **Continue** | F8 | Resume execution |
| **Step Over** | F6 | Execute current line, skip method calls |
| **Step Into** | F5 | Enter method calls |
| **Step Out** | F7 | Exit current method |
| **Suspend** | - | Pause running thread |
| **Disconnect** | Ctrl+Alt+D | End debug session |
| **Drop to Frame** | - | Rewind to earlier in method |

### Watch Variables
1. Open **Variables** view: `Window` → `Show View` → `Variables`
2. In Debug mode, hover over any variable to see its value
3. Right-click variable → `Add to Expressions` to monitor it

### Conditional Breakpoints
1. Right-click breakpoint dot → **Breakpoint Properties**
2. Check **Conditional**
3. Enter condition (e.g., `username.equals("admin")`)
4. Only pauses when condition is true

---

## Network Troubleshooting

### Port Already in Use
```bash
# Find what's using port 5005
lsof -i :5005

# Kill if needed (careful!)
kill -9 <PID>
```

### Docker Network Issue
```bash
# Check container network
docker network ls
docker network inspect secure-project-hub_secure-network

# Inspect container IP
docker inspect secure-project-hub-app-1 | grep IPAddress
```

### Firewall Blocking (macOS)
1. System Preferences → Security & Privacy → Firewall
2. Add Eclipse to allowed apps if needed
3. Or disable temporarily for testing

---

## Full Log Capture (for debugging)

Get detailed app startup logs:
```bash
docker logs secure-project-hub-app-1 2>&1 | head -50  # First 50 lines
docker logs secure-project-hub-app-1 2>&1 | tail -50  # Last 50 lines
docker logs secure-project-hub-app-1 2>&1             # All logs
```

Get real-time logs:
```bash
docker logs -f secure-project-hub-app-1
```

---

## Eclipse-Specific Settings

### Increase Debug Timeout
If connection times out:
1. `Eclipse` → `Preferences` (or `Eclipse` → `Settings` on macOS)
2. `Java` → `Debug`
3. Increase **Socket timeout (ms)** from 10000 to 30000
4. Apply and restart Eclipse

### Enable Debug Output
1. Window → Show View → Console
2. Add new console: click + icon
3. Select `Debug Console`
4. This shows JDWP protocol messages

---

## Reference: Dockerfile Debug Configuration

Current setting (in `/Users/jguo/work/eclipse-workspace/secure-project-hub/Dockerfile`):

```dockerfile
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005", \
    "-jar", "app.jar"]
```

**To change back to suspend=y** (wait for debugger before starting):
```dockerfile
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005
```

Then rebuild:
```bash
docker-compose down
docker-compose up --build -d
```

---

## Quick Test Checklist

- [ ] App container is running: `docker ps | grep secure-project-hub-app`
- [ ] Port 5005 is listening: `netstat -an | grep 5005`
- [ ] Can connect: `nc -zv localhost 5005`
- [ ] Eclipse project refreshed: Right-click project → Refresh
- [ ] Launch config exists: Run → Debug Configurations
- [ ] Source path configured: In Debug Configurations → Source tab
- [ ] Breakpoint set: Blue dot on line number
- [ ] Ready to debug: Click Debug button!

---

## Still Having Issues?

1. **Check Eclipse Error Log**:
   - Help → Show Error Log
   - Look for JDWP-related errors

2. **Try Manual Connection**:
   - Run → Debug Configurations
   - Create new Remote Java Application
   - Fill in manually instead of using saved config

3. **Rebuild Everything**:
   ```bash
   docker-compose down -v
   docker system prune -a
   docker-compose up --build -d
   ```

4. **Check Source Code Matches**:
   - Project must be built with `maven`
   - Binary in Docker must match source in Eclipse
   - `Project` → `Build Project` in Eclipse

---

**Last Updated**: April 14, 2026  
**Status**: ✅ Ready to Debug  
**Next Step**: Open Eclipse and click Debug!
