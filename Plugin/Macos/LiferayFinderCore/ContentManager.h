//
//  ContentManager.h
//  LiferayFinderCore
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 10/23/12.
//

#import <Foundation/Foundation.h>

@interface ContentManager : NSObject
{
    int currentId_;
	NSMutableDictionary *fileNamesCache_;
    BOOL overlaysEnabled_;
}

+(ContentManager  *) sharedInstance;
-(NSNumber*) iconByPath : (NSString*) path;
-(void) setIcon : (NSNumber*) icon forFile : (NSString*) path;
-(void) removeIconFromFile : (NSString*) path;

@end
