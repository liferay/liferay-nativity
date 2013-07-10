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
#include "UtilConstants.h"

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

		if(nativityMessage->GetCommand()->compare(CMD_SET_SYSTEM_FOLDER) == 0)
		{
			_SetSystemFolder(nativityMessage->GetValue());
		}
		else if(nativityMessage->GetCommand()->compare(CMD_REFRESH_FILES) == 0)
		{
			_RefreshFiles(nativityMessage->GetValue());
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

bool ServiceWorker::_RefreshFiles(wstring* value)
{
	if(!ParserUtil::IsList(value))
	{
		_RefreshFile(value->c_str());
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
		_RefreshFile(path->c_str());
		delete *it;
	}

	return true;
}	

bool ServiceWorker::_RefreshFile(const wchar_t* file)
{
	DWORD fileAttributes = GetFileAttributes(file);

	if(fileAttributes & INVALID_FILE_ATTRIBUTES)
	{
		SHChangeNotify(SHCNE_UPDATEITEM, SHCNF_PATH | SHCNF_FLUSH, file, 0);
	}

	return true;
}
