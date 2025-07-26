@echo off
echo ==========================================
echo Starting DomiSMP Server
echo ==========================================
echo.
echo Current directory: %CD%
echo.
echo Checking Java version...
java -version
echo.
echo Checking if JAR file exists...
if exist "smp-springboot\target\smp-springboot-5.2-SNAPSHOT-exec.jar" (
    echo JAR file found: smp-springboot\target\smp-springboot-5.2-SNAPSHOT-exec.jar
) else (
    echo ERROR: JAR file not found!
    pause
    exit /b 1
)
echo.
echo Checking configuration file...
if exist "smp-config\smp.config.properties" (
    echo Config file found: smp-config\smp.config.properties
) else (
    echo ERROR: Configuration file not found!
    pause
    exit /b 1
)
echo.
echo Starting DomiSMP server...
echo Server will be available at: http://localhost:8080/smp/
echo Press Ctrl+C to stop the server
echo.
echo ==========================================
echo SERVER STARTUP MESSAGES:
echo ==========================================
java -Dsmp.configuration.file=./smp-config/smp.config.properties -jar smp-springboot/target/smp-springboot-5.2-SNAPSHOT-exec.jar
echo.
echo ==========================================
echo Server has stopped
echo ==========================================
pause
