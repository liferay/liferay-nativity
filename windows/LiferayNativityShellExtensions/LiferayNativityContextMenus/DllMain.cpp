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

#include "ContextMenuFactory.h"
#include "NativityContextMenuRegistrationHandler.h"
#include "ContextMenuConstants.h"
#include <Guiddef.h>
#include <windows.h>

HINSTANCE instanceHandle = NULL;

long dllReferenceCount = 0;

BOOL APIENTRY DllMain(HMODULE hModule, DWORD dwReason, LPVOID lpReserved)
{
	switch (dwReason)
	{
		case DLL_PROCESS_ATTACH:
			instanceHandle = hModule;
			DisableThreadLibraryCalls(hModule);
			break;
		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
		case DLL_PROCESS_DETACH:
			break;
	}

	return TRUE;
}

STDAPI DllGetClassObject(REFCLSID rclsid, REFIID riid, void** ppv)
{
	HRESULT hResult = CLASS_E_CLASSNOTAVAILABLE;

	GUID guid;

	hResult = CLSIDFromString(CONTEXT_MENU_GUID, (LPCLSID)&guid);

	if (FAILED(hResult))
	{
		return hResult;
	}

	if (!IsEqualCLSID(guid, rclsid))
	{
		return hResult;
	}

	hResult = E_OUTOFMEMORY;

	wchar_t szModule[MAX_PATH];

	if (GetModuleFileName(instanceHandle, szModule, ARRAYSIZE(szModule)) == 0)
	{
		hResult = HRESULT_FROM_WIN32(GetLastError());

		return hResult;
	}

	ContextMenuFactory* contextMenuFactory = new ContextMenuFactory(szModule);

	if (contextMenuFactory)
	{
		hResult = contextMenuFactory->QueryInterface(riid, ppv);

		contextMenuFactory->Release();
	}

	return hResult;
}

STDAPI DllCanUnloadNow(void)
{
	return dllReferenceCount > 0 ? S_FALSE : S_OK;
}

HRESULT _stdcall DllRegisterServer(void)
{
	HRESULT hResult = S_OK;

	wchar_t szModule[MAX_PATH];

	if (GetModuleFileName(instanceHandle, szModule, ARRAYSIZE(szModule)) == 0)
	{
		hResult = HRESULT_FROM_WIN32(GetLastError());

		return hResult;
	}

	GUID guid;

	hResult = CLSIDFromString(CONTEXT_MENU_GUID, (LPCLSID)&guid);

	if (FAILED(hResult))
	{
		return hResult;
	}

	hResult = NativityContextMenuRegistrationHandler::RegisterCOMObject(szModule, guid);

	if (FAILED(hResult))
	{
		return hResult;
	}

	hResult = NativityContextMenuRegistrationHandler::MakeRegistryEntries(guid);

	if (FAILED(hResult))
	{
		return hResult;
	}

	return S_OK;
}

STDAPI DllUnregisterServer(void)
{
	HRESULT hResult = S_OK;

	wchar_t szModule[MAX_PATH];

	if (GetModuleFileName(instanceHandle, szModule, ARRAYSIZE(szModule)) == 0)
	{
		hResult = HRESULT_FROM_WIN32(GetLastError());

		return hResult;
	}

	GUID guid;

	hResult = CLSIDFromString(CONTEXT_MENU_GUID, (LPCLSID)&guid);

	if (FAILED(hResult))
	{
		return hResult;
	}

	hResult = NativityContextMenuRegistrationHandler::UnregisterCOMObject(guid);

	if (FAILED(hResult))
	{
		return hResult;
	}

	hResult = NativityContextMenuRegistrationHandler::RemoveRegistryEntries();

	if (FAILED(hResult))
	{
		return hResult;
	}

	return S_OK;
}