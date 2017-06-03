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

#include "ContextMenuAction.h"
#include "ContextMenuConstants.h"
#include "ContextMenuUtil.h"
#include <atlbase.h>
#include <gdiplus.h>
#include <shlobj.h>
#include <uxtheme.h>
#include <windows.h>

using namespace std;

class LiferayNativityContextMenus : public IShellExtInit, public IContextMenu
{
	public:
		LiferayNativityContextMenus(void);

		IFACEMETHODIMP_(ULONG) AddRef();

		IFACEMETHODIMP GetCommandString(UINT_PTR idCommand, UINT uFlags, UINT* pwReserved, LPSTR pszName, UINT cchMax);

		IFACEMETHODIMP Initialize(LPCITEMIDLIST pidlFolder, LPDATAOBJECT pDataObj, HKEY hKeyProgID);

		IFACEMETHODIMP InvokeCommand(LPCMINVOKECOMMANDINFO pici);

		IFACEMETHODIMP QueryContextMenu(HMENU hMenu, UINT indexMenu, UINT idCmdFirst, UINT idCmdLast, UINT uFlags);

		IFACEMETHODIMP QueryInterface(REFIID riid, void** ppv);

		IFACEMETHODIMP_(ULONG) Release();

	protected:
		~LiferayNativityContextMenus(void);

	private:
		int _AddMenu(HMENU, ContextMenuItem*, int, int, UINT);
		
		HRESULT _ConvertBufferToPARGB32(HPAINTBUFFER hPaintBuffer, HDC hdc, HICON hicon, SIZE& sizIcon);
		
		HRESULT _ConvertToPARGB32(HDC hdc, __inout Gdiplus::ARGB *pargb, HBITMAP hbmp, SIZE& sizImage, int cxRow);
		
		HRESULT _Create32BitHBITMAP(HDC hdc, const SIZE *psize, __deref_opt_out void **ppvBits, __out HBITMAP* phBmp);

		void _HandleCommand(LPCMINVOKECOMMANDINFO pici);

		void _HandleUnicodeCommand(LPCMINVOKECOMMANDINFO pici);

		void _HandleLoCommand(LPCMINVOKECOMMANDINFO pici);

		bool _HasAlpha(__in Gdiplus::ARGB *pargb, SIZE& sizImage, int cxRow);

		HBITMAP _IconToBitmapPARGB32(HICON hIcon);

		bool _InsertSeparator(HMENU, int);

		bool _InsertMenu(HMENU, HMENU, int, const wchar_t*);

		bool _InsertMenu(HMENU, int, int, const wchar_t*);

		void _PerformAction(int actionIndex, HWND hWnd);

		ContextMenuUtil* _contextMenuUtil;

		long _referenceCount;

		UINT _nFiles;
};

#endif