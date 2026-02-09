@echo off
echo ============================================
echo EduLinguaGhana APK Installation Script
echo ============================================
echo.

cd /d C:\Users\user\AndroidStudioProjects\EduLinguaGhana

echo [Step 1/4] Checking APK exists...
if not exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ERROR: APK not found!
    echo Location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Please build the APK first:
    echo   gradlew.bat assembleDebug
    pause
    exit /b 1
)

echo ✓ APK found: app-debug.apk
for %%A in ("app\build\outputs\apk\debug\app-debug.apk") do (
    echo   Size: %%~zA bytes (12.15 MB)
)
echo.

echo [Step 2/4] Checking for ADB...
where adb >nul 2>&1
if errorlevel 1 (
    echo WARNING: ADB not found in PATH
    echo.
    echo Trying Android Studio SDK location...

    if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
        echo ✓ Found: Android Studio ADB
        goto :adb_found
    )

    if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
        set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
        echo ✓ Found: ANDROID_HOME ADB
        goto :adb_found
    )

    echo.
    echo ============================================
    echo ADB NOT FOUND
    echo ============================================
    echo.
    echo Please choose an installation method:
    echo.
    echo METHOD 1: Install via Android Studio
    echo   1. Open Android Studio
    echo   2. Open this project
    echo   3. Click Run (green play button)
    echo   4. Select device
    echo.
    echo METHOD 2: Manual Installation
    echo   1. Copy APK to your phone:
    echo      app\build\outputs\apk\debug\app-debug.apk
    echo   2. On phone: Enable "Unknown sources" in Settings
    echo   3. Tap APK file to install
    echo.
    echo METHOD 3: Install ADB
    echo   Download: https://developer.android.com/studio/releases/platform-tools
    echo   Then run this script again
    echo.
    echo See: APK_INSTALLATION_GUIDE.md for detailed instructions
    echo ============================================
    pause
    exit /b 1
) else (
    set "ADB=adb"
    echo ✓ ADB found in PATH
)

:adb_found
echo.

echo [Step 3/4] Checking for connected devices...
"%ADB%" devices | findstr /R "device$" >nul
if errorlevel 1 (
    echo.
    echo ============================================
    echo NO DEVICES DETECTED
    echo ============================================
    echo.
    echo Please do ONE of the following:
    echo.
    echo OPTION A: Connect Physical Device
    echo   1. Enable USB debugging on device:
    echo      Settings → About Phone → Tap "Build number" 7 times
    echo      Settings → Developer Options → Enable "USB debugging"
    echo   2. Connect device via USB cable
    echo   3. Accept "Allow USB debugging" prompt on device
    echo   4. Run this script again
    echo.
    echo OPTION B: Start Android Emulator
    echo   1. Open Android Studio
    echo   2. Tools → Device Manager
    echo   3. Start an emulator
    echo   4. Run this script again
    echo.
    echo OPTION C: Manual Installation
    echo   See: APK_INSTALLATION_GUIDE.md
    echo ============================================

    echo.
    echo Connected devices:
    "%ADB%" devices
    echo.
    pause
    exit /b 1
)

echo.
echo ✓ Device(s) detected:
"%ADB%" devices | findstr /V "List"
echo.

echo [Step 4/4] Installing APK...
echo This may take 30-60 seconds...
echo.

"%ADB%" install -r "app\build\outputs\apk\debug\app-debug.apk"

if errorlevel 1 (
    echo.
    echo ============================================
    echo INSTALLATION FAILED
    echo ============================================
    echo.
    echo Common solutions:
    echo.
    echo 1. If "INSTALL_FAILED_UPDATE_INCOMPATIBLE":
    echo    Uninstall old version first:
    echo      %ADB% uninstall com.edulinguaghana
    echo    Then run this script again
    echo.
    echo 2. If "INSTALL_FAILED_INSUFFICIENT_STORAGE":
    echo    Free up space on device (need ~50MB)
    echo.
    echo 3. If "INSTALL_PARSE_FAILED":
    echo    Rebuild APK:
    echo      gradlew.bat clean assembleDebug
    echo    Then run this script again
    echo.
    echo See: APK_INSTALLATION_GUIDE.md for more troubleshooting
    echo ============================================
    pause
    exit /b 1
)

echo.
echo ============================================
echo ✅ INSTALLATION SUCCESSFUL!
echo ============================================
echo.
echo App installed: EduLinguaGhana
echo Package: com.edulinguaghana
echo.
echo NEXT STEPS:
echo.
echo 1. Deploy Firebase Rules (CRITICAL!)
echo    File: firebase-database-rules.json
echo    → Firebase Console → Realtime Database → Rules → Publish
echo.
echo 2. Launch app on device
echo    Look for "EduLinguaGhana" in app drawer
echo.
echo 3. Create test accounts:
echo    - student@edulingua.test (Student)
echo    - teacher@edulingua.test (Teacher)
echo    - parent@edulingua.test (Parent)
echo.
echo 4. Set roles in Firebase Database:
echo    users/{userId}/role = "TEACHER" or "PARENT" or "STUDENT"
echo.
echo 5. Test real-time features!
echo    Student completes quiz → Teacher sees update instantly!
echo.
echo See: BUILD_SUCCESSFUL_FINAL_REPORT.md for testing guide
echo ============================================
echo.

choice /C YN /M "Launch app now"
if errorlevel 2 goto :end
if errorlevel 1 (
    echo.
    echo Launching app...
    "%ADB%" shell am start -n com.edulinguaghana/.MainActivity
    echo.
    echo App should open on your device!
)

:end
pause

