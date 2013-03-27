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

#ifndef LIFERAYNATIVITYCONTEXTMENUS_H
#define LIFERAYNATIVITYCONTEXTMENUS_H

#pragma once

#pragma warning (disable : 4251)

#include "ContextMenuUtil.h"

#include <windows.h>
#include <shlobj.h>     


class LiferayNativityContextMenus : public IShellExtInit, public IContextMenu
{
public:
	LiferayNativityContextMenus(void);

	IFACEMETHODIMP_(ULONG) AddRef();

	IFACEMETHODIMP GetCommandString(UINT_PTR idCommand, UINT uFlags, UINT *pwReserved, LPSTR pszName, UINT cchMax);

	IFACEMETHODIMP Initialize(LPCITEMIDLIST pidlFolder, LPDATAOBJECT pDataObj, HKEY hKeyProgID);

	IFACEMETHODIMP InvokeCommand(LPCMINVOKECOMMANDINFO pici);

	IFACEMETHODIMP QueryContextMenu(HMENU hMenu, UINT indexMenu, UINT idCmdFirst, UINT idCmdLast, UINT uFlags);

    IFACEMETHODIMP QueryInterface(REFIID riid, void **ppv);
    
	IFACEMETHODIMP_(ULONG) Release();

protected:
    ~LiferayNativityContextMenus(void);

private:
	HRESULT _GetHelpText(UINT_PTR idCommand, LPSTR pszName, UINT cchMax);

	HRESULT _GetVerb(UINT_PTR idCommand, LPSTR pszName, UINT cchMax);

	void _HandleCommand(LPCMINVOKECOMMANDINFO pici);

	void _HandleUnicodeCommand(LPCMINVOKECOMMANDINFO pici);

	void _HandleLoCommand(LPCMINVOKECOMMANDINFO pici);

	void _PerformAction(int actionIndex, HWND hWnd);

	bool _InsertSeparator(HMENU, int);

	bool _InsertRootMenu(HMENU, HMENU, int);

	bool _InsertMenu(HMENU, int location, int command, const wchar_t*);

	ContextMenuUtil* _contextMenuUtil;

    long _referenceCount;

	UINT _nFiles;
};

#endif