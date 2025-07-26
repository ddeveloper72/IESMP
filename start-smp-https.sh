#!/bin/bash
# DomiSMP Spring Boot Startup Script
# This script starts the DomiSMP server with HTTPS support

echo "Starting DomiSMP Server with HTTPS support..."
echo

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Set the configuration file location (relative to script location)
SMP_CONFIG_FILE="$SCRIPT_DIR/smp-config/smp.config.properties"

# Set Java options for SMP
JAVA_OPTS="-Xms512m -Xmx2048m"
JAVA_OPTS="$JAVA_OPTS -Dsmp.config.file=$SMP_CONFIG_FILE"
JAVA_OPTS="$JAVA_OPTS -Dspring.config.location=file:$SMP_CONFIG_FILE"
JAVA_OPTS="$JAVA_OPTS -Djava.net.useSystemProxies=true"

# Change to script directory
cd "$SCRIPT_DIR"

# Start the SMP server
echo "Starting SMP server on HTTPS port 8443..."
echo "Configuration file: $SMP_CONFIG_FILE"
echo
echo "The server will be available at:"
echo "  - HTTPS: https://localhost:8443/"
echo "  - Admin UI: https://localhost:8443/ui/"
echo
echo "Press Ctrl+C to stop the server"
echo

java $JAVA_OPTS -jar "smp-springboot/target/smp-springboot-5.2-SNAPSHOT-exec.jar"
