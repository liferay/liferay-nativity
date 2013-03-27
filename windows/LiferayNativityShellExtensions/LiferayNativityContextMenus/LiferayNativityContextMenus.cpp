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

#include "LiferayNativityContextMenus.h"

#include "ContextMenuContants.h"

#include <Shellapi.h>
#include <Shlwapi.h>
#include <stdlib.h>
#include <Strsafe.h>

#include <iostream>
#include <fstream>

using namespace std;

#pragma comment(lib, "shlwapi.lib")

extern HINSTANCE instanceHandle;

#define IDM_DISPLAY 0  
#define IDB_BMP 160

LiferayNativityContextMenus::LiferayNativityContextMenus(): _contextMenuUtil(0), _referenceCount(1), _nFiles(0)
{	
}

LiferayNativityContextMenus::~LiferayNativityContextMenus(void)
{
}

IFACEMETHODIMP_(ULONG) LiferayNativityContextMenus::AddRef()
{
	return InterlockedIncrement(&_referenceCount);
}

IFACEMETHODIMP LiferayNativityContextMenus::GetCommandString(UINT_PTR idCommand, UINT uFlags, UINT *pwReserved, LPSTR pszName, UINT cchMax)
{
	HRESULT hResult = S_OK;

    switch (uFlags)
    {
    case GCS_HELPTEXTW:
        hResult = _GetHelpText(idCommand, pszName, cchMax);

        break;

    case GCS_VERBW:
        hResult = _GetVerb(idCommand, pszName, cchMax);

		break;

    default:
        hResult = S_OK;
    }

	return hResult;
}

IFACEMETHODIMP LiferayNativityContextMenus::Initialize(LPCITEMIDLIST pidlFolder, LPDATAOBJECT pDataObj, HKEY hKeyProgID)
{
    if (NULL == pDataObj)
    {
        return E_INVALIDARG;
    }

	HRESULT hr = E_FAIL;

    FORMATETC fe = { CF_HDROP, NULL, DVASPECT_CONTENT, -1, TYMED_HGLOBAL };

    STGMEDIUM stm;

    if (!SUCCEEDED(pDataObj->GetData(&fe, &stm)))
    {
		return E_FAIL;
	}

    HDROP hDrop = static_cast<HDROP>(GlobalLock(stm.hGlobal));

    if (hDrop == NULL)
    {
		return E_FAIL;
	}

    _nFiles = DragQueryFile(hDrop, 0xFFFFFFFF, NULL, 0);

    if (_nFiles > 0)
    {
		_contextMenuUtil = new ContextMenuUtil();
        wchar_t szFileName[MAX_PATH];
    
		for (UINT i = 0; i < _nFiles; i++)
        {
			UINT success = DragQueryFile(hDrop, i, szFileName, ARRAYSIZE(szFileName));

            if (success != 0)
            {
				wstring* file = new wstring(szFileName);
				_contextMenuUtil->AddFile(file);
				delete file;
            }
        }
    }

    GlobalUnlock(stm.hGlobal);
    
	ReleaseStgMedium(&stm);

	return S_OK;
}

IFACEMETHODIMP LiferayNativityContextMenus::InvokeCommand(LPCMINVOKECOMMANDINFO pici)
{
	if(_contextMenuUtil == 0)
	{
		_contextMenuUtil = new ContextMenuUtil();
	}

   bool unicode = false;
   int actionIndex = -1;

   if (pici->cbSize == sizeof(CMINVOKECOMMANDINFOEX))
   {
		if (pici->fMask & CMIC_MASK_UNICODE)
        {
		    unicode = true;
        }
    }

    if (!unicode && HIWORD(pici->lpVerb))
    {
		size_t num_chars;
		
		string command = pici->lpVerb;
		
		wchar_t* buf = new wchar_t[ command.size() ];

		errno_t result = mbstowcs_s(&num_chars, buf, command.size(), command.c_str(), _TRUNCATE);
		
		if(result != 0)
		{
			return false;
		}

		wstring* wcommand = new wstring( buf, num_chars );
		delete[] buf; 
        
		actionIndex = _contextMenuUtil->GetActionIndex(wcommand);  

		delete wcommand;
    }
    else if (unicode && HIWORD(((CMINVOKECOMMANDINFOEX*)pici)->lpVerbW))
    {
		wstring* command = new wstring(((CMINVOKECOMMANDINFOEX*)pici)->lpVerbW);

		actionIndex = _contextMenuUtil->GetActionIndex(command); 

		delete command;
    }
    else
    {
        actionIndex = LOWORD(pici->lpVerb);	
    }

	if(actionIndex != -1)
	{
		_contextMenuUtil->PerformAction(actionIndex); 
	}

    return S_OK;
}

