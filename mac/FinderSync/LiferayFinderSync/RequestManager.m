/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

#include <assert.h>
#include <pwd.h>
#include <sys/types.h>
#include <unistd.h>

#import <objc/runtime.h>
#import "FinderSync.h"
#import "JSONKit.h"
#import "RequestManager.h"

NSString* const USER_HOME_RELATIVE_PORT_FILE_PATH = @".liferay-nativity/port";
NSInteger const RECEIVED_CALLBACK_RESPONSE = 2;
NSInteger const WAITING_FOR_CALLBACK_RESPONSE = 1;

static NSTimeInterval maxCallbackRequestWaitTime = 0.25f;
static RequestManager* sharedInstance = nil;

@implementation RequestManager

+ (RequestManager*) sharedInstance {
	@synchronized(self) {
		if (!sharedInstance) {
			sharedInstance = [[self alloc] init];
		}
	}

	return sharedInstance;
}

- (id) init {
	if ((self = [super init])) {
		_callbackLock = [[NSConditionLock alloc] init];
		_connected = NO;
		_menuUuidDictionary = [[NSMutableDictionary alloc] init];
		_observedFolders = [[NSMutableSet alloc] init];
		_removeBadgesOnClose = YES;
		_socket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];

		[self connect];
	}

	return self;
}

- (void) addChildrenSubMenuItems:(NSMenuItem*)parentMenuItem withChildren:(NSArray*)menuItemsDictionaries forFiles:(NSArray*)files {
	NSMenu* menu = [[NSMenu alloc] init];

	for (uint i = 0; i < [menuItemsDictionaries count]; i++) {
		NSDictionary* menuItemDictionary = menuItemsDictionaries[i];

		NSString* subMenuTitle = menuItemDictionary[@"title"];
		BOOL enabled = [menuItemDictionary[@"enabled"] boolValue];
		NSString* iconName = menuItemDictionary[@"iconName"];
		NSString* uuid = menuItemDictionary[@"uuid"];
		NSArray* childrenSubMenuItems = (NSArray*)menuItemDictionary[@"contextMenuItems"];

		if ([subMenuTitle isEqualToString:@"_SEPARATOR_"]) {
			[menu addItem:[NSMenuItem separatorItem]];
		}
		else if (childrenSubMenuItems && [childrenSubMenuItems count] != 0) {
			NSMenuItem* subMenuItem = [menu addItemWithTitle:subMenuTitle action:nil keyEquivalent:@""];

			[self addChildrenSubMenuItems:subMenuItem withChildren:childrenSubMenuItems forFiles:files];
		}
		else {
			[self createActionMenuItem:menu title:subMenuTitle index:i enabled:enabled uuid:uuid iconName:iconName files:files];
		}
	}

	[parentMenuItem setSubmenu:menu];
}

- (void) connect {
	NSError* error = nil;

	NSString* path = [NSString stringWithUTF8String:getpwuid(getuid())->pw_dir];

	path = [path stringByAppendingPathComponent:USER_HOME_RELATIVE_PORT_FILE_PATH];

	NSFileManager* fileManager = [NSFileManager defaultManager];

	if (![fileManager fileExistsAtPath:path]) {
		#ifdef DEBUG
			NSLog(@"Failed to connect. File not found %@", path);
		#endif

		[self performSelector:@selector(connect) withObject:self afterDelay:1.0];

		return;
	}

	NSString* portNumberString = [[NSString alloc] initWithContentsOfFile:path encoding:NSUTF8StringEncoding error:&error];

	int port = [portNumberString intValue];

	if (port <= 0) {
		#ifdef DEBUG
			NSLog(@"Failed to connect. File content is not an int value: %@", path);
		#endif

		[self performSelector:@selector(connect) withObject:self afterDelay:1.0];

		return;
	}

	if (![_socket connectToHost:@"localhost" onPort:port error:&error]) {
		#ifdef DEBUG
			NSLog(@"Connection failed with error: %@", error);
		#endif
	}
	else {
		#ifdef DEBUG
			NSLog(@"Connecting to port %d", port);
		#endif
	}
}

