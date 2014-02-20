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
 * - (Andrew Rondeau) Added the ability to group icons by connection, this allows
 * disabling / clearing icons for one program, while leaving another unaffected
 */

#import <AppKit/NSApplication.h>
#import <AppKit/NSWindow.h>
#import <objc/runtime.h>
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
		_fileNamesCacheByConnection = [[NSMapTable alloc] initWithKeyOptions:NSMapTableObjectPointerPersonality valueOptions:NSMapTableStrongMemory capacity:0];
		_fileIconsEnabled = [[NSHashTable alloc] initWithOptions:NSHashTableObjectPointerPersonality capacity:0];
	}

	return self;
}

- (void)dealloc
{
	for(id connection in _fileNamesCacheByConnection)
	{
		[self removeAllIconsFor:connection];
	}
	
	[_fileNamesCacheByConnection release];
	[_fileIconsEnabled release];
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

- (void)enableFileIconsFor:(id)connection enabled:(BOOL)enable
{
	if (enable)
	{
		[_fileIconsEnabled addObject:connection];
	}
	else
	{
		[_fileIconsEnabled removeObject:connection];
	}

	[self repaintAllWindows];
}

- (NSNumber*)iconByPath:(NSString*)path
{
	NSString* normalizedPath = [path decomposedStringWithCanonicalMapping];

	for(id connection in _fileIconsEnabled)
	{
		NSDictionary* fileNamesCache = [_fileNamesCacheByConnection objectForKey:connection];
		
		if (nil != fileNamesCache)
		{
			NSNumber* result = [fileNamesCache objectForKey:normalizedPath];
		
			if (nil != result)
			{
				return result;
			}
		}
	}
	
	return nil;
}

- (void)removeAllIconsFor:(id)connection
{
	[_fileNamesCacheByConnection removeObjectForKey:connection];

	[self repaintAllWindows];
}

- (void)removeIconsFor:(id)connection paths:(NSArray*)paths
{
	NSMutableDictionary* fileNamesCache = [_fileNamesCacheByConnection objectForKey:connection];
	
	if (nil != fileNamesCache)
	{
		for (NSString* path in paths)
		{
			NSString* normalizedPath = [path decomposedStringWithCanonicalMapping];

			[fileNamesCache removeObjectForKey:normalizedPath];
		}
	}

	[self repaintAllWindows];
}

- (void)repaintAllWindows
{
	NSArray* windows = [[NSApplication sharedApplication] windows];

	for (int i = 0; i < [windows count]; i++)
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

				struct TFENodeVector* targetPath;

				if ([browserWindowController respondsToSelector:@selector(targetPath)])
				{
					// 10.7 & 10.8
					targetPath = [browserWindowController targetPath];
				}
				else if ([browserWindowController respondsToSelector:@selector(activeContainer)])
				{
					// 10.9
					targetPath = [[browserWindowController activeContainer] targetPath];
				}
				else
				{
					NSLog(@"LiferayNativityFinder: refreshing icon badges failed");

					return;
				}

				NSArray* folderPaths = [menuManager pathsForNodes:targetPath];

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
				if ([browserWindowController respondsToSelector:@selector(browserViewController)])
				{
					// 10.7 & 10.8
					NSObject* browserViewController = [browserWindowController browserViewController];

					NSObject* browserView = [browserViewController browserView];

					dispatch_async(dispatch_get_main_queue(), ^{[browserView setNeedsDisplay:YES];});
				}
				else if ([browserWindowController respondsToSelector:@selector(activeBrowserViewController)])
				{
					// 10.9
					NSObject* browserViewController = [browserWindowController activeBrowserViewController];

					NSObject* browserView = [browserViewController browserView];

					if ([browserView isKindOfClass:(id)objc_getClass("TListView")])
					{
						// List or Coverflow View
						[self setNeedsDisplayForListView:browserView];
					}
					else
					{
						// Icon or Column View
						dispatch_async(dispatch_get_main_queue(), ^{[browserView setNeedsDisplay:YES];});
					}
				}
				else
				{
					NSLog(@"LiferayNativityFinder: refreshing icon badges failed");

					return;
				}
			}
		}
	}
}

- (void)setNeedsDisplayForListView:(NSView*)view
{
	NSArray* subviews = [view subviews];
		}
	}
}

- (void)setIconsFor:(id)connection iconIdsByPath:(NSDictionary*)iconDictionary filterByFolder:(NSString*)filterFolder
{
	NSMutableDictionary* fileNamesCache = [_fileNamesCacheByConnection objectForKey:connection];
	
	if (nil == fileNamesCache)
	{
		fileNamesCache = [[NSMutableDictionary alloc] init];
		[_fileNamesCacheByConnection setObject:fileNamesCache forKey:connection];
	}
	
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
			[fileNamesCache removeObjectForKey:normalizedPath];
		}
		else
		{
			[fileNamesCache setObject:iconId forKey:normalizedPath];
		}
	}

	if (0 == fileNamesCache.count)
	{
		[_fileNamesCacheByConnection removeObjectForKey:connection];
	}
	
	[self repaintAllWindows];
}

@end

