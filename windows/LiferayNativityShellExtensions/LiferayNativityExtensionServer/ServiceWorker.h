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

#ifndef SERVICEWORKER_H
#define SERVICEWORKER


#pragma once

#include "stdafx.h"

class ServiceWorker
{
public:

    ServiceWorker();

	~ServiceWorker();

	bool ProcessMessages(std::map<std::wstring*, std::vector<std::wstring*>*>*);
	
private:
	
	bool _ClearFileIcons(std::vector<std::wstring*>*);
	bool _EnableFileIcons(std::vector<std::wstring*>*);
	bool _MarkSystem(std::vector<std::wstring*>*);
	bool _SetMenuTitle(std::vector<std::wstring*>*);
	bool _SetRootFolder(std::vector<std::wstring*>*);
	bool _UpdateOverlay(std::vector<std::wstring*>*);
};

#endif