/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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

/**
 * Syncplicity, LLC © 2014
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * If you would like a copy of source code for this product, EMC will provide a
 * copy of the source code that is required to be made available in accordance
 * with the applicable open source license.  EMC may charge reasonable shipping
 * and handling charges for such distribution.  Please direct requests in writing
 * to EMC Legal, 176 South St., Hopkinton, MA 01748, ATTN: Open Source Program
 * Office.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Changes:
 * - (Andrew Rondeau) Fixed a race condition in menuItemsForFiles:. Now clearing
 * _callbackMsgs before requesting menu items for given paths
 * - (Ivan Burlakov) Added ability to register an icon for use in context menus
 * - (Andrew Rondeau) Started tracking programname in the socket's userData, so
 * different programs don't conflict with each other
 * - (Andrew Rondeau) Switched to NSHashTable for performance reasons
 * - (Andrew Rondeau) Fixed a lot of thread safety issues issues via queuing
 * - (Andrew Rondeau) Added command to repaint all windows, added ability to query
 * the program for the file's icon, made getting the context manu faster
 * - (Andrew Rondeau) Switched from semaphores to NSConditionLock for to resolve
 * unreliability issues
 */

#import "ContentManager.h"
#import "IconCache.h"
#import "JSONKit.h"
#import "RequestManager.h"

static RequestManager* sharedInstance = nil;

@implementation RequestManager

static NSTimeInterval MAX_CALLBACK_REQUEST_WAIT_TIMEINTERVAL = 0.25f;
static NSTimeInterval DISABLE_ICON_OVERLAYS_ON_TIMEOUT_TIMEINTERVAL = 5.0f;

static NSInteger WAITING_FOR_CALLBACK_RESPONSE = 1;
static NSInteger GOT_CALLBACK_RESPONSE = 2;

- (id)init
{
	if ((self = [super init]))
	{
		_listenQueue = dispatch_queue_create("listen queue", nil);
		_listenSocket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:_listenQueue];

		_callbackQueue = dispatch_queue_create("callback queue", nil);
		_callbackSocket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:_callbackQueue];

		_connectedListenSockets = [[NSHashTable alloc] initWithOptions:NSHashTableObjectPointerPersonality capacity:0];
		_connectedCallbackSocketsCount = 0;
		_connectedListenSocketsWithIconCallbacks = [[NSHashTable alloc] initWithOptions:NSHashTableObjectPointerPersonality capacity:0];
		_connectedListenSocketsWithIconCallbacksCount = 0;
		_connectedCallbackSockets = [[NSHashTable alloc] initWithOptions:NSHashTableObjectPointerPersonality capacity:0];
		_callbackMsgs = [[NSMutableDictionary alloc] init];

		_automaticCleanupPrograms = [[NSHashTable alloc] initWithOptions:NSHashTableObjectPointerPersonality capacity:0];

		_filterFolders = nil;

		_numberFormatter = [[NSNumberFormatter alloc] init];
		[_numberFormatter setNumberStyle:NSNumberFormatterDecimalStyle];

		_isRunning = NO;
		_debugMode = NO;

		_allIconsConnection = [[NSObject alloc] init];

		_callbackLock = [[NSConditionLock alloc] init];
		_waitForIconOverlaysUntil = [[NSDate distantPast] retain];
		_disableIconOverlaysUntil = [[NSDate distantPast] retain];

		[self start];
	}

	return self;
}

- (void)dealloc
{
	[_listenSocket setDelegate:nil delegateQueue:NULL];
	[_listenSocket disconnect];
	[_listenSocket release];

	dispatch_release(_listenQueue);

	[_callbackSocket setDelegate:nil delegateQueue:NULL];
	[_callbackSocket disconnect];
	[_callbackSocket release];

	dispatch_release(_callbackQueue);

	[_automaticCleanupPrograms release];

	for (GCDAsyncSocket* socket in _connectedListenSockets)
	{
		[socket setDelegate:nil delegateQueue:NULL];
		[socket disconnect];
	}

	[_connectedListenSockets release];
	[_connectedListenSocketsWithIconCallbacks release];

	for (GCDAsyncSocket* socket in _connectedCallbackSockets)
	{
		[socket setDelegate:nil delegateQueue:NULL];
		[socket disconnect];
	}

	[_connectedCallbackSockets release];
	[_callbackMsgs release];

	[_numberFormatter release];

	[_filterFolders release];
	[_allIconsConnection release];

	sharedInstance = nil;

	[_callbackLock release];
	[_waitForIconOverlaysUntil release];
	[_disableIconOverlaysUntil release];

	[super dealloc];
}

