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
		listenSocket = [[AsyncSocket alloc] initWithDelegate:self];
		listenSocket2 = [[AsyncSocket alloc] initWithDelegate:self];

		connectedSocket = nil;
		callbackSocket = nil;
		callbackCondition = nil;
		callbackMsg = nil;
		rootFolder = nil;

		numberFormatter = [[NSNumberFormatter alloc] init];
		[numberFormatter setNumberStyle:NSNumberFormatterDecimalStyle];

		[listenSocket setRunLoopModes:[NSArray arrayWithObject:NSRunLoopCommonModes]];
		[listenSocket2 setRunLoopModes:[NSArray arrayWithObject:NSRunLoopCommonModes]];

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

- (void)execCommand:(NSData*)data replyTo:(AsyncSocket*)sock
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
	else if ([command isEqualToString:@"setMenuTitle"])
	{
		[self execSetMenuTitleCmd:value replyTo:sock];
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

- (void)execEnableOverlaysCmd:(NSData*)cmdData replyTo:(AsyncSocket*)sock
{
	NSNumber* enabled = (NSNumber*)cmdData;

	[[ContentManager sharedInstance] enableOverlays:enabled];

	[self replyString:@"1" toSocket:sock];
}

- (void)execRegisterIconCmd:(NSData*)cmdData replyTo:(AsyncSocket*)sock
{
	NSString* path = (NSString*)cmdData;

	NSNumber* index = [[IconCache sharedInstance] registerIcon:path];

	if (!index)
	{
		index = [NSNumber numberWithInt:-1];
	}

	[self replyString:[numberFormatter stringFromNumber:index] toSocket:sock];
}

- (void)execRemoveAllFileIconsCmd:(NSData*)cmdData replyTo:(AsyncSocket*)sock
{
	[[ContentManager sharedInstance] removeAllIcons];

	[self replyString:@"1" toSocket:sock];
}

- (void)execRemoveFileIconsCmd:(NSData*)cmdData replyTo:(AsyncSocket*)sock
{
	NSArray* paths = (NSArray*)cmdData;

	[[ContentManager sharedInstance] removeIcons:paths];

	[self replyString:@"1" toSocket:sock];
}

- (void)execSetFileIconsCmd:(NSData*)cmdData replyTo:(AsyncSocket*)sock
{
	NSDictionary* iconDictionary = (NSDictionary*)cmdData;

	[[ContentManager sharedInstance] setIcons:iconDictionary filterByFolder:rootFolder];

	[self replyString:@"1" toSocket:sock];
}

- (void)execSetMenuTitleCmd:(NSData*)cmdData replyTo:(AsyncSocket*)sock
{
	NSString* title = (NSString*)cmdData;

	[[MenuManager sharedInstance] setMenuTitle:title];

	[self replyString:@"1" toSocket:sock];
}

- (void)execSetRootFolderCmd:(NSData*)cmdData replyTo:(AsyncSocket*)sock
{
	rootFolder = (NSString*)cmdData;
	[rootFolder retain];

	[self replyString:@"1" toSocket:sock];
}

- (void)execUnregisterIconCmd:(NSData*)cmdData replyTo:(AsyncSocket*)sock
{
	NSNumber* iconId = (NSNumber*)cmdData;

	[[IconCache sharedInstance] unregisterIcon:iconId];

	[self replyString:@"1" toSocket:sock];
}

- (void)menuItemClicked:(NSDictionary*)actionDictionary
{
	if (callbackSocket == nil)
	{
		return;
	}

	NSDictionary* menuExecDictionary = [[NSMutableDictionary alloc] init];

	[menuExecDictionary setValue:@"menuExec" forKey:@"command"];

	[menuExecDictionary setValue:actionDictionary forKey:@"value"];

	NSString* jsonString = [menuExecDictionary JSONString];

	NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];
	[data retain];

	[callbackSocket writeData:data withTimeout:-1 tag:0];
}

- (NSArray*)menuItemsForFiles:(NSArray*)files
{
	if (callbackSocket == nil)
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

	NSData* data = [[jsonString stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];
	[data retain];

	[callbackSocket writeData:data withTimeout:-1 tag:0];

	NSRunLoop* runLoop = [NSRunLoop currentRunLoop];

	callbackMsg = nil;

	while (callbackMsg == nil)
	{
		[runLoop runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];

		if (callbackSocket == nil)
		{
			return nil;
		}
	}

	NSDictionary* responseDictionary = [callbackMsg objectFromJSONString];

	return (NSArray*)[responseDictionary objectForKey:@"value"];
}

- (void)onSocket:(AsyncSocket*)sock didAcceptNewSocket:(AsyncSocket*)newSocket
{

	if (sock == listenSocket)
	{
		[connectedSocket disconnect];
		connectedSocket = newSocket;
	}
	if (sock == listenSocket2)
	{
		[callbackSocket disconnect];
		callbackSocket = newSocket;
	}
}

- (void)onSocket:(AsyncSocket*)sock didConnectToHost:(NSString*)host port:(UInt16)port
{
	[sock readDataToData:[AsyncSocket CRLFData] withTimeout:-1 tag:0];
}

- (void)onSocket:(AsyncSocket*)sock didReadData:(NSData*)data withTag:(long)tag
{
	if (sock == connectedSocket)
	{
		[self execCommand:[data subdataWithRange:NSMakeRange(0, [data length] - 2)] replyTo:sock];

		[sock readDataToData:[AsyncSocket CRLFData] withTimeout:-1 tag:0];
	}
	if (sock == callbackSocket)
	{
		NSData* strData = [data subdataWithRange:NSMakeRange(0, [data length] - 2)];
		callbackMsg = [[NSString alloc] initWithData:strData encoding:NSUTF8StringEncoding];

		[sock readDataToData:[AsyncSocket CRLFData] withTimeout:-1 tag:0];
		if (callbackCondition != nil)
		{
			[callbackCondition signal];
		}
	}
}

- (NSTimeInterval)onSocket:(AsyncSocket*)sock shouldTimeoutReadWithTag:(long)tag elapsed:(NSTimeInterval)elapsed bytesDone:(NSUInteger)length
{
	return 0.0;
}

- (NSRunLoop*)onSocket:(AsyncSocket*)sock wantsRunLoopForNewSocket:(AsyncSocket*)newSocket
{
	return [[NSRunLoop alloc] init];
}

- (void)onSocket:(AsyncSocket*)sock willDisconnectWithError:(NSError*)err
{
}

- (void)onSocketDidDisconnect:(AsyncSocket*)sock
{
	if (connectedSocket == sock)
	{
		connectedSocket = nil;

		[[ContentManager sharedInstance] enableOverlays:false];
	}

	if (callbackSocket == sock)
	{
		callbackSocket = nil;
	}
}

- (void)replyString:(NSString*)text toSocket:(AsyncSocket*)sock
{
	NSData* data = [[text stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];

	[data retain];

	[sock writeData:data withTimeout:-1 tag:0];
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
