//
//  ContentManager.m
//  LiferayFinderCore
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 10/23/12.
//

#import "ContentManager.h"
#import <AppKit/NSWorkspace.h>
#import <AppKit/NSApplication.h>
#import <AppKit/NSWindow.h>
#import <Carbon/Carbon.h>


static ContentManager* sharedInstance = nil;

OSStatus    SendFinderSyncEvent( const FSRef* inObjectRef )
{
    AppleEvent  theEvent = { typeNull, NULL };
    AppleEvent  replyEvent = { typeNull, NULL };
    AliasHandle itemAlias = NULL;
    const OSType    kFinderSig = 'MACS';
    
    OSStatus    err = FSNewAliasMinimal( inObjectRef, &itemAlias );
    if (err == noErr)
    {
        err = AEBuildAppleEvent( kAEFinderSuite, kAESync, typeApplSignature,
                                &kFinderSig, sizeof(OSType), kAutoGenerateReturnID,
                                kAnyTransactionID, &theEvent, NULL, "'----':alis(@@)", itemAlias );
        
        if (err == noErr)
        {
            err = AESendMessage( &theEvent, &replyEvent, kAENoReply,
                                kAEDefaultTimeout );
            
            AEDisposeDesc( &replyEvent );
            AEDisposeDesc( &theEvent );
        }
        
        DisposeHandle( (Handle)itemAlias );
    }
    
    return err;
}

@implementation ContentManager
- init
{
	if (self == [super init])
	{
		fileNamesCache_ = [[NSMutableDictionary alloc] init];
		currentId_ = 0;
        overlaysEnabled_ = FALSE;
	};
	
	return self;
}

+ (ContentManager*)sharedInstance 
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

-(NSNumber*) iconByPath : (NSString*) path
{
    if (!overlaysEnabled_)
        return nil;
    
    NSNumber* result = [fileNamesCache_ objectForKey:path];    
    return result;
}

-(void) enableOverlays : (BOOL) enable
{
    overlaysEnabled_ = enable;
}

-(void) setIcon : (NSNumber*) icon forFile : (NSString*) path
{
    [fileNamesCache_ setObject:icon forKey:path];

    FSRef ref;
    CFURLGetFSRef((CFURLRef)[NSURL fileURLWithPath: path], &ref);
    SendFinderSyncEvent(&ref);
    
    [[NSWorkspace sharedWorkspace] noteFileSystemChanged];
    
    NSArray* windows = [[NSApplication sharedApplication] windows];
    
    for (int i=0;i<[windows count];++i)
    {
        NSWindow* window = [windows objectAtIndex:i];
        [window update];
        
        if ([[window className] isEqualToString:@"TBrowserWindow"])
        {
            NSObject* controller = [window browserWindowController];
            
            [controller updateViewLayout];
            [controller viewContentChanged];
            [controller drawCompletelyIntoBackBuffer];
        }
    }
}

-(void) removeIconFromFile : (NSString*) path
{
    [fileNamesCache_ removeObjectForKey:path];
}

@end
