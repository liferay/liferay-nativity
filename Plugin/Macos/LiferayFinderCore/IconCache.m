//
//  IconCache.m
//  LiferayFinderCore
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 08.10.12.
//

#import "IconCache.h"

@implementation IconCache

static IconCache* sharedInstance = nil;

@synthesize dictionary_;

- init
{
	if (self == [super init])
	{
		dictionary_ = [[NSMutableDictionary alloc] init];
		currentIconId_ = 0;
	};
	
	return self;
}

+ (IconCache*)sharedInstance 
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

-(void) unregisterIcon: (NSNumber*) iconId
{
	[dictionary_ removeObjectForKey:iconId];
}

-(NSNumber*) registerIcon: (NSString*) path
{
	NSImage* image = [[NSImage alloc]initWithContentsOfFile: path];
	if (image == nil)
	{
		return nil;
	}
	
	currentIconId_++;
	NSNumber* index = [NSNumber numberWithInt:currentIconId_];

	[dictionary_ setObject:image forKey:index];
	[image release];
	
	return [NSNumber numberWithInt:currentIconId_];
}

-(NSImage*) getIcon: (NSNumber*) iconId
{
	NSImage* image = [dictionary_ objectForKey:iconId];
	return image;
}

@end
