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

#ifndef CONFIGURATIONUTIL_H
#define CONFIGURATIONUTIL_H

#pragma once

#include "ConfigConstants.h"
#include "com_liferay_nativity_control_win_WindowsNativityUtil.h"

#include <Shlobj.h>
#include <Windows.h>
#include <iostream>
#include <string>

extern "C" __declspec(dllexport) bool SetSystemFolder(const wchar_t* syncRoot);
extern "C" __declspec(dllexport) bool UpdateExplorer(const wchar_t* syncRoot);

#endif
