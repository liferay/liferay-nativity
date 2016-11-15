/*
 EBLaunchServices.m
 Copyright (c) eric_bro, 2012 (eric.broska@me.com)
 
 Permission to use, copy, modify, and/or distribute this software for any
 purpose with or without fee is hereby granted, provided that the above
 copyright notice and this permission notice appear in all copies.
 
 THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

#import "EBLaunchServices.h"

/* Private prototype from the LaunchServices framework */
OSStatus _LSCopyAllApplicationURLs(CFArrayRef *array);
typedef  id (^EBMappingBlock)(id obj);

@implementation EBLaunchServicesListItem
@synthesize url, name, icon;

- (id)init
{
    if ((self = [super init])) {
        self.name = nil;
        self.url  = nil;
        self.icon = nil;
    } else self = nil;
    
    return self;
}
- (void)dealloc
{
    [url release];
    /* We don't release other ivars, because thay are all autorelease'd */
    [super dealloc];
}

@end


@interface EBLaunchServices (Private)
+ (NSArray *)mappingArray:(NSArray *)array usingBlock:(EBMappingBlock)block;
+ (NSInteger)indexOfItemWithURL:(NSURL *)url inList:(CFStringRef)list_name;
+ (NSArray *)prepareArray:(NSArray *)array withFormat:(enum EBItemsViewFormat)format;
@end

@implementation EBLaunchServices

#pragma mark Shared Lists

+ (NSArray *)allItemsFromList:(CFStringRef)list_name
{
    LSSharedFileListRef list = LSSharedFileListCreate(NULL, (CFStringRef)list_name, NULL);
    NSArray *tmp = (NSArray *)LSSharedFileListCopySnapshot(list, NULL);
    CFRelease(list);
    id value = [EBLaunchServices mappingArray: tmp usingBlock:^id(id obj) {
        EBLaunchServicesListItem *item =[[EBLaunchServicesListItem alloc] init];
        [item setName: [(NSString *)LSSharedFileListItemCopyDisplayName((LSSharedFileListItemRef)obj) autorelease]];
        
        CFErrorRef cfErrorRef = nil;
        CFURLRef cfURLRef = LSSharedFileListItemCopyResolvedURL((LSSharedFileListItemRef)obj, 0, &cfErrorRef);
        
        if (cfErrorRef) {
            CFRelease(cfErrorRef);
            
            return NULL;
        }
        
        NSURL *url = (__bridge NSURL *)cfURLRef;

        if (url) [item setUrl: url];
        [item setIcon: [[[NSImage alloc] initWithIconRef:
                         LSSharedFileListItemCopyIconRef((LSSharedFileListItemRef)obj)] autorelease]];
        return [item autorelease];
    }];
    CFRelease(tmp);
    return (value);
}

+ (BOOL)addItemWithURL:(NSURL *)url toList:(CFStringRef)list_name
{
    LSSharedFileListRef list = LSSharedFileListCreate(NULL, (CFStringRef)list_name, NULL);
    if (!list) return NO;
    LSSharedFileListItemRef item = LSSharedFileListInsertItemURL(list,
                                                                 kLSSharedFileListItemBeforeFirst,
                                                                 NULL, NULL,
                                                                 (CFURLRef)url,
                                                                 NULL, NULL);
    CFRelease(list);
    return item ? (CFRelease(item), YES) : NO;
}

+ (BOOL)removeItemWithIndex:(NSInteger)index fromList:(CFStringRef)list_name
{
    if (index == -1) return NO;
    LSSharedFileListRef list = LSSharedFileListCreate(NULL, (CFStringRef)list_name, NULL);
    LSSharedFileListItemRef item_to_remove = (LSSharedFileListItemRef)[(NSArray *)LSSharedFileListCopySnapshot(list, NULL)
                                                                       objectAtIndex: index];
    if (!item_to_remove || !list) return (NO);
    LSSharedFileListItemRemove(list , item_to_remove);
    CFRelease(list);
    return (YES);
}

+ (BOOL)removeItemWithURL:(NSURL *)url fromList:(CFStringRef)list_name
{
    return [EBLaunchServices removeItemWithIndex:
            [EBLaunchServices indexOfItemWithURL: url inList: (CFStringRef)list_name]
                                        fromList: list_name];
}

+ (BOOL)clearList:(CFStringRef)list_name
{
    LSSharedFileListRef list = LSSharedFileListCreate(NULL, (CFStringRef)list_name, NULL);
    BOOL isok = (LSSharedFileListRemoveAllItems(list) == noErr);
    return (CFRelease(list), isok);
}


