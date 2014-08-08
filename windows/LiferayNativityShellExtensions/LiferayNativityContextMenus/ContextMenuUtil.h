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

#include "ContextMenuAction.h"
#include "ContextMenuItem.h"
#include "ContextMenuConstants.h"
#include "FileUtil.h"
#include "NativityMessage.h"
#include "RegistryUtil.h"
#include "StringUtil.h"
#include "UtilConstants.h"
#include "json/json.h"
#include "stdafx.h"

#include <vector>

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

		bool _ParseContextMenuList(std::wstring*, std::vector<ContextMenuItem*>*);

		bool _ParseContextMenuItem(const Json::Value&, ContextMenuItem*);

		bool _ProcessContextMenus(NativityMessage*);

		CommunicationSocket* _communicationSocket;

		std::vector<ContextMenuItem*>* _menuList;

		std::vector<std::wstring>* _selectedFiles;
};

#endif
