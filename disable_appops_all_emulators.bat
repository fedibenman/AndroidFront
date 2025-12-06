@echo off
echo Disabling AppOps restrictions for all emulators...
echo.

set ADB="C:\Users\chams\AppData\Local\Android\Sdk\platform-tools\adb.exe"

REM Get list of devices
for /f "tokens=1" %%d in ('%ADB% devices ^| findstr "emulator"') do (
    echo Configuring %%d...
    %ADB% -s %%d shell "appops set com.example.myapplication RECORD_AUDIO allow"
    %ADB% -s %%d shell "appops set com.example.myapplication CONTROL_AUDIO allow"
    %ADB% -s %%d shell "appops set com.example.myapplication CONTROL_AUDIO_PARTIAL allow"
    echo [OK] %%d configured successfully
    echo.
)

echo.
echo All emulators configured!
pause
