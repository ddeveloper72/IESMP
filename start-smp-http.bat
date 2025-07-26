@echo off
echo Starting DomiSMP Server with HTTP support...
echo.

set SMP_CONFIG_FILE=.\smp-config\smp.config.properties

set JAVA_OPTS=-Xms512m -Xmx2048m
set JAVA_OPTS=%JAVA_OPTS% -Dsmp.configuration.file=%SMP_CONFIG_FILE%
set JAVA_OPTS=%JAVA_OPTS% -Dspring.main.web-application-type=servlet
set JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.io=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.lang=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED

echo Starting SMP server on HTTP port 8080...
echo Configuration file: %SMP_CONFIG_FILE%
echo.
echo The server will be available at:
echo   - HTTP: http://localhost:8080/smp/
echo   - Admin UI: http://localhost:8080/smp/ui/
echo.
echo Press Ctrl+C to stop the server
echo.

java %JAVA_OPTS% -jar "smp-springboot\target\smp-springboot-5.2-SNAPSHOT-exec.jar"

echo.
echo Server has stopped.
pause
