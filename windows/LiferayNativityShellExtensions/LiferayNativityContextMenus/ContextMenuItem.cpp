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

#include "ContextMenuItem.h"

using namespace std;

ContextMenuItem::ContextMenuItem() : _enabled(false), _helpText(0), _id(0), _title(0)
{
	_contextMenuItems = new vector<ContextMenuItem*>;
}

ContextMenuItem::~ContextMenuItem()
{
}

ContextMenuItem::ContextMenuItem(const ContextMenuItem& other)
{
    operator=(other);
}

ContextMenuItem& ContextMenuItem::operator=(const ContextMenuItem& other)
{
	ContextMenuItem tmp( other );
	
	swap(_contextMenuItems, tmp._contextMenuItems);
	swap(_enabled, tmp._enabled);
	swap(_helpText, tmp._helpText);
	swap(_id, tmp._id);
	swap(_index, tmp._index);
	swap(_title, tmp._title);

	return *this;
}


std::vector<ContextMenuItem*>* ContextMenuItem::GetContextMenuItems()
{
	return _contextMenuItems;
}

bool ContextMenuItem::GetEnabled()
{
	return _enabled;
}
	
wstring* ContextMenuItem::GetHelpText()
{
	return _helpText;
}
	
long ContextMenuItem::GetId()
{
	return _id;
}

int ContextMenuItem::GetIndex()
{
	return _index;
}
	
wstring* ContextMenuItem::GetTitle()
{
	return _title;
}

bool ContextMenuItem::HasSubMenus()
{
	if(_contextMenuItems == 0)
	{
		return false;
	}

	if(_contextMenuItems->size() == 0)
	{
		return false;
	}

	return true;
}

void ContextMenuItem::SetContextMenuItems(vector<ContextMenuItem*>* list)
{
	_contextMenuItems = list;
}

void ContextMenuItem::SetEnabled(bool enabled)
{
	_enabled = enabled;
}
	
void ContextMenuItem::SetHelpText(wstring* helpText)
{
	_helpText = helpText;
}

void ContextMenuItem::SetId(long id)
{
	_id = id;
}

void ContextMenuItem::SetIndex(int index)
{
	_index = index;
}
	
void ContextMenuItem::SetTitle(wstring* title)
{
	_title = title;
}
