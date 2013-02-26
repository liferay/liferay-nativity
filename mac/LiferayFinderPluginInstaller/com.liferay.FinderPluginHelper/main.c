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

#include <syslog.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/syscall.h>
#include <Carbon/Carbon.h>

OSErr FindProcessBySignature(OSType type, OSType creator, ProcessSerialNumber* psn);
void checkFinder(void);
void checkUpdate(void);

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

static pid_t g_lastFinderPid = 0;

void checkFinder(void)
{
	ProcessSerialNumber psn;

	FindProcessBySignature('FNDR', 'MACS', &psn);

	pid_t pid;

	GetProcessPID(&psn, &pid);

	if (pid != g_lastFinderPid)
	{
		sleep(1);

		int err = system("\"/Library/Application Support/Liferay/LiferayFinderPlugin.app/Contents/MacOS/LiferayFinderPlugin\"");

		char buff[40];

		sprintf(buff, "error: %d", err);

		g_lastFinderPid = pid;
	}
}

void checkUpdate(void)
{
	char buffer[1024];
	char sysCall[2048];

	FILE* f = fopen("/tmp/liferayPlugin.info", "r");

	if (!f)
	{
		return;
	}

	fgets(buffer, 1024, f);

	fclose(f);
	remove("/tmp/liferayPlugin.info");

	system("rm -r \"/Library/Application Support/Liferay\"");
	system("mkdir \"/Library/Application Support/Liferay\"");

	sprintf(sysCall, "cp -r \"%s\" \"/Library/Application Support/Liferay\"", buffer);

	system(sysCall);
}

int main(int argc, const char* argv[])
{
	checkUpdate();

	for (;;)
	{
		checkFinder();

		sleep(5);
	}

	return 0;
}
