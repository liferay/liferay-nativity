//
//  main.m
//  LiferayFinderPluginInstaller
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 11/22/12.
//

#import <Cocoa/Cocoa.h>
#import <ServiceManagement/ServiceManagement.h>
#import <Security/Authorization.h>


void createInstallInfo(const char* path)
{
    FILE* f = fopen("/tmp/liferayPlugin.info", "w+");

    
    NSString* strPath = [[[NSString stringWithUTF8String:path] stringByDeletingLastPathComponent] stringByDeletingLastPathComponent];
    
    fprintf(f, [strPath UTF8String]);
    fprintf(f, "/Resources/LiferayFinderPlugin.app");
    
    fclose(f);
}

int main(int argc, char *argv[])
{
    NSError *error = nil;
	BOOL result = NO;
    
    
	AuthorizationItem authItem		= { kSMRightBlessPrivilegedHelper, 0, NULL, 0 };
	AuthorizationRights authRights	= { 1, &authItem };
	AuthorizationFlags flags		=	kAuthorizationFlagDefaults				| 
    kAuthorizationFlagInteractionAllowed	|
    kAuthorizationFlagPreAuthorize			|
    kAuthorizationFlagExtendRights;
    
	AuthorizationRef authRef = NULL;
	
	/* Obtain the right to install privileged helper tools (kSMRightBlessPrivilegedHelper). */
	OSStatus status = AuthorizationCreate(&authRights, kAuthorizationEmptyEnvironment, flags, &authRef);
	if (status != errAuthorizationSuccess) {
		NSLog(@"Failed to create AuthorizationRef, return code %i", status);
	} else {
        createInstallInfo(argv[0]);
		result = SMJobBless(kSMDomainSystemLaunchd, (CFStringRef)@"com.liferay.FinderPluginHelper", authRef, (CFErrorRef *)error);
	}
	
	return result;

    return 1;//NSApplicationMain(argc, (const char **)argv);
}
