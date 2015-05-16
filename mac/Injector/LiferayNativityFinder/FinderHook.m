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
 * - (Andrew Rondeau) Made the method names randomize themselves at compile time.
 * This avoids swizzling conflicts with other products, or with plugins that borrow
 * code from liferay-nativity
 */

#import "ContentManager.h"
#import "FinderHook.h"
#import "IconCache.h"
#import "objc/objc-class.h"
#import "RequestManager.h"

// This randomly changes the method names that we're swizling in with each build.
// This is because Google Drive's plugin has a conflicting name. In the event that
// someone "borrows" this plugin, randomizing names prevents runtime conflicts
#include "MethodNames.h"

static BOOL installed = NO;

@implementation FinderHook

+ (void)hookClassMethod:(SEL)oldSelector inClass:(NSString*)className toCallToTheNewMethod:(SEL)newSelector
{
	Class hookedClass = NSClassFromString(className);
	Method oldMethod = class_getClassMethod(hookedClass, oldSelector);
	Method newMethod = class_getClassMethod(hookedClass, newSelector);

	method_exchangeImplementations(newMethod, oldMethod);
}

+ (void)hookMethod:(SEL)oldSelector inClass:(NSString*)className toCallToTheNewMethod:(SEL)newSelector
{
	Class hookedClass = NSClassFromString(className);
	Method oldMethod = class_getInstanceMethod(hookedClass, oldSelector);
	Method newMethod = class_getInstanceMethod(hookedClass, newSelector);

	method_exchangeImplementations(newMethod, oldMethod);
}

+ (void)install
{
	if (installed)
	{
		NSLog(@"LiferayNativityFinder: already installed");

		return;
	}

	NSLog(@"LiferayNativityFinder: installing");

	[RequestManager sharedInstance];

	// Icons
	[self hookMethod:@selector(drawImage:) inClass:@"IKImageBrowserCell" toCallToTheNewMethod:@selector(IconOverlayHandlers_IKImageBrowserCell_drawImage:)];     // 10.7 & 10.8 & 10.9 (Icon View arrange by name)

	[self hookMethod:@selector(drawImage:) inClass:@"IKFinderReflectiveIconCell" toCallToTheNewMethod:@selector(IconOverlayHandlers_IKFinderReflectiveIconCell_drawImage:)];     // 10.7 & 10.8 & 10.9 (Icon View arrange by everything else)

	[self hookMethod:@selector(drawIconWithFrame:) inClass:@"TColumnCell" toCallToTheNewMethod:@selector(IconOverlayHandlers_drawIconWithFrame:)];     // 10.7 & 10.8 & 10.9 & 10.10 Column View

	[self hookMethod:@selector(drawRect:) inClass:@"TDimmableIconImageView" toCallToTheNewMethod:@selector(IconOverlayHandlers_drawRect:)];     // 10.9 (List and Coverflow Views)

	// Context Menus
	[self hookClassMethod:@selector(addViewSpecificStuffToMenu:browserViewController:context:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_addViewSpecificStuffToMenu:browserViewController:context:)];     // 10.7 & 10.8

	[self hookClassMethod:@selector(addViewSpecificStuffToMenu:clickedView:browserViewController:context:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_addViewSpecificStuffToMenu:clickedView:browserViewController:context:)];     // 10.9

	[self hookClassMethod:@selector(handleContextMenuCommon:nodes:event:view:windowController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:view:windowController:addPlugIns:)];     // 10.7

	[self hookClassMethod:@selector(handleContextMenuCommon:nodes:event:view:browserController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:view:browserController:addPlugIns:)];     // 10.8

	[self hookClassMethod:@selector(handleContextMenuCommon:nodes:event:clickedView:browserViewController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:clickedView:browserViewController:addPlugIns:)];     // 10.9

	[self hookMethod:@selector(configureWithNodes:windowController:container:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_configureWithNodes:windowController:container:)];     // 10.7

	[self hookMethod:@selector(configureWithNodes:browserController:container:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_configureWithNodes:browserController:container:)];     // 10.8

	[self hookMethod:@selector(configureFromMenuNeedsUpdate:clickedView:container:event:selectedNodes:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_configureFromMenuNeedsUpdate:clickedView:container:event:selectedNodes:)];     // 10.9

	installed = YES;

	NSLog(@"LiferayNativityFinder: installed");
}

+ (void)uninstall
{
	if (!installed)
	{
		NSLog(@"LiferayNativityFinder: not installed");

		return;
	}

	NSLog(@"LiferayNativityFinder: uninstalling");

	[[ContentManager sharedInstance] dealloc];

	[[IconCache sharedInstance] dealloc];

	[[RequestManager sharedInstance] dealloc];

	// Icons
	[self hookMethod:@selector(IconOverlayHandlers_IKImageBrowserCell_drawImage:) inClass:@"IKImageBrowserCell" toCallToTheNewMethod:@selector(drawImage:)];     // 10.7 & 10.8 & 10.9 (Icon View arrange by name)

	[self hookMethod:@selector(IconOverlayHandlers_IKFinderReflectiveIconCell_drawImage:) inClass:@"IKFinderReflectiveIconCell" toCallToTheNewMethod:@selector(drawImage:)];     // 10.7 & 10.8 & 10.9 (Icon View arrange by everything else)

	[self hookMethod:@selector(IconOverlayHandlers_drawIconWithFrame:) inClass:@"TListViewIconAndTextCell" toCallToTheNewMethod:@selector(drawIconWithFrame:)];     // 10.7 & 10.8 & 10.9 Column View

	[self hookMethod:@selector(IconOverlayHandlers_drawRect:) inClass:@"TDimmableIconImageView" toCallToTheNewMethod:@selector(drawRect:)];     // 10.9 (List and Coverflow Views)

	// Context Menus
	[self hookClassMethod:@selector(ContextMenuHandlers_addViewSpecificStuffToMenu:browserViewController:context:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(addViewSpecificStuffToMenu:browserViewController:context:)];     // 10.7 & 10.8

	[self hookClassMethod:@selector(ContextMenuHandlers_addViewSpecificStuffToMenu:clickedView:browserViewController:context:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(addViewSpecificStuffToMenu:clickedView:browserViewController:context:)];     // 10.9

	[self hookClassMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:view:windowController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(handleContextMenuCommon:nodes:event:view:windowController:addPlugIns:)];     // 10.7

	[self hookClassMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:view:browserController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(handleContextMenuCommon:nodes:event:view:browserController:addPlugIns:)];     // 10.8

	[self hookClassMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:clickedView:browserViewController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(handleContextMenuCommon:nodes:event:clickedView:browserViewController:addPlugIns:)];     // 10.9

	[self hookMethod:@selector(ContextMenuHandlers_configureWithNodes:windowController:container:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(configureWithNodes:windowController:container:)];     // 10.7

	[self hookMethod:@selector(ContextMenuHandlers_configureWithNodes:browserController:container:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(configureWithNodes:browserController:container:)];     // 10.8

	[self hookMethod:@selector(ContextMenuHandlers_configureFromMenuNeedsUpdate:clickedView:container:event:selectedNodes:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(configureFromMenuNeedsUpdate:clickedView:container:event:selectedNodes:)];     // 10.9

	installed = NO;

	NSLog(@"LiferayNativityFinder: uninstalled");
}

@end