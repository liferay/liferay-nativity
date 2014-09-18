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

#ifndef COMMUNICATIONSOCKET_H
#define COMMUNICATIONSOCKET_H

#pragma once
#pragma warning (disable : 4251)

#include "UtilConstants.h"

#include <fstream>
#include <iostream>
#include <string>
#include <vector>

class __declspec(dllexport) CommunicationSocket
{
	public:
		CommunicationSocket(int port);
		~CommunicationSocket();

		bool ReceiveResponseOnly(std::wstring*);

		bool SendMessageReceiveResponse(const wchar_t*, std::wstring*);

	private:
		bool _ConvertData(wchar_t* buf, int bytesRead, char* rec_buf);

		int _port;
};

#endif