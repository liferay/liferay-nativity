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

#import "MenuManager.h"
#import "Finder/finder.h"
#import "RequestManager.h"

@implementation MenuManager

@synthesize menuItems = _menuItems;

static MenuManager* sharedInstance = nil;

+ (MenuManager*)sharedInstance
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

- init
{
	if (self == [super init])
	{
		menuTitle = [[NSString alloc] initWithString:@"Liferay"];
	}

	return self;
}

- (void)addItemsToMenu:(TContextMenu*)menu forPaths:(NSArray*)selectedItems
{
	NSArray* items = [[RequestManager sharedInstance] menuItemsForFiles:selectedItems];

	if (items == nil)
	{
		return;
	}

	if ([items count] == 0)
	{
		return;
	}

	NSString* firstElement = [items objectAtIndex:0];
	if ([firstElement isEqualToString:@""])
	{
		return;
	}

	NSInteger menuIndex = 2;

	BOOL hasSeparatorBefore = [[menu itemAtIndex:menuIndex - 1] isSeparatorItem];
	BOOL hasSeparatorAfter = [[menu itemAtIndex:menuIndex] isSeparatorItem];

	if (!hasSeparatorAfter)
	{
		[menu insertItem:[NSMenuItem separatorItem] atIndex:menuIndex];
	}

	NSMenuItem* mainMenu = [menu insertItemWithTitle:menuTitle action:nil keyEquivalent:@"" atIndex:menuIndex];

	if (!hasSeparatorBefore)
	{
		[menu insertItem:[NSMenuItem separatorItem] atIndex:menuIndex];
	}

	NSMenu* submenu = [[NSMenu alloc] init];

	for (int i = 0; i < [items count]; ++i)
	{
		NSString* itemTitle = [items objectAtIndex:i];
		NSArray* titleElements = [itemTitle componentsSeparatedByString:@","];

		if ([itemTitle isEqualToString:@"_SEPARATOR_"])
		{
			[submenu addItem:[NSMenuItem separatorItem]];
		}
		else
		{
			NSMenuItem* menuItem = [submenu addItemWithTitle:[titleElements objectAtIndex:0] action:@selector(menuItemClicked:) keyEquivalent:@""];

			if ([titleElements count] > 1)
			{

				NSString* state = [titleElements objectAtIndex:1];

				if ([state isEqualToString:@"true"])
				{
					[menuItem setTarget:self];
				}
			}
			else
			{
				[menuItem setTarget:self];
			}

			[menuItem setRepresentedObject:[NSNumber numberWithInt:i]];
		}
	}

	[mainMenu setSubmenu:submenu];
}

- (void)menuItemClicked:(id)param
{
	[[RequestManager sharedInstance] menuItemClicked:[param representedObject] withTitle:[param title]];
}

- (NSArray*)pathsForNodes:(const struct TFENodeVector*)nodes
{
	struct TFENode* start = nodes->_M_impl._M_start;
	struct TFENode* end = nodes->_M_impl._M_finish;

	int count = end - start;

	NSMutableArray* selectedItems = [[NSMutableArray alloc] initWithCapacity:count];
	struct TFENode* current;

	for (current = start; current < end; ++current)
	{
		FINode* node = (FINode*)[NSClassFromString(@"FINode") nodeFromNodeRef:current->fNodeRef];

		[selectedItems addObject:[[node previewItemURL] path]];
	}

	return selectedItems;
}

- (void)setMenuTitle:(NSString*)title
{
	menuTitle = [[NSString alloc] initWithString:title];
}

@end