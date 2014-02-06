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
 * - (Ivan Burlakov) Added registerMenuIcon
 */

#import "IconCache.h"

@implementation IconCache

static IconCache* sharedInstance = nil;

- init
{
	self = [super init];

	if (self)
	{
		_iconIdDictionary = [[NSMutableDictionary alloc] init];
		_iconPathDictionary = [[NSMutableDictionary alloc] init];
		_currentIconId = 0;
	}

	return self;
}

- (void)dealloc
{
	[_iconIdDictionary release];
	[_iconPathDictionary release];
	sharedInstance = nil;

	[super dealloc];
}

+ (IconCache*)sharedInstance
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

- (NSImage*)getIcon:(NSNumber*)iconId
{
	NSImage* image = [_iconIdDictionary objectForKey:iconId];

	return image;
}

- (NSNumber*)registerIcon:(NSString*)path
{
	if (path == nil)
	{
		return [NSNumber numberWithInt:-1];
	}

	NSImage* image = [[NSImage alloc]initWithContentsOfFile:path];

	if (image == nil)
	{
		return [NSNumber numberWithInt:-1];
	}

	NSNumber* iconId = [_iconPathDictionary objectForKey:path];

	if (iconId == nil)
	{
		_currentIconId++;

		iconId = [NSNumber numberWithInt:_currentIconId];

		[_iconPathDictionary setObject:iconId forKey:path];
	}

	[_iconIdDictionary setObject:image forKey:iconId];
	[image release];

	return iconId;
}

- (NSNumber*)registerMenuIcon:(NSString*)path
{
	NSNumber* menuIconId = [self registerIcon:path];
	
	NSImage* menuIconImage = [self getIcon:menuIconId];
	
	NSSize size;
	size.width = size.height = [[NSFont menuFontOfSize:0] pointSize];
	[menuIconImage setSize:size];
	
	return menuIconId;
}

- (void)unregisterIcon:(NSNumber*)iconId
{
	NSString* path = @"";

	for (NSString* key in _iconPathDictionary)
	{
		NSNumber* value = [_iconPathDictionary objectForKey:key];

		if ([value isEqualToNumber:iconId])
		{
			path = key;

			break;
		}
	}

	[_iconPathDictionary removeObjectForKey:path];

	[_iconIdDictionary removeObjectForKey:iconId];
}

@end