+ (RequestManager*)sharedInstance
{
	@synchronized(self)
	{
		if (!sharedInstance)
		{
			sharedInstance = [[self alloc] init];
		}
	}

	return sharedInstance;
}

- (void)execCommand:(NSData*)data replyTo:(GCDAsyncSocket*)sock
{
	if (!data || [data length] == 0)
	{
		NSLog(@"LiferayNativityFinder: cannot parse empty data");

		[self replyString:@"-1" toSocket:sock];

		return;
	}

	NSDictionary* jsonDictionary = [data objectFromJSONData];

	NSString* command = [jsonDictionary objectForKey:@"command"];
	NSData* value = [jsonDictionary objectForKey:@"value"];

	if (!command)
	{
		NSString* strData = [[NSString alloc]initWithData:data encoding:NSUTF8StringEncoding];
		NSLog(@"LiferayNativityFinder: failed to parse data: %@", strData);
		[strData release];

		[self replyString:@"-1" toSocket:sock];

		return;
	}
	else if ([command isEqualToString:@"enableAutomaticCleanup"])
	{
		[self execEnableAutomaticCleanupCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"setFileIcons"])
	{
		[self execSetFileIconsCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"removeFileIcons"])
	{
		[self execRemoveFileIconsCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"removeAllFileIcons"])
	{
		[self execRemoveAllFileIconsCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"enableFileIcons"])
	{
		[self execEnableFileIconsCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"enableFileIconsWithCallback"])
	{
		[self execEnableFileIconsWithCallbackCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"registerContextMenuIcon"]) {
		[self execRegisterContextMenuIcon:value replyTo:sock];
	}
	else if ([command isEqualToString:@"registerIcon"])
	{
		[self execRegisterIconCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"registerIconWithId"])
	{
		[self execRegisterIconWithIdCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"unregisterIcon"])
	{
		[self execUnregisterIconCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"setFilterPath"])
	{
		// Deprecated as of 1.0.2. Check for backward compatibility with 1.0.1.

		[self execSetFilterPathCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"setFilterPaths"])
	{
		[self execSetFilterPathsCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"repaintAllIcons"])
	{
		[self execRepaintAllIcons:value replyTo:sock];
	}
	else if ([command isEqualToString:@"enableDebugMode"])
	{
		[self execEnableDebugMode:value replyTo:sock];
	}
	else if ([command isEqualToString:@"disableDebugMode"])
	{
		[self execDisableDebugMode:value replyTo:sock];
	}
	else
	{
		NSLog(@"LiferayNativityFinder: failed to find command: %@", command);

		[self replyString:@"-1" toSocket:sock];

		return;
	}
}

- (void)execEnableAutomaticCleanupCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	// Once automatic cleanup is enabled, if it can be re-enabled, then the old icons can't be cleaned up!
	if (_allIconsConnection == sock.userData)
	{
		sock.userData = [[NSObject alloc] init];

		[_automaticCleanupPrograms addObject:sock];
	}

	[self replyString:@"1" toSocket:sock];
}

- (void)execEnableFileIconsCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSNumber* enabledNumber = (NSNumber*)cmdData;
	BOOL enabled = (BOOL)enabledNumber;

	dispatch_async(dispatch_get_main_queue(), ^{
		[[ContentManager sharedInstance] enableFileIconsFor:sock.userData enabled:enabled];
	});

	[self replyString:@"1" toSocket:sock];
}

- (void)execEnableFileIconsWithCallbackCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSNumber* enabledNumber = (NSNumber*)cmdData;
	BOOL enabled = (BOOL)enabledNumber;

	if (enabled)
	{
		[_connectedListenSocketsWithIconCallbacks addObject:sock];
	}
	else
	{
		[_connectedListenSocketsWithIconCallbacks removeObject:sock];
	}
	_connectedListenSocketsWithIconCallbacksCount = _connectedListenSocketsWithIconCallbacks.count;

	OSMemoryBarrier();

	dispatch_async(dispatch_get_main_queue(), ^{
		[_disableIconOverlaysUntil autorelease];
		_disableIconOverlaysUntil = [[NSDate distantPast] retain];
		[[ContentManager sharedInstance] repaintAllWindows];
	});

	[self replyString:@"1" toSocket:sock];
}

