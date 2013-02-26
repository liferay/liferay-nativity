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

#import "FinderHook.h"
#import "Finder/Finder.h"
#import "IconCache.h"
#include "ContentManager.h"
#include "objc/objc-class.h"
#import "RequestManager.h"

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
	NSString* iconPath = [[NSBundle bundleForClass:[FinderHook class]] pathForResource:@"TestIcon" ofType:@"icns"];

	[RequestManager sharedInstance];
	[[IconCache sharedInstance] registerIcon:iconPath];

	[self hookMethod:@selector(drawImage:) inClass:@"TIconViewCell" toCallToTheNewMethod:@selector(IconOverlayHandlers_drawImage:)]; // Lion & Mountain Lion

	[self hookMethod:@selector(drawIconWithFrame:) inClass:@"TListViewIconAndTextCell" toCallToTheNewMethod:@selector(IconOverlayHandlers_drawIconWithFrame:)]; // Lion & Mountain Lion

	[self hookClassMethod:@selector(addViewSpecificStuffToMenu:browserViewController:context:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_addViewSpecificStuffToMenu:browserViewController:context:)]; // Lion & Mountain Lion

	[self hookClassMethod:@selector(handleContextMenuCommon:nodes:event:view:windowController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:view:windowController:addPlugIns:)]; // Lion

	[self hookMethod:@selector(configureWithNodes:windowController:container:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_configureWithNodes:windowController:container:)]; // Lion

	[self hookClassMethod:@selector(handleContextMenuCommon:nodes:event:view:browserController:addPlugIns:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_handleContextMenuCommon:nodes:event:view:browserController:addPlugIns:)]; // Mountain Lion

	[self hookMethod:@selector(configureWithNodes:browserController:container:) inClass:@"TContextMenu" toCallToTheNewMethod:@selector(ContextMenuHandlers_configureWithNodes:browserController:container:)]; // Mountain Lion
}

@end