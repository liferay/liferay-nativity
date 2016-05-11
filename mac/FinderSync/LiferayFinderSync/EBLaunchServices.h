/*
 EBLaunchServices.h
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

//  This class allows you to deal with some LaunchServices functions (such a Shared Lists, files types and extension information) via ligth-weight API.
//  You can find the list of all availabe Shared Lists below:
//  
//  kLSSharedFileListFavoriteVolumes
//  kLSSharedFileListFavoriteItems
//  kLSSharedFileListRecentApplicationItems
//  kLSSharedFileListRecentDocumentItems
//  kLSSharedFileListRecentServerItems
//  kLSSharedFileListSessionLoginItems
//  kLSSharedFileListGlobalLoginItems

//
//extern CFStringRef kLSSharedFileListFavoriteVolumes;
//extern CFStringRef kLSSharedFileListFavoriteItems;
//extern CFStringRef kLSSharedFileListRecentApplicationItems;
//extern CFStringRef kLSSharedFileListRecentDocumentItems; 
//extern CFStringRef kLSSharedFileListRecentServerItems;
//extern CFStringRef kLSSharedFileListSessionLoginItems; 
//extern CFStringRef kLSSharedFileListGlobalLoginItems; 

#import <Cocoa/Cocoa.h>


enum EBItemsViewFormat {
    EBItemsAsBundleIDs,
    EBItemsAsPaths,
    EBItemsAsNames
}EBItemsViewFormat;

@interface EBLaunchServices : NSObject

/* Shared lists */
+ (NSArray *)allItemsFromList:(CFStringRef)list_name;
+ (BOOL)addItemWithURL:(NSURL *)url toList:(CFStringRef)list_name;
+ (BOOL)removeItemWithIndex:(NSInteger)index fromList:(CFStringRef)list_name;
+ (BOOL)removeItemWithURL:(NSURL *)url fromList:(CFStringRef)list_name;
+ (BOOL)clearList:(CFStringRef)list_name;

/* Application abilities */
+ (NSArray *)allApplicationsFormattedAs:(enum EBItemsViewFormat)response_format;
+ (NSArray *)allApplicationsAbleToOpenFileExtension:(NSString *)extension responseFormat:(enum EBItemsViewFormat)response_format;

+ (NSArray *)allAvailableFileTypesForApplication:(NSString *)full_path;
+ (NSArray *)allAvailableMIMETypesForApplication:(NSString *)full_path;
+ (NSArray *)allAvailableFileExtensionsForApplication:(NSString *)full_path;

/* General file info - MIME type, preferred extension and human-readable type*/
+ (NSString *)humanReadableTypeForFile:(NSString *)full_path;
+ (NSString *)mimeTypeForFile:(NSString *)full_path;
+ (NSString *)preferredFileExtensionForMIMEType:(NSString *)mime_type;

+ (NSArray *)allAvailableFileExtensionsForUTI:(NSString *)file_type;
+ (NSArray *)allAvailableFileExtensionsForMIMEType:(NSString *)mime_type;
+ (NSArray *)allAvailableFileExtensionsForPboardType:(NSString *)pboard_type;
+ (NSArray *)allAvailableFileExtensionsForFileExtension:(NSString *)extension;

@end

@interface EBLaunchServicesListItem : NSObject 
@property (nonatomic, retain) NSString *name;
@property (nonatomic, retain) NSURL *url;
@property (nonatomic, retain) NSImage *icon;
@end