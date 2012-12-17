//
//  AppDelegate.m
//  LiferayFinderPluginInstaller
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 11/22/12.
//

#import <ServiceManagement/ServiceManagement.h>
#import <Security/Authorization.h>
#import "AppDelegate.h"

@implementation AppDelegate

//@synthesize window = _window;

- (void)applicationDidFinishLaunching:(NSNotification *)notification
{
	NSError *error = nil;
	if (![self blessHelperWithLabel:@"com.liferay.FinderPluginHelper" error:&error]) {
		NSLog(@"Something went wrong!");
        NSLog([error localizedDescription]);
	} else {
		/* At this point, the job is available. However, this is a very
		 * simple sample, and there is no IPC infrastructure set up to
		 * make it launch-on-demand. You would normally achieve this by
		 * using a Sockets or MachServices dictionary in your launchd.plist.
		 */
		NSLog(@"Job is available!");
	}
}

- (BOOL)blessHelperWithLabel:(NSString *)label error:(NSError **)error;
{
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
		/* This does all the work of verifying the helper tool against the application
		 * and vice-versa. Once verification has passed, the embedded launchd.plist
		 * is extracted and placed in /Library/LaunchDaemons and then loaded. The
		 * executable is placed in /Library/PrivilegedHelperTools.
		 */
		result = SMJobBless(kSMDomainSystemLaunchd, (CFStringRef)label, authRef, (CFErrorRef *)error);
	}
	
	return result;
}
@end


