# Liferay Nativity

**Table of Contents**

- [Introduction](#introduction)
- [Native Plugins](#native-plugins)
	- [Mac OS X](#mac-os-x)
		- [Build](#build)
		- [Deployment](#deployment)
			- [NVTYload event](#nvtyload-event)
			- [NVTYunld event](#nvtyunld-event)
			- [NVTYlded event](#nvtylded-event)
		- [Code Architecture](#code-architecture)
			- [LiferayNativityInjector](#liferaynativityinjector)
			- [LiferayNativityFinder](#liferaynativityfinder)
	- [Windows](#windows)
	- [Linux](#linux)
- [Java Client](#java-client)
- [Example Code](#example-code)
- [Issue Tracking](#issue-tracking)
- [Licensing](#licensing)
- [Contact](#contact)


# Introduction

Liferay Nativity is a cross-platform library for adding icon overlays and context menus to file browsers.

The following operating systems are currently supported:

* Windows Vista or greater (tested up to Windows 8)
* Mac OS X 10.7 or greater (tested up to OS X 10.8.4)
* Linux GNOME Nautilus 3.x or greater (tested up to Nautilus 3.6)

Currently the client code is only available for Java. The following clients are currently supported. Contributions for other clients like Ruby, C++, etc are welcome.

<img width="500" src="https://raw.github.com/liferay/liferay-nativity/master/extra/screenshot-mac.png">

# Native Plugins

## Mac OS X

There is no official API for custom file overlays and context menus in Finder, so LiferayNativity uses a technique called "[method swizzling](http://cocoadev.com/wiki/MethodSwizzling)" to swap Finder's code with our own custom code.

The LiferayNativityFinder bundle is responsible for "method swizzling" the code for icon overlays and context menus as well as handling client requests. LiferayNativityInjector is a scripting addition responsible for injecting the LiferayNativityFinder bundle into the running instance of Finder.

**Note:** Since method swizzling into Finder is not supported by Apple, any upgrade to Finder can break LiferayNativity's injected code. LiferayNativityInjector has an optional version check that can throw a warning if a newer, untested version of Finder is detected. Also, buggy injected code can cause Finder to crash or hang, so proceed with caution!

### Build

After cloning the liferay-nativity github project, both LiferayNativityInjector and LiferayNativityFinder XCode projects should build without errors. The LiferayNativityFinder bundle should be referenced and copied by LiferayNativityInjector's project. LiferayNativity.osax is the scripting addition target built by LiferayNativityInjector.

### Deployment

Copy LiferayNativity.osax (LiferayNativityInjector's target) into /Library/ScriptingAdditions (this will prompt for root privileges). Alternatively, LiferayNativity.osax can be copied into the user's ~/Library/ScriptingAdditions but is not recommended because each request to run a script will prompt for root privileges.

After the scripts have been copied, the following scripting commands will be available. Send the NVTYload event using the example below to inject the LiferayNativityFinder bundle into the running instance of Finder.

#### NVTYload event

Loads LiferayNativityFinder into the running Finder.app.

    tell application "Finder"
        try
            «event NVTYload»
        end try
    end tell

#### NVTYunld event

Unloads LiferayNativityFinder from the running Finder.app. Note, this does not actually remove the injected bundle but rather reverses all method swizzling and frees memory used by LiferayNativityFinder.

    tell application "Finder"
        try
            «event NVTYunld»
        end try
    end tell

#### NVTYlded event

Check if LiferayNativityFinder is installed in the running Finder.app. Returns 0 if enabled, the error number otherwise.

    tell application "Finder"
        try
            «event NVTYlded»
            set the result to 0
        on error msg number code
            set the result to code
        end try
    end tell

### Code Architecture

#### LiferayNativityInjector

The code for LiferayNativityInjector is modified from the scripting additions used by [TotalFinder](http://totalfinder.binaryage.com/). The original source code is available [here](https://github.com/binaryage/totalfinder-osax). This method proved much simpler than using SIMBL or mach_inject. You can see our previous injection method using a combination of mach_inject and a priviliged helper tool installed by SMJobBless by checking out a commit *before* [c82910f1b3](https://github.com/liferay/liferay-nativity/commit/c82910f1b302fae012e52b9021b3ddaf8224c527). Many thanks to BinaryAge for open sourcing their injection code!

#### LiferayNativityFinder

Once injected, LiferayNativityFinder is responsible for method swizzling the overlay icons and context menus into Finder. The original source code for method swizzling (as well as the previously used injection method through mach_inject) was written by the talented developers at [TeamDev](http://www.teamdev.com/). Kudos to TeamDev for tackling an extremely challenging programming task!

Below is a brief description of the classes inside LiferayNativityFinder.

* **[GCDAsyncSocket](https://github.com/robbiehanson/CocoaAsyncSocket)** - Used for interprocess communication with the client.
* **[JSONKit](https://github.com/johnezang/JSONKit)** - Used to encapsulate messages sent across sockets.
* **FinderHook** - Responsible for method swizzling. Replaces the methods needed for icon overlays and context menus.
* **ContextMenuHandler** - Swizzled methods for context menus.
* **IconOverlayHander** - Swizzled methods for icon overlays.
* **RequestManager** - Manager for receiving and sending commands to the client.
* **ContentManager** - Provides functions for associating icon overlays with files.
* **IconCache** - Manages registered icon overlays.
* **MenuManager** - Provides functions for associating custom menu items to selected files.

## Windows

*Instructions coming soon*

## Linux

*Instructions coming soon*

# Java Client

The clients communicate with the Native Plugins via JSON messages over sockets.

The java client can be distributed as a jar file. Run the ant task "build-jar" to automate building the jar file. Build properties are configured in build.properties. You can override build.properties with user-specific values by creating a build.\<username\>.properties file where \<username\> is your computer account name (e.g.: build.dennisju.properties).

The key classes for the java client are:

* Package **com.liferay.nativity.control**
	* **NativityControl** - Controller for interacting with the native plugin. Required for all other modules. This class is documented in the source.
	* **NativityControlUtil** - Utility for returning a NativityControl instance.
* Package **com.liferay.nativity.modules.contextmenu**
	* **ContextMenuControl** - Controller module for interacting with context menus. This class is documented in the source.
	* **ContextMenuControlUtil** - Utility for returning a ContextMenuControl instance.
	* **ContextMenuControlCallback** - Callback class that must be implemented to respond to context menu requests.
* Package **com.liferay.nativity.modules.fileicon**
	* **FileIconControl** - Controller module for interacting with file icon overlays. This class is documented in the source.
	* **FileIconControlUtil** - Utility for returning a FileIconControl instance.
	* **FileIconControlCallback** - Callback class that must be implemented to respond to file icon requests. (Currently only needed for Windows)

# Example Code

The following example Java code will overlay testFile.txt with testIcon.icns and create a context menu item titled "Nativity Test".

	NativityControl nativityControl = NativityControlUtil.getNativityControl();

	nativityControl.connect();

	/* File Icons */

	// FileIconControlCallback only used by Windows
	FileIconControlCallback fileIconControlCallback = new FileIconControlCallback() {
		@Override
		public int getIconForFile(String path) {
			return 1;
		}
	};

	FileIconControl fileIconControl = FileIconControlUtil.getFileIconControl(
		nativityControl, fileIconControlCallback);

	fileIconControl.enableFileIcons();

	String testFilePath = "/Users/liferay/Desktop/testFile.txt";
	int testIconId = fileIconControl.registerIcon("/Users/liferay/Desktop/testIcon.icns");

	// FileIconControl.setFileIcon() method only used by Mac and Linux
	fileIconControl.setFileIcon(testFilePath, testIconId);

	/* Context Menus */

	ContextMenuControlCallback contextMenuControlCallback = new ContextMenuControlCallback() {
		@Override
		public List<ContextMenuItem> getContextMenuItems(String[] paths) {
			ContextMenuItem contextMenuItem = new ContextMenuItem("Nativity Test");

			ContextMenuAction contextMenuAction = new ContextMenuAction() {
				@Override
				public void onSelection(String[] paths) {
					for (String path : paths) {
						System.out.print(path + ", ");
					}

					System.out.println("selected");
				}
			};

			contextMenuItem.setContextMenuAction(contextMenuAction);

			List<ContextMenuItem> contextMenuItems = new ArrayList<ContextMenuItem>() {};
			contextMenuItems.add(contextMenuItem);

			return contextMenuItems;
		}
	};

	ContextMenuControlUtil.getContextMenuControl(nativityControl, contextMenuControlCallback);

# Issue Tracking

LiferayNativity is an open source project and community members are encouraged to submit bug fixes and enhancements. Please create tickets on http://issues.liferay.com under the **PUBLIC - Nativity** project.

# Licensing

Check [license.txt](https://github.com/liferay/liferay-nativity/blob/master/copyright.txt) for the latest licensing information.

# Contact

Email nativity@liferay.com with questions or comments.