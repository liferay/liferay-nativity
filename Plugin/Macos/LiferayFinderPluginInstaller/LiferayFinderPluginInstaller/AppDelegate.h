//
//  AppDelegate.h
//  LiferayFinderPluginInstaller
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 11/22/12.
//

#import <Cocoa/Cocoa.h>

@interface AppDelegate : NSObject <NSApplicationDelegate>

//@property (assign) IBOutlet NSWindow *window;

- (BOOL)blessHelperWithLabel:(NSString *)label error:(NSError **)error;

@end
