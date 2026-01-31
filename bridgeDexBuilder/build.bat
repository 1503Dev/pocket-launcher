@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Java to DEX Compiler, Created by 1503Dev
echo ========================================

set JAVA_DIR=.\java
set OUTPUT_DIR=.\output
set CLASS_DIR=.\classes
set LIB_DIR=.\libs
set DEX_FILE=bridge.dex
set TEMP_JAR=bridge.jar

set ANDROID_SDK=D:\Android\Sdk
set BUILD_TOOLS_VERSION=36.0.0
set PLATFORM_VERSION=36
set MIN_API=26

set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

if not exist "%ANDROID_SDK%\build-tools\%BUILD_TOOLS_VERSION%\d8.bat" (
    echo Error: Could not find d8.bat in %ANDROID_SDK%\build-tools\%BUILD_TOOLS_VERSION%
    pause
    exit /b 1
)

if not exist "%ANDROID_SDK%\platforms\android-%PLATFORM_VERSION%\android.jar" (
    echo Error: Could not find android.jar in %ANDROID_SDK%\platforms\android-%PLATFORM_VERSION%
    pause
    exit /b 1
)

set D8="%ANDROID_SDK%\build-tools\%BUILD_TOOLS_VERSION%\d8.bat"
set ANDROID_JAR="%ANDROID_SDK%\platforms\android-%PLATFORM_VERSION%\android.jar"

if exist %OUTPUT_DIR% rmdir /s /q %OUTPUT_DIR%
if exist %CLASS_DIR% rmdir /s /q %CLASS_DIR%
mkdir %OUTPUT_DIR%
mkdir %CLASS_DIR%

echo Searching for Java files in %JAVA_DIR%...
set JAVA_FILES=
set COUNT=0
for /r "%JAVA_DIR%" %%f in (*.java) do (
    set JAVA_FILES=!JAVA_FILES! "%%f"
    set /a COUNT+=1
    echo   %%f
)

if %COUNT% EQU 0 (
    echo Error: No Java files found in %JAVA_DIR%
    pause
    exit /b 1
)
echo Found %COUNT% Java files in %JAVA_DIR%

echo.
echo Compiling Java files...

set CLASSPATH=%ANDROID_JAR%
if exist "%LIB_DIR%" (
    for %%f in ("%LIB_DIR%\*.jar") do (
        set CLASSPATH=!CLASSPATH!;"%%f"
    )
)

javac -source 1.8 -target 1.8 -encoding UTF-8 -d "%CLASS_DIR%" -cp "%CLASSPATH%" %JAVA_FILES%
if errorlevel 1 (
    echo Error: Java compilation failed
    pause
    exit /b 1
)
echo Compilation completed successfully.

echo.
echo Checking for class files in %CLASS_DIR%...
dir "%CLASS_DIR%" /s /b | findstr "\.class$" >nul
if errorlevel 1 (
    echo Error: No class files found in %CLASS_DIR%
    echo Directory structure:
    tree "%CLASS_DIR%" /f
    pause
    exit /b 1
)

set /a CLASS_COUNT=0
for /r "%CLASS_DIR%" %%f in (*.class) do (
    set /a CLASS_COUNT+=1
)
echo Found !CLASS_COUNT! class files.

echo.
echo Creating JAR from compiled classes only...
cd "%CLASS_DIR%"
"C:\Program Files\Java\jdk-21\bin\jar" cf "%TEMP_JAR%" *
cd ..

echo .
echo Compiling JAR to DEX (libs as compile-only dependencies)...

set PROGUARD_FILE=proguard_rules.txt
echo -dontwarn > "%PROGUARD_FILE%"
echo -ignorewarnings >> "%PROGUARD_FILE%"
echo -keepattributes *Annotation* >> "%PROGUARD_FILE%"
echo -keep class !com.yourpackage.** { *; } >> "%PROGUARD_FILE%"
echo -keep class com.yourpackage.** { *; } >> "%PROGUARD_FILE%"

set CLASSPATH_FOR_D8=%ANDROID_JAR%
if exist "%LIB_DIR%" (
    for %%f in ("%LIB_DIR%\*.jar") do (
        set CLASSPATH_FOR_D8=!CLASSPATH_FOR_D8! --classpath "%%f"
    )
)

%D8% "%CLASS_DIR%\%TEMP_JAR%" --classpath %CLASSPATH_FOR_D8% --min-api %MIN_API% --output "%OUTPUT_DIR%" --release

if errorlevel 1 (
    echo Method 1 failed, trying method 2...
    set LIB_PATHS=%ANDROID_JAR%
    if exist "%LIB_DIR%" (
        for %%f in ("%LIB_DIR%\*.jar") do (
            set LIB_PATHS=!LIB_PATHS! "%%f"
        )
    )
    %D8% "%CLASS_DIR%\%TEMP_JAR%" --lib %LIB_PATHS% --min-api %MIN_API% --output "%OUTPUT_DIR%" --release --no-desugaring
)

if errorlevel 1 (
    echo Error: Failed to compile JAR to DEX
    if exist "%PROGUARD_FILE%" del "%PROGUARD_FILE%"
    pause
    exit /b 1
)

echo DEX compilation successful
if exist "%PROGUARD_FILE%" del "%PROGUARD_FILE%"

echo.
if exist "%OUTPUT_DIR%\classes.dex" (
    echo Found classes.dex, renaming to %DEX_FILE%...
    if exist "%OUTPUT_DIR%\%DEX_FILE%" del "%OUTPUT_DIR%\%DEX_FILE%"
    move /Y "%OUTPUT_DIR%\classes.dex" "%OUTPUT_DIR%\%DEX_FILE%" >nul
    echo DEX file created: %OUTPUT_DIR%\%DEX_FILE%
    
    for %%I in ("%OUTPUT_DIR%\%DEX_FILE%") do (
        set DEX_SIZE=%%~zI
    )
    echo DEX file size: !DEX_SIZE! bytes
    
    echo.
    echo Checking DEX contents...
    "%ANDROID_SDK%\build-tools\%BUILD_TOOLS_VERSION%\dexdump" -d "%OUTPUT_DIR%\%DEX_FILE%" | findstr /i "\.class.*def" | findstr /v "com.yourpackage" > nul
    if errorlevel 1 (
        echo ? DEX file contains only your package classes
    ) else (
        echo ?  Warning: DEX file may contain external library classes
        echo    Run manually: "%ANDROID_SDK%\build-tools\%BUILD_TOOLS_VERSION%\dexdump" -d "%OUTPUT_DIR%\%DEX_FILE%"
    )
    
) else (
    echo Error: No classes.dex file created
    echo Current directory contents:
    dir "%OUTPUT_DIR%"
)

echo.
echo Cleaning up temporary files...
if exist "%CLASS_DIR%\%TEMP_JAR%" del "%CLASS_DIR%\%TEMP_JAR%"
if exist "%CLASS_DIR%" rmdir /s /q %CLASS_DIR% 2>nul

sleep 1
copy ".\output\classes.dex" "..\app\src\main\assets\_pocket_launcher\bridge.dex" /Y

echo.
echo ========================================
echo Compilation completed successfully!
echo DEX file size: !DEX_SIZE! bytes
echo ========================================
echo Note: libs/*.jar are compile-time dependencies ONLY
echo       They are NOT included in the final DEX
echo ========================================
pause
endlocal