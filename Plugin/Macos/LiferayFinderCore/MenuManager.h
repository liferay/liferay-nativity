//
//  MenuManager.h
//  LiferayFinderCore
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 11/28/12.
//

#import <Foundation/Foundation.h>

@class TContextMenu;
struct TFENodeVector;

@interface MenuManager : NSObject
{
    NSString* menuTitle;
}

+ (MenuManager *)sharedInstance;
@property (nonatomic, strong) NSMutableArray *menuItems;
- (NSArray*)pathsForNodes:(const struct TFENodeVector *)nodes;
- (void)addItemsToMenu:(TContextMenu *)menu forPaths:(NSArray*)selectedItems;

-(void) setMenuTitle: (NSString*) title;

@end
