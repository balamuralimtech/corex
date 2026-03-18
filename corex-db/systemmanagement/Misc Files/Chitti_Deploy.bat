echo JAVA_HOME=%JAVA_HOME%

rem Execute the shutdown.bat to stop Apache Tomcat
echo Shutting down Apache Tomcat...
call "D:\Postie\Servers\apache-tomcat-9.0.31\bin\shutdown.bat"

rem Check if Tomcat process is still running (look for java.exe or another Tomcat process identifier)
:checkTomcat
tasklist /FI "IMAGENAME eq java.exe" | find /I "java.exe" >nul
if "%ERRORLEVEL%"=="0" (
    echo Apache Tomcat is still stopping...
    timeout /t 2 /nobreak >nul
    goto checkTomcat
) else (
    echo Apache Tomcat has stopped.
)



rem Delete the coretix.war file from webapps
del /q "D:\Postie\Servers\apache-tomcat-9.0.31\webapps\coretix.war"

rem Delete the coretix folder from webapps
rd /s /q "D:\Postie\Servers\apache-tomcat-9.0.31\webapps\coretix"

rd /s /q "D:\Postie\Servers\apache-tomcat-9.0.31\logs"
mkdir "D:\Postie\Servers\apache-tomcat-9.0.31\logs"

rem Echo refresh message
echo Refreshing target folder (similar to F5)

rem Simulate refresh by listing contents of target folder
dir "D:\Postie\Projects\BMSolutions\coretix-web\target"

rem Echo copy message
echo Copying coretix.war to apache-tomcat webapps folder

rem Copy the new coretix.war to the webapps folder
copy /y "D:\Postie\Projects\BMSolutions\coretix-web\target\coretix.war" "D:\Postie\Servers\apache-tomcat-9.0.31\webapps\coretix.war"

rem Optional: Echo success message when done
echo coretix.war copied successfully to apache-tomcat webapps folder.

rem Start Apache Tomcat
echo Starting Apache Tomcat...
call "D:\Postie\Servers\apache-tomcat-9.0.31\bin\startup.bat" > "D:\Postie\Servers\apache-tomcat-9.0.31\logs\startup_log.txt" 2>&1

rem Optional: Echo success message for Tomcat startup
echo Apache Tomcat started successfully.


rem Wait for Tomcat to start by checking for java.exe
:waitForTomcat
timeout /t 2 /nobreak >nul
tasklist /FI "IMAGENAME eq java.exe" | find /I "java.exe" >nul
if "%ERRORLEVEL%"=="0" (
    echo Apache Tomcat has started successfully.
) else (
    echo Waiting for Apache Tomcat to start...
    goto waitForTomcat
)

rem Open Chrome and navigate to the application
start chrome "http://localhost:8080/coretix/"