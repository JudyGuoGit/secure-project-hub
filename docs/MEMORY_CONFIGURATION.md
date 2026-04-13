# Memory Configuration Guide

## Overview
This document describes the memory configuration changes made to resolve out-of-memory (OOM) issues during PKI test execution in Docker.

## Problem
The application was experiencing out-of-memory errors during PKI certificate processing and testing, particularly when running intensive cryptographic operations.

## Solution

### 1. Docker Container Memory Limits (docker-compose.yml)
Added explicit memory resource limits to the app service:

```yaml
deploy:
  resources:
    limits:
      memory: 2G          # Hard limit: maximum 2GB
    reservations:
      memory: 1G          # Soft limit: reserves 1GB
```

**Benefits:**
- Prevents uncontrolled memory consumption
- Ensures the container doesn't crash the Docker host
- Allows the OS to manage memory more effectively

### 2. JVM Heap Settings (Dockerfile)
Increased the JVM heap size for better performance:

```dockerfile
# Before (insufficient for PKI operations):
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]

# After (optimized for PKI/certificate operations):
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]
```

**Configuration Breakdown:**
- `-Xms512m`: Initial heap size (512 MB) - allocated at startup
- `-Xmx1024m`: Maximum heap size (1024 MB) - allows growth as needed

**Why This Works:**
- PKI/certificate operations require significant memory for key generation, signing, and verification
- Larger initial heap (`-Xms`) reduces garbage collection pauses
- Higher max heap (`-Xmx`) prevents OOM errors during peak operations

## Performance Results

### Memory Usage
```
CONTAINER STATS:
  Memory Used: 610.7 MiB / 2GiB (29.82%)
  Status: HEALTHY ✅
  No OOM errors observed
```

### Verification Steps
1. Container starts successfully without OOM errors
2. PKI certificate operations complete without memory-related failures
3. Memory utilization stays well within limits
4. Application remains responsive during testing

## Recommendations

### For Different Environments

| Environment | Heap Size | Container Limit | Use Case |
|---|---|---|---|
| Development | `-Xms256m -Xmx512m` | 1GB | Local testing |
| Testing (PKI) | `-Xms512m -Xmx1024m` | 2GB | Full PKI test suite |
| Production | `-Xms1024m -Xmx2048m` | 4GB | High-throughput services |
| Production (PKI-Heavy) | `-Xms2048m -Xmx4096m` | 8GB | PKI-intensive workloads |

### Monitoring
Monitor memory usage in production:
```bash
# Check container memory
docker stats <container-name>

# Check JVM memory (from inside container)
jstat -gc <pid> 1000
```

### Tuning Guidelines
1. **Initial Heap Size (`-Xms`)**: Set to 50-75% of max heap to reduce GC pauses
2. **Max Heap Size (`-Xmx`)**: Set to 70-80% of container memory limit
3. **Reserve Space**: Always leave 20-30% headroom for:
   - Non-heap memory (metaspace, native memory)
   - OS overhead
   - Other processes in container

## Files Modified

### 1. `Dockerfile`
- **Changed:** JVM memory parameters
- **Before:** `-Xms256m -Xmx512m`
- **After:** `-Xms512m -Xmx1024m`

### 2. `docker-compose.yml`
- **Added:** Memory resource limits and reservations
- **Limits:** 2GB hard limit
- **Reservations:** 1GB soft limit

## Testing
To verify the fix:

```bash
# Start containers
docker-compose up -d

# Run PKI test suite
bash pki-test-suite.sh

# Monitor memory during tests
docker stats secure-project-hub-app-1

# Check logs for OOM errors
docker logs secure-project-hub-app-1 | grep -i "out of memory"
```

## References
- [Docker Memory Limits Documentation](https://docs.docker.com/config/containers/resource_constraints/)
- [JVM Memory Configuration](https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html)
- [Spring Boot Docker Memory Optimization](https://spring.io/guides/gs/spring-boot-docker/)

## Date Modified
- **Implemented:** April 13, 2026
- **Status:** ✅ RESOLVED - No OOM errors observed
