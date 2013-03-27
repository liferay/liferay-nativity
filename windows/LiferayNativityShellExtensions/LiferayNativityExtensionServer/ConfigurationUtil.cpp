/**
*  Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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
#include <Shlobj.h>

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
