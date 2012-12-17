//
//  main.c
//  com.liferay.FinderPluginHelper
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 11/22/12.
//

#include <syslog.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/syscall.h>
#include <Carbon/Carbon.h>


OSErr
FindProcessBySignature(
					   OSType				type,
					   OSType				creator,
					   ProcessSerialNumber	*psn )
{
    ProcessSerialNumber tempPSN = { 0, kNoProcess };
    ProcessInfoRec procInfo = {0};
    OSErr err = noErr;
    
    procInfo.processInfoLength = sizeof( ProcessInfoRec );
    procInfo.processName = nil;
    //procInfo.processAppSpec = nil;
    
    while( !err ) {
        err = GetNextProcess( &tempPSN );
        if( !err )
            err = GetProcessInformation( &tempPSN, &procInfo );
        if( !err
		   && procInfo.processType == type
		   && procInfo.processSignature == creator ) {
            *psn = tempPSN;
            return noErr;
        }
    }
    
    return err;
}


void checkUpdate()
{
    char buffer[1024];
    char sysCall[2048];
    
    FILE* f = fopen("/tmp/liferayPlugin.info","r");
    if (!f)
        return;
    
    fgets(buffer,1024,f);
    
    fclose(f);
    remove("/tmp/liferayPlugin.info");
    
    system("rm -r \"/Library/Application Support/Liferay\"");
    system("mkdir \"/Library/Application Support/Liferay\"");
    
    sprintf(sysCall , "cp -r \"%s\" \"/Library/Application Support/Liferay\"", buffer);
    
    system(sysCall);
}

static pid_t g_lastFinderPid = 0;

void checkFinder()
{
    ProcessSerialNumber psn;
	
	FindProcessBySignature( 'FNDR', 'MACS', &psn );
	
	pid_t pid;
	GetProcessPID(&psn, &pid);
    
    if (pid != g_lastFinderPid)
    {
        system("\"/Library/Application Support/Liferay/LiferayFinderPlugin.app/Contents/MacOS/LiferayFinderPlugin\"");
        g_lastFinderPid = pid;
        
    }
}

int main (int argc, const char * argv[])
{    
    checkUpdate();
    
    for(;;)
    {
        checkFinder();
        
        sleep(5);
    }
    return 0;
}

