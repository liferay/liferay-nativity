/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

#import "FinderSync.h"
#import "RequestManager.h"

@interface FinderSync ()
@end

@implementation FinderSync

- (instancetype) init {
	NSLog(@"Liferay Nativity Finder Integration launched from %@", [[NSBundle mainBundle] bundlePath]);

	self = [super init];

	[[RequestManager sharedInstance] connect];

	NSSet* registeredUrls = [[RequestManager sharedInstance] registeredUrls];

	if ([registeredUrls count] > 0) {
		#ifdef DEBUG
			NSLog(@"Found previously registered urls: %@", registeredUrls);
		#endif

		[FIFinderSyncController defaultController].directoryURLs = registeredUrls;
	}

	NSSet* registeredBadges = [[RequestManager sharedInstance] registeredBadges];

	if ([registeredBadges count] > 0) {
		for (NSDictionary* dictionary in registeredBadges) {
			#ifdef DEBUG
				NSLog(@"Found previously registered badge: %@", dictionary);
			#endif

			NSString* path = dictionary[@"path"];
			NSString* label = dictionary[@"label"];
			NSString* iconId = dictionary[@"iconId"];

			NSFileManager* fileManager = [NSFileManager defaultManager];

			if (![fileManager fileExistsAtPath:path]) {
				NSLog(@"Failed to register file badge. File not found: %@", path);

				continue;
			}

			[[FIFinderSyncController defaultController] setBadgeImage:[[NSImage alloc] initWithContentsOfFile:path] label:label forBadgeIdentifier:iconId];
		}

		[[RequestManager sharedInstance] refreshBadges];
	}

	return self;
}

- (void) beginObservingDirectoryAtURL:(NSURL*)url {
	#ifdef DEBUG
		NSLog(@"Start observing folder: %@", url.filePathURL);
	#endif

	[[RequestManager sharedInstance] sendObservingFolder:url start:YES];
}

- (void) endObservingDirectoryAtURL:(NSURL*)url {
	#ifdef DEBUG
		NSLog(@"End observing folder: %@", url.filePathURL);
	#endif

	[[RequestManager sharedInstance] sendObservingFolder:url start:NO];
}

- (NSMenu*) menuForMenuKind:(FIMenuKind)whichMenu {
	NSArray* selectedItemURLs = [[FIFinderSyncController defaultController] selectedItemURLs];

	NSMutableArray* selectedItemPaths = [[NSMutableArray alloc] initWithCapacity:[selectedItemURLs count]];

	for (NSURL* selectedItemURL in selectedItemURLs) {
		[selectedItemPaths addObject:[selectedItemURL path]];
	}

	return [[RequestManager sharedInstance] menuForFiles:selectedItemPaths];
}

- (void) requestBadgeIdentifierForURL:(NSURL*)url {
	#ifdef DEBUG
		NSLog(@"Request badge for: %@", url.filePathURL);
	#endif

	[[RequestManager sharedInstance] requestFileBadgeId:url];
}

- (NSString*) toolbarItemName {
	return @"Liferay Nativity";
}

- (NSString*) toolbarItemToolTip {
	return @"Liferay Nativity: show options for the selected items";
}

- (NSImage*) toolbarItemImage {
	return [NSImage imageNamed:@"logo"];
}

@end