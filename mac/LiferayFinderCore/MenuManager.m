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
	return [super init];
}

- (void)addItemsToMenu:(TContextMenu*)menu forPaths:(NSArray*)selectedItems
{
	NSArray* menuItemsArray = [[RequestManager sharedInstance] menuItemsForFiles:selectedItems];

	if (menuItemsArray == nil)
	{
		return;
	}

	if ([menuItemsArray count] == 0)
	{
		return;
	}

	NSInteger menuIndex = 4;

	BOOL hasSeparatorBefore = [[menu itemAtIndex:menuIndex - 1] isSeparatorItem];

	if (!hasSeparatorBefore)
	{
		[menu insertItem:[NSMenuItem separatorItem] atIndex:menuIndex];
	}

	for (int i = 0; i < [menuItemsArray count]; ++i)
	{
		NSDictionary* menuItemDictionary = [menuItemsArray objectAtIndex:i];

		NSString* mainMenuTitle = [menuItemDictionary objectForKey:@"title"];

		if ([mainMenuTitle isEqualToString:@""])
		{
			continue;
		}

		menuIndex++;

		NSArray* childrenSubMenuItems = (NSArray*)[menuItemDictionary objectForKey:@"contextMenuItems"];

		if (childrenSubMenuItems != nil && [childrenSubMenuItems count] != 0)
		{
			NSMenuItem* mainMenuItem = [menu insertItemWithTitle:mainMenuTitle action:nil keyEquivalent:@"" atIndex:menuIndex];

			[self addChildrenSubMenuItems:mainMenuItem withChildren:childrenSubMenuItems forPaths:selectedItems];
		}
		else
		{
			NSMenuItem* mainMenuItem = [menu insertItemWithTitle:mainMenuTitle action:@selector(menuItemClicked:) keyEquivalent:@"" atIndex:menuIndex];

			if ([[menuItemDictionary objectForKey:@"enabled"] boolValue])
			{
				[mainMenuItem setTarget:self];
			}

			NSDictionary* menuActionDictionary = [[NSMutableDictionary alloc] init];
			[menuActionDictionary setValue:[menuItemDictionary objectForKey:@"uuid"] forKey:@"uuid"];
			NSMutableArray* filesArray = [selectedItems copy];
			[menuActionDictionary setValue:filesArray forKey:@"files"];

			[mainMenuItem setRepresentedObject:menuActionDictionary];

			[filesArray release];
			[menuActionDictionary release];
		}
	}

	BOOL hasSeparatorAfter = [[menu itemAtIndex:menuIndex + 1] isSeparatorItem];

	if (!hasSeparatorAfter)
	{
		[menu insertItem:[NSMenuItem separatorItem] atIndex:menuIndex + 1];
	}
}

- (void)addChildrenSubMenuItems:(NSMenuItem*)parentMenuItem withChildren:(NSArray*)childrenMenuItemsDictionary forPaths:(NSArray*)selectedItems
{
	NSMenu* submenu = [[NSMenu alloc] init];

	for (int i = 0; i < [childrenMenuItemsDictionary count]; ++i)
	{
		NSDictionary* submenuDictionary = [childrenMenuItemsDictionary objectAtIndex:i];

		NSString* submenuTitle = [submenuDictionary objectForKey:@"title"];

		NSArray* childrenSubMenuItems = (NSArray*)[submenuDictionary objectForKey:@"contextMenuItems"];

		if ([submenuTitle isEqualToString:@"_SEPARATOR_"])
		{
			[submenu addItem:[NSMenuItem separatorItem]];
		}
		else if (childrenSubMenuItems != nil && [childrenSubMenuItems count] != 0)
		{
			NSMenuItem* submenuItem = [submenu addItemWithTitle:submenuTitle action:nil keyEquivalent:@""];

			[self addChildrenSubMenuItems:submenuItem withChildren:childrenSubMenuItems forPaths:selectedItems];
		}
		else
		{
			NSMenuItem* submenuItem = [submenu addItemWithTitle:submenuTitle action:@selector(menuItemClicked:) keyEquivalent:@""];

			if ([[submenuDictionary objectForKey:@"enabled"] boolValue])
			{
				[submenuItem setTarget:self];
			}

			NSDictionary* menuActionDictionary = [[NSMutableDictionary alloc] init];
			[menuActionDictionary setValue:[submenuDictionary objectForKey:@"uuid"] forKey:@"uuid"];
			NSMutableArray* filesArray = [selectedItems copy];
			[menuActionDictionary setValue:filesArray forKey:@"files"];

			[submenuItem setRepresentedObject:menuActionDictionary];

			[filesArray release];
			[menuActionDictionary release];
		}
	}

	[parentMenuItem setSubmenu:submenu];

	[submenu release];
}

- (void)menuItemClicked:(id)param
{
	[[RequestManager sharedInstance] menuItemClicked:[param representedObject]];
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

	return [selectedItems autorelease];
}

@end
