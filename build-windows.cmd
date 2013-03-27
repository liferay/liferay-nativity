@ECHO OFF

SET COMPONENT=%1
SET CODE_PATH=%2
SET TOOLKIT_PATH=%3
SET FRAMEWORK_PATH=%4

ECHO %COMPONENT% Building %CODE_PATH% with %TOOLKIT_PATH% and %FRAMEWORK_PATH%

SET CL=/AI FRAMEWORK_PATH

PATH=%PATH%;%TOOLKIT_PATH%;%FRAMEWORK_PATH%

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::Build x64

ECHO Building x64 Release

MSBuild.exe %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Clean /p:Configuration=Release;Platform=x64
MSBuild.exe %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Rebuild /p:Configuration=Release;Platform=x64

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::Build x86

ECHO Building x64 Release

MSBuild.exe %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Clean /p:Configuration=Release;Platform=Win32
MSBuild.exe %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Rebuild /p:Configuration=Release;Platform=Win32
