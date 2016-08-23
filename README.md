# Liferay Nativity

**Table of Contents**

- [Liferay Nativity](#liferay-nativity)
- [Introduction](#introduction)
- [Native Plugins](#native-plugins)
  - [Mac OS X](#mac-os-x)
    - [Finder Sync](#finder-sync)
      - [Build and Deployment](#build-and-deployment)
      - [Limitations and Issues](#limitations-and-issues)
    - [Injector](#injector)
      - [Build](#build)
      - [Deployment](#deployment)
      - [Code Architecture](#code-architecture)
  - [Windows](#windows)
    - [JNI Interface](#jni-interface)
    - [Shell Extensions](#shell-extensions)
      - [Build Properties](#build-properties)
    - [Ant Scripts](#ant-scripts)
      - [Windows Util DLL](#windows-util-dll)
      - [Context Menu DLL](#context-menu-dll)
      - [Icon Overlay DLL](#icon-overlay-dll)
  - [Linux](#linux)
    - [Build](#build-1)
      - [Deployment](#deployment-1)
- [Java Client](#java-client)
- [Example Code](#example-code)
- [Issue Tracking and Contributions](#issue-tracking-and-contributions)
- [Licensing](#licensing)
- [Contact](#contact)

# Introduction

Liferay Nativity is a cross-platform library for adding icon overlays and context menus to file browsers.

The following operating systems are currently supported:

* Windows Vista or greater (tested up to Windows 10)
* Mac OS X 10.7 or greater (tested up to OS X 10.11)
* Linux GNOME Nautilus 3.x or greater (tested up to Nautilus 3.6)

Currently the client code is only available for Java. Contributions for other clients like Ruby, C++, etc are welcome.

<img width="400" src="https://raw.github.com/liferay/liferay-nativity/master/extra/screenshot-win.png">

<img width="400" src="https://raw.github.com/liferay/liferay-nativity/master/extra/screenshot-mac.png">

<img width="400" src="https://raw.github.com/liferay/liferay-nativity/master/extra/screenshot-linux.png">

# Native Plugins

## Mac OS X

As of OS X 10.10 Yosemite, icon overlays and context menus are officially supported through the new Finder Sync API. A sample [Finder Sync plugin](https://github.com/liferay/liferay-nativity/tree/master/mac/FinderSync) is included which works with the existing Nativity architecture.

Liferay Nativity continues to work for OS X 10.9 and below. This code has been moved to the [Injector](https://github.com/liferay/liferay-nativity/tree/master/mac/Injector) folder.

### Finder Sync

As of OS X 10.10 Yosemite, Apple has *finally* added an API for extending Finder with plugins. The benefits of using the new [Finder Sync API](https://developer.apple.com/library/ios/documentation/General/Conceptual/ExtensibilityPG/Finder.html) are significant (future-proof supported API, separate process eliminates Finder crashes, performance, etc), so developers are strongly advised to make use of this new API.

#### Build and Deployment

After cloning the liferay-nativity project, open LiferayShellApp.xcodeproj. Configure the appropriate signing identities and build. The LiferayShellApp can be ignored as it is a sample parent app used for debugging the plugin.

The LiferayFinderSync.appex bundle should be placed under the /Contents/Plugins/ folder of your application.

#### Limitations and Issues

There are several known issues with the Finder Sync API that require a change in behavior from the existing Nativity API.

##### Conflicts With Multiple Finder Sync Plugins

If multiple Finder Sync plugins monitor the same folders, only the first Finder Sync process that launched will be able to request/set icon overlay. This means if a Finder Sync extension "greedily" decides to monitor a parent folder like ~/Documents (like Dropbox currently does), then any Finder Sync extension monitoring files under the ~/Documents parent will not show icon overlays if the greedy extension launched first.

Ideally, Apple needs to figure out an intelligent mechanism for deciding which Finder Sync gets to draw the icon overlay. Until then, developers are advised to be "polite" and monitor only the folders that explicitly belong to your application. Context menus will work fine with multiple extensions.

##### Sandbox

Finder Sync plugins must be sandboxed. By default, sandboxed applications cannot read data outside its own containers. In order to allow icons to be registered from any path as well as the ability to refresh icon overlays (which requires reading an observed folder's children), the entitlement [com.apple.security.temporary-exception.files.absolute-path.read-only](https://developer.apple.com/library/ios/documentation/Miscellaneous/Reference/EntitlementKeyReference/Chapters/AppSandboxTemporaryExceptionEntitlements.html) is included, but any app submitted to the Mac App Store using this entitlement will likely be rejected.

##### Flat Context Menus

*Fixed in OS X 10.11 El Capitan.*

~~Finder Sync plugins also do not support sub-menus. The context menus returned by the client must be a flat list. Attempting to generate a tree of context menu items will result in only the parent level appearing in the context menu.~~

### Injector

For OS X 10.9 Mavericks and below, there is no official API for custom file overlays and context menus in Finder, so Liferay Nativity uses a technique called "[method swizzling](http://cocoadev.com/MethodSwizzling)" to swap Finder's code with our own custom code. The Finder Sync plugin only works on OS X 10.10 Yosemite and above.

The LiferayNativityFinder bundle is responsible for "method swizzling" the code for icon overlays and context menus as well as handling client requests. LiferayNativityInjector is a scripting addition responsible for injecting the LiferayNativityFinder bundle into the running instance of Finder.

**Note:** Since method swizzling into Finder is not supported by Apple, any upgrade to Finder can break Liferay Nativity's injected code. LiferayNativityInjector has an optional version check that can throw a warning if a newer, untested version of Finder is detected. Also, buggy injected code can cause Finder to crash or hang, so proceed with caution!

#### Build

After cloning the liferay-nativity project, open LiferayNativity.xcworkspace and build using the LiferayNativity.osax scheme. The LiferayNativity.osax binary will be under LiferayNativityInjector's Products folder. You can ignore LiferayNativityFinder.bundle as it's already copied into LiferayNativity.osax's Resources folder.

#### Deployment

Copy LiferayNativity.osax (LiferayNativityInjector's target) into /Library/ScriptingAdditions (this will prompt for administrator privileges). Alternatively, LiferayNativity.osax can be copied into the user's ~/Library/ScriptingAdditions but is not recommended because each request to run a script will prompt for administrator privileges.

After the scripts have been copied, the following scripting commands will be available. Send the NVTYload event using the example below to inject the LiferayNativityFinder bundle into the running instance of Finder.

##### NVTYload event

Loads LiferayNativityFinder into the running Finder.app.

    tell application "Finder"
        try
            «event NVTYload»
        end try
    end tell

##### NVTYunld event

Unloads LiferayNativityFinder from the running Finder.app. Note, this does not actually remove the injected bundle but rather reverses all method swizzling and frees memory used by LiferayNativityFinder.

    tell application "Finder"
        try
            «event NVTYunld»
        end try
    end tell

##### NVTYlded event

Check if LiferayNativityFinder is installed in the running Finder.app. Returns 0 if enabled, the error number otherwise.

    tell application "Finder"
        try
            «event NVTYlded»
            set the result to 0
        on error msg number code
            set the result to code
        end try
    end tell

Upon successful deployment, log messages will be written to the system log.

#### Code Architecture

##### LiferayNativityInjector

The code for LiferayNativityInjector is modified from the scripting additions used by [TotalFinder](http://totalfinder.binaryage.com/). The original source code is available [here](https://github.com/binaryage/totalfinder-osax). This method proved much simpler than using SIMBL or mach_inject. You can see our previous injection method using a combination of mach_inject and a priviliged helper tool installed by SMJobBless by checking out a commit *before* [c82910f1b3](https://github.com/liferay/liferay-nativity/commit/c82910f1b302fae012e52b9021b3ddaf8224c527). Many thanks to BinaryAge for open sourcing their injection code!

##### LiferayNativityFinder

Once injected, LiferayNativityFinder is responsible for method swizzling the icon overlays and context menus into Finder. The original source code for method swizzling (as well as the previously used injection method through mach_inject) was written by the talented developers at [TeamDev](http://www.teamdev.com/). Kudos to TeamDev for tackling an extremely challenging programming task!

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

For Windows, Nativity makes use of both a JNI interface as well as Windows Shell Extensions. There are several DLL’s which must be built and configured to use Nativity on Windows.
* One context menu DLL
* One DLL for each file icon overlay
* One utility DLL shared by the context menu shell extension and the icon overlay extension
* One DLL for the JNI interface

### JNI Interface
The JNI interface allows the Java side of Nativity to interact with the native side of Nativity. It only provides the ability to set a folder to a system folder. In windows if you want to set a folder icon through an desktop.ini file you must set the folder to be a system folder. So Nativity provides this functionality even though it is available in java 1.7, however Nativity also provides it do older versions of java can be supported.

    public static native boolean setSystemFolder(String folder)

The JNI interface also allows interaction with Explorer.  It notifies Explorer to redraw an icon overlay.  This provides the ability to refresh the when they have changed.

    public static native boolean EpdateExplorer(String filePath);

To use the JNI interface, the Liferay Nativity Windows Util project must be build and the resulting DLL named

    LiferayNativityWindowsUtil_x64.dll
    LiferayNativityWindowsUtil_x86.dll

This DLL must also be in your java.library.path.

### Shell Extensions
The shell extensions must be built and registered to be used by Explorer, Explorer also must be restarted for the icon overlays to display.

#### Build Properties

Add the following properties to your ant properties file:
* **nativity.dir** This is the location of the nativity code.
* **nativity.version** This is the version for your build of nativity.
* **ms.sdk.7.1.dir** This is the directory that that MS SDK 7.1 resides.
* **framework.dir** This is the directory where the .NET Framework resides.

* **context.menu.guid** This is the GUID you have assigned to your context menu DLL.

* **overlay.name.?** This is the name for your icon overlay DLL, remember you need one icon overlay DLL for each icon overlay.
* **overlay.guid.?** This is the GUID you have assigned to this icon overlay DLL.
* **overlay.id.?** This is the int value that this icon overlay will display for. The DLL will query the java side for the id value, if the id value received is equal to this value, the icon overlay will display.
* **overlay.path.?** This is the path to the icon overlay, this icon will be placed in the DLL during the build process.

##### Sample

    nativity.dir=D:/newrepository/liferay-nativity
    nativity.version=1.0.1
    ms.sdk.7.1.dir=C:/Program Files/Microsoft SDKs/Windows/v7.1
    framework.dir=C:/Windows/Microsoft.NET/Framework64/v4.0.30319

    context.menu.guid={0131E070-C6A7-4878-A856-02C048A778DB}

    overlay.name.ok=LiferayNativityOKOverlay
    overlay.guid.ok={8138AEF6-77F2-4DA2-9BBE-BB55FEC64601}
    overlay.id.ok=1
    overlay.path.ok=D:/newrepository/liferay-sync-ee/windows/scripts/include/images/ok_overlay.ico

    overlay.name.syncing=LiferayNativitySyncingOverlay
    overlay.guid.syncing={6C800597-BD1E-4ECC-B099-B95D17C9801E}
    overlay.id.syncing=2
    overlay.path.syncing=D:/newrepository/liferay-sync-ee/windows/scripts/include/images/syncing_overlay.ico

    overlay.name.error=LiferayNativityErrorOverlay
    overlay.guid.error={B04AF108-48F3-4180-92BC-E210FFBCF176}
    overlay.id.error=3
    overlay.path.error=D:/newrepository/liferay-sync-ee/windows/scripts/include/images/error_overlay.ico


### Ant Scripts

#### Windows Util DLL

This DLL is required for both the Context Menus and the Icon Overlays.

    <ant dir="${nativity.dir}" target="build-windows-util" inheritAll="false">
      <property name="nativity.dir" value="${nativity.dir}" />
      <property name="target.os" value="windows" />
      <property name="ms.sdk.7.1.dir" value="${ms.sdk.7.1.dir}" />
      <property name="framework.dir" value="${framework.dir}" />
    </ant>

#### Context Menu DLL

    <ant dir="${nativity.dir}" target="build-windows-menus" inheritAll="false">
      <property name="nativity.dir" value="${nativity.dir}" />
      <property name="target.os" value="windows" />
      <property name="ms.sdk.7.1.dir" value="${ms.sdk.7.1.dir}" />
      <property name="framework.dir" value="${framework.dir}" />

      <property name="context.menu.guid" value="${context.menu.guid}" />
    </ant>

#### Icon Overlay DLL

One DLL must be built for each icon overlay.

    <ant dir="${nativity.dir}" target="build-windows-overlays" inheritAll="false">
      <property name="nativity.dir" value="${nativity.dir}" />
      <property name="dist.dir" value="${project.dir}/dist" />
      <property name="target.os" value="windows" />
      <property name="ms.sdk.7.1.dir" value="${ms.sdk.7.1.dir}" />
      <property name="framework.dir" value="${framework.dir}" />

      <property name="overlay.name" value="${overlay.name.?}" />
      <property name="overlay.guid" value="${overlay.guid.?}" />
      <property name="overlay.id" value="${overlay.id.?}" />
      <property name="overlay.path" value="${overlay.path.?}" />
    </ant>

##### Sample
    <ant dir="${nativity.dir}" target="build-windows-overlays" inheritAll="false">
      <property name="nativity.dir" value="${nativity.dir}" />
      <property name="dist.dir" value="${project.dir}/dist" />
      <property name="target.os" value="windows" />
      <property name="ms.sdk.7.1.dir" value="${ms.sdk.7.1.dir}" />
      <property name="framework.dir" value="${framework.dir}" />

      <property name="overlay.name" value="${overlay.name.syncing}" />
      <property name="overlay.guid" value="${overlay.guid.syncing}" />
      <property name="overlay.id" value="${overlay.id.syncing}" />
      <property name="overlay.path" value="${overlay.path.syncing}" />
    </ant>

Be sure to register your context menu and icon overlay DLL's using regsvr32 (for example: "regsvr32 LiferayNativityOKOverlay_x64.dll").

#### Limitations and Issues

Windows limits the total number of icon overlays to 15. Windows uses 4-6 of these, so only 9-11 remain for use by other applications. If you use other applications like Dropbox or TortoiseSVN, your icon overlays may not appear (Windows will load the first 15 icon overlays alphabetically).

You can see the your list of registered icon overlays by navigating to the following registry key HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Explorer\ShellIconOverlayIdentifiers.

## Linux

Liferay Nativity currently only supports Nautilus file manager. Hooks for Nemo are available, but the icon overlays and context menus do not appear.

### Build

    git clone https://github.com/liferay/liferay-nativity

    cd liferay-nativity/linux/nautilus/src

    sudo apt-get install cmake build-essential libgtk2.0-dev libnautilus-extension-dev libboost-all-dev

    cmake .

    make

#### Deployment

##### Nautilus

    sudo ln -s `pwd`/libliferaynativity.so /usr/lib/nautilus/extensions-3.0/libliferaynativity.so
    killall -9 nautilus

##### Nemo (Nautilus fork)

    sudo ln -s `pwd`/libliferaynativity.so /usr/lib/nemo/extensions-3.0/libliferaynativity.so
    killall -9 nemo

Upon successful deployment, log messages will be written to `~/.liferay-nativity/liferaynativity.log`.

*Further instructions coming soon*


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

    // Setting filter folders is required for Mac's Finder Sync plugin
    nativityControl.setFilterFolder("/Users/liferay/Desktop");

    /* File Icons */

    int testIconId;

    // FileIconControlCallback used by Windows and Mac
    FileIconControlCallback fileIconControlCallback = new FileIconControlCallback() {
      @Override
      public int getIconForFile(String path) {
        return testIconId;
      }
    };

    FileIconControl fileIconControl = FileIconControlUtil.getFileIconControl(
      nativityControl, fileIconControlCallback);

    fileIconControl.enableFileIcons();

    String testFilePath = "/Users/liferay/Desktop/testFile.txt";

    if (OSDetector.isWindows()) {
      // This id is determined when building the DLL
      testIconId = 1;
    }
    else if (OSDetector.isMinimumAppleVersion(OSDetector.MAC_YOSEMITE_10_10)) {
      // Used by Mac Finder Sync. This unique id can be set at runtime.
      testIconId = 1;

      fileIconControl.registerIconWithId("/Users/liferay/Desktop/testIcon.icns", "test label", testIconId);
    }
    else if (OSDetector.isLinux() || OSDetector.isMinimumAppleVersion()) {
      // Used by Mac Injector and Linux
      testIconId = fileIconControl.registerIcon("/Users/liferay/Desktop/testIcon.icns");
    }

    // FileIconControl.setFileIcon() method only used by Linux
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

        // Mac Finder Sync will only show the parent level of context menus
        return contextMenuItems;
      }
    };

    ContextMenuControlUtil.getContextMenuControl(nativityControl, contextMenuControlCallback);


# Issue Tracking and Contributions

Liferay Nativity is an open source project and community members are encouraged to submit bug fixes and enhancements.

Please report all bugs and feature requests here: [http://issues.liferay.com/browse/NVTY](http://issues.liferay.com/browse/NVTY) (you will need to create a free account).

To contribute code, create or find the issue on Nativity's ticket management page. From the ticket page, click on "Workflow" -> "Contribute Solution" and include the pull request link. You can choose "Automatic" for the Assignee.

Review the guidelines for contributions to Liferay projects here: [link](https://www.liferay.com/community/welcome/contribute)

# Licensing

Liferay Nativity is licensed under the LGPL. Check [license.txt](https://github.com/liferay/liferay-nativity/blob/master/copyright.txt) for the latest licensing information.

# Contact

Email nativity@liferay.com with questions or comments.