- (void) createActionMenuItem:(NSMenu*)menu title:(NSString*)title index:(NSInteger)index enabled:(BOOL)enabled uuid:(NSString*)uuid iconName:(NSString*)iconName files:(NSArray*)files {
	NSMenuItem* mainMenuItem = nil;

	if (!enabled) {
		mainMenuItem = [menu insertItemWithTitle:title action:nil keyEquivalent:@"" atIndex:index];
	}
	else {
		NSDictionary* menuUuidDictionary = [[NSMutableDictionary alloc] init];
		NSArray* filesArray = [files copy];

		[menuUuidDictionary setValue:filesArray forKey:@"files"];
		[menuUuidDictionary setValue:uuid forKey:@"uuid"];

		[_menuUuidDictionary setValue:menuUuidDictionary forKey:uuid];

		SEL actionSelector = sel_registerName([[@"__CONTEXT_MENU_ACTION_" stringByAppendingString:[@(index)stringValue]] UTF8String]);

		IMP methodIMP = imp_implementationWithBlock(^(id _self) {
			[[RequestManager sharedInstance] sendMenuItemClicked:uuid];
		});

		if (![FinderSync instancesRespondToSelector:actionSelector]) {
			class_addMethod([FinderSync class], actionSelector, methodIMP, "v@:");
		}

		Method method = class_getInstanceMethod([FinderSync class], actionSelector);

		method_setImplementation(method, methodIMP);

		mainMenuItem = [menu insertItemWithTitle:title action:actionSelector keyEquivalent:@"" atIndex:index];
	}

	if (enabled) {
		[mainMenuItem setTarget:self];
	}

	[mainMenuItem setEnabled:enabled];

	if (iconName) {
		NSImage* image = [NSImage imageNamed:iconName];

		if (image) {
			[mainMenuItem setImage:image];
		}
	}
}

- (void) executeCommand:(NSData*)data {
	if (!data || [data length] == 0) {
		NSLog(@"Cannot parse empty data");

		return;
	}

	NSDictionary* jsonDictionary = [data objectFromJSONData];

	NSString* command = jsonDictionary[@"command"];
	NSData* value = jsonDictionary[@"value"];

	if (!command) {
		NSLog(@"Failed to parse data: %@", data);

		return;
	}
	else if ([command isEqualToString:@"menuItems"]) {
		[self processMenuItems:value];
	}
	else if ([command isEqualToString:@"registerContextMenuIcon"]) {
		[self registerContextMenuIcon:value];
	}
	else if ([command isEqualToString:@"registerIconWithId"]) {
		[self registerBadgeImage:value];
	}
	else if ([command isEqualToString:@"setFileIcons"]) {
		[self setFileBadges:value];
	}
	else if ([command isEqualToString:@"setFilterPaths"]) {
		[self setFilterFolders:value];
	}
	else {
		NSLog(@"Failed to find command: %@", command);
	}
}

- (NSMenu*) menuForFiles:(NSArray*)files {
	NSMenu* menu = [[NSMenu alloc] initWithTitle:@""];

	NSArray* menuItemsArray = [self requestMenuItemsForFiles:files];

	if (!menuItemsArray || [menuItemsArray count] == 0) {
		return menu;
	}

	for (uint i = 0; i < [menuItemsArray count]; i++) {
		NSDictionary* menuItemDictionary = menuItemsArray[i];
		NSString* mainMenuTitle = menuItemDictionary[@"title"];

		if ([mainMenuTitle isEqualToString:@""]) {
			continue;
		}

		NSArray* childrenSubMenuItems = (NSArray*)menuItemDictionary[@"contextMenuItems"];
		BOOL enabled = [menuItemDictionary[@"enabled"] boolValue];
		NSString* iconName = menuItemDictionary[@"iconName"];
		NSString* uuid = menuItemDictionary[@"uuid"];

		if (childrenSubMenuItems && [childrenSubMenuItems count] != 0) {
			NSMenuItem* mainMenuItem = [[NSMenuItem alloc] initWithTitle:mainMenuTitle action:nil keyEquivalent:@""];

			[menu insertItem:mainMenuItem atIndex:i];

			[self addChildrenSubMenuItems:mainMenuItem withChildren:childrenSubMenuItems forFiles:files];
		}
		else {
			[self createActionMenuItem:menu title:mainMenuTitle index:i enabled:enabled uuid:uuid iconName:iconName files:files];
		}
	}

	return menu;
}

- (void) processMenuItems:(NSData*)cmdData {
	#ifdef DEBUG
		NSLog(@"Processing menu items: %@", cmdData);
	#endif

	[_callbackLock lock];

	_callbackData = cmdData;

	[_callbackLock unlockWithCondition:RECEIVED_CALLBACK_RESPONSE];
}

