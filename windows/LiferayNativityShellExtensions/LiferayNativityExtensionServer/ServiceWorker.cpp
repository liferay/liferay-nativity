/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

#include "ServiceWorker.h"
#include "stdafx.h"
#include "ConfigurationConstants.h"
#include "CommunicationProcessor.h"
#include "RegistryUtil.h"
using namespace std;

ServiceWorker::ServiceWorker()
{
}

ServiceWorker::~ServiceWorker()
{
}

bool ServiceWorker::ProcessMessages(map<wstring*, vector<wstring*>*>* messages)
{
	map<wstring*, std::vector<wstring*>*>::iterator it;

	for(it=messages->begin(); it !=messages->end(); it++) 
	{
		wstring* command = it->first;
		vector<wstring*>* args = it->second;

		if(command->compare(CMD_PI_ENABLE_FILE_ICONS) == 0)
		{
			wcout<<command->c_str()<<endl;
			_EnableFileIcons(args);
		}
		else if(command->compare(CMD_PI_SET_MENU_TITLE) == 0)
		{
			wcout<<command->c_str()<<endl;
			_SetMenuTitle(args);
		}
		else if(command->compare(CMD_PI_SET_ROOT_FOLDER) == 0)
		{
			wcout<<command->c_str()<<endl;
			_SetRootFolder(args);
		}
		else if(command->compare(CMD_PI_SET_SYSTEM_FOLDER) == 0)
		{
			wcout<<command->c_str()<<endl;
			_MarkSystem(args);
		}
		else if(command->compare(CMD_PI_UPDATE_FILE_ICON) == 0)
		{
			wcout<<command->c_str()<<endl;
			_UpdateOverlay(args);
		}
		else if(command->compare(CMD_PI_CLEAR_FILE_ICON) == 0)
		{
			wcout<<command->c_str()<<endl;
			_ClearFileIcons(args);
		}

		delete command;
		delete args;
	}

	return true;
}

bool ServiceWorker::_ClearFileIcons(std::vector<std::wstring*>* arguments)
{
	for (vector<wstring*>::iterator it = arguments->begin() ; it != arguments->end(); it++)
	{
		SHChangeNotify(SHCNE_DELETE, SHCNF_PATH | SHCNF_FLUSH, *it, 0);
		delete *it;
	}

	return true;
}

bool ServiceWorker::_EnableFileIcons(std::vector<std::wstring*>* arguments)
{
	if(arguments->size() < 1)
	{
		return false;
	}

	wstring* value = arguments->at(0);

	int overlays = 0;

	if(value->compare(L"0") == 0)
	{
		overlays = 0;
	}
	else if(value->compare(L"1") == 0)
	{
		overlays = 1;
	}
	else if(value->compare(L"true") == 0)
	{
		overlays = 1;
	}
	else if(value->compare(L"false") == 0)
	{
		overlays = 0;
	}

	RegistryUtil::WriteRegistry(REGISTRY_ROOT_KEY, REGISTRY_ENABLE_OVERLAY, overlays);
	
	delete value;

	return true;
}

bool ServiceWorker::_MarkSystem(std::vector<std::wstring*>* arguments)
{
	for (vector<wstring*>::iterator it = arguments->begin() ; it != arguments->end(); it++)
	{
		SetFileAttributes((*it)->c_str(), FILE_ATTRIBUTE_SYSTEM);

		delete *it;
	}

	return true;
}

bool ServiceWorker::_SetMenuTitle(std::vector<std::wstring*>* arguments)
{
	if(arguments->size() != 1)
	{
		return false;
	}

	wstring *title = arguments->at(0);

	RegistryUtil::WriteRegistry(REGISTRY_ROOT_KEY, REGISTRY_MENU_TITLE, title->c_str());
	
	delete title;

	return true;
}

bool ServiceWorker::_SetRootFolder(std::vector<std::wstring*>* arguments)
{
	if(arguments->size() != 1)
	{
		return false;
	}

	wstring *rootFolder = arguments->at(0);

	RegistryUtil::WriteRegistry(REGISTRY_ROOT_KEY, REGISTRY_ROOT_FOLDER, rootFolder->c_str());
	
	delete rootFolder;

	return true;
}

bool ServiceWorker::_UpdateOverlay(std::vector<std::wstring*>* arguments)
{
	for (vector<wstring*>::iterator it = arguments->begin() ; it != arguments->end(); it++)
	{
		wstring* file = *it;
		DWORD fileAttributes = GetFileAttributes(file->c_str());

		if(fileAttributes & INVALID_FILE_ATTRIBUTES)
		{
		}
			//		File temp = new File(fileModel.getFilePath());
	
			//if (!temp.exists()) {
			//	return;
			//}
	
			//if (currentState == FileState.DELETED_REMOTE) {
			//	if (fileModel.isDirectory()) {
			//		WindowsUtil.updateExplorer(
			//			windowsPath, ExplorerEventType.SHCNE_RMDIR.ordinal());
	
			//		try {
			//			fileStateChanged(
			//				PathUtil.getParentPath(path), FileState.DOWNLOADED);
			//		}
			//		catch (Exception e) {
			//			_logger.error(e.getMessage(), e);
			//		}
	
			//	}
			//	else {
			//		WindowsUtil.updateExplorer(
			//			windowsPath, ExplorerEventType.SHCNE_DELETE.ordinal());
			//	}
			//}
			//else {
			//	WindowsUtil.updateExplorer(
			//		windowsPath, ExplorerEventType.SHCNE_UPDATE_ITEM.ordinal());
			//}


		SHChangeNotify(SHCNE_UPDATEITEM, SHCNF_PATH | SHCNF_FLUSH, *it, 0);
		delete *it;
	}

	return true;
}	
