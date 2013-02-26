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
#import <mach_inject_bundle/mach_inject_bundle.h>

static

OSErr FindProcessBySignature(OSType type, OSType creator, ProcessSerialNumber* psn)
{
	ProcessSerialNumber tempPSN = { 0, kNoProcess };
	ProcessInfoRec procInfo = { 0 };
	OSErr err = noErr;

	procInfo.processInfoLength = sizeof(ProcessInfoRec);
	procInfo.processName = nil;

	while (!err)
	{
		err = GetNextProcess(&tempPSN);

		if (!err)
		{
			err = GetProcessInformation(&tempPSN, &procInfo);
		}
		if (!err && procInfo.processType == type && procInfo.processSignature == creator)
		{
			*psn = tempPSN;
			return noErr;
		}
	}

	return err;
}

int main(int argc, char* argv[])
{
	NSString* bundlePath = [[NSBundle mainBundle] pathForResource:@"LiferayFinderCore" ofType:@"bundle"];

	ProcessSerialNumber psn;

	FindProcessBySignature('FNDR', 'MACS', &psn);

	pid_t pid;

	GetProcessPID(&psn, &pid);

	mach_inject_bundle_pid([bundlePath fileSystemRepresentation], pid);

	return 1;
}