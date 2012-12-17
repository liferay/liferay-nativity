//
//  MenuManager.m
//  LiferayFinderCore
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 11/28/12.
//

#import "MenuManager.h"
#import "Finder/finder.h"
#import "RequestManager.h"

@implementation MenuManager

@synthesize menuItems = _menuItems;
static MenuManager *sharedInstance = nil;

+ (MenuManager *)sharedInstance
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

- init
{
	if (self == [super init])
	{
		menuTitle = [[NSString alloc] initWithCString:"Liferay"];
        
	};
	
	return self;
}


-(void) setMenuTitle: (NSString*) title
{
    menuTitle = [[NSString alloc] initWithString:title];
}

- (NSArray *)pathsForNodes:(const struct TFENodeVector *)nodes
{
    struct TFENode* start = nodes->_M_impl._M_start;
    struct TFENode* end = nodes->_M_impl._M_finish;
    
    int count = end - start;
    
    //int index=0;
    NSMutableArray *selectedItems = [[NSMutableArray alloc] initWithCapacity:count];
    struct TFENode* current;
    for (current = start; current < end; ++current)
    {
        FINode *node = (FINode *)[NSClassFromString(@"FINode") nodeFromNodeRef:current->fNodeRef];
        
        [selectedItems addObject:[[node previewItemURL] path]]; 
    }
    
    return selectedItems;
}

- (void)menuItemClicked:(id) param
{
    [[RequestManager sharedInstance] menuItemClicked: [param representedObject]];
}

- (void)addItemsToMenu:(TContextMenu *)menu forPaths:(NSArray *)selectedItems
{
    NSArray* items = [[RequestManager sharedInstance] menuItemsForFiles:selectedItems];
    if (items == nil)
        return;
    
    if ([items count] == 0)
        return;
    
    NSString* firstElement = [items objectAtIndex:0];
    if ([firstElement isEqualToString:@""])
        return;
    
    NSInteger menuIndex = 2;
    
    BOOL hasSeparatorBefore = [[menu itemAtIndex:menuIndex - 1] isSeparatorItem];
    BOOL hasSeparatorAfter = [[menu itemAtIndex:menuIndex] isSeparatorItem];
    
    if (!hasSeparatorAfter)
        [menu insertItem:[NSMenuItem separatorItem] atIndex:menuIndex];
    
    NSMenuItem *mainMenu = [menu insertItemWithTitle:menuTitle action:nil keyEquivalent:@"" atIndex:menuIndex];
    
    if (!hasSeparatorBefore)
        [menu insertItem:[NSMenuItem separatorItem] atIndex:menuIndex];
    
    NSMenu *submenu = [[NSMenu alloc] init];
    
    for (int i=0;i<[items count];++i)
    {
        NSString* itemTitle = [items objectAtIndex:i];
        NSArray* titleElements = [itemTitle componentsSeparatedByString:@","];
        
        if ([itemTitle isEqualToString:@"_SEPARATOR_"])
        {
            [submenu addItem:[NSMenuItem separatorItem]];
        }
        else
        {
            NSMenuItem *menuItem = [submenu addItemWithTitle:[titleElements objectAtIndex:0] action:@selector(menuItemClicked:) keyEquivalent:@""];
        
            if ([titleElements count] > 1)
            {
                
                NSString* state = [titleElements objectAtIndex:1];
        
                if ([state isEqualToString:@"true"])
                    [menuItem setTarget:self];
            }
            else
                [menuItem setTarget:self];
   
            [menuItem setRepresentedObject:[NSNumber numberWithInt:i]];
        }
    }
    
    [mainMenu setSubmenu:submenu];
}


@end
