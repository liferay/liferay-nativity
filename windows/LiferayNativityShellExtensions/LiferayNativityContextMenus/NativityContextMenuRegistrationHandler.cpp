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

#include "NativityContextMenuRegistrationHandler.h"
#include "ContextMenuContants.h"

HRESULT NativityContextMenuRegistrationHandler::MakeRegistryEntries(const CLSID& clsid)
{
	HRESULT hResult;

	HKEY allKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegCreateKeyEx(HKEY_CLASSES_ROOT, REGISTRY_ALL_CONTEXT_MENU, 0, NULL,REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &allKey, NULL));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

	HKEY folderKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegCreateKeyEx(HKEY_CLASSES_ROOT, REGISTRY_FOLDER_CONTEXT_MENU, 0, NULL,REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &folderKey, NULL));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

	wchar_t stringCLSID[MAX_PATH];

	StringFromGUID2(clsid, stringCLSID, ARRAYSIZE(stringCLSID));

	LPCTSTR value = stringCLSID;

	hResult = RegSetValueEx(allKey, NULL, 0, REG_SZ, (LPBYTE)value, (DWORD)(wcslen(value)+1) * sizeof(TCHAR));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

	hResult = RegSetValueEx(folderKey, NULL, 0, REG_SZ, (LPBYTE)value, (DWORD)(wcslen(value)+1) * sizeof(TCHAR));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

	return S_OK;
}

HRESULT NativityContextMenuRegistrationHandler::RemoveRegistryEntries()
{
	HRESULT hResult;

	hResult = HRESULT_FROM_WIN32(RegDeleteKey(HKEY_CLASSES_ROOT, REGISTRY_ALL_CONTEXT_MENU));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

	hResult = HRESULT_FROM_WIN32(RegDeleteKey(HKEY_CLASSES_ROOT, REGISTRY_FOLDER_CONTEXT_MENU));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

	hResult = HRESULT_FROM_WIN32(RegFlushKey(HKEY_CLASSES_ROOT));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

	return S_OK;
}

HRESULT NativityContextMenuRegistrationHandler::RegisterCOMObject(PCWSTR modulePath, const CLSID& clsid)
{
    if (modulePath == NULL)
    {
        return E_FAIL;
    }

    wchar_t stringCLSID[MAX_PATH];

    StringFromGUID2(clsid, stringCLSID, ARRAYSIZE(stringCLSID));

	HRESULT hResult;

	HKEY hKey = NULL;

	hResult = HRESULT_FROM_WIN32(
		RegOpenKeyEx(HKEY_CLASSES_ROOT, REGISTRY_CLSID, 0, KEY_WRITE, &hKey));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

	HKEY clsidKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegCreateKeyEx(hKey, stringCLSID, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &clsidKey, NULL));

	if(!SUCCEEDED(hResult))
	{
		return hResult;
	}

	HKEY inprocessKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegCreateKeyEx(clsidKey, REGISTRY_IN_PROCESS, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &inprocessKey, NULL));

	if(!SUCCEEDED(hResult))
	{
		return hResult;
	}

	DWORD cbData = lstrlen(modulePath) * sizeof(*modulePath);

	hResult = HRESULT_FROM_WIN32(RegSetValue(inprocessKey, NULL, REG_SZ, modulePath, cbData));

	if(!SUCCEEDED(hResult))
	{
		return hResult;
	}

    hResult = HRESULT_FROM_WIN32(RegSetValueEx(inprocessKey, REGISTRY_THREADING, 0, REG_SZ, (LPBYTE)REGISTRY_APARTMENT, (DWORD)(wcslen(REGISTRY_APARTMENT)+1) * sizeof(TCHAR)));

	if(!SUCCEEDED(hResult))
	{
		return hResult;
	}

	hResult = HRESULT_FROM_WIN32(RegSetValueEx(inprocessKey, REGISTRY_VERSION, 0, REG_SZ, (LPBYTE)REGISTRY_VERSION_NUMBER, (DWORD)(wcslen(REGISTRY_VERSION_NUMBER)+1) * sizeof(TCHAR)));

	if(!SUCCEEDED(hResult))
	{
		return hResult;
	}

	return S_OK;
}

HRESULT NativityContextMenuRegistrationHandler::UnregisterCOMObject(const CLSID& clsid)
{
   HRESULT hr = S_OK;

   wchar_t stringCLSID[MAX_PATH];

   StringFromGUID2(clsid, stringCLSID, ARRAYSIZE(stringCLSID));

	HRESULT hResult;

	HKEY clsidKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegOpenKeyEx(HKEY_CLASSES_ROOT, REGISTRY_CLSID, 0, DELETE, &clsidKey));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

	HKEY hKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegOpenKeyEx(clsidKey, stringCLSID, 0, DELETE, &hKey));

	hResult = HRESULT_FROM_WIN32(RegDeleteKey(hKey, REGISTRY_IN_PROCESS));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

	hResult = HRESULT_FROM_WIN32(RegDeleteKey(clsidKey, stringCLSID));

	if (!SUCCEEDED(hResult))
	{
		return hResult;
	}

    return S_OK;
}