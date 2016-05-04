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

#ifndef CONTEXTMENUITEM_H
#define CONTEXTMENUITEM_H

#include "stdafx.h"

class __declspec(dllexport) ContextMenuItem
{
	public:
		ContextMenuItem(void);
		~ContextMenuItem(void);

		ContextMenuItem(const ContextMenuItem&);

		ContextMenuItem& operator=(const ContextMenuItem&);

		void AddContextMenuItem(ContextMenuItem*);

		std::vector<ContextMenuItem*>* GetContextMenuItems();

		bool GetEnabled();

		std::wstring* GetHelpText();

		std::wstring* GetIconPath();

		long GetId();

		int GetIndex();

		std::wstring* GetTitle();

		std::wstring* GetUuid();

		bool HasSubMenus();

		void SetContextMenuItems(std::vector<ContextMenuItem*>*);

		void SetEnabled(bool);

		void SetHelpText(std::wstring*);

		void SetIconPath(std::wstring*);

		void SetId(long);

		void SetIndex(int);

		void SetTitle(std::wstring*);

		void SetUuid(std::wstring*);

	private:
		std::vector<ContextMenuItem*>* _contextMenuItems;

		bool _enabled;

		std::wstring* _helpText;

		std::wstring* _iconPath;

		long _id;

		int _index;

		std::wstring* _title;

		std::wstring* _uuid;
};

#endif