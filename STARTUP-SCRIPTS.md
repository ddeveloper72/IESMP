# DomiSMP Startup Scripts

This directory contains platform-specific startup scripts for the DomiSMP service.

## Scripts Available

- `start-smp-https.bat` / `start-smp-https.sh` - Start SMP with HTTPS support
- `start-smp-http.bat` / `start-smp-http.sh` - Start SMP with HTTP support  
- `start-smp.bat` - Basic startup script

## Usage

All scripts use relative paths and will work from any directory where the DomiSMP project is cloned.

### Windows

```bash
start-smp-https.bat
```

### Linux/Mac/Git Bash

```bash
./start-smp-https.sh
```

## Configuration

The scripts automatically locate the configuration file at:

- `./smp-config/smp.config.properties` (relative to script location)

No hardcoded paths are used, making the scripts portable across different user environments and operating systems.

## Environment Variables

For additional customization, you can set these environment variables before running the scripts:

- `JAVA_OPTS` - Additional Java options
- `SMP_CONFIG_FILE` - Override the default config file location

## Docker Alternative

For containerized deployment, use:

```bash
docker-compose up
```

This provides a consistent environment across all platforms.
