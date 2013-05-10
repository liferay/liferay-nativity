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

#import "RequestManager.h"
#import "ContentManager.h"
#import "IconCache.h"
#import "JSONKit.h"
#include "MenuManager.h"

@implementation RequestManager

- (id)init
{
	if ((self = [super init]))
	{
		listenSocket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];
		listenSocket2 = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];

		connectedSockets = [[NSMutableArray alloc] init];
		callbackSockets = [[NSMutableArray alloc] init];
		callbackMsgs = [[NSMutableDictionary alloc] init];

		rootFolder = nil;

		numberFormatter = [[NSNumberFormatter alloc] init];
		[numberFormatter setNumberStyle:NSNumberFormatterDecimalStyle];

		isRunning = NO;

		[self start];
	}

	return self;
}

+ (RequestManager*)sharedInstance
{
	static RequestManager* sharedInstance = nil;

	static dispatch_once_t onceToken;

	dispatch_once(&onceToken, ^{ sharedInstance = [[RequestManager alloc] init]; });

	return sharedInstance;
}

- (void)execCommand:(NSData*)data replyTo:(GCDAsyncSocket*)sock
{
	NSDictionary* jsonDictionary = [data objectFromJSONData];

	NSString* command = [jsonDictionary objectForKey:@"command"];
	NSData* value = [jsonDictionary objectForKey:@"value"];

	if (!command)
	{
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
		[self execEnableOverlaysCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"registerIcon"])
	{
		[self execRegisterIconCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"unregisterIcon"])
	{
		[self execUnregisterIconCmd:value replyTo:sock];
	}
	else if ([command isEqualToString:@"setRootFolder"])
	{
		[self execSetRootFolderCmd:value replyTo:sock];
	}
	else
	{
		[self replyString:@"-1" toSocket:sock];
	}
}

- (void)execEnableOverlaysCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	NSNumber* enabled = (NSNumber*)cmdData;

	[[ContentManager sharedInstance] enableOverlays:enabled];

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

	[self replyString:[numberFormatter stringFromNumber:index] toSocket:sock];
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

	[[ContentManager sharedInstance] setIcons:iconDictionary filterByFolder:rootFolder];

	[self replyString:@"1" toSocket:sock];
}

- (void)execSetRootFolderCmd:(NSData*)cmdData replyTo:(GCDAsyncSocket*)sock
{
	rootFolder = (NSString*)cmdData;

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
	if ([callbackSockets count] == 0)
	{
		return;
	}

	NSDictionary* menuExecDictionary = [[NSMutableDictionary alloc] init];

	[menuExecDictionary setValue:@"menuExec" forKey:@"command"];

	[menuExecDictionary setValue:actionDictionary forKey:@"value"];

	NSString* jsonString = [menuExecDictionary JSONString];

	[menuExecDictionary release];

	NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	for (GCDAsyncSocket* callbackSocket in callbackSockets)
	{
		[callbackSocket writeData:data withTimeout:-1 tag:0];
	}
}

- (NSArray*)menuItemsForFiles:(NSArray*)files
{
	if ([callbackSockets count] == 0)
	{
		return nil;
	}

	if (rootFolder)
	{
		NSString* file = [files objectAtIndex:0];

		if (![file hasPrefix:rootFolder])
		{
			return nil;
		}
	}

	NSDictionary* menuQueryDictionary = [[NSMutableDictionary alloc] init];

	[menuQueryDictionary setValue:@"menuQuery" forKey:@"command"];
	[menuQueryDictionary setValue:files forKey:@"value"];

	NSString* jsonString = [menuQueryDictionary JSONString];
	[menuQueryDictionary release];

	NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	for (GCDAsyncSocket* callbackSocket in callbackSockets)
	{
		[callbackSocket writeData:data withTimeout:-1 tag:0];
	}

	NSRunLoop* runLoop = [NSRunLoop currentRunLoop];

	[callbackMsgs removeAllObjects];

	while ([callbackMsgs count] != [callbackSockets count])
	{
		[runLoop runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];

		if ([callbackSockets count] == 0)
		{
			return nil;
		}
	}

	NSMutableArray* menuItems = [[NSMutableArray alloc] init];

	for (NSValue* key in callbackMsgs)
	{
		NSString* callbackMsg = [callbackMsgs objectForKey:key];
		NSDictionary* responseDictionary = [callbackMsg objectFromJSONString];

		[menuItems addObjectsFromArray:(NSArray*)[responseDictionary objectForKey:@"value"]];
	}

	return [menuItems autorelease];
}

- (void)socket:(GCDAsyncSocket*)socket didAcceptNewSocket:(GCDAsyncSocket*)newSocket
{

	if (socket == listenSocket)
	{
		[connectedSockets addObject:newSocket];
	}
	if (socket == listenSocket2)
	{
		[callbackSockets addObject:newSocket];
	}

	[newSocket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
}

- (void)socket:(GCDAsyncSocket*)socket didConnectToHost:(NSString*)host port:(UInt16)port
{
	[socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
}

- (void)socket:(GCDAsyncSocket*)socket didReadData:(NSData*)data withTag:(long)tag
{
	if ([connectedSockets containsObject:socket])
	{
		[self execCommand:[data subdataWithRange:NSMakeRange(0, [data length] - 2)] replyTo:socket];

		[socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
	}

	if ([callbackSockets containsObject:socket])
	{
		NSData* strData = [data subdataWithRange:NSMakeRange(0, [data length] - 2)];
		NSString* callbackString = [[NSString alloc] initWithData:strData encoding:NSUTF8StringEncoding];

		[callbackMsgs setValue:callbackString forKey:(NSString*)[NSValue valueWithPointer:socket]];

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
	if ([connectedSockets containsObject:socket])
	{
		[connectedSockets removeObject:socket];

		[[ContentManager sharedInstance] enableOverlays:false];
	}

	if ([callbackSockets containsObject:socket])
	{
		[callbackSockets removeObject:socket];
	}
}

- (void)replyString:(NSString*)text toSocket:(GCDAsyncSocket*)socket
{
	NSData* data = [[text stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	[socket writeData:data withTimeout:-1 tag:0];
}

- (void)start
{
	if (!isRunning)
	{
		NSError* error = nil;

		if (![listenSocket acceptOnPort:33001 error:&error])
		{
			return;
		}

		if (![listenSocket2 acceptOnPort:33002 error:&error])
		{
			return;
		}

		isRunning = YES;
	}
}

@end
