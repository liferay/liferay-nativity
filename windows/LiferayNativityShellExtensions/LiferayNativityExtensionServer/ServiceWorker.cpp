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
		
		delete nativityMessage;
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

	if(!ParserUtil::ParseJsonList(value, list))
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

	if(!ParserUtil::ParseJsonList(value, list))
	{
		return false;
	}

	for (vector<wstring*>::iterator it = list->begin() ; it != list->end(); it++)
	{
		wstring* path = *it;

		SHChangeNotify(SHCNE_UPDATEITEM, SHCNF_PATH | SHCNF_FLUSH, path->c_str(), 0);
		
		delete *it;
	}

	return true;
}	

bool ServiceWorker::_RefreshFile(const wchar_t* file)
{
	SHChangeNotify(SHCNE_UPDATEITEM, SHCNF_PATH | SHCNF_FLUSH, file, 0);

	return true;
}
