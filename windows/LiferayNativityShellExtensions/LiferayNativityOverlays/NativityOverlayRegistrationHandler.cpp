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

#include "NativityOverlayRegistrationHandler.h"

using namespace std;

HRESULT NativityOverlayRegistrationHandler::MakeRegistryEntries(const CLSID& clsid, PWSTR friendlyName)
{
	HRESULT hResult;

	HKEY shellOverlayKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegOpenKeyEx(HKEY_LOCAL_MACHINE, REGISTRY_OVERLAY_KEY, 0, KEY_WRITE, &shellOverlayKey));

	if (FAILED(hResult))
	{
		hResult = RegCreateKey(HKEY_LOCAL_MACHINE, REGISTRY_OVERLAY_KEY, &shellOverlayKey);

		if (FAILED(hResult))
		{
			return hResult;
		}
	}

	HKEY syncExOverlayKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegCreateKeyEx(shellOverlayKey, friendlyName, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &syncExOverlayKey, NULL));

	if (FAILED(hResult))
	{
		return hResult;
	}

	wchar_t stringCLSID[MAX_PATH];

	StringFromGUID2(clsid, stringCLSID, ARRAYSIZE(stringCLSID));

	LPCTSTR value = stringCLSID;

	hResult = RegSetValueEx(syncExOverlayKey, NULL, 0, REG_SZ, (LPBYTE)value, (DWORD)((wcslen(value) + 1) * sizeof(TCHAR)));

	if (FAILED(hResult))
	{
		return hResult;
	}

	return hResult;
}

HRESULT NativityOverlayRegistrationHandler::RemoveRegistryEntries(PWSTR friendlyName)
{
	HRESULT hResult;

	HKEY shellOverlayKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegOpenKeyEx(HKEY_LOCAL_MACHINE, REGISTRY_OVERLAY_KEY, 0, KEY_WRITE, &shellOverlayKey));

	if (FAILED(hResult))
	{
		return hResult;
	}

	HKEY syncExOverlayKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegDeleteKeyEx(shellOverlayKey, friendlyName, DELETE, 0));

	if (FAILED(hResult))
	{
		return hResult;
	}

	return hResult;
}

HRESULT NativityOverlayRegistrationHandler::RegisterCOMObject(PCWSTR modulePath, const CLSID& clsid)
{
	if (modulePath == NULL)
	{
		return E_FAIL;
	}

	wchar_t stringCLSID[MAX_PATH];

	StringFromGUID2(clsid, stringCLSID, ARRAYSIZE(stringCLSID));

	HRESULT hResult;

	HKEY hKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegOpenKeyEx(HKEY_CLASSES_ROOT, REGISTRY_CLSID, 0, KEY_WRITE, &hKey));

	if (FAILED(hResult))
	{
		return hResult;
	}

	HKEY clsidKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegCreateKeyEx(hKey, stringCLSID, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &clsidKey, NULL));

	if (FAILED(hResult))
	{
		return hResult;
	}

	HKEY inprocessKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegCreateKeyEx(clsidKey, REGISTRY_IN_PROCESS, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &inprocessKey, NULL));

	if (FAILED(hResult))
	{
		return hResult;
	}

	DWORD cbData = lstrlen(modulePath) * sizeof(*modulePath);

	hResult = HRESULT_FROM_WIN32(RegSetValue(inprocessKey, NULL, REG_SZ, modulePath, cbData));

	if (FAILED(hResult))
	{
		return hResult;
	}

	hResult = HRESULT_FROM_WIN32(RegSetValueEx(inprocessKey, REGISTRY_THREADING, 0, REG_SZ, (LPBYTE)REGISTRY_APARTMENT, (DWORD)((wcslen(REGISTRY_APARTMENT) + 1) * sizeof(TCHAR))));

	if (FAILED(hResult))
	{
		return hResult;
	}

	hResult = HRESULT_FROM_WIN32(RegSetValueEx(inprocessKey, REGISTRY_VERSION, 0, REG_SZ, (LPBYTE)REGISTRY_VERSION_NUMBER, (DWORD)(wcslen(REGISTRY_VERSION_NUMBER) + 1) * sizeof(TCHAR)));

	if (FAILED(hResult))
	{
		return hResult;
	}

	return S_OK;
}

HRESULT NativityOverlayRegistrationHandler::UnregisterCOMObject(const CLSID& clsid)
{
	wchar_t stringCLSID[MAX_PATH];

	StringFromGUID2(clsid, stringCLSID, ARRAYSIZE(stringCLSID));

	HRESULT hResult;

	HKEY hKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegOpenKeyEx(HKEY_CLASSES_ROOT, REGISTRY_CLSID, 0, DELETE, &hKey));

	if (FAILED(hResult))
	{
		return hResult;
	}

	HKEY clsidKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegOpenKeyEx(hKey, stringCLSID, 0, DELETE, &clsidKey));

	if (FAILED(hResult))
	{
		return hResult;
	}

	hResult = HRESULT_FROM_WIN32(RegDeleteKeyEx(clsidKey, REGISTRY_IN_PROCESS, DELETE, 0));

	if (FAILED(hResult))
	{
		return hResult;
	}

	hResult = HRESULT_FROM_WIN32(RegDeleteKeyEx(hKey, stringCLSID, DELETE, 0));

	if (FAILED(hResult))
	{
		return hResult;
	}

	return S_OK;
}