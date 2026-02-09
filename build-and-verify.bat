@echo off
echo ================================================
echo EduLinguaGhana Build Verification Script
echo ================================================
echo.

cd /d C:\Users\user\AndroidStudioProjects\EduLinguaGhana

echo [1/3] Checking Gradle...
call gradlew.bat --version
if errorlevel 1 (
    echo ERROR: Gradle not found or not working
    pause
    exit /b 1
)

echo.
echo [2/3] Starting build...
echo This may take 2-5 minutes...
echo.

call gradlew.bat clean assembleDebug --console=plain

if errorlevel 1 (
    echo.
    echo ============================================
    echo BUILD FAILED
    echo ============================================
    echo Check the error messages above
    pause
    exit /b 1
) else (
    echo.
    echo ============================================
    echo BUILD SUCCESSFUL!
    echo ============================================
    echo.
    echo [3/3] Checking APK...
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        echo.
        echo APK Generated Successfully!
        echo Location: app\build\outputs\apk\debug\app-debug.apk
        echo.
        for %%A in ("app\build\outputs\apk\debug\app-debug.apk") do (
            echo Size: %%~zA bytes
        )
        echo.
        echo ============================================
        echo READY FOR TESTING!
        echo ============================================
        echo.
        echo Next steps:
        echo 1. Deploy Firebase rules from firebase-database-rules.json
        echo 2. Install APK: adb install app\build\outputs\apk\debug\app-debug.apk
        echo 3. Test with 3 accounts ^(student, teacher, parent^)
        echo.
    ) else (
        echo ERROR: APK file not found!
    )
    pause
)

