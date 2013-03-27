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

#include "stdafx.h"

class __declspec(dllexport) ContextMenuUtil
{
public:
	ContextMenuUtil();

	~ContextMenuUtil(void);

	bool AddFile(std::wstring*);

	int GetActionIndex(std::wstring*);

	bool GetHelpText(unsigned int, std::wstring*);

	bool GetMenus(std::vector<std::wstring*>*);

	bool GetRootText(std::wstring*);

	bool GetVerbText(int, std::wstring&);

	bool IsMenuNeeded(void);

	bool InitMenus(void); 
	
	bool PerformAction(int);

private:
	bool _GetMenuList(void);

	bool _GetCommandText(unsigned int,std::wstring&);
	
	bool _GetHelpText(void);

	//bool _GenerateMessage(const wchar_t* , int cmdIndex, std::wstring*);
	
	CommunicationSocket* _communicationSocket;
	
	std::vector<std::wstring*>* _helpTextList;

	std::vector<std::wstring*>* _menuList;

	std::wstring* _rootMenu;

	std::vector<std::wstring>* _selectedFiles;

};

#endif
