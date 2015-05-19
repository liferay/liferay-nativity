set posixDestinationPath to "/Library/ScriptingAdditions/LiferayNativity.osax"
set destinationPath to (POSIX file posixDestinationPath) as text

tell application "Finder"
	delete destinationPath
end tell