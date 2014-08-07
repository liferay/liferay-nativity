@ECHO OFF

SET COMPONENT=%1
SET CODE_PATH=%2

ECHO %COMPONENT% Building %CODE_PATH% with %TOOLKIT_PATH% and %FRAMEWORK_PATH%

SET CURRENT_CPU=x64

SET Configuration=Release

SET ORIGINALPATH=%PATH%
SET ORIGINAL_LIB=%LIB%
SET ORIGINAL_LIB_PATH=%LIBPATH%
SET ORIGINAL_INCLUDE=%INCLUDE%
 
SET TARGET_PLATFORM=WIN7

SET PlatformToolset=Windows7.1SDK

SET ToolsVersion=4.0

SET WindowsSDKVersionOverride=v7.1

SET RegKeyPath=HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Microsoft\VisualStudio\SxS\VC7

SET VSRegKeyPath=HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Microsoft\VisualStudio\SxS\VS7

SET WinSDKRegKeyPath=HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Microsoft SDKs\Windows\v7.1

SET WindowsSDKDir=%ProgramFiles%\Microsoft SDKs\Windows\v7.1\
SET "sdkdir=%WindowsSDKDir%"

FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%RegKeyPath%" /v FrameworkDir32') DO SET FrameworkDir32=%%B
FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%RegKeyPath%" /v FrameworkDir64') DO SET FrameworkDir64=%%B
FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%RegKeyPath%" /v FrameworkVer32') DO SET FrameworkVersion32=%%B
FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%RegKeyPath%" /v FrameworkVer64') DO SET FrameworkVersion64=%%B

SET FrameworkDir=%FrameworkDir64%
SET FrameworkVersion=%FrameworkVersion64%
SET Framework35Version=v3.5

SET CL=/AI %FrameworkDir%\%FrameworkVersion%

FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%RegKeyPath%" /v 10.0') DO SET VCINSTALLDIR=%%B
FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%VSRegKeyPath%" /v 10.0') DO SET VSINSTALLDIR=%%B

SET DevEnvDir=%VSINSTALLDIR%Common7\IDE
SET VSTools=%VSINSTALLDIR%Common7\IDE;%VSINSTALLDIR%Common7\Tools;
SET VCLibraries=%VCINSTALLDIR%Lib
SET VCIncludes=%VCINSTALLDIR%INCLUDE

SET OSLibraries=%WindowsSdkDir%Lib
SET OSIncludes=%WindowsSdkDir%INCLUDE;%WindowsSdkDir%INCLUDE\gl

ECHO Setting SDK environment relative to %WindowsSdkDir%.

SET "VCTools=%VCINSTALLDIR%Bin"

SET FRAMEWORK_PATH=%FrameworkDir%\%FrameworkVersion%


:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::Build x64

SET TARGET_CPU=x64

SET FRAMEWORK_PATH=%FrameworkDir%\%FrameworkVersion%

SET "VCTools=%VCINSTALLDIR%Bin"

SET "VCTools=%VCTools%\amd64;%VCTools%\VCPackages;"
SET "SdkTools=%WindowsSdkDir%Bin\NETFX 4.0 Tools\x64;%WindowsSdkDir%Bin\x64;%WindowsSdkDir%Bin;"

SET CommandPromptType=Native

SET Path=%FRAMEWORK_PATH%;%FRAMEWORK35%;%VSTools%;%VCTools%;%SdkTools%;%ORIGINALPATH%

SET LIB=%VCLibraries%;%OSLibraries%;%FxTools%;%LIB%
SET LIBPATH=%FxTools%;%VCLibraries%;%LIBPATH%
SET INCLUDE=%FRAMEWORK_PATH%;%VCIncludes%;%OSIncludes%;%INCLUDE%

SET INCLUDE=%VCINSTALLDIR%\ATLMFC\INCLUDE;%INCLUDE%
SET LIB=%VCINSTALLDIR%\ATLMFC\LIB\AMD64;%LIB%

SET LIB=%VCLibraries%\amd64;%OSLibraries%\X64;%LIB%
SET LIBPATH=%FxTools%;%VCLibraries%\amd64;%LIBPATH%
SET INCLUDE=%VCIncludes%;%OSIncludes%;%INCLUDE%

ECHO Targeting Windows 7 %TARGET_CPU% Release
ECHO.
SET APPVER=6.1
TITLE Microsoft Windows 7 %TARGET_CPU% Release Build Environment

MSBuild.exe %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Clean /p:Configuration=Release;Platform=x64
MSBuild.exe %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Rebuild /p:Configuration=Release;Platform=x64

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
SET TARGET_PLATFORM=WIN7
SET TARGET_CPU=x86

SET PlatformToolset=Windows7.1SDK

SET ToolsVersion=4.0