#pragma mark Applications

+ (NSArray *)allApplicationsFormattedAs:(enum EBItemsViewFormat)response_format
{
    NSArray *tmp = nil;
    _LSCopyAllApplicationURLs((CFArrayRef *)&tmp);
    return [EBLaunchServices prepareArray: [tmp autorelease] withFormat: response_format];
    
}

+ (NSArray *)allApplicationsAbleToOpenFileExtension:(NSString *)extension
                                     responseFormat:(enum EBItemsViewFormat)response_format
{
    CFStringRef uttype = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension,
                                                               (CFStringRef)extension, NULL);
    CFArrayRef bundles = LSCopyAllRoleHandlersForContentType(uttype, kLSRolesAll);
    CFRelease(uttype);
    if (!bundles) return nil;
    return [EBLaunchServices prepareArray: [(NSArray *)bundles autorelease]
                               withFormat: response_format];
}


+ (NSArray *)allAvailableFileTypesForApplication:(NSString *)full_path
{
    NSArray *all_doc_types = [[NSDictionary dictionaryWithContentsOfFile: full_path]
                              objectForKey: @"CFBundleDocumentTypes"];
    if ( ! all_doc_types) return nil;
    
    return [EBLaunchServices mappingArray: all_doc_types
                               usingBlock:^id(NSDictionary * obj) {
                                   /* Use 0 as index because it's highest level of file types' hierarchy */
                                   id tmp =  [[obj objectForKey: @"LSItemContentTypes"] objectAtIndex: 0];
                                   return tmp;
                               }];
}

/* Return only MIMEs defined in LaunchService database */
+ (NSArray *)allAvailableMIMETypesForApplication:(NSString *)full_path
{
    NSArray *all_doc_types = [[NSDictionary dictionaryWithContentsOfFile: full_path]
                              objectForKey: @"CFBundleDocumentTypes"];
    if ( ! all_doc_types) return nil;
    
    return [EBLaunchServices mappingArray: all_doc_types usingBlock:^id(id obj) {
        
        NSArray * tmp_array = [obj objectForKey: @"LSItemContentTypes"];
        /* If we can't recognize a MIME type for some file type - take a look on it' parent type */
        /* e.g. com.pkware.zip-archive (no MIME type) --> public.zip-archive (MIME is OK)*/
        id value = nil;
        for (NSUInteger i = 0; i < [tmp_array count] && !value; i++) {
            value = (NSString *)UTTypeCopyPreferredTagWithClass((CFStringRef)[tmp_array objectAtIndex: i],
                                                                kUTTagClassMIMEType);
        }
        return value;
    }];
}


+ (NSArray *)allAvailableFileExtensionsForApplication:(NSString *)full_path
{
    NSArray *all_doc_types = [[NSDictionary dictionaryWithContentsOfFile: full_path]
                              objectForKey: @"CFBundleDocumentTypes"];
    if ( !all_doc_types) {
        return nil;
    }
    NSMutableArray *value = [[NSMutableArray alloc] init];
    [all_doc_types enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        [value addObjectsFromArray: [obj objectForKey: @"CFBundleTypeExtensions"]];
    }];
    return [value autorelease];
}




#pragma mark Files

#define fileExists(x) ([[NSFileManager defaultManager] fileExistsAtPath: (x)])

+ (NSString *)humanReadableTypeForFile:(NSString *)full_path
{
    if ( ! fileExists(full_path)) return nil;
    FSRef fsref;
    CFStringRef ftype;
    FSPathMakeRef((const UInt8 *)[full_path fileSystemRepresentation], &fsref, NULL);
    LSCopyKindStringForRef(&fsref, &ftype);
    NSString *value = [NSString stringWithString: (NSString *)ftype];
    CFRelease(ftype);
    return value;
}

+ (NSString *)mimeTypeForFile:(NSString *)full_path
{
    if ( ! fileExists(full_path)) return nil;
    
    NSString *extension = [full_path pathExtension];
    if (!extension || [extension isEqualToString: @""]) {
        return nil;
    }
    CFStringRef uttype = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension,
                                                               (CFStringRef)extension, NULL);
    CFStringRef mime = UTTypeCopyPreferredTagWithClass(uttype, kUTTagClassMIMEType);
    CFRelease(uttype);
    return mime ? [(NSString *)mime autorelease] : nil;
}