IFACEMETHODIMP LiferayNativityContextMenus::QueryContextMenu(HMENU hMenu, UINT indexMenu, UINT idCmdFirst, UINT idCmdLast, UINT uFlags)
{
	//No need for menu, this should not happen
	if(_nFiles == 0)
	{
		return MAKE_HRESULT(SEVERITY_SUCCESS, 0, USHORT(0));
	}

	//Only default menus
    if (CMF_DEFAULTONLY & uFlags)
    {
        return MAKE_HRESULT(SEVERITY_SUCCESS, 0, USHORT(0));
    }

	if(!_contextMenuUtil->IsMenuNeeded())
	{
		return MAKE_HRESULT(SEVERITY_SUCCESS, 0, USHORT(0));
	}

	if(!_contextMenuUtil->InitMenus())
	{
		return MAKE_HRESULT(SEVERITY_SUCCESS, 0, USHORT(0));
	}

	vector<wstring*>* menus = new vector<wstring*>();

	if(!_contextMenuUtil->GetMenus(menus))
	{
		return MAKE_HRESULT(SEVERITY_SUCCESS, 0, USHORT(0));
	}

	if(menus == 0)
	{
		return MAKE_HRESULT(SEVERITY_SUCCESS, 0, USHORT(0));
	}

	//No menus for these files
	if((menus->size() == 0) || menus->empty())
	{
		delete menus;
		return MAKE_HRESULT(SEVERITY_SUCCESS, 0, USHORT(0));
	}

	HMENU subMenuHandle = CreatePopupMenu();

	int cmdCount = 0;

	bool success = true;

	if(_InsertRootMenu(hMenu, subMenuHandle, indexMenu + 2))
	{
		for (vector<wstring*>::iterator it = menus->begin(); it != menus->end(); it++)
		{
			wstring* menu = *it;
			
			if(menu->compare(SEPARATOR) == 0)
			{
				_InsertSeparator(hMenu, cmdCount);
			}
			else
			{
				_InsertMenu(subMenuHandle, cmdCount, idCmdFirst, menu->c_str());
			}

			cmdCount++;
		}
	}

	delete menus;

	//Add menus
	
	idCmdLast = idCmdFirst + cmdCount;

	return MAKE_HRESULT(SEVERITY_SUCCESS, 0, USHORT(IDM_DISPLAY + 1));
}

bool LiferayNativityContextMenus::_InsertSeparator(HMENU hMenu, int location)
{
	MENUITEMINFO sep = { sizeof(sep) };
    sep.fMask = MIIM_TYPE;
    sep.fType = MFT_SEPARATOR;

	if(!InsertMenuItem(hMenu, location, TRUE, &sep))
	{
		return false;
	}

	return true;
}

bool LiferayNativityContextMenus::_InsertRootMenu(HMENU hMenu, HMENU subMenuHandle, int location)
{
	MENUITEMINFO liferaySyncMenuItem = { sizeof(liferaySyncMenuItem) };
		
	liferaySyncMenuItem.fMask = MIIM_STRING | MIIM_SUBMENU;

	wstring* text = new wstring();
	if(!_contextMenuUtil->GetRootText(text))
	{
		return false;
	}
	
	size_t wlen = wcslen(text->c_str());

	wchar_t* wdest = new wchar_t[wlen + 1];

	errno_t error = wcscpy_s(wdest, wlen + 1, text->c_str());

	liferaySyncMenuItem.dwTypeData = (LPWSTR)wdest;

	liferaySyncMenuItem.hSubMenu = subMenuHandle;

	if(!InsertMenuItem(hMenu, location, TRUE, &liferaySyncMenuItem))
	{
		return false;
	}

	return true;
}

bool LiferayNativityContextMenus::_InsertMenu(HMENU subMenuHandle, int location, int command, const wchar_t* text)
{
	MENUITEMINFO menuItem = { sizeof(menuItem) };
	
	menuItem.fMask = MIIM_STRING | MIIM_ID;
	
	size_t wlen = wcslen(text);

	wchar_t* wdest = new wchar_t[wlen + 1];

	errno_t error = wcscpy_s(wdest, wlen + 1, text);

	menuItem.dwTypeData = (LPWSTR)wdest;

	menuItem.wID = command;
		
	if(!InsertMenuItem(subMenuHandle, location, TRUE, &menuItem))
	{
		return false;		
	}

	return true;
}

IFACEMETHODIMP LiferayNativityContextMenus::QueryInterface(REFIID riid, void **ppv)
{
	HRESULT hr = S_OK;

    if (IsEqualIID(IID_IUnknown, riid) || 
        IsEqualIID(IID_IContextMenu, riid))
    {
        *ppv = static_cast<IContextMenu *>(this);
    }
    else if (IsEqualIID(IID_IShellExtInit, riid))
    {
        *ppv = static_cast<IShellExtInit *>(this);
    }
    else
    {
        hr = E_NOINTERFACE;
        *ppv = NULL;
    }

    if (*ppv)
    {
        AddRef();
    }

    return hr;
}

IFACEMETHODIMP_(ULONG) LiferayNativityContextMenus::Release()
{
    long cRef = InterlockedDecrement(&_referenceCount);

	if (0 == cRef)
    {
	    delete this;
    }

	return cRef;
}

HRESULT LiferayNativityContextMenus::_GetHelpText(UINT_PTR idCommand, LPSTR pszName, UINT cchMax)
{
	HRESULT hResult = E_FAIL;

	wstring* helpText = new wstring();

	if(!_contextMenuUtil->GetHelpText((int)idCommand, helpText))
	{
		return hResult;
	}

	hResult = StringCchCopy(
		reinterpret_cast<PWSTR>(pszName), cchMax, helpText->c_str());

	return hResult;
}

HRESULT LiferayNativityContextMenus::_GetVerb(UINT_PTR idCommand, LPSTR pszName, UINT cchMax)
{
	HRESULT hResult = E_FAIL;

	wstring* verbText = new wstring();

	if(!_contextMenuUtil->GetVerbText((int)idCommand, *verbText))
	{
		return hResult;
	}

	hResult = StringCchCopy(
		reinterpret_cast<PWSTR>(pszName), cchMax, verbText->c_str());

	return hResult;
}