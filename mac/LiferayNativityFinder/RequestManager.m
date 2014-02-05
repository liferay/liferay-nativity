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
 * - Fixed a race condition in menuItemsForFiles:. Now clearing _callbackMsgs
 * before requesting menu items for given paths
 */

#import "ContentManager.h"
#import "IconCache.h"
#import "JSONKit.h"
#import "RequestManager.h"

static RequestManager* sharedInstance = nil;

@implementation RequestManager

static double maxMenuItemsRequestWaitMilliSec = 250;

- (id)init
{
	if ((self = [super init]))
	{
		_listenQueue = dispatch_queue_create("listen queue", nil);
		_listenSocket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:_listenQueue];

		_callbackQueue = dispatch_queue_create("callback queue", nil);
		_callbackSocket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:_callbackQueue];

		_connectedListenSockets = [[NSMutableArray alloc] init];
		_connectedCallbackSockets = [[NSMutableArray alloc] init];
		_callbackMsgs = [[NSMutableDictionary alloc] init];

		_filterFolder = nil;

		_numberFormatter = [[NSNumberFormatter alloc] init];
		[_numberFormatter setNumberStyle:NSNumberFormatterDecimalStyle];

		_isRunning = NO;

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

	for (GCDAsyncSocket* socket in _connectedListenSockets)
	{
		[socket setDelegate:nil delegateQueue:NULL];
		[socket disconnect];
	}

	[_connectedListenSockets release];

	for (GCDAsyncSocket* socket in _connectedCallbackSockets)
	{
		[socket setDelegate:nil delegateQueue:NULL];
		[socket disconnect];
	}

	[_connectedCallbackSockets release];
	[_callbackMsgs release];

	[_numberFormatter release];

	[_filterFolder release];

	sharedInstance = nil;

	[super dealloc];
}

+ (RequestManager*)sharedInstance
{
	@synchronized(self)
	{
		if (sharedInstance == nil)
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

		[self replyString:@"-1" toSocket:sock];

		return;
	}
	if ([command isEqualToString:@"setFileIcons"])
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
	else if ([command isEqualToString:@"registerIcon"])
	{
		[self execRegisterIconCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"unregisterIcon"])
	{
		[self execUnregisterIconCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"setFilterPath"])
	{
		[self execSetFilterPathCmd:value replyTo:sock];
	}
	else
	{
		NSLog(@"LiferayNativityFinder: failed to find command: %@", command);

		[self replyString:@"-1" toSocket:sock];

		return;
	}
}

- (void)execEnableFileIconsCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSNumber* enabled = (NSNumber*)cmdData;

	[[ContentManager sharedInstance] enableFileIcons:enabled];

	[self replyString:@"1" toSocket:sock];
}

- (void)execRegisterIconCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSString* path = (NSString*)cmdData;

	NSNumber* index = [[IconCache sharedInstance] registerIcon:path];

	if (!index)
	{
		index = [NSNumber numberWithInt:-1];
	}

	[self replyString:[_numberFormatter stringFromNumber:index] toSocket:sock];
}

- (void)execRemoveAllFileIconsCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	[[ContentManager sharedInstance] removeAllIcons];

	[self replyString:@"1" toSocket:sock];
}

- (void)execRemoveFileIconsCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSArray* paths = (NSArray*)cmdData;

	[[ContentManager sharedInstance] removeIcons:paths];

	[self replyString:@"1" toSocket:sock];
}

- (void)execSetFileIconsCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSDictionary* iconDictionary = (NSDictionary*)cmdData;

	[[ContentManager sharedInstance] setIcons:iconDictionary filterByFolder:_filterFolder];

	[self replyString:@"1" toSocket:sock];
}

- (void)execSetFilterPathCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	[self setFilterFolder:(NSString*)cmdData];

	[self replyString:@"1" toSocket:sock];
}

