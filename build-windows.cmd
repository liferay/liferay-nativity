SET COMPONENT=%1
SET CODE_PATH=%2
SET FRAMEWORK_PATH=%4
SET Path=%FRAMEWORK_PATH%

SET PlatformToolset=10.0.14393.0

"C:\Program Files (x86)\MSBuild\14.0\Bin\MSBuild.exe" %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Rebuild /p:Configuration=Release;Platform=x64
"C:\Program Files (x86)\MSBuild\14.0\Bin\MSBuild.exe" %CODE_PATH%/LiferayNativityShellExtensions/LiferayNativityShellExtensions.sln /t:%COMPONENT%:Rebuild /p:Configuration=Release;Platform=Win32