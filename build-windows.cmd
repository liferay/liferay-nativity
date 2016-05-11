SET COMPONENT=%1
SET CODE_PATH=%2
SET FRAMEWORK_PATH=%4
SET Path=%FRAMEWORK_PATH%

SET PlatformToolset=Windows7.1SDK

MSBuild.exe %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Rebuild /p:Configuration=Release;Platform=x64
MSBuild.exe %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Rebuild /p:Configuration=Release;Platform=Win32