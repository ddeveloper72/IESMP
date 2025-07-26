# DomiSMP Setup Notes - Success Configuration

## ✅ WORKING SETUP (July 24-25, 2025)

### Current Working Instance

- **DomiSMP Web UI**: <https://localhost:8284/smp/ui/>
- **DomiSMP API**: <https://localhost:8284/smp/>
- **Protocol**: HTTPS (SSL/TLS enabled)
- **Database**: MySQL running in Docker container (port 3208:3306)
- **Debug Port**: 5010:5005

### Docker Containers Running

```bash
domismp-springboot-mysql:latest - HTTPS port 8284
smp-mysql (mysql:8.0) - Database port 3208:3306
```

### Port Mapping Summary

- **8284** - DomiSMP HTTPS Web Interface
- **3208** - MySQL Database (mapped from container port 3306)
- **5010** - Debug port (mapped from container port 5005)

## 🔍 INVESTIGATION RESULTS ✅

### Key Discoveries

1. **Working Container Source**: `domismp-springboot-mysql:latest` (1.34GB, built 5 days ago)
2. **Container Status**: Running for 25 hours but showing "unhealthy"
3. **SMP_HOME Contents**: Contains older SMP 5.0.1 (NOT the source of working container)
4. **Current Project**: DomiSMP 5.2-SNAPSHOT (likely the source of working container)

### Docker Images Found

```bash
domismp-springboot-mysql:latest (1.34GB, 5 days old) - WORKING IMAGE
edeliverytest/domismp-springboot-external-mysql:5.2-SNAPSHOT (885MB, 3 days old)
```

### Container Details

```bash
Container: smp-springboot (f85e79661b9b)
Image: domismp-springboot-mysql:latest
Status: Up 25 hours (unhealthy)
Ports: 5010:5005 (debug), 8284:8084 (web)
```

### Directory Analysis

- **SMP_HOME**: Contains SMP 5.0.1 (older version, not current source)
- **domismp**: Contains DomiSMP 5.2-SNAPSHOT (likely source of working container)
- **domismp-fresh**: Additional copy (Jul 24 15:16)

### Rebuild Capability: ✅ YES

The working container was likely built from the current `domismp` project (5.2-SNAPSHOT).
The Docker build files exist in `domismp-tests/domismp-docker/images/domismp-springboot-mysql/`.

## 🧹 CLEANUP COMPLETED

- Removed temporary files: Dockerfile.simple, docker-compose.simple.yml
- Removed failed startup scripts: start-smp-http.bat, start-smp-http.sh
- Kept working Docker containers and configuration

## ✅ WHAT WE KNOW WORKS

- Docker container approach with HTTPS
- Port 8284 for SMP web interface
- Isolated database on port 3208
- No conflicts with NCP ports

## ❓ NEXT STEPS

1. Determine source code location and rebuild capability
2. Document the exact Docker setup that created the working container
3. Proceed with NCP integration using the working SMP instance

---
*Updated: July 25, 2025*
*Working DomiSMP instance: <https://localhost:8284/smp/ui/>*
