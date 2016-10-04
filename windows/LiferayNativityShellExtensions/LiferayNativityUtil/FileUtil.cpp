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

#include "FileUtil.h"

using namespace std;

bool FileUtil::IsChildFile(const wchar_t* rootFolder, const wchar_t* file)
{
	wstring* f = new wstring(file);

	size_t found = f->find(rootFolder);

	if (found != string::npos)
	{
		return true;
	}

	return false;
}

bool FileUtil::IsFileFiltered(const wchar_t* file)
{
	wstring* rootFolder = new wstring();

	if (!RegistryUtil::ReadRegistry(REGISTRY_ROOT_KEY, REGISTRY_FILTER_FOLDERS, rootFolder))
	{
		// Deprecated as of 1.0.2. Check REGISTRY_FILTER_FOLDER value for backward compatibility with 1.0.1.

		if (RegistryUtil::ReadRegistry(REGISTRY_ROOT_KEY, REGISTRY_FILTER_FOLDER, rootFolder) && IsChildFile(rootFolder->c_str(), file))
		{
			delete rootFolder;

			return true;
		}

		delete rootFolder;

		return true;
	}

	Json::Reader jsonReader;
	Json::Value jsonFilterFolders;

	if (!jsonReader.parse(StringUtil::toString(*rootFolder), jsonFilterFolders))
	{
		delete rootFolder;

		return true;
	}

	if (jsonFilterFolders.size() == 0) {
		delete rootFolder;

		return true;
	}

	for (unsigned int i = 0; i < jsonFilterFolders.size(); i++)
	{
		wstring* filterFolder = new wstring();

		filterFolder->append(StringUtil::toWstring(jsonFilterFolders[i].asString()));

		if (IsChildFile(filterFolder->c_str(), file))
		{
			delete filterFolder;
			delete rootFolder;

			return true;
		}

		delete filterFolder;
	}

	delete rootFolder;

	return false;
}