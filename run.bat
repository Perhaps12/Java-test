@echo off
echo Compiling Java files..
javac *.java -d bin
if errorlevel 1 (
    echo compilation failed!
    pause
    exit /b 1
)

echo compiling to bin...
if not exist "bin\Sprites" mkdir "bin\Sprites"
if not exist "bin\textures" mkdir "bin\textures"
if not exist "bin\Static" mkdir "bin\Static"

xcopy /E /Y "Sprites\*" "bin\Sprites\" >nul 2>&1
xcopy /E /Y "textures\*" "bin\textures\" >nul 2>&1
xcopy /E /Y "Static\*" "bin\Static\" >nul 2>&1

echo compiled, running...
java -cp bin Main
pause
