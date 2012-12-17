//
//  ContextMenuHandlers.m
//  LiferayFinderCore
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 11/28/12.
//

#import "ContextMenuHandlers.h"
#import "Finder/Finder.h"
#import "MenuManager.h"

@implementation NSObject (ContextMenuHandlers)

+ (void)ContextMenuHandlers_handleContextMenuCommon:(unsigned int)arg1 nodes:(const struct TFENodeVector *)arg2 event:(id)arg3 view:(id)arg4 windowController:(id)arg5 addPlugIns:(BOOL)arg6  // Lion
{
    MenuManager *contexMenuUtils = [MenuManager sharedInstance];
    contexMenuUtils.menuItems = (NSMutableArray *)[contexMenuUtils pathsForNodes:arg2];
    
    [self ContextMenuHandlers_handleContextMenuCommon:arg1 nodes:arg2 event:arg3 view:arg4 windowController:arg5 addPlugIns:arg6];
}

+ (void)ContextMenuHandlers_handleContextMenuCommon:(unsigned int)arg1 nodes:(const struct TFENodeVector *)arg2 event:(id)arg3 view:(id)arg4 browserController:(id)arg5 addPlugIns:(BOOL)arg6 // Mountain Lion
{
    MenuManager *contexMenuUtils = [MenuManager sharedInstance];
    contexMenuUtils.menuItems = (NSMutableArray *)[contexMenuUtils pathsForNodes:arg2];
  
    [self ContextMenuHandlers_handleContextMenuCommon:arg1 nodes:arg2 event:arg3 view:arg4 browserController:arg5 addPlugIns:arg6];
}

+ (void)ContextMenuHandlers_addViewSpecificStuffToMenu:(id)arg1 browserViewController:(id)arg2 context:(unsigned int)arg3
{
    [self ContextMenuHandlers_addViewSpecificStuffToMenu:arg1 browserViewController:arg2 context:arg3];
    
    if ([MenuManager sharedInstance].menuItems.count > 0) {
        MenuManager *contexMenuUtils = [MenuManager sharedInstance];
        [contexMenuUtils addItemsToMenu:arg1 forPaths:contexMenuUtils.menuItems];
        [contexMenuUtils.menuItems removeAllObjects];
    }
}

- (void)ContextMenuHandlers_configureWithNodes:(const struct TFENodeVector *)arg1 windowController:(id)arg2 container:(BOOL)arg3  // Lion
{
    [self ContextMenuHandlers_configureWithNodes:arg1 windowController:arg2 container:arg3];
    
    TContextMenu *realSelf = (TContextMenu *)self;
    MenuManager *contexMenuUtils = [MenuManager sharedInstance];
    
    NSArray *selectedItems = [contexMenuUtils pathsForNodes:arg1];
    [contexMenuUtils addItemsToMenu:realSelf forPaths:selectedItems];
}

- (void)ContextMenuHandlers_configureWithNodes:(const struct TFENodeVector *)arg1 browserController:(id)arg2 container:(BOOL)arg3 // Mountain Lion
{   
    [self ContextMenuHandlers_configureWithNodes:arg1 browserController:arg2 container:arg3];
    
    TContextMenu *realSelf = (TContextMenu *)self;
    MenuManager *contexMenuUtils = [MenuManager sharedInstance];
    
    NSArray *selectedItems = [contexMenuUtils pathsForNodes:arg1];
    [contexMenuUtils addItemsToMenu:realSelf forPaths:selectedItems];
}


@end