SET WindowsSDKVersionOverride=v7.1

SET RegKeyPath=HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Microsoft\VisualStudio\SxS\VC7

SET VSRegKeyPath=HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Microsoft\VisualStudio\SxS\VS7

SET WinSDKRegKeyPath=HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Microsoft SDKs\Windows\v7.1

SET WindowsSDKDir=%ProgramFiles%\Microsoft SDKs\Windows\v7.1\
SET "sdkdir=%WindowsSDKDir%"

FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%RegKeyPath%" /v FrameworkDir32') DO SET FrameworkDir32=%%B
FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%RegKeyPath%" /v FrameworkDir64') DO SET FrameworkDir64=%%B
FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%RegKeyPath%" /v FrameworkVer32') DO SET FrameworkVersion32=%%B
FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%RegKeyPath%" /v FrameworkVer64') DO SET FrameworkVersion64=%%B

SET FrameworkDir=%FrameworkDir64%
SET FrameworkVersion=%FrameworkVersion64%
SET Framework35Version=v3.5

SET CL=/AI %FrameworkDir%\%FrameworkVersion%

FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%RegKeyPath%" /v 10.0') DO SET VCINSTALLDIR=%%B
FOR /F "tokens=2* delims= " %%A IN ('REG QUERY "%VSRegKeyPath%" /v 10.0') DO SET VSINSTALLDIR=%%B

SET DevEnvDir=%VSINSTALLDIR%Common7\IDE
SET VSTools=%VSINSTALLDIR%Common7\IDE;%VSINSTALLDIR%Common7\Tools;
SET VCLibraries=%VCINSTALLDIR%Lib
SET VCIncludes=%VCINSTALLDIR%INCLUDE

SET OSLibraries=%WindowsSdkDir%Lib
SET OSIncludes=%WindowsSdkDir%INCLUDE;%WindowsSdkDir%INCLUDE\gl

ECHO Setting SDK environment relative to %WindowsSdkDir%.

SET "VCTools=%VCINSTALLDIR%Bin"

SET FRAMEWORK_PATH=%FrameworkDir%\%FrameworkVersion%

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::Build x86

echo ****************************************************************************
echo ****************************************************************************
echo ****************************************************************************
echo ****************************************************************************
echo ****************************************************************************
echo ****************************************************************************
echo ****************************************************************************
echo ****************************************************************************
echo ****************************************************************************


SET VCTools=%VCINSTALLDIR%Bin
SET LIB=%ORIGINAL_LIB%
SET LIBPATH=%ORIGINAL_PATH%
SET INCLUDE=%ORIGINAL_INCLUDE%

SET "VSINSTALLDIR=%ProgramFiles(x86)%\Microsoft Visual Studio 10.0\"

SET FRAMEWORK_PATH=%FrameworkDir32%%FrameworkVersion%
SET FRAMEWORK35=%FrameworkDir32%%Framework35Version%

SET DevEnvDir=%VSINSTALLDIR%Common7\IDE
SET VSTools=%VSINSTALLDIR%Common7\IDE;%VSINSTALLDIR%Common7\Tools;
SET VCLibraries=%VCINSTALLDIR%Lib
SET VCIncludes=%VCINSTALLDIR%INCLUDE
SET VCTools=%VCTools%;%VCTools%\VCPackages;
SET "SdkTools=%WindowsSdkDir%Bin\NETFX 4.0 Tools;%WindowsSdkDir%Bin;"

SET CommandPromptType=Cross

SET Path=%FRAMEWORK_PATH%;%FRAMEWORK35%;%VCTools%;%SdkTools%;%VSTOOLS%;%ORIGINALPATH%

SET "LIB=%FRAMEWORK_PATH%;%FRAMEWORK35%;%VCLibraries%;%OSLibraries%
SET "LIBPATH=%FRAMEWORK_PATH%;%FRAMEWORK35%;%VCLibraries%
SET "INCLUDE=%VCIncludes%;%OSIncludes%;

SET "LIB=%VCLibraries%;;%OSLibraries%;%LIB%"
SET "LIBPATH=%VCLibraries%;;%LIBPATH%"
SET "INCLUDE=%VCIncludes%;%OSIncludes%;%INCLUDE%"

ECHO Targeting Windows 7 %TARGET_CPU% Release
ECHO.
ECHO %PATH%
ECHO.
SET APPVER=6.1
TITLE Microsoft Windows 7 %TARGET_CPU% Release Build Environment

MSBuild.exe %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Clean /p:Configuration=Release;Platform=Win32
MSBuild.exe %CODE_PATH%\LiferayNativityShellExtensions\LiferayNativityShellExtensions.sln /t:%COMPONENT%:Rebuild /p:Configuration=Release;Platform=Win32
