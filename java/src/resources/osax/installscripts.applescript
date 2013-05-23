set posixSourcePath to "{0}"
set sourcePath to (POSIX file posixSourcePath) as text
set posixDestinationPath to "/Library/ScriptingAdditions"
set destinationPath to (POSIX file posixDestinationPath) as text

tell application "Finder"
	duplicate sourcePath to destinationPath with replacing
end tell