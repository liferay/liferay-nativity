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

using namespace std;

#define SIZE 4096

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

	UpdateExplorer(wideString);

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

	SetSystemFolder(wideString);

	env->ReleaseStringChars(filePath, rawString);

	return JNI_TRUE;
}

bool UpdateExplorer(const wchar_t* syncRoot)
{
	SHChangeNotify(SHCNE_UPDATEITEM, SHCNF_PATH | SHCNF_FLUSH, syncRoot, 0);

	return true;
}

bool SetSystemFolder(const wchar_t* syncRoot)
{
	SetFileAttributes(syncRoot, FILE_ATTRIBUTE_SYSTEM);

	return true;
}
