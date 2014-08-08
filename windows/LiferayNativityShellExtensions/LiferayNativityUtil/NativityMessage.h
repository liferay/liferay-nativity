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

#ifndef NATIVITYMESSAGE_H
#define NATIVITYMESSAGE_H

#include "StringUtil.h"
#include "UtilConstants.h"

#include <fstream>
#include <iostream>
#include <string>

#pragma once

class __declspec(dllexport) NativityMessage
{
	public:
		NativityMessage(void);
		~NativityMessage(void);

		std::wstring* GetCommand();

		std::wstring* GetValue();

		void SetCommand(std::wstring*);

		void SetValue(std::wstring*);

	private:
		std::wstring* _command;
		std::wstring* _value;
};

#endif