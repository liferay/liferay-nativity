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

#import <Cocoa/Cocoa.h>
#import <ServiceManagement/ServiceManagement.h>
#import <Security/Authorization.h>

void createInstallInfo(const char* path);

void createInstallInfo(const char* path)
{
	FILE* f = fopen("/tmp/liferayPlugin.info", "w+");

	NSString* strPath = [[[NSString stringWithUTF8String:path] stringByDeletingLastPathComponent] stringByDeletingLastPathComponent];

    fputs([strPath UTF8String], f);
	fprintf(f, "/Resources/LiferayFinderPlugin.app");

	fclose(f);
}

int main(int argc, char* argv[])
{
	NSError* error = nil;
	BOOL result = NO;

	AuthorizationItem authItem = { kSMRightBlessPrivilegedHelper, 0, NULL, 0 };
	AuthorizationRights authRights = { 1, &authItem };
	AuthorizationFlags flags = kAuthorizationFlagDefaults | kAuthorizationFlagInteractionAllowed | kAuthorizationFlagPreAuthorize | kAuthorizationFlagExtendRights;
	AuthorizationRef authRef = NULL;

	/* Obtain the right to install privileged helper tools (kSMRightBlessPrivilegedHelper). */
	OSStatus status = AuthorizationCreate(&authRights, kAuthorizationEmptyEnvironment, flags, &authRef);

	if (status == errAuthorizationSuccess)
	{
		createInstallInfo(argv[0]);
		
		result = SMJobBless(kSMDomainSystemLaunchd, (CFStringRef)@"com.liferay.FinderPluginHelper", authRef, (CFErrorRef*)error);

		if (result)
		{
			return 0;
		}
		else
		{
			NSLog(@"SMJobBless failed to execute, error %@", error);

			return 1;
		}
	}
	else
	{
		NSLog(@"Failed to create AuthorizationRef, return code %i", status);

		return 1;
	}
}