- (void)execRegisterContextMenuIcon:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSDictionary* dictionary = (NSDictionary*)cmdData;

	NSString* path = dictionary[@"path"];
	NSString* iconId = dictionary[@"iconId"];

	NSFileManager* fileManager = [NSFileManager defaultManager];

	if (![fileManager fileExistsAtPath:path]) {
		NSLog(@"Failed to register icon. File not found: %@", path);

		return;
	}

	NSImage* logoImage = [[NSImage alloc] initWithContentsOfFile:path];

	[logoImage setName:iconId];

	NSSize size;

	size.height = [[NSFont menuFontOfSize:0] pointSize];
	size.width = [[NSFont menuFontOfSize:0] pointSize];

	[logoImage setSize:size];

	[self replyString:@"1" toSocket:sock];
}

- (void)execRegisterIconCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSString* path = (NSString*)cmdData;

	__block NSNumber* index;

	dispatch_sync(dispatch_get_main_queue(), ^{
		index = [[IconCache sharedInstance] registerIcon:path];
	});

	if (!index)
	{
		index = [NSNumber numberWithInt:-1];
	}

	[self replyString:[_numberFormatter stringFromNumber:index] toSocket:sock];
}

- (void)execRegisterIconWithIdCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSDictionary* dictionary = (NSDictionary*)cmdData;

	NSString* path = [dictionary objectForKey:@"path"];
	NSString* iconId = [dictionary objectForKey:@"iconId"];

	dispatch_sync(dispatch_get_main_queue(), ^{
		[[IconCache sharedInstance] registerIconWithId:path iconId:iconId];
	});

	[self replyString:@"1" toSocket:sock];
}

- (void)execRemoveAllFileIconsCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	dispatch_async(dispatch_get_main_queue(), ^{
		[[ContentManager sharedInstance] removeAllIconsFor:sock.userData];
	});

	[self replyString:@"1" toSocket:sock];
}

- (void)execRemoveFileIconsCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSArray* paths = (NSArray*)cmdData;

	dispatch_async(dispatch_get_main_queue(), ^{
		[[ContentManager sharedInstance] removeIconsFor:sock.userData paths:paths];
	});

	[self replyString:@"1" toSocket:sock];
}

- (void)execSetFileIconsCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSDictionary* iconDictionary = (NSDictionary*)cmdData;

	dispatch_async(dispatch_get_main_queue(), ^{
		[[ContentManager sharedInstance] setIconsFor:sock.userData iconIdsByPath:iconDictionary filterByFolders:_filterFolders];
	});

	[self replyString:@"1" toSocket:sock];
}

- (void)execSetFilterPathCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSString* path = (NSString*)cmdData;
	NSArray* paths = [NSArray arrayWithObject:path];

	[self setFilterFolders:paths];

	[self replyString:@"1" toSocket:sock];
}

- (void)execSetFilterPathsCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	@try {
		NSArray* paths = (NSArray*) cmdData;

		if (paths && [paths count] == 1) {
			NSString* path = [paths objectAtIndex:0];

			if (path && [path length] == 0) {
				paths = nil;
			}
		}

		[self setFilterFolders:paths];

		[self replyString:@"1" toSocket:sock];
	}
	@catch (NSException* exception)
	{
		NSLog(@"LiferayNativityFinder: invalid filter path");

		[self replyString:@"-1" toSocket:sock];
	}
}

- (void)execUnregisterIconCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSNumber* iconId = (NSNumber*)cmdData;

	dispatch_async(dispatch_get_main_queue(), ^{
		[[IconCache sharedInstance] unregisterIcon:iconId];
	});

	[self replyString:@"1" toSocket:sock];
}

- (void)execRepaintAllIcons:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	dispatch_async(dispatch_get_main_queue(), ^{
		[[ContentManager sharedInstance] repaintAllWindows];
	});

	[self replyString:@"1" toSocket:sock];
}

- (void)execEnableDebugMode:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	_debugMode = YES;

	[self replyString:@"1" toSocket:sock];
}

- (void)execDisableDebugMode:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	_debugMode = NO;

	[self replyString:@"1" toSocket:sock];
}

