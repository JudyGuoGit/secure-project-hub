## Eclipse Remote Debugger Setup - Complete Troubleshooting Guide

### Status: ✅ Ready to Debug
- **Docker Container**: Running with debugger listening on port 5005
- **Configuration**: `suspend=y` (app waits for debugger before startup)
- **Port**: Confirmed open and accessible

---

## Method 1: Using the Pre-Created Launch Configuration (RECOMMENDED)

### Quick Start
1. **In Eclipse**, go to `Run` → `Debug Configurations`
2. Look for **"Docker Debug 5005"** in the list
3. Click **Debug** button
4. The app should attach and you'll see the debugger suspend at startup

**File Location**: `.eclipse/launch/Docker Debug 5005.launch`

---

## Method 2: Manual Setup (If launch config doesn't appear)

### Step 1: Open Debug Configurations
1. In Eclipse menu: `Run` → `Debug Configurations...`
2. Click **New** (or right-click and select "New")

### Step 2: Create Remote Java Debug Configuration
1. Select **"Remote Java Application"** from the left panel
2. Click the **"New launch configuration"** button (or icon)
3. Fill in the following:
   - **Name**: `Docker Debug 5005`
   - **Connection Type**: Choose **"Standard (Socket Attach)"** from the dropdown
   - **Host**: `localhost`
   - **Port**: `5005`
   - **Project**: Select `secure-project-hub` from dropdown

### Step 3: Configure Advanced Settings (Optional but Recommended)
1. Go to **"Source"** tab:
   - Click **"Add..."**
   - Select **"Java Project"**
   - Choose `secure-project-hub`
   - Click **OK**

2. Go to **"Common"** tab:
   - Under "Display in Favorites", check `Debug`
   - This makes it easier to access later

### Step 4: Save and Debug
1. Click **"Debug"** button
2. You should see:
   - Connection established in Console
   - Breakpoints will be active
   - App will resume after you continue (if needed)

---

## Troubleshooting Connection Issues

### Issue 1: "Connection refused" or "Connection reset"
**Cause**: Debugger port not accessible

**Fix**:
```bash
# Verify port is open
nc -zv localhost 5005

# Check if containers are running
docker ps | grep secure-project

# Check app logs
docker logs secure-project-hub-app-1 | grep -i "listening\|debug"
```

### Issue 2: "Connection timed out"
**Cause**: App started before debugger attached (suspend=n)

**Fix**: Currently fixed in Dockerfile (suspend=y). App will wait for debugger.

### Issue 3: Debugger attaches but no breakpoints work
**Cause**: Source code mapping issue

**Fix**:
1. Right-click project → `Properties`
2. Go to `Java Build Path` → `Source` tab
3. Verify source folders are mapped correctly
4. Rebuild: `Project` → `Clean...`

### Issue 4: App still running without debugger connection
**Cause**: Container might have old image or suspend setting is not applied

**Fix**:
```bash
# Remove everything and rebuild fresh
docker-compose down
docker system prune -a
docker-compose up --build -d

# Wait 10 seconds for container to start
sleep 10

# Verify debugger is listening
docker logs secure-project-hub-app-1 | grep "Listening"
```

---

## Docker Container Debug Status

### Current Configuration (Dockerfile)
```dockerfile
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", 
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005", 
    "-jar", "app.jar"]
```

**Flags Explained**:
- `transport=dt_socket` - Uses socket connection (not shared memory)
- `server=y` - JVM runs debug server (listens for Eclipse)
- `suspend=y` - **JVM suspends until debugger connects**
- `address=0.0.0.0:5005` - Listens on all interfaces, port 5005

### Docker Compose Port Mapping
```yaml
ports:
  - "5005:5005"  # Maps container port 5005 to localhost 5005
```

---

## Using the Debugger

### Basic Controls
- **Resume** (F8): Continue execution
- **Suspend**: Pause running code
- **Step Over** (F6): Execute current line, skip methods
- **Step Into** (F5): Enter method calls
- **Step Out** (F7): Exit current method
- **Disconnect** (Ctrl+Alt+D): End debug session

### Setting Breakpoints
1. Double-click line number in Editor
2. OR right-click line → `Toggle Breakpoint`
3. Conditional: Right-click breakpoint → `Properties`

### Monitoring Variables
- **Variables** view: See variable values at breakpoints
- **Expressions** view: Evaluate custom expressions
- **Watch** view: Monitor specific variables

---

## Quick Test Steps

1. **In Eclipse**:
   - Open a Java file (e.g., `SecureProjectHubApplication.java`)
   - Add a breakpoint on the first line of `main()` method
   - Go to `Run` → `Debug Configurations`
   - Select **"Docker Debug 5005"**
   - Click **Debug**

2. **Expected Result**:
   - Console shows: `"Listening for transport dt_socket..."`
   - Then shows: Connected message
   - Execution pauses at your breakpoint

3. **Debug**:
   - Inspect variables in Variables panel
   - Step through code using F5/F6/F7
   - Click Resume (F8) to continue

---

## Advanced: Debug Environment Variables

To pass environment variables to the debugger:
```bash
# Edit docker-compose.yml environment section
environment:
  DEBUG_ENABLED: "true"
  DEBUG_PORT: "5005"
```

Or in terminal:
```bash
docker-compose exec app sh
# Inside container:
echo $DEBUG_ENABLED
```

---

## References
- [Eclipse Remote Debugging Docs](https://www.eclipse.org/articles/article.php?file=Article-Debugging-Across-Network/index.html)
- [Java Debug Wire Protocol](https://docs.oracle.com/en/java/javase/21/docs/specs/jdwp/protocol.html)
- [Docker Compose Networking](https://docs.docker.com/compose/networking/)

---

## Support Commands

### View real-time logs
```bash
docker logs -f secure-project-hub-app-1
```

### Restart container with fresh debugger
```bash
docker-compose restart app
```

### Full rebuild from scratch
```bash
cd /Users/jguo/work/eclipse-workspace/secure-project-hub
docker-compose down -v
docker-compose up --build -d
```

### Check if debugger process is active
```bash
docker ps -a | grep secure-project-hub-app-1
```

### Get container ID for manual connection
```bash
docker ps | grep "secure-project-hub-app-1" | awk '{print $1}'
```

---

**Last Updated**: April 14, 2026
**Status**: ✅ Ready to Debug
**Next Action**: Use Method 1 in Eclipse to start debugging!
