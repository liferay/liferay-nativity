# LiferayNativity.osax

This code injects LiferayNativity into Finder. The code is modified from the scripting additions used by TotalFinder. The original source code is available here: https://github.com/binaryage/totalfinder-osax.

## NVTYinst event

Installs LiferayNativity into the running Finder.app.

    tell application "Finder"
        try
            «event NVTYinst»
        end try
    end tell

## NVTYunin event

Uninstalls LiferayNativity from the running Finder.app. Note, this does not actually remove the injected bundle but rather reverses all method swizzling and frees memory used by LiferayNativityCore.

    tell application "Finder"
        try
            «event NVTYunin»
        end try
    end tell

## NVTYchck event

Check if LiferayNativity is installed in the running Finder.app. Returns 0 if enabled, the error number otherwise.

    tell application "Finder"
        try
            «event NVTYchck»
            set the result to 0
        on error msg number code
            set the result to code
        end try
    end tell