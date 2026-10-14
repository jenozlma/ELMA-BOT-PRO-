@echo off
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (gradle %*) else (echo Gradle n'est pas installe. Ouvrez le projet dans Android Studio. & exit /b 127)
