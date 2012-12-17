//
//  FinderHook.h
//  LiferayFinderPlugin
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 18.09.12.
//

#import <Cocoa/Cocoa.h>
#import "RequestManager.h"


@interface FinderHook : NSObject 
 
+ (void)hookMethod:(SEL)oldSelector inClass:(NSString *)className toCallToTheNewMethod:(SEL)newSelector;
+ (void)hookClassMethod:(SEL)oldSelector inClass:(NSString *)className toCallToTheNewMethod:(SEL)newSelector;


@end