- (void)menuItemClicked:(NSDictionary*)actionDictionary
{
	if ([_connectedCallbackSockets count] == 0)
	{
		return;
	}

	NSDictionary* menuExecDictionary = [[NSMutableDictionary alloc] init];

	[menuExecDictionary setValue:@"contextMenuAction" forKey:@"command"];

	[menuExecDictionary setValue:actionDictionary forKey:@"value"];

	NSString* jsonString = [menuExecDictionary JSONString];

	[menuExecDictionary release];

	NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	for (GCDAsyncSocket* callbackSocket in _connectedCallbackSockets)
	{
		[callbackSocket writeData:data withTimeout:-1 tag:0];
	}
}


- (NSArray*)menuItemsForFiles:(NSArray*)files
{
	// Why not just call [_connectedCallbackSockets count] directly?
	// Thread-safety! _connectedCallbackSockets is manipulated on the socket's thread,
	// but this method is called on the main thread
	OSMemoryBarrier();
	if (_connectedCallbackSocketsCount == 0)
	{
		return nil;
	}

	if (nil == files) {
		return nil;
	}
	
	if (_filterFolders)
	{
		NSString* file = [files objectAtIndex:0];

		if (![[ContentManager sharedInstance] isFileFiltered:file filterByFolders:_filterFolders])
		{
			return nil;
		}
	}

	NSDictionary* menuQueryDictionary = [[NSMutableDictionary alloc] init];

	[menuQueryDictionary setValue:@"getContextMenuList" forKey:@"command"];
	[menuQueryDictionary setValue:files forKey:@"value"];

	NSString* jsonString = [menuQueryDictionary JSONString];
	[menuQueryDictionary release];

	[_callbackLock lock];

	@try {
		[_callbackMsgs removeAllObjects];
		_expectedCallbackResults = _connectedCallbackSocketsCount;

		NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

		// Perform the callbacks on the _callbackQueue for thread-safety
		dispatch_async(_callbackQueue, ^{
			for (GCDAsyncSocket* callbackSocket in _connectedCallbackSockets)
			{
				[callbackSocket writeData:data withTimeout:-1 tag:0];
			}
		});
	}
	@finally {
		[_callbackLock unlockWithCondition:WAITING_FOR_CALLBACK_RESPONSE];
	}

	if (![_callbackLock lockWhenCondition:GOT_CALLBACK_RESPONSE beforeDate:[NSDate dateWithTimeIntervalSinceNow:MAX_CALLBACK_REQUEST_WAIT_TIMEINTERVAL]])
	{
		NSLog(@"LiferayNativityFinder: menu item request timed out");
		[_callbackLock lock];
	}

	@try {
		OSMemoryBarrier();

		NSMutableArray* menuItems = [[NSMutableArray alloc] init];

		for (NSValue* key in _callbackMsgs)
		{
			NSString* callbackMsg = [_callbackMsgs objectForKey:key];

			@try {
				NSDictionary* responseDictionary = [callbackMsg objectFromJSONString];
				NSArray* menuItemDictionaries = [responseDictionary objectForKey:@"value"];

				if ([menuItemDictionaries isKindOfClass:[NSArray class]])
				{
					for (NSDictionary* menuItemDictionary in (NSArray*)[responseDictionary objectForKey:@"value"])
					{
						if ([menuItemDictionary isKindOfClass:[NSDictionary class]])
						{
							[menuItems addObject:menuItemDictionary];
						}
						else
						{
							NSLog(@"LiferayNativityFinder: Invalid context menu response: %@", callbackMsg);
						}
					}
				}
				else
				{
					NSLog(@"LiferayNativityFinder: Invalid context menu response: %@", callbackMsg);
				}
			}
			@catch (NSException* exception) {
				NSLog(@"LiferayNativityFinder: Invalid context menu response: %@", callbackMsg);
			}
		}

		return [menuItems autorelease];
	}
	@finally {
		[_callbackMsgs removeAllObjects];
		[_callbackLock unlock];
	}
}


