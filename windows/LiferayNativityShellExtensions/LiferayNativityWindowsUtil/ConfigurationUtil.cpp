/**
*  Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
*  
*  This library is free software; you can redistribute it and/or modify it under
*  the terms of the GNU Lesser General Public License as published by the Free
*  Software Foundation; either version 2.1 of the License, or (at your option)
*  any later version.
*  
*  This library is distributed in the hope that it will be useful, but WITHOUT
*  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
*  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
*  details.
*/

#include "ConfigurationUtil.h"
#include "ConfigConstants.h"
#include "com_liferay_nativity_util_windows_WindowsUtil.h"

#include <Shlobj.h>
#include <Windows.h>
#include <string>

#include <iostream>

using namespace std;

#define SIZE 4096

JNIEXPORT jboolean JNICALL Java_com_liferay_sync_util_windows_WindowsUtil_setRootFolder
	(JNIEnv *env, jclass jclazz, jstring filePath)
{
	if(env == NULL)
	{ 
		return JNI_FALSE;
	}

	if(filePath == NULL)
	{
		return JNI_FALSE;
	}

	int len = env->GetStringLength(filePath);

    const jchar* rawString = env->GetStringChars(filePath, NULL);

    if (rawString == NULL)
	{
        return NULL;
	}

    wchar_t* wideString = new wchar_t[len+1];

    memcpy(wideString, rawString, len*2);

    wideString[len] = 0;

	bool value = ConfigurationUtil::SetSyncRootFolder(wideString);

	env->ReleaseStringChars(filePath, rawString);

	if(value){
		return JNI_TRUE;
	}
	else{
		return JNI_FALSE;
	}
}

JNIEXPORT jboolean JNICALL Java_com_liferay_sync_util_windows_WindowsUtil_updateExplorer
	(JNIEnv *env, jclass jclazz, jstring filePath, jint type)
{
	if(env == NULL)
	{ 
		return JNI_FALSE;
	}

	if(filePath == NULL)
	{
		return JNI_FALSE;
	}

	int len = env->GetStringLength(filePath);

    const jchar* rawString = env->GetStringChars(filePath, NULL);

    if (rawString == NULL)
	{
        return NULL;
	}

    wchar_t* wideString = new wchar_t[len+1];

    memcpy(wideString, rawString, len*2);

    wideString[len] = 0;

	ConfigurationUtil::UpdateExplorer(wideString, (int)type);

	env->ReleaseStringChars(filePath, rawString);

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_liferay_sync_util_windows_WindowsUtil_updateRenameExplorer
	(JNIEnv *env, jclass jclazz, jstring oldPath, jstring filePath, jint type)
{
	if(env == NULL)
	{ 
		return JNI_FALSE;
	}

	if(filePath == NULL)
	{
		return JNI_FALSE;
	}

	int len = env->GetStringLength(filePath);

	int len2 = env->GetStringLength(oldPath);

	const jchar* rawString2 = env->GetStringChars(oldPath, NULL);

    if (rawString2 == NULL)
	{
        return NULL;
	}

    wchar_t* wideString2 = new wchar_t[len2+1];

    memcpy(wideString2, rawString2, len*2);

    wideString2[len2] = 0;

    const jchar* rawString = env->GetStringChars(filePath, NULL);

    if (rawString == NULL)
	{
        return NULL;
	}

    wchar_t* wideString = new wchar_t[len+1];

    memcpy(wideString, rawString, len*2);

    wideString[len] = 0;

	ConfigurationUtil::UpdateExplorer(wideString2, wideString, (int)type);

	env->ReleaseStringChars(filePath, rawString);

	return JNI_TRUE;
}

bool ConfigurationUtil::UpdateExplorer(const wchar_t* syncRoot, int value)
{
	long wEventId = ConfigurationUtil::GetEventId(value);

	SHChangeNotify(wEventId, SHCNF_PATH | SHCNF_FLUSH, syncRoot, 0);

	return true;
}

bool ConfigurationUtil::UpdateExplorer(const wchar_t* oldPath, const wchar_t* syncRoot, int value)
{
	long wEventId = ConfigurationUtil::GetEventId(value);

	SHChangeNotify(wEventId, SHCNF_PATH | SHCNF_FLUSH, syncRoot, oldPath);

	return true;

}

long ConfigurationUtil::GetEventId(int value)
{
	switch(value)
	{
	case 0:
		return SHCNE_CREATE;
	case 1:
		return SHCNE_DELETE;
	case 2:
		return SHCNE_MKDIR;
	case 3:
		return SHCNE_RENAMEFOLDER;
	case 4:
		return SHCNE_RENAMEITEM;
	case 5:
		return SHCNE_RMDIR;
	case 6:
		return SHCNE_UPDATEDIR;
	default:
		return SHCNE_UPDATEITEM;
	}
}

bool ConfigurationUtil::SetSyncRootFolder(const wchar_t* syncRoot)
{
	HRESULT hResult;

	HKEY liferayKey = NULL;

	hResult = HRESULT_FROM_WIN32(
		RegCreateKeyEx(
		HKEY_CURRENT_USER, REGISTRY_LIFERAY_KEY, 0, NULL, 
		REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &liferayKey, NULL));

	if (!SUCCEEDED(hResult))
	{
		return false;
	}

	hResult = RegSetValueEx(
		liferayKey, REGISTRY_SYNC_ROOT_KEY, 0, REG_SZ, (LPBYTE)syncRoot, 
		(DWORD)wcslen(syncRoot) * sizeof(TCHAR));

	if (!SUCCEEDED(hResult))
	{
		return false;
	}

	hResult = SetFileAttributes(syncRoot, FILE_ATTRIBUTE_SYSTEM);
	HRESULT hResult2 = RegCloseKey(liferayKey);

	if (!SUCCEEDED(hResult) || !SUCCEEDED(hResult2))
	{
		return false;
	}

	return true;
}

wstring ConfigurationUtil::GetSyncRootFolder()
{
	HRESULT hResult;

	HKEY liferayKey = NULL;

	hResult = HRESULT_FROM_WIN32(
		RegOpenKeyEx(
			HKEY_CURRENT_USER, REGISTRY_LIFERAY_KEY, NULL, KEY_READ, &liferayKey));

	if(!SUCCEEDED(hResult))
	{
		return L"";
	}

	wchar_t value[SIZE];
	DWORD value_length = SIZE;
	
    hResult = RegQueryValueEx(
		liferayKey, REGISTRY_SYNC_ROOT_KEY, NULL, NULL, (LPBYTE)value,
        &value_length );

	if(!SUCCEEDED(hResult))
	{
		return L"";
	}

	RegCloseKey(liferayKey);

	return value;
}


int ConfigurationUtil::GetRPCServerPort()
{
	HRESULT hResult;

	HKEY liferayKey = NULL;

	hResult = HRESULT_FROM_WIN32(
		RegOpenKeyEx(
			HKEY_CURRENT_USER, REGISTRY_LIFERAY_KEY, NULL, KEY_READ, &liferayKey));

	if(!SUCCEEDED(hResult))
	{
		return -1;
	}

	wchar_t value[SIZE];
	DWORD value_length = SIZE;
	
    hResult = RegQueryValueEx(
		liferayKey, REGISTRY_RPC_PORT_KEY, NULL, NULL, (LPBYTE)value,
        &value_length );

	if(!SUCCEEDED(hResult))
	{
		return -1;
	}

	RegCloseKey(liferayKey);

	int port = _wtoi(value);

	return port;
}


