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
#include <atlbase.h>

using namespace std;

JNIEXPORT jboolean JNICALL Java_com_liferay_nativity_control_win_WindowsNativityUtil_updateExplorer
(JNIEnv* env, jclass jclazz, jstring filePath)
{
	if (env == NULL)
	{
		return JNI_FALSE;
	}

	if (filePath == NULL)
	{
		return JNI_FALSE;
	}

	int len = env->GetStringLength(filePath);

	const jchar* rawString = env->GetStringChars(filePath, NULL);

	if (rawString == NULL)
	{
		return NULL;
	}

	wchar_t* wideString = new wchar_t[len + 1];

	memcpy(wideString, rawString, len * 2);

	wideString[len] = 0;

	ConfigurationUtil::UpdateExplorer(wideString);

	env->ReleaseStringChars(filePath, rawString);

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_liferay_nativity_control_win_WindowsNativityUtil_setSystemFolder
(JNIEnv* env, jclass jclazz, jstring filePath)
{
	if (env == NULL)
	{
		return JNI_FALSE;
	}

	if (filePath == NULL)
	{
		return JNI_FALSE;
	}

	int len = env->GetStringLength(filePath);

	const jchar* rawString = env->GetStringChars(filePath, NULL);

	if (rawString == NULL)
	{
		return NULL;
	}

	wchar_t* wideString = new wchar_t[len + 1];

	memcpy(wideString, rawString, len * 2);

	wideString[len] = 0;

	ConfigurationUtil::SetSystemFolder(wideString);

	env->ReleaseStringChars(filePath, rawString);

	return JNI_TRUE;
}

bool ConfigurationUtil::AddFavoritesPath(const wchar_t* path)
{
	wchar_t* linksPath;

	wchar_t fullPath[MAX_PATH] = {0};

	if (SUCCEEDED(SHGetKnownFolderPath(FOLDERID_Links, 0, NULL, &linksPath))) {
		PathAppend(fullPath, linksPath);
		PathAppend(fullPath, PathFindFileName(path));

		wcscat_s(fullPath, L".lnk");

		if (SUCCEEDED(_CreateLink(path, fullPath, L"")))
		{
			return true;
		}
	}
	
	return false;
}

bool ConfigurationUtil::RemoveFavoritesPath(const wchar_t* path)
{
	wchar_t* linksPath;

	wchar_t fullPath[MAX_PATH] = { 0 };

	if (SUCCEEDED(SHGetKnownFolderPath(FOLDERID_Links, 0, NULL, &linksPath))) {
		PathAppend(fullPath, linksPath);
		PathAppend(fullPath, PathFindFileName(path));

		wcscat_s(fullPath, L".lnk");

		if (SUCCEEDED(DeleteFile(fullPath)))
		{
			return true;
		}
	}
		
	return false;
}

bool ConfigurationUtil::UpdateExplorer(const wchar_t* syncRoot)
{
	SHChangeNotify(SHCNE_UPDATEITEM, SHCNF_PATH | SHCNF_FLUSH, syncRoot, 0);

	return true;
}

bool ConfigurationUtil::SetSystemFolder(const wchar_t* syncRoot)
{
	SetFileAttributes(syncRoot, FILE_ATTRIBUTE_SYSTEM);

	return true;
}