+ (NSArray *)allAvailableFileExtensionsForUTI:(NSString *)file_type
{
    NSDictionary * tmp_dict = (NSDictionary *)UTTypeCopyDeclaration((CFStringRef)file_type);
    id value = [[tmp_dict valueForKey: @"UTTypeTagSpecification"]
                valueForKey: @"public.filename-extension"];
    [tmp_dict release];
    
    return [value isKindOfClass:NSClassFromString(@"NSArray")] ? value : [NSArray arrayWithObject: value];
}

+ (NSString *)preferredFileExtensionForMIMEType:(NSString *)mime_type
{
    CFStringRef uttype = UTTypeCreatePreferredIdentifierForTag(kUTTagClassMIMEType,
                                                               (CFStringRef)mime_type, NULL);
    CFStringRef extension = UTTypeCopyPreferredTagWithClass(uttype, kUTTagClassFilenameExtension);
    CFRelease(uttype);
    return extension ? [(NSString *)extension autorelease] : nil;
}

+ (NSArray *)allAvailableFileExtensionsForMIMEType:(NSString *)mime_type
{
    CFStringRef uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassMIMEType,
                                                            (CFStringRef)mime_type, NULL);
    id value = [EBLaunchServices allAvailableFileExtensionsForUTI: (NSString *)uti];
    return (CFRelease(uti), value);
}

+ (NSArray  *)allAvailableFileExtensionsForPboardType:(NSString *)pboard_type
{
    CFStringRef uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassNSPboardType,
                                                            (CFStringRef)pboard_type, NULL);
    id value = [EBLaunchServices allAvailableFileExtensionsForUTI: (NSString *)uti];
    return (CFRelease(uti), value);
}

+ (NSArray *)allAvailableFileExtensionsForFileExtension:(NSString *)extension
{
    CFStringRef uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension,
                                                            (CFStringRef)extension, NULL);
    id value = [EBLaunchServices allAvailableFileExtensionsForUTI: (NSString *)uti];
    return (CFRelease(uti), value);
}



#pragma mark Private

+ (NSArray *)prepareArray:(NSArray *)array withFormat:(enum EBItemsViewFormat)format
{
    switch ((int)format) {
        case EBItemsAsPaths: {
            return [EBLaunchServices mappingArray: array usingBlock:^id(id obj) {
                id path = [[NSWorkspace sharedWorkspace] absolutePathForAppBundleWithIdentifier: obj];
                return path;;
            }];
        }
        case EBItemsAsNames: {
            return [EBLaunchServices mappingArray: array usingBlock:^id(id obj) {
                id name = [[NSFileManager defaultManager] displayNameAtPath:
                           [[NSWorkspace sharedWorkspace] absolutePathForAppBundleWithIdentifier: obj]];
                return  name;
            }];
        }
        default:
            //case EBItemsAsURLs:
            return array;
    }
}

+ (NSInteger)indexOfItemWithURL:(NSURL *)url inList:(CFStringRef)list_name
{
    NSArray *tmp = [EBLaunchServices allItemsFromList: list_name];
    
    if (!tmp) {
        return -1;
    }
    
    NSUInteger idx = [tmp indexOfObjectPassingTest: ^BOOL(id obj, NSUInteger idx, BOOL *stop) {
        return [[(EBLaunchServicesListItem *)obj url] isEqualTo: url];
    }];
    
    if (idx == NSNotFound || idx == -1) {
        return -1;
    }
    
    return idx;
}

/* Going throw an array's elements doing something with them, and create items for a new array */

+ (NSArray *)mappingArray:(NSArray *)array usingBlock:(EBMappingBlock)block
{
    NSUInteger count = [array count];
    id *objects = malloc(sizeof(objects)*count);
    [array enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        objects[idx] = [block(obj) retain];
        if ( ! objects[idx]) objects[idx] = [NSNull null];
    }];
    NSMutableArray *return_value = [NSMutableArray arrayWithArray:
                                    [NSArray arrayWithObjects: objects count: count]];
    [return_value removeObjectsAtIndexes:
     [return_value indexesOfObjectsPassingTest:^BOOL(id obj, NSUInteger idx, BOOL *stop) {
        return [obj isEqualTo: [NSNull null]];
    }]];
    
    for (NSUInteger i = 0; i < count; i++) {
        [objects[i] release];
    }
    free(objects);
    return (return_value);
}
@end
