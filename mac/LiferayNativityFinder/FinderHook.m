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
#import "FinderHook.h"
#import "IconCache.h"
#import "objc/objc-class.h"
#import "RequestManager.h"

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

+ (void)load
{
	[FinderHook install];
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

	[self hookMethod:@selector(drawImage:) inClass:@"TIconViewCell" toCallToTheNewMethod:@selector(IconOverlayHandlers_drawImage:)]; // Lion & Mountain Lion

	[self hookMethod:@selector(drawIconWithFrame:) inClass:@"TListViewIconAndTextCell" toCallToTheNewMethod:@selector(IconOverlayHandlers_drawIconWithFrame:)]; // Lion & Mountain Lion

	[self hookClassMethod:@selector(addViewSpecificStuffToMenu:browserViewController:context:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_addViewSpecificStuffToMenu:browserViewController:context:)]; // Lion & Mountain Lion

	[self hookClassMethod:@selector(handleContextMenuCommon:nodes:event:view:windowController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:view:windowController:addPlugIns:)]; // Lion

	[self hookMethod:@selector(configureWithNodes:windowController:container:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_configureWithNodes:windowController:container:)]; // Lion

	[self hookClassMethod:@selector(handleContextMenuCommon:nodes:event:view:browserController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:view:browserController:addPlugIns:)]; // Mountain Lion

	[self hookMethod:@selector(configureWithNodes:browserController:container:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_configureWithNodes:browserController:container:)]; // Mountain Lion

	installed = YES;
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

	[self hookMethod:@selector(IconOverlayHandlers_drawImage:) inClass:@"TIconViewCell" toCallToTheNewMethod:@selector(drawImage:)]; // Lion & Mountain Lion

	[self hookMethod:@selector(IconOverlayHandlers_drawIconWithFrame:) inClass:@"TListViewIconAndTextCell" toCallToTheNewMethod:@selector(drawIconWithFrame:)]; // Lion & Mountain Lion

	[self hookClassMethod:@selector(ContextMenuHandlers_addViewSpecificStuffToMenu:browserViewController:context:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(addViewSpecificStuffToMenu:browserViewController:context:)]; // Lion & Mountain Lion

	[self hookClassMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:view:windowController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(handleContextMenuCommon:nodes:event:view:windowController:addPlugIns:)]; // Lion

	[self hookMethod:@selector(ContextMenuHandlers_configureWithNodes:windowController:container:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(configureWithNodes:windowController:container:)]; // Lion

	[self hookClassMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:view:browserController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(handleContextMenuCommon:nodes:event:view:browserController:addPlugIns:)]; // Mountain Lion

	[self hookMethod:@selector(ContextMenuHandlers_configureWithNodes:browserController:container:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(configureWithNodes:browserController:container:)]; // Mountain Lion

	installed = NO;
}

@end