- (void) refreshFiles {
	NSFileManager* fileManager = [NSFileManager defaultManager];

	for (NSURL* observedFolder in [_observedFolders copy]) {
		NSError* error = nil;

		NSArray* urls = [fileManager contentsOfDirectoryAtURL:observedFolder includingPropertiesForKeys:nil options:0 error:&error];

		if (error) {
			NSLog(@"Failed to refresh badges for %@. Error: %@", observedFolder, error);

			continue;
		}

		for (NSURL* url in urls) {
			[self requestFileBadgeId:url];
		}
	}

	for (NSURL* url in [[FIFinderSyncController defaultController].directoryURLs copy]) {
		[self requestFileBadgeId:url];
	}
}

- (void) registerBadgeImage:(NSData*)cmdData {
	#ifdef DEBUG
		NSLog(@"Registering file badge: %@", cmdData);
	#endif

	NSDictionary* dictionary = (NSDictionary*)cmdData;

	NSString* path = dictionary[@"path"];
	NSString* label = dictionary[@"label"];
	NSNumber* id = dictionary[@"id"];

	NSFileManager* fileManager = [NSFileManager defaultManager];

	if (![fileManager fileExistsAtPath:path]) {
		NSLog(@"Failed to register file badge. File not found: %@", path);

		return;
	}

	[[FIFinderSyncController defaultController] setBadgeImage:[[NSImage alloc] initWithContentsOfFile:path] label:label forBadgeIdentifier:[id stringValue]];
}

- (void) registerContextMenuIcon:(NSData*)cmdData {
	#ifdef DEBUG
		NSLog(@"Registering icon: %@", cmdData);
	#endif

	NSDictionary* dictionary = (NSDictionary*)cmdData;

	NSString* path = dictionary[@"path"];
	NSString* name = dictionary[@"name"];

	NSFileManager* fileManager = [NSFileManager defaultManager];

	if (![fileManager fileExistsAtPath:path]) {
		NSLog(@"Failed to register icon. File not found: %@", path);

		return;
	}

	NSImage* logoImage = [[NSImage alloc] initWithContentsOfFile:path];

	[logoImage setName:name];
}

- (void) requestFileBadgeId:(NSURL*)url {
	NSDictionary* dictionary = [NSMutableDictionary dictionaryWithCapacity:2];

	[dictionary setValue:@"getFileIconId" forKey:@"command"];
	[dictionary setValue:[url path] forKey:@"value"];

	NSString* jsonString = [dictionary JSONString];

	NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	[_socket writeData:data withTimeout:-1 tag:0];
}

- (NSArray*) requestMenuItemsForFiles:(NSArray*)files {
	if (!_connected) {
		return nil;
	}

	NSDictionary* dictionary = [[NSMutableDictionary alloc] initWithCapacity:2];

	[dictionary setValue:@"getContextMenuList" forKey:@"command"];
	[dictionary setValue:files forKey:@"value"];

	NSString* jsonString = [dictionary JSONString];

	NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	[_callbackLock lock];

	#ifdef DEBUG
		NSLog(@"Requesting context menu items for %@", files);
	#endif

	[_socket writeData:data withTimeout:-1 tag:0];

	[_callbackLock unlockWithCondition:WAITING_FOR_CALLBACK_RESPONSE];

	if (![_callbackLock lockWhenCondition:RECEIVED_CALLBACK_RESPONSE beforeDate:[NSDate dateWithTimeIntervalSinceNow:maxCallbackRequestWaitTime]]) {
		NSLog(@"Context menu item request timed out");

		[_callbackLock lock];
	}

	NSMutableArray* menuItems = [[NSMutableArray alloc] init];

	@try {
		NSArray* menuItemDictionaries = (NSArray*)_callbackData;

		if ([_callbackData isKindOfClass:[NSArray class]]) {
			for (NSDictionary* menuItemDictionary in menuItemDictionaries) {
				if ([menuItemDictionary isKindOfClass:[NSDictionary class]]) {
					[menuItems addObject:menuItemDictionary];
				}
				else {
					NSLog(@"Invalid context menu response: %@", _callbackData);
				}
			}
		}
		else {
			NSLog(@"Invalid context menu response: %@", _callbackData);
		}
	}
	@catch (NSException* exception) {
		NSLog(@"Invalid context menu response: %@", _callbackData);
	}

	[_callbackLock unlock];

	return menuItems;
}

