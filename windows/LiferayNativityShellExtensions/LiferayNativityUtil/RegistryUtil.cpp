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

#include "RegistryUtil.h"

#include <windows.h>
#include <fstream>
#include <iostream>

using namespace std;

#define SIZE 4096

bool RegistryUtil::ReadRegistry(const wchar_t* key, const wchar_t* name, int* result)
{
	HRESULT hResult;

	HKEY rootKey = NULL;

	hResult = HRESULT_FROM_WIN32(
		RegOpenKeyEx(
			HKEY_CURRENT_USER, (LPCWSTR)key, NULL, KEY_READ, &rootKey));

	if(!SUCCEEDED(hResult))
	{
		return false;
	}

	wchar_t value[SIZE];
	DWORD value_length = SIZE;
	
    hResult = RegQueryValueEx(rootKey, (LPCWSTR)name, NULL, NULL, (LPBYTE)value, &value_length );

	if(!SUCCEEDED(hResult))
	{
		return false;
	}

	*result = value[0];

	RegCloseKey(rootKey);

	return true;
}

bool RegistryUtil::ReadRegistry(const wchar_t* key, const wchar_t* name, wstring* result)
{
	HRESULT hResult;

	HKEY rootKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegOpenKeyEx(HKEY_CURRENT_USER, (LPCWSTR)key, NULL, KEY_READ, &rootKey));

	if(!SUCCEEDED(hResult))
	{
		return false;
	}

	wchar_t value[SIZE];
	DWORD value_length = SIZE;
	
    hResult = RegQueryValueEx(rootKey, (LPCWSTR)name, NULL, NULL, (LPBYTE)value, &value_length );

	if(!SUCCEEDED(hResult))
	{
		return false;
	}

	*result = value;

	RegCloseKey(rootKey);

	return true;
}

bool RegistryUtil::WriteRegistry(const wchar_t* key, const wchar_t* name, int value)
{
	HRESULT hResult;

	HKEY rootKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegCreateKeyEx(HKEY_CURRENT_USER, (LPCWSTR)key, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &rootKey, NULL));

	if (!SUCCEEDED(hResult))
	{
		return false;
	}

	hResult = RegSetValueEx(rootKey, (LPCWSTR)name, 0, REG_DWORD, (LPBYTE)value, 1);

	if (!SUCCEEDED(hResult))
	{
		return false;
	}

	HRESULT hResult2 = RegCloseKey(rootKey);

	if (!SUCCEEDED(hResult) || !SUCCEEDED(hResult2))
	{
		return false;
	}

	return true;
}

bool RegistryUtil::WriteRegistry(const wchar_t* key, const wchar_t* name, const wchar_t* value)
{
	HRESULT hResult;

	HKEY rootKey = NULL;

	hResult = HRESULT_FROM_WIN32(RegCreateKeyEx(HKEY_CURRENT_USER, (LPCWSTR)key, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &rootKey, NULL));

	if (!SUCCEEDED(hResult))
	{
		return false;
	}

	hResult = RegSetValueEx(rootKey, (LPCWSTR)name, 0, REG_SZ, (LPBYTE)value, (DWORD)wcslen(value) * sizeof(TCHAR));

	if (!SUCCEEDED(hResult))
	{
		return false;
	}

	HRESULT hResult2 = RegCloseKey(rootKey);

	if (!SUCCEEDED(hResult) || !SUCCEEDED(hResult2))
	{
		return false;
	}

	return true;
}

