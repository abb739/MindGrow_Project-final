@echo off
set "JAVA_HOME=C:\Users\21658\.jdks\jbr-17.0.12"
set "BIN_DIR=%~dp0target\classes"
set "LIB_DIR=C:\Users\21658\.m2\repository"

echo [1/3] Compiling AutoTestRunner...
"%JAVA_HOME%\bin\javac" -d "%BIN_DIR%" -cp "%BIN_DIR%;%LIB_DIR%\org\openjfx\javafx-controls\17.0.2\javafx-controls-17.0.2.jar;%LIB_DIR%\org\openjfx\javafx-base\17.0.2\javafx-base-17.0.2.jar;%LIB_DIR%\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar" src\main\java\org\example\utils\AutoTestRunner.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/3] Running Automated Tests...
"%JAVA_HOME%\bin\java" -cp "%BIN_DIR%;%LIB_DIR%\org\openjfx\javafx-controls\17.0.2\javafx-controls-17.0.2.jar;%LIB_DIR%\org\openjfx\javafx-base\17.0.2\javafx-base-17.0.2.jar;%LIB_DIR%\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar;%LIB_DIR%\com\google\protobuf\protobuf-java\3.25.1\protobuf-java-3.25.1.jar;%LIB_DIR%\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar;%LIB_DIR%\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar" org.example.utils.AutoTestRunner

echo [3/3] Done.
pause
