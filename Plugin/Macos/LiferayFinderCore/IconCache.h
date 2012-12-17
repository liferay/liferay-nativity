//
//  IconCache.h
//  LiferayFinderCore
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 08.10.12.
//

#import <Cocoa/Cocoa.h>


@interface IconCache : NSObject {
	int currentIconId_;
	NSMutableDictionary *dictionary_;
}

+ (IconCache*) sharedInstance;

@property(nonatomic, retain) NSMutableDictionary *dictionary_;

@end
