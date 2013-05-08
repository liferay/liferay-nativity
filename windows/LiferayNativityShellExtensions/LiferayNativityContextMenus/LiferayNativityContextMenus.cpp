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
#include "ContextMenuAction.h"

#include <Shellapi.h>
#include <Shlwapi.h>
#include <stdlib.h>
#include <Strsafe.h>

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
	ContextMenuItem* item;

    switch (uFlags)
    {
    case GCS_HELPTEXTW:
		if(!_contextMenuUtil->GetContextMenuItem((int)idCommand, &item))
		{
			return E_FAIL;
		}

		if(item->GetHelpText() == 0)
		{
			return E_FAIL;
		}

		wcscpy_s((wchar_t*)pszName, cchMax, item->GetHelpText()->c_str());
        break;

    case GCS_VERBW:
        if(!_contextMenuUtil->GetContextMenuItem((int)idCommand, &item))
		{
			return E_FAIL;
		}

		_itow_s(item->GetId(), (wchar_t*)pszName, cchMax, 10);
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
				_contextMenuUtil->AddFile(szFileName);
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
   int index = -1;

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

		wstring *wcommand = new wstring( buf, num_chars );

		index = _wtoi(wcommand->c_str());

		delete[] buf; 
    }
    else if (unicode && HIWORD(((CMINVOKECOMMANDINFOEX*)pici)->lpVerbW))
    {
		wstring* wcommand = new wstring(((CMINVOKECOMMANDINFOEX*)pici)->lpVerbW);

		index = _wtoi(wcommand->c_str());
    }
    else
    {
        index = LOWORD(pici->lpVerb);			
    }

	if(index < 0)
	{
		return E_FAIL;
	}

	if(!_contextMenuUtil->PerformAction(index))
	{
		return E_FAIL;
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

	vector<ContextMenuItem*>* menus = new vector<ContextMenuItem*>();

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

	bool success = true;

	int cmdCount = idCmdFirst;

	_InsertSeparator(hMenu, idCmdFirst);
	
	cmdCount++;

	int location = 0;

	for (vector<ContextMenuItem*>::iterator it = menus->begin(); it != menus->end(); it++)
	{
		ContextMenuItem* menu = *it;
		cmdCount = _AddMenu(hMenu, menu, idCmdFirst + 1, cmdCount, idCmdFirst);
		
		location ++;
	}

	_InsertSeparator(hMenu, idCmdFirst + location);

	cmdCount++;

	return MAKE_HRESULT(SEVERITY_SUCCESS, 0, cmdCount);
}

int LiferayNativityContextMenus::_AddMenu(HMENU hMenu, ContextMenuItem* menu, int location, int cmdCount, UINT offset)
{
	wstring* text = menu->GetTitle();

	menu->SetIndex(cmdCount - offset);

	if(menu->HasSubMenus())
	{
		HMENU subMenuHandle = CreatePopupMenu();

		if(_InsertMenu(hMenu, subMenuHandle, location, text->c_str()))
		{
			cmdCount++;

			vector<ContextMenuItem*>* menus = menu->GetContextMenuItems();
			for (vector<ContextMenuItem*>::iterator it = menus->begin(); it != menus->end(); it++)
			{
				ContextMenuItem* menuA = *it;
				int subLocation = 0;
				
				cmdCount = _AddMenu(subMenuHandle, menuA, subLocation, cmdCount, offset);
				
				subLocation++;
			}
		}
	}
	else if(text->compare(SEPARATOR) == 0)
	{
		_InsertSeparator(hMenu, location);
	}
	else
	{
		if(_InsertMenu(hMenu, location, cmdCount, text->c_str()))
		{
			cmdCount++;
		}
	}

	return cmdCount;
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

bool LiferayNativityContextMenus::_InsertMenu(HMENU hMenu, HMENU subMenuHandle, int location, const wchar_t* text)
{

	MENUITEMINFO menuItem = { sizeof(menuItem) };
		
	menuItem.fMask = MIIM_STRING | MIIM_SUBMENU;

	menuItem.dwTypeData = (LPWSTR)text;

	menuItem.hSubMenu = subMenuHandle;

	if(!InsertMenuItem(hMenu, location, TRUE, &menuItem))
	{
		return false;
	}

	return true;
}

bool LiferayNativityContextMenus::_InsertMenu(HMENU hMenu, int location, int command, const wchar_t* text)
{
	MENUITEMINFO menuItem = { sizeof(menuItem) };
	
	menuItem.fMask = MIIM_STRING | MIIM_ID;

	menuItem.dwTypeData = (LPWSTR)text;

	menuItem.wID = command;
		
	if(!InsertMenuItem(hMenu, location, TRUE, &menuItem))
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