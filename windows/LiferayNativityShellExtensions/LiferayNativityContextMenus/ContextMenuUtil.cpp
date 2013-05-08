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
#include "ContextMenuItem.h"

#include "RegistryUtil.h"
#include "FileUtil.h"
#include "UtilConstants.h"
#include "ParserUtil.h"

#include <vector>

using namespace std;

ContextMenuUtil::ContextMenuUtil() : _menuList(0)
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
		
	if(_menuList != 0)
	{
		delete _menuList;
	}

	_selectedFiles = 0;
}

bool ContextMenuUtil::AddFile(wstring file)
{
	_selectedFiles->push_back(file);

	return true;
}

bool ContextMenuUtil::GetContextMenuItem(int index, ContextMenuItem** item)
{
	return _GetContextMenuItem(index, _menuList, item);
}

bool ContextMenuUtil::_GetContextMenuItem(int index, vector<ContextMenuItem*>* menus, ContextMenuItem** item)
{
	for(vector<ContextMenuItem*>::iterator it = menus->begin(); it != menus->end(); it++)
	{
		ContextMenuItem* temp = *it;
		if(temp->GetIndex() == index)
		{
			*item = temp;
			return true;
		}

		if(temp->GetContextMenuItems() != 0 && temp->GetContextMenuItems()->size() > 0)
		{
			if(_GetContextMenuItem(index, temp->GetContextMenuItems(), item))
			{
				return true;
			}
		}
	}

	return false;
}

bool ContextMenuUtil::GetMenus(vector<ContextMenuItem*> *menuList)
{
	if(_menuList == 0)
	{
		return false;
	}

	menuList->insert(menuList->end(), _menuList->begin(), _menuList->end());

	return true;
}

bool ContextMenuUtil::IsMenuNeeded(void)
{  
	if(FileUtil::IsChildFileOfRoot(_selectedFiles))
	{
		return true;
	}

	return false;
}

bool ContextMenuUtil::InitMenus(void)
{
	wstring* getMenuMessage = new wstring();
	wstring* getMenuReceived = new wstring();
	_menuList = new vector<ContextMenuItem*>();

	bool success = false;

	wstring* files = new wstring();
	if(!ParserUtil::SerializeList(_selectedFiles, files, false))
	{
		delete files;

		return false;
	}

	NativityMessage* nativityMessage = new NativityMessage();
	nativityMessage->SetCommand(new wstring(GET_CONTEXT_MENU_LIST));
	nativityMessage->SetValue(files);

	if(!ParserUtil::SerializeMessage(nativityMessage, getMenuMessage))
	{
		delete getMenuMessage;
		delete nativityMessage;
		delete files;

		return false;
	}

	if(_communicationSocket->SendMessageReceiveResponse(getMenuMessage->c_str(), getMenuReceived))
	{
		NativityMessage* nativityMessage = new NativityMessage();

		if(nativityMessage->InitFromMessage(getMenuReceived))
		{
			if(_ProcessContextMenus(nativityMessage))
			{
				success = true;
			}
		}
	}

	delete getMenuMessage;
	delete getMenuReceived;

	return success;
}

bool ContextMenuUtil::GetContextMenuAction(std::wstring* title, ContextMenuAction** item)
{
	for(vector<ContextMenuItem*>::iterator it = _menuList->begin(); it != _menuList->end(); it++)
	{
		ContextMenuItem* temp = *it;
		wstring* currentTitle = temp->GetTitle();

		if(currentTitle->compare(*title) == 0)
		{
			ContextMenuAction* action = new ContextMenuAction();
			action->SetId(temp->GetId());
			action->SetFiles(_selectedFiles);

			item = &action;
			return true;
		}
	}

	return false;
}

bool ContextMenuUtil::GetContextMenuAction(int action, ContextMenuAction** item)
{
	ContextMenuItem* contextMenuItem;

	if(GetContextMenuItem(action, &contextMenuItem))
	{
			ContextMenuAction* action = new ContextMenuAction();
			action->SetId(contextMenuItem->GetId());
			action->SetFiles(_selectedFiles);

			item = &action;
			
			return true;
	}

	return false;
}