- (void)execUnregisterIconCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSNumber* iconId = (NSNumber*)cmdData;

	[[IconCache sharedInstance] unregisterIcon:iconId];

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
	if ([_connectedCallbackSockets count] == 0)
	{
		return nil;
	}

	if (_filterFolder)
	{
		NSString* file = [files objectAtIndex:0];

		if (![file hasPrefix:_filterFolder])
		{
			return nil;
		}
	}

	NSDictionary* menuQueryDictionary = [[NSMutableDictionary alloc] init];

	[menuQueryDictionary setValue:@"getContextMenuList" forKey:@"command"];
	[menuQueryDictionary setValue:files forKey:@"value"];

	NSString* jsonString = [menuQueryDictionary JSONString];
	[menuQueryDictionary release];

	[_callbackMsgs removeAllObjects];

	NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	for (GCDAsyncSocket* callbackSocket in _connectedCallbackSockets)
	{
		[callbackSocket writeData:data withTimeout:-1 tag:0];
	}

	NSRunLoop* runLoop = [NSRunLoop currentRunLoop];

	NSDate* startDate = [NSDate date];

	while ([_callbackMsgs count] < [_connectedCallbackSockets count])
	{
		[runLoop runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];

		if ([_connectedCallbackSockets count] == 0)
		{
			return nil;
		}

		if (([startDate timeIntervalSinceNow] * -1000) > maxMenuItemsRequestWaitMilliSec)
		{
			NSLog(@"LiferayNativityFinder: menu item request timed out");

			break;
		}
	}

	NSMutableArray* menuItems = [[NSMutableArray alloc] init];

	for (NSValue* key in _callbackMsgs)
	{
		NSString* callbackMsg = [_callbackMsgs objectForKey:key];
		NSDictionary* responseDictionary = [callbackMsg objectFromJSONString];

		[menuItems addObjectsFromArray:(NSArray*)[responseDictionary objectForKey:@"value"]];
	}

	return [menuItems autorelease];
}

- (void)socket:(GCDAsyncSocket*)socket didAcceptNewSocket:(GCDAsyncSocket*)newSocket
{

	if (socket == _listenSocket)
	{
		[_connectedListenSockets addObject:newSocket];
	}
	if (socket == _callbackSocket)
	{
		[_connectedCallbackSockets addObject:newSocket];
	}

	[newSocket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
}

- (void)socket:(GCDAsyncSocket*)socket didConnectToHost:(NSString*)host port:(UInt16)port
{
	[socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
}

- (void)socket:(GCDAsyncSocket*)socket didReadData:(NSData*)data withTag:(long)tag
{
	if ([_connectedListenSockets containsObject:socket])
	{
		[self execCommand:[data subdataWithRange:NSMakeRange(0, [data length] - 2)] replyTo:socket];

		[socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
	}

	if ([_connectedCallbackSockets containsObject:socket])
	{
		NSData* strData = [data subdataWithRange:NSMakeRange(0, [data length] - 2)];
		NSString* callbackString = [[NSString alloc] initWithData:strData encoding:NSUTF8StringEncoding];

		[_callbackMsgs setValue:callbackString forKey:(NSString*)[NSValue valueWithPointer:socket]];

		[callbackString release];

		[socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
	}
}

- (NSTimeInterval)socket:(GCDAsyncSocket*)socket shouldTimeoutReadWithTag:(long)tag elapsed:(NSTimeInterval)elapsed bytesDone:(NSUInteger)length
{
	return 0.0;
}

- (void)socketDidDisconnect:(GCDAsyncSocket*)socket withError:(NSError*)err
{
	if ([_connectedListenSockets containsObject:socket])
	{
		[_connectedListenSockets removeObject:socket];

		[[ContentManager sharedInstance] enableFileIcons:false];
	}

	if ([_connectedCallbackSockets containsObject:socket])
	{
		[_connectedCallbackSockets removeObject:socket];
	}
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
			return;
		}

		if (![_callbackSocket acceptOnInterface:@"localhost" port:33002 error:&error])
		{
			return;
		}

		_isRunning = YES;
	}
}

@end
