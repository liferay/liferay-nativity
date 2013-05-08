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

#include "NativityMessage.h"
#include "ParserUtil.h"
#include "UtilConstants.h"

#include <string>

#include <fstream>
#include <iostream>

using namespace std;

NativityMessage::NativityMessage(void)
{
	_command = new wstring();
	_value = new wstring();
}

NativityMessage::~NativityMessage(void)
{
}

bool NativityMessage::InitFromMessage(const wstring* message)
{
	if(message->length() == 0)
	{
		return false;
	}
	
	if(!ParserUtil::GetItem(COMMAND, message, _command))
	{
		return false;
	}

	if(!ParserUtil::GetItem(VALUE, message, _value))
	{
		return false;
	}

	return true;
}
	
std::wstring* NativityMessage::GetCommand()
{
	return _command;
}

std::wstring* NativityMessage::GetValue()
{
	return _value;
}

void NativityMessage::SetCommand(std::wstring* command)
{
	_command = command;
}

void NativityMessage::SetValue(std::wstring* value)
{
	_value = value;
}
