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
#include "RegistryUtil.h"
#include "ParserUtil.h"

using namespace std;

ServiceWorker::ServiceWorker()
{
}

ServiceWorker::~ServiceWorker()
{
}

bool ServiceWorker::ProcessMessages(vector<NativityMessage*>* messages)
{
	vector<NativityMessage*>::iterator it;

	for(it=messages->begin(); it !=messages->end(); it++) 
	{
		NativityMessage* nativityMessage = *it;

		if(nativityMessage->GetCommand()->compare(CMD_ENABLE_FILE_ICONS) == 0)
		{
			_EnableFileIcons(nativityMessage->GetValue());
		}
		else if(nativityMessage->GetCommand()->compare(CMD_SET_FILTER_PATH) == 0)
		{
			_SetFilterPath(nativityMessage->GetValue());
		}
		else if(nativityMessage->GetCommand()->compare(CMD_SET_SYSTEM_FOLDER) == 0)
		{
			_SetSystemFolder(nativityMessage->GetValue());
		}
		else if(nativityMessage->GetCommand()->compare(CMD_UPDATE_FILE_ICON) == 0)
		{
			_UpdateFileIcons(nativityMessage->GetValue());
		}
		else if(nativityMessage->GetCommand()->compare(CMD_CLEAR_FILE_ICON) == 0)
		{
			_ClearFileIcon(nativityMessage->GetValue());
		}

		delete nativityMessage;
	}

	return true;
}

bool ServiceWorker::_ClearFileIcon(wstring* value)
{
	if(!ParserUtil::IsList(value))
	{
		SHChangeNotify(SHCNE_DELETE, SHCNF_PATH | SHCNF_FLUSH, value, 0);
		return true;
	}

	vector<wstring*>* list = new vector<wstring*>();

	if(!ParserUtil::ParseList(value, list))
	{
		return false;
	}

	for (vector<wstring*>::iterator it = list->begin() ; it != list->end(); it++)
	{
		SHChangeNotify(SHCNE_DELETE, SHCNF_PATH | SHCNF_FLUSH, *it, 0);
		delete *it;
	}

	return true;
}

bool ServiceWorker::_EnableFileIcons(wstring* value)
{
	if(value->size() < 1)
	{
		return false;
	}

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

bool ServiceWorker::_SetSystemFolder(wstring* value)
{
	if(!ParserUtil::IsList(value))
	{
		SetFileAttributes(value->c_str(), FILE_ATTRIBUTE_SYSTEM);
		return true;
	}

	vector<wstring*>* list = new vector<wstring*>();

	if(!ParserUtil::ParseList(value, list))
	{
		return false;
	}

	for (vector<wstring*>::iterator it = list->begin() ; it != list->end(); it++)
	{
		SetFileAttributes((*it)->c_str(), FILE_ATTRIBUTE_SYSTEM);
		delete *it;
	}

	return true;
}

bool ServiceWorker::_SetFilterPath(wstring* value)
{
	if(value->size() < 1)
	{
		return false;
	}

	while(value->find(L"\\\\", 0) != string::npos)
	{
		size_t temp = value->find(L"\\\\", 0);
		
		value->replace(temp, 2, L"\\");
	}

	RegistryUtil::WriteRegistry(REGISTRY_ROOT_KEY, REGISTRY_FILTER_PATH, value->c_str());
	
	return true;
}

bool ServiceWorker::_UpdateFileIcons(wstring* value)
{
	if(!ParserUtil::IsList(value))
	{
		_UpdateFileIcon(value->c_str());
		return true;
	}

	vector<wstring*>* list = new vector<wstring*>();

	if(!ParserUtil::ParseList(value, list))
	{
		return false;
	}

	for (vector<wstring*>::iterator it = list->begin() ; it != list->end(); it++)
	{
		wstring* path = *it;
		_UpdateFileIcon(path->c_str());
		delete *it;
	}

	return true;
}	

bool ServiceWorker::_UpdateFileIcon(const wchar_t* file)
{
	DWORD fileAttributes = GetFileAttributes(file);

	if(fileAttributes & INVALID_FILE_ATTRIBUTES)
	{
		SHChangeNotify(SHCNE_UPDATEITEM, SHCNF_PATH | SHCNF_FLUSH, file, 0);
	}

	return true;
}
