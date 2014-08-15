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
 * - (Andrew Rondeau) Added the ability to group icons by connection, this allows
 * disabling / clearing icons for one program, while leaving another unaffected
 * - (Andrew Rondeau) Made repaintAllWindows public
 */

#import <Foundation/Foundation.h>

@interface ContentManager : NSObject
{
	NSMapTable* _fileNamesCacheByConnection;
	NSHashTable* _fileIconsEnabledConnections;
}

+ (ContentManager*)sharedInstance;

- (void)enableFileIconsFor:(id)connection enabled:(BOOL)enable;
- (NSNumber*)iconByPath:(NSString*)path;
- (BOOL)isFileFiltered:(NSString*)path filterByFolders:(NSArray*)filterFolders;
- (void)removeAllIconsFor:(id)connection;
- (void)removeIconsFor:(id)connection paths:(NSArray*)paths;
- (void)setIconsFor:(id)connection iconIdsByPath:(NSDictionary*)iconDictionary filterByFolders:(NSArray*)filterFolders;
- (void)repaintAllWindows;

@end