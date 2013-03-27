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

@synthesize dictionary_;

- init
{
	if (self == [super init])
	{
		dictionary_ = [[NSMutableDictionary alloc] init];
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
	NSImage* image = [dictionary_ objectForKey:iconId];

	return image;
}

- (NSNumber*)registerIcon:(NSString*)path
{
	NSImage* image = [[NSImage alloc]initWithContentsOfFile:path];

	if (image == nil)
	{
		return nil;
	}

	currentIconId_++;
	NSNumber* index = [NSNumber numberWithInt:currentIconId_];

	[dictionary_ setObject:image forKey:index];
	[image release];

	return [NSNumber numberWithInt:currentIconId_];
}

- (void)unregisterIcon:(NSNumber*)iconId
{
	[dictionary_ removeObjectForKey:iconId];
}

@end