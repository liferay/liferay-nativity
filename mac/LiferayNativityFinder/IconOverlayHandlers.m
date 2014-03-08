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
#import "IconCache.h"
#import "IconOverlayHandlers.h"
#import "RequestManager.h"
#import "Finder/Finder.h"
#import <objc/runtime.h>

// This randomly changes the method names that we're swizling in with each build.
// This is because Google Drive's plugin has a conflicting name. In the event that
// someone "borrows" this plugin, randomizing names prevents runtime conflicts
#include "MethodNames.h"


@implementation NSObject (IconOverlayHandlers)

// 10.7 & 10.8 & 10.9 Column View
- (void)IconOverlayHandlers_drawIconWithFrame:(struct CGRect)arg1
{
	[self IconOverlayHandlers_drawIconWithFrame:arg1];

	NSURL* url = [[NSClassFromString(@"FINode") nodeFromNodeRef:[(TIconAndTextCell*)self node]->fNodeRef] previewItemURL];

	for (NSNumber* imageIndex in [[RequestManager sharedInstance] iconIdForFile:[url path]])
	{
		if ([imageIndex intValue] > 0)
		{
			NSImage* image = [[IconCache sharedInstance] getIcon:imageIndex];

			if (image != nil)
			{
				struct CGRect arg2 = [(TIconViewCell*)self imageRectForBounds:arg1];

				[image drawInRect:NSMakeRect(arg2.origin.x, arg2.origin.y, arg2.size.width, arg2.size.height) fromRect:NSZeroRect operation:NSCompositeSourceOver fraction:1.0 respectFlipped:TRUE hints:nil];
			}
		}
	}
}

// 10.7 & 10.8 & 10.9 (Icon View)
- (void)IconOverlayHandlers_drawImage:(id)arg1
{
	TIconViewCell* realSelf = (TIconViewCell*)self;
	FINode* node = (FINode*)[realSelf representedItem];

	NSURL* url = [node previewItemURL];

	for (NSNumber* imageIndex in [[RequestManager sharedInstance] iconIdForFile:[url path]])
	{
		if ([imageIndex intValue] > 0)
		{
			NSImage* icon = [arg1 _nsImage];

			[icon lockFocus];

			CGContextRef myContext = [[NSGraphicsContext currentContext] graphicsPort];

			NSImage* iconimage = [[IconCache sharedInstance] getIcon:[NSNumber numberWithInt:[imageIndex intValue]]];

			if (iconimage != nil)
			{
				CGImageSourceRef source;
				NSData* data = [iconimage TIFFRepresentation];

				source = CGImageSourceCreateWithData((CFDataRef)data, NULL);
				CGImageRef maskRef = CGImageSourceCreateImageAtIndex(source, 0, NULL);
				CGContextDrawImage(myContext, CGRectMake(0, 0, [icon size].width, [icon size].height), maskRef);
				CFRelease(source);
				CFRelease(maskRef);
			}

			[icon unlockFocus];

			arg1 = [[IKImageWrapper alloc] initWithNSImage:icon];
			[arg1 autorelease];
		}
	}

	[self IconOverlayHandlers_drawImage:arg1];
}

// 10.9 (List and Coverflow Views)
- (void)IconOverlayHandlers_drawRect:(struct CGRect)arg1
{
	[self IconOverlayHandlers_drawRect:arg1];

	//TODO: [ContentManager repaintWindows] does not seem to redraw the list/coverflow views.

	NSView* supersuperView = [[(NSView*)self superview] superview];
	
	id cTListRowView = objc_getClass("TListRowView");
		
	if([supersuperView isKindOfClass:cTListRowView]) {
		TListRowView *lrv = (TListRowView*)supersuperView;
		FINode *finode;
		
		object_getInstanceVariable(lrv, "_node", (void**)&finode);
		NSURL *fp;
		
		if([finode respondsToSelector:@selector(previewItemURL)]) {
			fp = [finode previewItemURL];
		} else {
			return;
		}
		
		for (NSNumber* imageIndex in [[RequestManager sharedInstance] iconIdForFile:[fp path]])
		{
			if ([imageIndex intValue] > 0)
			{
				NSImage* image = [[IconCache sharedInstance] getIcon:imageIndex];
			
				if (image != nil)
				{
					[image drawInRect:NSMakeRect(arg1.origin.x, arg1.origin.y, arg1.size.width, arg1.size.height) fromRect:NSZeroRect operation:NSCompositeSourceOver fraction:1.0 respectFlipped:TRUE hints:nil];
				}
			}
		}
	}
}

@end