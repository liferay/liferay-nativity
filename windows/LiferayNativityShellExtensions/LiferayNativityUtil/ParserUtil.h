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

#ifndef PARSERUTIL_H
#define PARSERUTIL_H

#pragma once

#include <string>
#include <vector>
#include <map>

#include "NativityMessage.h"

class __declspec(dllexport) ParserUtil
{
public:
	ParserUtil();
	~ParserUtil();
	
	static bool GetItem(const wchar_t*, const std::wstring*, std::wstring*);

	static bool GetList(size_t, const std::wstring*, std::wstring*);
	
	static size_t GetNextItemInList(const std::wstring*, size_t, std::wstring*);
	
	static bool GetString(size_t, size_t, const std::wstring*, std::wstring*);

	static bool GetValue(size_t, const std::wstring*, std::wstring*);

	static bool IsList(std::wstring*);

	static bool ParseList(std::wstring*, std::vector<std::wstring*>*);

	static bool SerializeList(std::vector<std::wstring>*, std::wstring*, bool);

	static bool SerializeMessage(std::map<std::wstring*, std::wstring*>*, std::wstring*, bool);

	static bool SerializeMessage(NativityMessage*, std::wstring*);

	static bool ParseNativityMessageList(std::wstring*, std::vector<NativityMessage*>*);

};

#endif