bool ContextMenuUtil::PerformAction(int command)
{
	ContextMenuItem*  item;
	
	if(!GetContextMenuItem(command, &item))
	{
		return false;
	}

	wstring* list = new wstring();

	if(!ParserUtil::SerializeList(_selectedFiles, list, true))
	{
		return false;
	}
	
	wchar_t* buffer = new wchar_t(10);

	_itow_s(item->GetId(), buffer, 10, 10);

	wstring* idString = new wstring(buffer);

	map<wstring*, wstring*>* message = new map<wstring*, wstring*>();
	wstring* id = new wstring(ID);
	wstring* files = new wstring(FILES);

	message->insert(make_pair(id, idString));
	message->insert(make_pair(files, list));

	wstring* messageString = new wstring();

	if(!ParserUtil::SerializeMessage(message, messageString, true))
	{
		return false;
	}

	NativityMessage* nativityMessage = new NativityMessage();
	
	wstring* title = new wstring(PERFORM_ACTION);
	nativityMessage->SetCommand(title);
	nativityMessage->SetValue(messageString);

	wstring* nativityMessageString = new wstring();

	if(!ParserUtil::SerializeMessage(nativityMessage, nativityMessageString))
	{
		return false;
	}

	wstring* response = new wstring();

	if(!_communicationSocket->SendMessageReceiveResponse(nativityMessageString->c_str(), response))
	{
		return false;
	}

	return true;
}

bool ContextMenuUtil::_ParseContextMenu(wstring* contextMenu, ContextMenuItem* contextMenuItem)
{
	wstring* id = new wstring();

	if(!ParserUtil::GetItem(ID, contextMenu, id))
	{
		return false;
	}

	int id_i = _wtoi(id->c_str());

	contextMenuItem->SetId(id_i);

	wstring* enabled = new wstring();
	
	if(!ParserUtil::GetItem(ENABLED, contextMenu, enabled))
	{
		return false;
	}

	if(enabled->compare(TRUE_TEXT) == 0)
	{
		contextMenuItem->SetEnabled(true);
	}
	else
	{
		contextMenuItem->SetEnabled(false);
	}

	wstring* title =  new wstring();

	if(!ParserUtil::GetItem(TITLE, contextMenu, title))
	{
		return false;
	}

	contextMenuItem->SetTitle(title);

	wstring* helpText = new wstring();

	if(!ParserUtil::GetItem(HELP_TEXT, contextMenu, helpText))
	{
		return false;
	}

	contextMenuItem->SetHelpText(helpText);

	wstring* contextMenuItems = new wstring();

	if(!ParserUtil::GetItem(CONTEXT_MENU_ITEMS, contextMenu, contextMenuItems))
	{
		return false;
	}

	vector<ContextMenuItem*>* contextMenus = new vector<ContextMenuItem*>();
	if(!_ParseContextMenuList(contextMenuItems, contextMenus))
	{
		return false;
	}

	contextMenuItem->SetContextMenuItems(contextMenus);

	return true;
}

bool ContextMenuUtil::_ParseContextMenuList(wstring* contextMenuList, vector<ContextMenuItem*>* contextMenus)
{
	size_t currentLocation = contextMenuList->find(OPEN_CURLY_BRACE, 0);

	vector<wstring*>* menus = new vector<wstring*>();

	while(currentLocation < contextMenuList->size())
	{
		wstring* contextMenu = new wstring();

		currentLocation = ParserUtil::GetNextItemInList(contextMenuList, currentLocation, contextMenu);

		currentLocation++;

		menus->push_back(contextMenu);
	}

	for(vector<wstring*>::iterator it = menus->begin(); it != menus->end(); it++)
	{
		wstring* temp = *it;

		ContextMenuItem* contextMenuItem = new ContextMenuItem();
		
		if(!_ParseContextMenu(temp, contextMenuItem))
		{
			return false;
		}

		contextMenus->push_back(contextMenuItem);
	}

	return true;
}

bool ContextMenuUtil::_ProcessContextMenus(NativityMessage* message)
{
	wstring* value = message->GetValue();

	if(!_ParseContextMenuList(value, _menuList))
	{
		return false;
	}

	return true;
}