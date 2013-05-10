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

#import "IconCache.h"

@implementation IconCache

static IconCache* sharedInstance = nil;

@synthesize iconIdDictionary_;
@synthesize iconPathDictionary_;

- init
{
	self = [super init];

	if (self)
	{
		iconIdDictionary_ = [[NSMutableDictionary alloc] init];
		iconPathDictionary_ = [[NSMutableDictionary alloc] init];
		currentIconId_ = 0;
	}

	return self;
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
	NSImage* image = [iconIdDictionary_ objectForKey:iconId];

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

	NSNumber* iconId = [iconPathDictionary_ objectForKey:path];

	if (iconId == nil)
	{
		currentIconId_++;

		iconId = [NSNumber numberWithInt:currentIconId_];

		[iconPathDictionary_ setObject:iconId forKey:path];
	}

	[iconIdDictionary_ setObject:image forKey:iconId];
	[image release];

	return iconId;
}

- (void)unregisterIcon:(NSNumber*)iconId
{
	NSString* path = @"";

	for (NSString* key in iconPathDictionary_)
	{
		NSNumber* value = [iconPathDictionary_ objectForKey:key];

		if ([value isEqualToNumber:iconId])
		{
			path = key;

			break;
		}
	}

	[iconPathDictionary_ removeObjectForKey:path];

	[iconIdDictionary_ removeObjectForKey:iconId];
}

@end
