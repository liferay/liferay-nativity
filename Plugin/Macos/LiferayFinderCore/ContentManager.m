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
