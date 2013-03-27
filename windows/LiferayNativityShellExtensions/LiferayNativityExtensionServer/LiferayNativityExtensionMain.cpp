/**
 *  Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *  
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *  
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 */

#include "stdafx.h"
#include "LiferayNativityExtensionService.h"
#include "ConfigurationConstants.h"
#include "ServiceInstaller.h"

using namespace std;

int wmain(int argc, wchar_t *argv[]) 
    { 
		if(argc <= 1)
		{
			LiferayNativityExtensionService service(SERVICE_NAME); 
			service.Test();
		}
        else if (argc == 2) 
        { 
            if (_wcsicmp(L"install", argv[1] + 1) == 0) 
            { 
                InstallService( 
                    SERVICE_NAME, SERVICE_DISPLAY_NAME, SERVICE_START_TYPE,
                    SERVICE_DEPENDENCIES, SERVICE_ACCOUNT, SERVICE_PASSWORD);
            } 
            else if (_wcsicmp(L"remove", argv[1] + 1) == 0) 
            { 
				UninstallService(SERVICE_NAME); 
            }
			else if (_wcsicmp(L"test", argv[1] + 1) == 0) 
            { 
            	LiferayNativityExtensionService service(SERVICE_NAME); 
				service.Test();
            }
        } 
        else 
        { 
            cout<<"Usage: LiferayNativityExtensionService -option"<<endl; 
            cout<<" -install \t to install Nativity Service"<<endl; 
            cout<<" -remove \t to remove Nativity Service"<<endl; 
			cout<<" none \t to run Nativity Service"<<endl;
        } 
 
 
        return 0; 
    } 