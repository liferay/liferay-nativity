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

#import "ContentManager.h"
#import <AppKit/NSWorkspace.h>
#import <AppKit/NSApplication.h>
#import <AppKit/NSWindow.h>
#import <Carbon/Carbon.h>

static ContentManager* sharedInstance = nil;

@implementation ContentManager
- init
{
	self = [super init];

	if (self)
	{
		_fileNamesCache = [[NSMutableDictionary alloc] init];
		_fileIconsEnabled = FALSE;
	}

	return self;
}

- (void)dealloc
{
	[self removeAllIcons];
	[_fileNamesCache release];
	sharedInstance = nil;

	[super dealloc];
}

+ (ContentManager*)sharedInstance
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

- (void)enableFileIcons:(BOOL)enable
{
	_fileIconsEnabled = enable;

	[self repaintAllWindows];
}

- (NSNumber*)iconByPath:(NSString*)path
{
	if (!_fileIconsEnabled)
	{
		return nil;
	}

	NSNumber* result = [_fileNamesCache objectForKey:path];

	return result;
}

- (void)removeAllIcons
{
	[_fileNamesCache removeAllObjects];

	[self repaintAllWindows];
}

- (void)removeIcons:(NSArray*)paths
{
	for (NSString* path in paths)
	{
		[_fileNamesCache removeObjectForKey:path];
	}

	[self repaintAllWindows];
}

- (void)repaintAllWindows
{
	NSArray* windows = [[NSApplication sharedApplication] windows];

	for (int i = 0; i < [windows count]; ++i)
	{
		NSWindow* window = [windows objectAtIndex:i];

		if (![window isVisible])
		{
			continue;
		}

		[window update];

		if ([[window className] isEqualToString:@"TBrowserWindow"])
		{
			NSObject* controller = [window browserWindowController];

			[controller updateViewLayout];
			[controller viewContentChanged];
			[controller drawCompletelyIntoBackBuffer];
		}
	}
}

- (void)setIcons:(NSDictionary*)iconDictionary filterByFolder:(NSString*)filterFolder
{
	for (NSString* path in iconDictionary)
	{
		if (filterFolder && ![path hasPrefix:filterFolder])
		{
			continue;
		}

		NSNumber* iconId = [iconDictionary objectForKey:path];

		if ([iconId intValue] == -1)
		{
			[_fileNamesCache removeObjectForKey:path];
		}
		else
		{
			[_fileNamesCache setObject:iconId forKey:path];
		}
	}

	[self repaintAllWindows];
}

@end
