@echo off
echo Compiling Java files...
javac *.java
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)
echo Compilation successful! Running game...
java Main
pause
