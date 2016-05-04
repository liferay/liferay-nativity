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

#import <FinderSync/FinderSync.h>
#import <Foundation/Foundation.h>
#import "GCDAsyncSocket.h"

@interface RequestManager : NSObject
{
	NSData* _callbackData;
	NSConditionLock* _callbackLock;
	BOOL _connected;
	NSMutableDictionary* _menuUuidDictionary;
	NSMutableSet* _observedFolders;
	int _port;
	BOOL _removeBadgesOnClose;
	GCDAsyncSocket* _socket;
}

@property (nonatomic, retain) NSMutableSet* registeredBadges;
@property (nonatomic, retain) NSMutableSet* registeredUrls;

+ (RequestManager*) sharedInstance;
- (void) connect;
- (NSMenu*) menuForFiles:(NSArray*)files;
- (void) refreshBadges;
- (void) requestFileBadgeId:(NSURL*)url;
- (void) sendMenuItemClicked:(NSString*)action;
- (void) sendObservingFolder:(NSURL*)url start:(BOOL)start;

@end
