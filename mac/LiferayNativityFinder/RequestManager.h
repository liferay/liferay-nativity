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
 * - (Andrew Rondeau) Started tracking programname in the socket's userData, so
 * different programs don't conflict with each other
 * - (Andrew Rondeau) Switched to NSHashTable for performance reasons
 * - (Andrew Rondeau) Added command to repaint all windows, added ability to query
 * the program for the file's icon, made getting the context manu faster
 */

#import <Foundation/Foundation.h>
#import "GCDAsyncSocket.h"
#import "RequestManager.h"

@interface RequestManager : NSObject
{
	dispatch_queue_t _listenQueue;
	dispatch_queue_t _callbackQueue;
	dispatch_semaphore_t _callbackSemaphore;
	
	GCDAsyncSocket* _listenSocket;
	GCDAsyncSocket* _callbackSocket;

	NSHashTable* _connectedListenSockets;
	NSHashTable* _connectedListenSocketsWithIconCallbacks;
	NSHashTable* _connectedCallbackSockets;
	NSMutableDictionary* _callbackMsgs;
	
	NSHashTable* _automaticCleanupPrograms;

	NSNumberFormatter* _numberFormatter;
	NSString* _filterFolder; // TODO: This should probably be a dictionary at some point

	BOOL _isRunning;
	
	id _allIconsConnection; // Key for identifying icon management requests that are global. This is purely for backwards compatibility
}

@property (nonatomic, retain) NSString* filterFolder;

+ (RequestManager*)sharedInstance;

- (void)menuItemClicked:(NSDictionary*)actionDictionary;
- (NSArray*)menuItemsForFiles:(NSArray*)files;
- (NSArray*)iconIdForFile:(NSString*)file;
- (void)start;

@end