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

#define SERVICE_NAME	             L"LiferayNativityExtensionService"
#define SERVICE_DISPLAY_NAME		 L"Liferay Nativity Extension Service"
#define SERVICE_START_TYPE	         SERVICE_AUTO_START
#define SERVICE_DEPENDENCIES		 L""
#define SERVICE_ACCOUNT			     L"NT AUTHORITY\\LocalService"
#define SERVICE_PASSWORD		     NULL

#define SOCKET_ADDRESS				 "127.0.0.1"
#define SOCKET_PORT_SEND			 33001
#define SOCKET_PORT_RECEIVE			 33002

#define CMD_ENABLE_FILE_ICONS		 L"enableFileIcons"
#define CMD_CLEAR_FILE_ICON			 L"clearFileIcon"
#define CMD_UPDATE_FILE_ICON		 L"updateFileIcon"
#define CMD_SET_FILTER_PATH			 L"setFilterPath"
#define CMD_SET_SYSTEM_FOLDER		 L"setSystemFolder"

#define REGISTRY_ROOT_KEY			 L"SOFTWARE\\Liferay Inc\\Liferay Nativity"
#define REGISTRY_ENABLE_OVERLAY		 L"EnableOverlay"
#define REGISTRY_FILTER_PATH		 L"FilterPath"
