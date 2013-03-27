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

#import <Foundation/Foundation.h>

@interface ContentManager : NSObject
{
	int currentId_;
	NSMutableDictionary* fileNamesCache_;
	BOOL overlaysEnabled_;
}

+ (ContentManager*)sharedInstance;

- (void)enableOverlays:(BOOL)enable;
- (NSNumber*)iconByPath:(NSString*)path;
- (void)removeAllIcons;
- (void)removeIcons:(NSArray*)paths;
- (void)setIcons:(NSDictionary*)iconDictionary filterByFolder:(NSString*)rootFolder;

@end