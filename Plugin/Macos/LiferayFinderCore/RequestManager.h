//
//  RequestManager.h
//  LiferayFinderCore
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 11/6/12.
//

#import <Foundation/Foundation.h>
#import "AsyncSocket.h"
#import "RequestManager.h"

@interface RequestManager : NSObject
{
    AsyncSocket *listenSocket;
	AsyncSocket *connectedSocket;
    
    AsyncSocket *listenSocket2;
	AsyncSocket *callbackSocket;
    
    NSNumberFormatter *numberFormatter;
    NSData* warningData;
    NSCondition* callbackCondition;
    NSString* callbackMsg;
    
	BOOL isRunning;
}

+ (RequestManager*) sharedInstance;
- (void)start;
- (NSArray* ) menuItemsForFiles: (NSArray*) files;
- (void) menuItemClicked: (NSNumber*) item;
@end
