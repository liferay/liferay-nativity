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

#include "ContextMenuUtil.h"
#include "ContextMenuContants.h"

#include "CommunicationProcessor.h"
#include "RegistryUtil.h"
#include "FileUtil.h"
#include "UtilConstants.h"

#include <fstream>
#include <iostream>
#include <vector>

using namespace std;

ContextMenuUtil::ContextMenuUtil() : _helpTextList(0), _menuList(0), _rootMenu(0)
{
	_communicationSocket = new CommunicationSocket(PORT);
	_selectedFiles = new vector<wstring>;
}

ContextMenuUtil::~ContextMenuUtil(void)
{
	if(_communicationSocket != 0)
	{
		delete _communicationSocket;
	}

	if(_helpTextList != 0)
	{
		delete _helpTextList;
	}

	if(_menuList != 0)
	{
		delete _menuList;
	}

	if(_rootMenu != 0)
	{
		delete _rootMenu;
	}

	_selectedFiles = 0;
}

bool ContextMenuUtil::AddFile(wstring* file)
{
	_selectedFiles->push_back(*file);

	return true;
}

int ContextMenuUtil::GetActionIndex(wstring* command)
{
	vector<wstring*>::iterator menuIterator = _menuList->begin();

	int index = 0;

	while(menuIterator != _menuList->end()) 
	{
		wstring* menuName = *menuIterator;

		if(menuName->compare(*command) == 0)
		{
			return index;
		}

		menuIterator++;
		index++;
	}

	return -1;
}

bool ContextMenuUtil::GetHelpText(unsigned int index, wstring* helpText)
{
	if(_helpTextList == 0)
	{
		return false;
	}

	if(index >= _helpTextList->size())
	{
		return false;
	}

	helpText = _helpTextList->at(index);
	return true;
}

bool ContextMenuUtil::GetMenus(vector<std::wstring*> *menuList)
{
	if(_menuList == 0)
	{
		return false;
	}

	*menuList = *_menuList;
	return true;
}

bool ContextMenuUtil::GetRootText(wstring* rootText)
{
	if(_rootMenu == 0)
	{
		return false;
	}

	*rootText = *_rootMenu;
	return true;
}

bool ContextMenuUtil::GetVerbText(int index, wstring& verbText)
{
	return _GetCommandText(index, verbText);
}

bool ContextMenuUtil::IsMenuNeeded(void)
{  
	bool needed = false;
	
	if(FileUtil::IsChildFileOfRoot(_selectedFiles))
	{
		needed = true;
	}

	return needed;
}

bool ContextMenuUtil::InitMenus(void)
{
	_rootMenu = new wstring();

	if(!RegistryUtil::ReadRegistry(REGISTRY_ROOT_KEY, REGISTRY_MENU_TITLE, _rootMenu))
	{
		return false;
	}

	if(!_GetMenuList())
	{
		return false;
	}

	if(!_GetHelpText())
	{
		return false;
	}

	return true;
}

bool ContextMenuUtil::PerformAction(int index)
{
	wstring* message = new wstring();
	wstring* response = new wstring();
	bool success = false;

	vector<wstring>* args = new vector<wstring>;
	wchar_t* buf =  new wchar_t(10);
	_itow_s(index, buf, 10, 10);
	wstring* temp = new wstring(buf);
	
	wstring* command = new wstring();

	command = _menuList->at(index);


	args->push_back(*temp);
	args->push_back(*command);

	if(CommunicationProcessor::CreateMessage(PERFORM_ACTION, args, message))
	{
		if(_communicationSocket->SendMessageReceiveResponse(message->c_str(), response))
		{
			success = true;
		}
	}

	delete temp;
	delete response;
	delete command;
	delete args;

	return success;
}

bool ContextMenuUtil::_GetMenuList(void)
{
	wstring* getMenuMessage = new wstring();
	wstring* getMenuReceived = new wstring();
	_menuList = new vector<wstring*>();

	bool success = false;

	if(CommunicationProcessor::CreateMessage(GET_MENU_LIST, _selectedFiles, getMenuMessage))
	{
		if(_communicationSocket->SendMessageReceiveResponse(getMenuMessage->c_str(), getMenuReceived))
		{
			if(CommunicationProcessor::ProcessResponse(getMenuReceived, _menuList))
			{
				success = true;
			}
		}
	}

	delete getMenuMessage;
	delete getMenuReceived;

	return success;
}

bool ContextMenuUtil::_GetCommandText(unsigned int index, wstring& commandText)
{
	if(index < _selectedFiles->size())
	{
		return false;
	}

	vector<wstring>::iterator commandIterator = _selectedFiles->begin();

    std::advance(commandIterator, index);

	commandText = *commandIterator;

	return true;
}

bool ContextMenuUtil::_GetHelpText(void)
{
	wstring* getHelpMessage = new wstring();
	wstring* getHelpReceived = new wstring();
	_helpTextList = new vector<wstring*>();

	bool success = false;

	if(CommunicationProcessor::CreateMessage(GET_HELP_ITEMS, _selectedFiles, getHelpMessage))
	{
		if(_communicationSocket->SendMessageReceiveResponse(getHelpMessage->c_str(), getHelpReceived))
		{
			if(CommunicationProcessor::ProcessResponse(getHelpReceived, _helpTextList))
			{
				success = true;
			}
		}
	}

	delete getHelpMessage;
	delete getHelpReceived;

	return success;
}
