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

#import "FinderSync.h"
#import "JSONKit.h"
#import "RequestManager.h"

const NSInteger RECEIVED_CALLBACK_RESPONSE = 2;
const NSInteger WAITING_FOR_CALLBACK_RESPONSE = 1;

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
		_menuActionDictionary = [[NSMutableDictionary alloc] init];
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
		NSString* uuid = menuItemDictionary[@"uuid"];
		NSString* action = menuItemDictionary[@"action"];
		NSArray* childrenSubMenuItems = (NSArray*)menuItemDictionary[@"contextMenuItems"];

		if ([subMenuTitle isEqualToString:@"_SEPARATOR_"]) {
			[menu addItem:[NSMenuItem separatorItem]];
		}
		else if (childrenSubMenuItems && [childrenSubMenuItems count] != 0) {
			NSMenuItem* subMenuItem = [menu addItemWithTitle:subMenuTitle action:nil keyEquivalent:@""];

			[self addChildrenSubMenuItems:subMenuItem withChildren:childrenSubMenuItems forFiles:files];
		}
		else {
			[self createActionMenuItem:menu action:action withTitle:subMenuTitle withIndex:i enabled:enabled withUuid:uuid forFiles:files];
		}
	}

	[parentMenuItem setSubmenu:menu];
}

- (void) connect {
	NSError* error = nil;

	if (![_socket connectToHost:@"localhost" onPort:33001 error:&error]) {
		#ifdef DEBUG
			NSLog(@"Connection failed with error: %@", error);
		#endif
	}
	else {
		#ifdef DEBUG
			NSLog(@"Connecting...");
		#endif
	}
}

- (void) createActionMenuItem:(NSMenu*)menu action:(NSString*)action withTitle:(NSString*)title withIndex:(NSInteger)index enabled:(BOOL)enabled withUuid:(NSString*)uuid forFiles:(NSArray*)files {
	NSMenuItem* mainMenuItem = nil;

	if (!action) {
		mainMenuItem = [menu insertItemWithTitle:title action:nil keyEquivalent:@"" atIndex:index];
	}
	else if ([action isEqualToString:@"sampleMenuItem"]) {
		mainMenuItem = [menu insertItemWithTitle:title action:@selector(sampleMenuItemClicked:) keyEquivalent:@"" atIndex:index];
	}
	else {
		NSLog(@"Failed to find context menu action: %@", action);

		return;
	}

	if (enabled) {
		[mainMenuItem setTarget:self];
	}

	[mainMenuItem setEnabled:enabled];

	NSDictionary* menuActionDictionary = [[NSMutableDictionary alloc] init];
	[menuActionDictionary setValue:uuid forKey:@"uuid"];
	NSArray* filesArray = [files copy];
	[menuActionDictionary setValue:filesArray forKey:@"files"];

	if (action) {
		[_menuActionDictionary setValue:menuActionDictionary forKey:action];
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

		NSString* action = menuItemDictionary[@"action"];
		NSArray* childrenSubMenuItems = (NSArray*)menuItemDictionary[@"contextMenuItems"];
		BOOL enabled = [menuItemDictionary[@"enabled"] boolValue];
		NSString* uuid = menuItemDictionary[@"uuid"];

		if (childrenSubMenuItems && [childrenSubMenuItems count] != 0) {
			NSMenuItem* mainMenuItem = [[NSMenuItem alloc] initWithTitle:mainMenuTitle action:nil keyEquivalent:@""];

			[menu insertItem:mainMenuItem atIndex:i];

			[self addChildrenSubMenuItems:mainMenuItem withChildren:childrenSubMenuItems forFiles:files];
		}
		else {
			[self createActionMenuItem:menu action:action withTitle:mainMenuTitle withIndex:i enabled:enabled withUuid:uuid forFiles:files];
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

- (void)registerBadgeImage:(NSData*)cmdData {
	#ifdef DEBUG
		NSLog(@"Registering file badge: %@", cmdData);
	#endif

	NSDictionary* dictionary = (NSDictionary*)cmdData;

	NSString* path = dictionary[@"path"];
	NSString* label = dictionary[@"label"];
	NSNumber* id = dictionary[@"id"];

	[[FIFinderSyncController defaultController] setBadgeImage:[[NSImage alloc] initWithContentsOfFile:path] label:label forBadgeIdentifier:[id stringValue]];
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

- (void) sendMenuItemClicked:(NSString*)action {
	#ifdef DEBUG
		NSLog(@"Menu item clicked with action %@", action);
	#endif

	NSDictionary* actionDictionary = _menuActionDictionary[action];

	NSDictionary* menuItemClickedDictionary = [[NSMutableDictionary alloc] initWithCapacity:2];

	[menuItemClickedDictionary setValue:@"contextMenuAction" forKey:@"command"];
	[menuItemClickedDictionary setValue:actionDictionary forKey:@"value"];

	NSData* data = [[[menuItemClickedDictionary JSONString] stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	[_socket writeData:data withTimeout:-1 tag:0];
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
	#ifdef DEBUG
		NSLog(@"Successfully connected to host %@ on port %hu", host, port);
	#endif

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
	#ifdef DEBUG
	if (_connected) {
		NSLog(@"Disconnected with error: %@", error);
	}
	#endif

	if (_connected && _removeBadgesOnClose) {
		[self removeAllBadges];
	}

	_connected = NO;

	[self performSelector:@selector(connect) withObject:self afterDelay:1.0];
}

@end