- (void) sendMenuItemClicked:(NSString*)uuid {
	#ifdef DEBUG
		NSLog(@"Menu item clicked with uuid %@", uuid);
	#endif

	NSDictionary* menuUuidDictionary = _menuUuidDictionary[uuid];

	NSDictionary* menuItemClickedDictionary = [[NSMutableDictionary alloc] initWithCapacity:2];

	[menuItemClickedDictionary setValue:@"contextMenuAction" forKey:@"command"];
	[menuItemClickedDictionary setValue:menuUuidDictionary forKey:@"value"];

	NSData* data = [[[menuItemClickedDictionary JSONString] stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	[_socket writeData:data withTimeout:-1 tag:0];

	[_menuUuidDictionary removeAllObjects];
}

- (void) sendObservingFolder:(NSURL*)url start:(BOOL)start {
	NSDictionary* dictionary = [NSMutableDictionary dictionaryWithCapacity:2];

	if (start) {
		[dictionary setValue:@"startObservingFolder" forKey:@"command"];

		[_observedFolders addObject:url];
	}
	else {
		[dictionary setValue:@"endObservingFolder" forKey:@"command"];

		[_observedFolders removeObject:url];
	}

	[dictionary setValue:[url path] forKey:@"value"];

	NSString* jsonString = [dictionary JSONString];

	NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	[_socket writeData:data withTimeout:-1 tag:0];
}

- (void) setFileBadges:(NSData*)cmdData {
	NSDictionary* dictionary = (NSDictionary*)cmdData;

	for (NSString* path in dictionary) {
		NSString* normalizedPath = [path decomposedStringWithCanonicalMapping];

		NSNumber* badgeId = dictionary[path];

		NSString* badgeIdString = nil;

		if ([badgeId intValue] == -1) {
			badgeIdString = @"";
		}
		else {
			badgeIdString = [badgeId stringValue];
		}

		NSURL* url = [NSURL fileURLWithPath:normalizedPath];

		[[FIFinderSyncController defaultController] setBadgeIdentifier:badgeIdString forURL:url];
	}
}

- (void) setFilterFolders:(NSData*)cmdData {
	#ifdef DEBUG
		NSLog(@"Setting filter paths: %@", cmdData);
	#endif

	NSArray* paths = (NSArray*)cmdData;

	NSMutableSet* urls = [[NSMutableSet alloc] init];

	NSMutableSet* newObservedFolders = [[NSMutableSet alloc] init];

	for (NSString* path in paths) {
		[urls addObject:[NSURL fileURLWithPath:path]];

		for (NSURL* observedFolder in [_observedFolders copy]) {
			if ([[observedFolder path] hasPrefix:path]) {
				[newObservedFolders addObject:observedFolder];
			}
		}
	}

	_observedFolders = newObservedFolders;

	[FIFinderSyncController defaultController].directoryURLs = urls;

	[self refreshFiles];
}

- (void) socket:(GCDAsyncSocket*)socket didConnectToHost:(NSString*)host port:(UInt16)port {
	NSLog(@"Successfully connected to host %@ on port %hu", host, port);

	_connected = YES;

	[socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
}

- (void) socket:(GCDAsyncSocket*)socket didReadData:(NSData*)data withTag:(long)tag {
	[self executeCommand:[data subdataWithRange:NSMakeRange(0, [data length] - 2)]];

	[socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
}

- (void) removeAllBadges {
	NSFileManager* fileManager = [NSFileManager defaultManager];

	for (NSURL* observedFolder in [_observedFolders copy]) {
		NSError* error = nil;

		NSArray* urls = [fileManager contentsOfDirectoryAtURL:observedFolder includingPropertiesForKeys:nil options:0 error:&error];

		if (error) {
			NSLog(@"Failed to remove badges for %@. Error: %@", observedFolder, error);

			continue;
		}

		for (NSURL* url in urls) {
			[[FIFinderSyncController defaultController] setBadgeIdentifier:@"" forURL:url];
		}
	}

	for (NSURL* url in [[FIFinderSyncController defaultController].directoryURLs copy]) {
		[[FIFinderSyncController defaultController] setBadgeIdentifier:@"" forURL:url];
	}
}

- (void) socketDidDisconnect:(GCDAsyncSocket*)socket withError:(NSError*)error {
	if (_connected) {
		NSLog(@"Disconnected with error: %@", error);
	}

	if (_connected && _removeBadgesOnClose) {
		[self removeAllBadges];
	}

	_connected = NO;

	[self performSelector:@selector(connect) withObject:self afterDelay:1.0];
}

@end