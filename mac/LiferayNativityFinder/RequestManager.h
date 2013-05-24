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

#import <Foundation/Foundation.h>
#import "GCDAsyncSocket.h"
#import "RequestManager.h"

@interface RequestManager : NSObject
{
	dispatch_queue_t _listenQueue;
	dispatch_queue_t _callbackQueue;
	
	GCDAsyncSocket* _listenSocket;
	GCDAsyncSocket* _callbackSocket;

	NSMutableArray* _connectedListenSockets;
	NSMutableArray* _connectedCallbackSockets;
	NSMutableDictionary* _callbackMsgs;

	NSNumberFormatter* _numberFormatter;
	NSString* _filterFolder;

	BOOL _isRunning;
}

@property (nonatomic, retain) NSString* filterFolder;

+ (RequestManager*)sharedInstance;

- (void)menuItemClicked:(NSDictionary*)actionDictionary;
- (NSArray*)menuItemsForFiles:(NSArray*)files;
- (void)start;

@end