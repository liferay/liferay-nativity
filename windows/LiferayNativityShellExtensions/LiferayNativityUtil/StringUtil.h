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

#ifndef STRINGUTIL_H
#define STRINGUTIL_H

#pragma once

#include <string>
#include <vector>
#include <map>

#include "UtilConstants.h"

#include <codecvt>

class __declspec(dllexport) StringUtil
{
	public:
		StringUtil();
		~StringUtil();

		static std::string toString(const std::wstring&);

		static std::wstring toWstring(const std::string&);
};

#endif
