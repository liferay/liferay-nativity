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
#include "MenuManager.h"

@implementation RequestManager

- (id)init
{
	if((self = [super init]))
	{
		listenSocket = [[AsyncSocket alloc] initWithDelegate:self];
        listenSocket2 = [[AsyncSocket alloc] initWithDelegate:self];
        
		connectedSocket = nil;
        callbackSocket = nil;
        callbackCondition = nil;
        callbackMsg = nil;
        
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
	dispatch_once(&onceToken, ^{
        sharedInstance = [[RequestManager alloc] init];
	});
    
    return sharedInstance;
}

- (void)replyString : (NSString*) text toSocket: (AsyncSocket*) sock
{
    NSData* data = [[text stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];
    [data retain];
    
    [sock writeData: data withTimeout:-1 tag:0];
}

- (void)start
{
	if(!isRunning)
	{
		NSError *error = nil;
		if(![listenSocket acceptOnPort:33001 error:&error])
		{
            return;
		}
        
        if(![listenSocket2 acceptOnPort:33002 error:&error])
		{
            return;
		}
        
		isRunning = YES;
        
	}
}

- (NSRunLoop*)onSocket:(AsyncSocket *)sock wantsRunLoopForNewSocket:(AsyncSocket *)newSocket
{
    return [[NSRunLoop alloc] init];
}

- (void)onSocket:(AsyncSocket *)sock didAcceptNewSocket:(AsyncSocket *)newSocket
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

- (void)onSocket:(AsyncSocket *)sock didConnectToHost:(NSString *)host port:(UInt16)port
{
	[sock readDataToData:[AsyncSocket CRLFData] withTimeout:-1 tag:0];
}

- (void) execEnableOverlaysCmd: (NSArray*) cmdData replyTo:(AsyncSocket*) sock
{
    if ([cmdData count] != 2)
        return;
    
    NSString* enabled = (NSString*)[cmdData objectAtIndex:1];
  
    [[ContentManager sharedInstance] enableOverlays: [enabled isEqualToString:@"1"]];
    [self replyString:@"1" toSocket:sock];
    
}

- (void) execRegisterIconCmd: (NSArray*) cmdData replyTo:(AsyncSocket*) sock
{
    if ([cmdData count] != 2)
        return;
    
    NSString* path = (NSString*)[cmdData objectAtIndex:1];
    
    NSNumber* index = [[IconCache sharedInstance] registerIcon : path];
    [self replyString:[numberFormatter stringFromNumber:index] toSocket:sock]; 
}

- (void) execUnregisterIconCmd: (NSArray*) cmdData replyTo:(AsyncSocket*) sock
{
    [self replyString:@"1" toSocket:sock];
}

- (void) execRemoveFileIconCmd: (NSArray*) cmdData replyTo:(AsyncSocket*) sock
{
    if ([cmdData count] != 2)
        return;
    
    NSString* fileName = (NSString*)[cmdData objectAtIndex:1];
    
    [[ContentManager sharedInstance] removeIconFromFile:fileName];
    [self replyString:@"1" toSocket:sock];
}

- (void) execRemoveFileIconsCmd: (NSArray*) cmdData replyTo:(AsyncSocket*) sock
{
    NSUInteger cmdDataCount = [cmdData count];

    if (cmdDataCount < 2)
        return;

    for (int i = 1; i < cmdDataCount; i++)
    {
        NSString* fileName = (NSString*)[cmdData objectAtIndex:i];

        [[ContentManager sharedInstance] removeIconFromFile:fileName];
    }

    [self replyString:@"1" toSocket:sock];
}

- (void) execSetFileIconCmd: (NSArray*) cmdData replyTo:(AsyncSocket*) sock
{
    if ([cmdData count] != 3)
        return;
    
    NSString* fileName = (NSString*)[cmdData objectAtIndex:1];
    NSString* iconIndex = (NSString*)[cmdData objectAtIndex:2];
    
    [[ContentManager sharedInstance] setIcon:[numberFormatter numberFromString:iconIndex] forFile:fileName];
    [self replyString:@"1" toSocket:sock];
}

- (void) execSetFileIconsCmd: (NSArray*) cmdData replyTo:(AsyncSocket*) sock
{
    NSUInteger cmdDataCount = [cmdData count];

    if ((cmdDataCount < 3) || (cmdDataCount + 1) % 2)
        return;

    NSDictionary* iconDictionary = [[NSMutableDictionary alloc] init];

    for (int i = 1; i < cmdDataCount - 1; i += 2)
    {
        NSString* iconIdString = [cmdData objectAtIndex:(i+1)];
        NSNumber* iconId = [numberFormatter numberFromString:iconIdString];

        NSString* path = [cmdData objectAtIndex:i];

        [iconDictionary setObject:iconId forKey:path];
    }

    [[ContentManager sharedInstance] setIcons:iconDictionary];
    [self replyString:@"1" toSocket:sock];
}

- (void) execSetMenuTitleCmd: (NSArray*) cmdData replyTo:(AsyncSocket*) sock
{
    if ([cmdData count] != 2)
        return;
    
    NSString* title = (NSString*)[cmdData objectAtIndex:1];
    
    [[MenuManager sharedInstance] setMenuTitle: title];
    [self replyString:@"1" toSocket:sock];
}

- (void) execCommand: (NSArray*) cmdData replyTo:(AsyncSocket*) sock
{
    if ([cmdData count] == 0)
        return;
    
    NSString* cmdId = (NSString*)[cmdData objectAtIndex:0];
    if ([cmdId isEqualToString:@"setFileIcon"])
        [self execSetFileIconCmd: cmdData replyTo:sock];
    else if ([cmdId isEqualToString:@"setFileIcons"])
        [self execSetFileIconsCmd: cmdData replyTo:sock];
    else if ([cmdId isEqualToString:@"removeFileIcon"])
        [self execRemoveFileIconCmd: cmdData replyTo:sock];
    else if ([cmdId isEqualToString:@"removeFileIcons"])
        [self execRemoveFileIconsCmd: cmdData replyTo:sock];
    else if ([cmdId isEqualToString:@"enableOverlays"])
        [self execEnableOverlaysCmd: cmdData replyTo:sock];
    else if ([cmdId isEqualToString:@"registerIcon"])
        [self execRegisterIconCmd: cmdData replyTo:sock];
    else if ([cmdId isEqualToString:@"unregisterIcon"])
        [self execUnregisterIconCmd: cmdData replyTo:sock];
    else if ([cmdId isEqualToString:@"setMenuTitle"])
        [self execSetMenuTitleCmd: cmdData replyTo:sock];

}

- (void)onSocket:(AsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag
{
    if (sock == connectedSocket)
    {
        NSData *strData = [data subdataWithRange:NSMakeRange(0, [data length] - 2)];
	    NSString *msg = [[NSString alloc] initWithData:strData encoding:NSUTF8StringEncoding];
    
        NSArray* cmdData =[msg componentsSeparatedByString:@":"]; 
        [self execCommand: cmdData replyTo: sock];
    
        [sock readDataToData:[AsyncSocket CRLFData] withTimeout:-1 tag:0];
    }
    if (sock == callbackSocket)
    {
        NSData *strData = [data subdataWithRange:NSMakeRange(0, [data length] - 2)];
	    callbackMsg = [[NSString alloc] initWithData:strData encoding:NSUTF8StringEncoding];
        
        [sock readDataToData:[AsyncSocket CRLFData] withTimeout:-1 tag:0];
        if (callbackCondition != nil)
        {
            [callbackCondition signal];
        }
    }
}

- (NSTimeInterval)onSocket:(AsyncSocket *)sock
  shouldTimeoutReadWithTag:(long)tag
				   elapsed:(NSTimeInterval)elapsed
				 bytesDone:(NSUInteger)length
{
	return 0.0;
}

- (void)onSocket:(AsyncSocket *)sock willDisconnectWithError:(NSError *)err
{
}

- (void)onSocketDidDisconnect:(AsyncSocket *)sock
{
	if (connectedSocket == sock)
        connectedSocket = nil;

    if (callbackSocket == sock)
        callbackSocket = nil;
}

- (NSArray* ) menuItemsForFiles: (NSArray*) files
{
    if (callbackSocket == nil)
        return nil;
    
    NSString* text = @"menuQuery";
    for(int i=0;i<[files count];++i)
    {
        text = [text stringByAppendingString:@":"];
        text = [text stringByAppendingString:[files objectAtIndex:i]];
    }
    
    NSData* data = [[text stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];
    [data retain];
    
    [callbackSocket writeData:data withTimeout:-1 tag:0];
    
    NSRunLoop* runLoop = [NSRunLoop currentRunLoop];
    callbackMsg = nil;
    while (callbackMsg == nil)
        [runLoop runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];
    
    return [callbackMsg componentsSeparatedByString:@":"];
}

- (void) menuItemClicked: (NSNumber*) item
{
    if (callbackSocket == nil)
        return;
    
    NSString* text = @"menuExec:";
  
    text = [text stringByAppendingString:[item stringValue]];
    
    NSData* data = [[text stringByAppendingString:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding];
    [data retain];

    [callbackSocket writeData:data withTimeout:-1 tag:0];
}


@end