- (NSArray*)iconIdForFile:(NSString*)file
{
	NSMutableArray* iconIds = [[NSMutableArray alloc] init];

	// sometimes the file is null
	if (nil == file) {
		return iconIds;
	}

	NSNumber* imageIndex = [[ContentManager sharedInstance] iconByPath:file];

	if ([imageIndex intValue] > 0)
	{
		[iconIds addObject:imageIndex];
	}

	// Why not just call [_connectedListenSocketsWithIconCallbacks count] directly?
	// Thread-safety! _connectedListenSocketsWithIconCallbacks is manipulated on the socket's thread,
	// but this method is called on the main thread
	OSMemoryBarrier();
	if (_connectedCallbackSocketsCount == 0)
	{
		return [iconIds autorelease];
	}

	if (_filterFolders)
	{
		if (![[ContentManager sharedInstance] isFileFiltered:file filterByFolders:_filterFolders])
		{
			return [iconIds autorelease];
		}
	}

	// If there are timeout problems with icon overlays, then they are outright disabled
	if (NSOrderedDescending == [_disableIconOverlaysUntil compare:[NSDate date]])
	{
		return [iconIds autorelease];
	}

	NSDictionary* menuQueryDictionary = [[NSMutableDictionary alloc] init];

	[menuQueryDictionary setValue:@"getFileIconId" forKey:@"command"];
	[menuQueryDictionary setValue:file forKey:@"value"];

	NSString* jsonString = [menuQueryDictionary JSONString];
	[menuQueryDictionary release];

	[_callbackLock lock];

	@try {
		[_callbackMsgs removeAllObjects];
		_expectedCallbackResults = _connectedCallbackSocketsCount;

		NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

		// Perform the callbacks on the _callbackQueue for thread-safety
		dispatch_async(_callbackQueue, ^{
			for (GCDAsyncSocket* callbackSocket in _connectedCallbackSockets)
			{
				[callbackSocket writeData:data withTimeout:-1 tag:0];
			}
		});
	}
	@finally {
		[_callbackLock unlockWithCondition:WAITING_FOR_CALLBACK_RESPONSE];
	}

	// Picking a date to wait on icon overlays is difficult. We can run 100s of queries in a single repaint,
	// yet having a wait of 100 milliseconds on each call would mean that Finder will crawl to a stop in
	// the case of problems.
	// Furthermore, we don't know when Finder starts to draw icons, thus this algorithm attempts to mitigate this issue
	// by starting with a generous timeout, and then attempting to detect when it needs to reset the timeout
	if (NSOrderedAscending == [_waitForIconOverlaysUntil compare:[NSDate date]])
	{
		[_waitForIconOverlaysUntil release];
		_waitForIconOverlaysUntil = [[[NSDate date] dateByAddingTimeInterval:MAX_CALLBACK_REQUEST_WAIT_TIMEINTERVAL] retain];
	}

	if (![_callbackLock lockWhenCondition:GOT_CALLBACK_RESPONSE beforeDate:[NSDate dateWithTimeIntervalSinceNow:MAX_CALLBACK_REQUEST_WAIT_TIMEINTERVAL]])
	{
		NSLog(@"LiferayNativityFinder: file icon request timed out: %@ : %d", file, _expectedCallbackResults);

		[_disableIconOverlaysUntil release];
		_disableIconOverlaysUntil = [[[NSDate date] dateByAddingTimeInterval:DISABLE_ICON_OVERLAYS_ON_TIMEOUT_TIMEINTERVAL] retain];

		return [iconIds autorelease];
	}

	@try {
		OSMemoryBarrier();

		for (NSValue* key in _callbackMsgs)
		{
			NSString* callbackMsg = [_callbackMsgs objectForKey:key];

			if (_debugMode) {
				NSLog(@"Processing icon callback for %@: %@", file, callbackMsg);
			}

			@try {
				NSDictionary* responseDictionary = [callbackMsg objectFromJSONString];
				NSNumber* imageIndex = [responseDictionary objectForKey:@"value"];

				if ([imageIndex isKindOfClass:[NSNumber class]])
				{
					[iconIds addObject:imageIndex];
				}
				else
				{
					NSLog(@"LiferayNativityFinder: Invalid icon overlay response: %@", callbackMsg);
				}
			}
			@catch (NSException* exception) {
				NSLog(@"LiferayNativityFinder: Invalid icon overlay response: %@", callbackMsg);
			}
		}

		return [iconIds autorelease];
	}
	@finally {
		[_callbackMsgs removeAllObjects];
		[_callbackLock unlock];
	}
}

