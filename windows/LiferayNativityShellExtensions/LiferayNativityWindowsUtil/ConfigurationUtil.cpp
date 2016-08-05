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
#include <Shlwapi.h>

using namespace std;

JNIEXPORT jboolean JNICALL Java_com_liferay_nativity_control_win_WindowsNativityUtil_addFavoritesPath(JNIEnv* env, jclass jclazz, jstring filePath)
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

	ConfigurationUtil::AddFavoritesPath(wideString);

	env->ReleaseStringChars(filePath, rawString);

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_liferay_nativity_control_win_WindowsNativityUtil_refreshExplorer(JNIEnv* env, jclass jclazz, jstring filePath)
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

	ConfigurationUtil::RefreshExplorer(wideString);

	env->ReleaseStringChars(filePath, rawString);

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_liferay_nativity_control_win_WindowsNativityUtil_removeFavoritesPath(JNIEnv* env, jclass jclazz, jstring filePath)
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

	ConfigurationUtil::RemoveFavoritesPath(wideString);

	env->ReleaseStringChars(filePath, rawString);

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_liferay_nativity_control_win_WindowsNativityUtil_setSystemFolder(JNIEnv* env, jclass jclazz, jstring filePath)
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

JNIEXPORT jboolean JNICALL Java_com_liferay_nativity_control_win_WindowsNativityUtil_updateExplorer(JNIEnv* env, jclass jclazz, jstring filePath)
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

bool ConfigurationUtil::RefreshExplorer(const wchar_t* path)
{
	CoInitialize(NULL);

	IShellWindows* iShellWindows = NULL;

	HRESULT hResult = CoCreateInstance(CLSID_ShellWindows, NULL, CLSCTX_INPROC_SERVER | CLSCTX_LOCAL_SERVER, IID_IShellWindows, (void**)&iShellWindows);

	if (FAILED(hResult))
	{
		iShellWindows->Release();

		CoUninitialize();

		return false;
	}

	long count;

	if (FAILED(hResult = iShellWindows->get_Count(&count)))
	{
		iShellWindows->Release();

		CoUninitialize();

		return false;
	}

	for (int i = count - 1; i >= 0; i--)
	{
		CComPtr<IDispatch> iDispatch;
		CComVariant index(i);

		if (FAILED(hResult = iShellWindows->Item(index, &iDispatch)))
		{
			continue;
		}

		CComPtr<IWebBrowser2> iWebBrowser2;

		if (FAILED(iDispatch->QueryInterface(IID_IWebBrowser2, (void**)&iWebBrowser2)))
		{
			continue;
		}

		CComPtr<IServiceProvider> iServiceProvider;

		if (FAILED(iDispatch->QueryInterface(IID_IServiceProvider, (void**)&iServiceProvider)))
		{
			continue;
		}

		CComPtr<IShellBrowser> iShellBrowser;

		if (FAILED(hResult = iServiceProvider->QueryService(SID_STopLevelBrowser, IID_IShellBrowser, (void**)&iShellBrowser)))
		{
			continue;
		}

		CComPtr<IShellView> iShellView;

		if (FAILED(hResult = iShellBrowser->QueryActiveShellView(&iShellView)))
		{
			continue;
		}

		CComPtr<IFolderView> iFolderView;

		if (FAILED(hResult = iShellView->QueryInterface(IID_IFolderView, (void**)&iFolderView)))
		{
			continue;
		}

		CComPtr<IPersistFolder2> iPersistFolder2;

		if (FAILED(hResult = iFolderView->GetFolder(IID_IPersistFolder2, (void**)&iPersistFolder2)))
		{
			continue;
		}

		LPITEMIDLIST currentFolder = NULL;

		if (FAILED(hResult = iPersistFolder2->GetCurFolder(&currentFolder)))
		{
			continue;
		}

		LPCITEMIDLIST child = NULL;
		CComPtr<IShellFolder> iShellFolder;

		if (FAILED(::SHBindToParent(currentFolder, IID_IShellFolder, (void**)&iShellFolder, &child)))
		{
			continue;
		}

		ULONG attrs = SFGAO_FILESYSTEM;

		if (SUCCEEDED(hResult = iShellFolder->GetAttributesOf(1, &child, &attrs)))
		{
			if (attrs & SFGAO_FILESYSTEM)
			{
				STRRET strret;

				if (SUCCEEDED(hResult = iShellFolder->GetDisplayNameOf(child, SHGDN_FORPARSING, &strret)))
				{
					LPWSTR fileName = NULL;

					if (SUCCEEDED(hResult = StrRetToStr(&strret, child, &fileName)))
					{
						int pathLength = lstrlen(path);

						if ((lstrlen(fileName) >= pathLength) && (::CompareString(LOCALE_USER_DEFAULT, NORM_IGNORECASE, path, pathLength, fileName, pathLength) == CSTR_EQUAL))
						{
							iWebBrowser2->Refresh();
						}
					}

					if (fileName)
					{
						CoTaskMemFree(fileName);

						fileName = NULL;
					}

					if (strret.pOleStr)
					{
						CoTaskMemFree(strret.pOleStr);
					}
				}
			}
		}

		if (currentFolder)
		{
			CoTaskMemFree(currentFolder);

			currentFolder = NULL;
		}
	}

	iShellWindows->Release();

	CoUninitialize();

	if (SUCCEEDED(hResult)) {
		return true;
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

		if (DeleteFile(fullPath))
		{
			return true;
		}
	}
		
	return false;
}

bool ConfigurationUtil::UpdateExplorer(const wchar_t* path)
{
	SHChangeNotify(SHCNE_UPDATEITEM, SHCNF_PATH | SHCNF_FLUSH, path, 0);

	return true;
}

bool ConfigurationUtil::SetSystemFolder(const wchar_t* path)
{
	SetFileAttributes(path, FILE_ATTRIBUTE_SYSTEM);

	return true;
}

HRESULT ConfigurationUtil::_CreateLink(LPCWSTR lpszPathObj, LPCWSTR lpszPathLink, LPCWSTR lpszDesc)
{
	CoInitialize(NULL);

	IShellLink* psl;

	// Get a pointer to the IShellLink interface. It is assumed that CoInitialize
	// has already been called.
	HRESULT hResult = CoCreateInstance(CLSID_ShellLink, NULL, CLSCTX_INPROC_SERVER, IID_IShellLink, (LPVOID*)&psl);

	if (SUCCEEDED(hResult))
	{
		IPersistFile* ppf;

		// Set the path to the shortcut target and add the description. 
		psl->SetPath(lpszPathObj);
		psl->SetDescription(lpszDesc);

		// Query IShellLink for the IPersistFile interface, used for saving the 
		// shortcut in persistent storage. 
		hResult = psl->QueryInterface(IID_IPersistFile, (LPVOID*)&ppf);

		if (SUCCEEDED(hResult))
		{
			// Save the link by calling IPersistFile::Save. 
			hResult = ppf->Save(lpszPathLink, TRUE);

			ppf->Release();
		}

		psl->Release();
	}

	CoUninitialize();

	return hResult;
}