Liferay Nativity is the cross-platform Java library for creating icon overlays and context menus. 

# Build

## Common (Public interface)

Common code place in the _Client_ folder. Public interface is represented as java class that communicates with native plugin code and provides methods for working with overlays and context menus. Detailed description of these methods provided in the javadoc comments. Java class is not require any additional build, code should be just placed to java project that need operate with file manager context menu or overlays.

## MacOSX 

Plugin has several projects, to build whole plugin, developer have to build them in the next order (using xcode):

1. mach_ inject_ bundle
2. LiferayFinderCore
3. LifreayFinderPlugin
4. LiferayFinderInstaller (this is optional project, that helps install plugin using SMJobBless framework, but actually plugin can be installed in other way)

## Linux

TBD

# Deployment

## MacOSX

To register plugin in the system user have to build LiferayFinderPlugin project and run it as sudo user. This operation should be done after each restart of finder in order to re-enable plugin. In order to simplify plugin installer project was created. This project provides the next functionality:

1. Install plugin with administrators rights. Prompt for admin credentials if need.
2. Run plugin on system startup
3. Re-run plugin on finder restart

## Linux

TBD

# Architecture

## MacOSX

There is no official finder plugin system for MacOSX 10.6 and later. Plugin implemented using code injection mechanism. To inject new code to finder process mach_ inject framework is used. Plugin has two part of code:

1. Code that inject new bundle to finder process. This code is represented in the LiferayFinderPlugin project. This code is pretty simple, it finds pid of finder process and call mach_ inject_ bundle_ pid function. When this code executes new bundle are attached to the finder process.
2. Code that working in context of finder process and implements new functionality. Code injected to finder process should to replace implementation of finder functions that draws icons and handle context menus. It done in the load method of FinderPlugin class. More detailed information about overloaded methods provided below.
3. Because plugin code working in context of finder process some kind of inter process communication mechanism is needed in order to communicate with java code. This mechanism implemented using sockets. Code incapsulated in AsyncSocket class. 

### Icon overlay feature

To draw overlay icon over file icon we need to override two finder methods:

1. drawImage method of class TIconViewCell. This method is draw file icon in the "icon view" mode.
2. drawIconWithFrame method of class TListViewIconAndTextCell. This method is draw file icon in the "list view" mode.

Both these methods check if any overlay associated with file and if any draws overlay over it.

### Context menu feature

To work with context menus we have to override the next finder methods:

1. addViewSpecificStuffToMenu method of TContextMenu class. This method is called by finder before display of context menu in the "icon view" mode. Unfortunately, parameters of this method doesn't contain list of selected files. To handle this situation we have to override handleContextMenuCommon method too and save list of files. Mountain Lion version of finder has changed type of parameters, so both methods should be overridden.
2. handleContextMenuCommon method of TContextMenu class. See previous comment.
3. configureWithNodes method of TContextMenu class. This method is called by finder to create list of nodes in the "list view" mode. Mountain Lion version of finder has changed type of parameters, so both methods should be overridden.


### Helper classes

A number of helper classes created in order to manage plugin logic. The brief description of these classes provided below:

1. IconCache. This class is wrapper on icon collection. It provides methods to load, unload, manage, etc. icons.
2. MenuManager. This class provides functionality for associating custom menu items with files.
3. RequestManager. This class manager requests from client interface.
4. ContentManager. This class contains information about association icon overlay with files.

## Linux

TBD