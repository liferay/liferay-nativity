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

#ifndef COMMUNICATIONPROCESSOR_H
#define COMMUNICATIONPROCESSOR_H

#pragma once

#include <string>
#include <vector>
#include <map>

class __declspec(dllexport) CommunicationProcessor
{
public:
	CommunicationProcessor();
	~CommunicationProcessor();
	
	static bool ProcessMessage(std::wstring*, std::map<std::wstring*, std::vector<std::wstring*>*>*);
	static bool ProcessResponse(const std::wstring*, std::vector<std::wstring*>*);

	static bool CreateMessage(const wchar_t*,std::vector<std::wstring>*, std::wstring*);
	static bool FormListAsResponse(std::vector<std::wstring>*, std::wstring*);

private:
	static bool ParseArgs(std::wstring*, std::vector<std::wstring*>*);
	static bool ParseMessages(std::wstring*, std::vector<std::wstring*>*);
	static bool ParseMessage(const std::wstring*, std::wstring*, std::wstring*);

	static bool RemoveQuotes(std::wstring*);

};

#endif