- (void)socket:(GCDAsyncSocket*)socket didAcceptNewSocket:(GCDAsyncSocket*)newSocket
{
	if (socket == _listenSocket)
	{
		// The userData allows programs to specify that all registered icon overlays will be removed
		// when the socket is broken, without interfering with other programs that use liferay-
		// nativity
		[newSocket setUserData:_allIconsConnection];
		[_connectedListenSockets addObject:newSocket];
	}
	if (socket == _callbackSocket)
	{
		[_connectedCallbackSockets addObject:newSocket];
		_connectedCallbackSocketsCount = [_connectedCallbackSockets count];
	}

	[newSocket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
}

- (void)socket:(GCDAsyncSocket*)socket didConnectToHost:(NSString*)host port:(UInt16)port
{
	[socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
}

- (void)socket:(GCDAsyncSocket*)socket didReadData:(NSData*)data withTag:(long)tag
{
	if (_debugMode) {
		NSLog(@"- (void)socket:(GCDAsyncSocket*)socket didReadData:(NSData*)data withTag:(long)tag");
	}

	dispatch_async(_listenQueue, ^{
		if ([_connectedListenSockets containsObject:socket])
		{
			[self execCommand:[data subdataWithRange:NSMakeRange(0, [data length] - 2)] replyTo:socket];

			[socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
		}
    });

    dispatch_async(_callbackQueue, ^{
		if ([_connectedCallbackSockets containsObject:socket])
		{
			if (_debugMode) {
				NSLog(@"... is on the callback queue");
			}

			NSData *strData = [data subdataWithRange:NSMakeRange(0, [data length] - 2)];
			NSString *callbackString = [[NSString alloc] initWithData:strData encoding:NSUTF8StringEncoding];

			[_callbackLock lock];
			@try {
				OSMemoryBarrier();

				if (_debugMode) {
					NSLog(@"Callback from %@: %@", (NSString *) [NSValue valueWithPointer:socket], callbackString);
				}

				[_callbackMsgs setValue:callbackString forKey:(NSString *) [NSValue valueWithPointer:socket]];

				OSMemoryBarrier();
			}
			@finally {
				if ([_callbackMsgs count] >= _expectedCallbackResults){
					[_callbackLock unlockWithCondition:GOT_CALLBACK_RESPONSE];
				}
				else {
					[_callbackLock unlockWithCondition:WAITING_FOR_CALLBACK_RESPONSE];
				}
			}


			[callbackString release];

			[socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
		}
	});

}

- (NSTimeInterval)socket:(GCDAsyncSocket*)socket shouldTimeoutReadWithTag:(long)tag elapsed:(NSTimeInterval)elapsed bytesDone:(NSUInteger)length
{
	return 0.0;
}

- (void)socketDidDisconnect:(GCDAsyncSocket*)socket withError:(NSError*)err
{
	// This callback can happen on either queue, yet each queue has private data
	// In order to ensure thread-safe reads from each collection, perform the actual disconnect logic on the appropriate queue

	dispatch_async(_listenQueue, ^{
		if ([_connectedListenSockets containsObject:socket])
		{
			[_connectedListenSockets removeObject:socket];

			if ([_connectedListenSocketsWithIconCallbacks containsObject:socket])
			{
				[_connectedListenSocketsWithIconCallbacks removeObject:socket];
				_connectedListenSocketsWithIconCallbacksCount = _connectedListenSocketsWithIconCallbacks.count;

				dispatch_async(dispatch_get_main_queue(), ^{
					[[ContentManager sharedInstance] repaintAllWindows];
				});
			}

			if ([_automaticCleanupPrograms containsObject:socket])
			{
				dispatch_async(dispatch_get_main_queue(), ^{
					[[ContentManager sharedInstance] removeAllIconsFor:socket.userData];
				});
			}

			dispatch_async(dispatch_get_main_queue(), ^{
				[[ContentManager sharedInstance] enableFileIconsFor:socket.userData enabled:false];
			});

			[_automaticCleanupPrograms removeObject:socket.userData];
		}
	});

	dispatch_async(_callbackQueue, ^{
		if ([_connectedCallbackSockets containsObject:socket])
		{
			[_connectedCallbackSockets removeObject:socket];
			_connectedCallbackSocketsCount = [_connectedCallbackSockets count];
		}
	});
}

- (void)replyString:(NSString*)text toSocket:(GCDAsyncSocket*)socket
{
	NSData* data = [[text stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	[socket writeData:data withTimeout:-1 tag:0];
}

- (void)start
{
	if (!_isRunning)
	{
		NSError* error = nil;

		if (![_listenSocket acceptOnInterface:@"localhost" port:33001 error:&error])
		{
			NSLog(@"Can not open listenSocket on port 33001");
			return;
		}

		if (![_callbackSocket acceptOnInterface:@"localhost" port:33002 error:&error])
		{
			NSLog(@"Can not open callbackSocket on port 33002");
			return;
		}

		_isRunning = YES;
	}
}

@end