//
//  main.m
//  LiferayFinderPlugin
//
//  Created by Vitaly Eremenko (vitaly.eremenko@teamdev.com) on 28.09.12.
//

#import <Cocoa/Cocoa.h>
#import <mach_inject_bundle/mach_inject_bundle.h>

static
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


int main(int argc, char *argv[])
{
    NSString *bundlePath = [[NSBundle mainBundle]
							pathForResource:@"LiferayFinderCore" ofType:@"bundle"];
	
	ProcessSerialNumber psn;
	
	FindProcessBySignature( 'FNDR', 'MACS', &psn );
	
	pid_t pid;
	GetProcessPID(&psn, &pid);				  
	
	mach_error_t err = mach_inject_bundle_pid([bundlePath fileSystemRepresentation], pid);
    
    return 1;
}
