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

#ifndef CONTEXTMENUUTIL_H
#define CONTEXTMENUUTIL_H

#pragma once

#include "ContextMenuItem.h"
#include "ContextMenuAction.h"
#include "NativityMessage.h"

#include "stdafx.h"

class __declspec(dllexport) ContextMenuUtil
{
public:
	ContextMenuUtil();

	~ContextMenuUtil(void);

	bool AddFile(std::wstring);

	bool GetMenus(std::vector<ContextMenuItem*>*);

	bool GetContextMenuItem(int, ContextMenuItem**);

	bool GetContextMenuAction(std::wstring*, ContextMenuAction**);

	bool GetContextMenuAction(int action, ContextMenuAction**);

	bool IsMenuNeeded(void);

	bool InitMenus(void); 
	
	bool PerformAction(int);

private:
	bool _GetContextMenuItem(int, std::vector<ContextMenuItem*>*, ContextMenuItem**);

	bool _ParseContextMenu(std::wstring*, ContextMenuItem*);

	bool _ParseContextMenuList(std::wstring*, std::vector<ContextMenuItem*>*);

	bool _ProcessContextMenus(NativityMessage*);

	CommunicationSocket* _communicationSocket;
	
	std::vector<ContextMenuItem*>* _menuList;

	std::vector<std::wstring>* _selectedFiles;

};

#endif
