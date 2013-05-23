set the plistfile_path to "{0}/Contents/Info.plist"

tell application "System Events"
	try
		set p_list to property list file (plistfile_path)
		value of property list item "CFBundleVersion" of p_list
	on error msg number code
		set the result to ""
	end try
end tell
