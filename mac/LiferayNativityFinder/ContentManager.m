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

#import <AppKit/NSApplication.h>
#import <AppKit/NSWindow.h>
#import "ContentManager.h"
#import "MenuManager.h"
#import "RequestManager.h"

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

	NSString* normalizedPath = [path decomposedStringWithCanonicalMapping];

	NSNumber* result = [_fileNamesCache objectForKey:normalizedPath];

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
		NSString* normalizedPath = [path decomposedStringWithCanonicalMapping];

		[_fileNamesCache removeObjectForKey:normalizedPath];
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

		MenuManager* menuManager = [MenuManager sharedInstance];
		RequestManager* requestManager = [RequestManager sharedInstance];

		if ([[window className] isEqualToString:@"TBrowserWindow"])
		{
			NSObject* browserWindowController = [window browserWindowController];

			BOOL repaintWindow = YES;

			NSString* filterFolder = [requestManager filterFolder];

			if (filterFolder)
			{
				repaintWindow = NO;

				NSArray* folderPaths = [menuManager pathsForNodes:[browserWindowController targetPath]];

				for (NSString* folderPath in folderPaths)
				{
					if ([folderPath hasPrefix:filterFolder] || [filterFolder hasPrefix:folderPath])
					{
						repaintWindow = YES;

						break;
					}
				}
			}

			if (repaintWindow)
			{
				NSObject* browserViewController;

				if ([browserWindowController respondsToSelector:@selector(browserViewController)])
				{
					// 10.7 & 10.8
					browserViewController = [browserWindowController browserViewController];
				}
				else if ([browserWindowController respondsToSelector:@selector(activeBrowserViewController)])
				{
					// 10.9
					browserViewController = [browserWindowController activeBrowserViewController];
				}
				else
				{
					NSLog(@"LiferayNativityFinder: refreshing icon badges failed");

					return;
				}

				NSObject* browserView = [browserViewController browserView];

				[browserView setNeedsDisplay:YES];
			}
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

		NSString* normalizedPath = [path decomposedStringWithCanonicalMapping];
		NSNumber* iconId = [iconDictionary objectForKey:path];

		if ([iconId intValue] == -1)
		{
			[_fileNamesCache removeObjectForKey:normalizedPath];
		}
		else
		{
			[_fileNamesCache setObject:iconId forKey:normalizedPath];
		}
	}

	[self repaintAllWindows];
}

@end
