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

#include "CommunicationProcessor.h"
#include "UtilConstants.h"

#include <iostream>
#include <fstream>

using namespace std;

CommunicationProcessor::CommunicationProcessor()
{
}

CommunicationProcessor::~CommunicationProcessor()
{
}

bool CommunicationProcessor::ProcessMessage(wstring* message, map<wstring*,vector<wstring*>*>* allMessages)
{
	//{"command":"blah","value":"["one","two"]"}
	vector<wstring*>* messages = new vector<wstring*>();

	if(!ParseMessages(message, messages))
	{
		delete messages;
		return false;
	}

	for (vector<wstring*>::iterator it = messages->begin() ; it != messages->end(); ++it)
	{
		wstring* command = new wstring();
		wstring* args = new wstring();
		vector<wstring*>* argList = new vector<wstring*>();
		wstring* currentMessage = *it;

		if(ParseMessage(message, command, args) && ParseArgs(args, argList))
		{
			allMessages->insert(pair<wstring*,vector<wstring*>*>(command, argList));
		}
		
		delete currentMessage;
	}

	return true;
}

bool CommunicationProcessor::ProcessResponse(const wstring* message, std::vector<std::wstring*>* responseList)
{
		//{"command":"getFileOverlayId","value":1}
		//{"command":"getMenuItems","value":["blah","blah","blah"]}

		if(message->length() == 0)
		{
			return false;
		}
	
		size_t firstComma = message->find(COMMA);
		size_t colon = message->find(COLON, firstComma);
		size_t end = message->find(CLOSE_CURLY_BRACE);

		if(colon == string::npos || end == string::npos)
		{
			return false;
		}

		wstring* args = new wstring();
		
		args->append(message->substr(colon + 1, end - colon - 1));
		bool success = true;

		if(!RemoveQuotes(args))
		{
			success = false;
		}

		if(!ParseArgs(args, responseList))
		{
			success = false;
		}

		return success;
}

bool CommunicationProcessor::ParseArgs(wstring* message, vector<wstring*>* args)
{
	//["blah","blah","blah"]
	//"blah"

	size_t openBrace = message->find(OPEN_BRACE, 0);
	
	if(openBrace == string::npos)
	{
		args->push_back(message);

		return true;
	}

	size_t closeBrace = message->find(CLOSE_BRACE, openBrace);

	if((closeBrace == string::npos) || ((openBrace + 1) == closeBrace))
	{
		return true;
	}

	size_t last = 0;
	size_t comma = 0;
	bool end = false;
	
	do
	{
		wstring* substring = new wstring();

		comma = message->find(COMMA, last + 1); 

		if(comma == string::npos)
		{
			comma = message->find(CLOSE_BRACE, last);
			end = true;
		}
				
		substring->append(message->substr(last + 1, (comma - last) - 1));	

		if(!RemoveQuotes(substring))
		{
			return false;
		}
		
		args->push_back(substring);

		last = comma;

	}while(!end);

	delete message;

	return true;
}

bool CommunicationProcessor::ParseMessages(wstring* message, vector<wstring*>* messages)
{
	size_t openPosition = 0;
	size_t closePosition = 0;

	do
	{
		openPosition = message->find(OPEN_CURLY_BRACE, closePosition); 
		closePosition = message->find(CLOSE_CURLY_BRACE, openPosition);

		if(openPosition != string::npos && closePosition != string::npos)
		{
			wstring* substring = new wstring();
			substring->assign(message->substr(openPosition, (closePosition - openPosition) + 1));
			messages->push_back(substring);
		}

	}while(openPosition != string::npos);

	return true;
}

bool CommunicationProcessor::ParseMessage(const wstring* message, std::wstring* command, std::wstring* args)
{
	size_t firstEquals = message->find(COLON, 0); 
	size_t comma = message->find(COMMA, firstEquals);
	size_t secondEquals = message->find(COLON, comma); 
	size_t closeBrace = message->find(CLOSE_CURLY_BRACE, secondEquals);

	if(firstEquals == string::npos || comma == string::npos || secondEquals == string::npos)
	{
		return false;
	}

	*command = message->substr(firstEquals + 1, comma - firstEquals - 1);

	if(!RemoveQuotes(command))
	{
		return false;
	}

	*args = message->substr(secondEquals + 1, closeBrace - secondEquals - 1);
	
	if(!RemoveQuotes(args))
	{
		return false;
	}

	return true;
}

bool CommunicationProcessor::RemoveQuotes(wstring* message)
{
	if(message->length() == 0){
		return true;
	}

	size_t firstQuote = 0;
	int start = 0;
	bool quote = true;
	while(quote)
	{
		wchar_t c = message->at(start);
		if(c == '"')
		{
			start++;
		}
		else
		{
			quote = false;
		}

	}

	quote = true;
	*message = message->substr(start, message->length() - start);
	size_t end = message->length() - 1;
	
	while(quote)
	{
		wchar_t c = message->at(end);
		if(c == '"')
		{
			end--;
		}
		else
		{
			quote = false;
		}
	}

	end++;

	if(end <= 0)
	{
		return false;
	}

	*message = message->substr(0, end);

	return true;
}

bool CommunicationProcessor::CreateMessage(const wchar_t* command, vector<wstring>* args, wstring* message)
{
	message->append(OPEN_CURLY_BRACE);
	message->append(QUOTE);
	message->append(COMMAND);
	message->append(QUOTE);
	message->append(COLON);
	message->append(QUOTE);
	message->append(command);
	message->append(QUOTE);
	message->append(COMMA);

	message->append(QUOTE);
	message->append(VALUES);
	message->append(QUOTE);
	message->append(COLON);
	message->append(OPEN_BRACE);

	if(!FormListAsResponse(args, message))
	{
		return false;
	}

	message->append(CLOSE_BRACE);
	message->append(CLOSE_CURLY_BRACE);

	return true;
}

bool CommunicationProcessor::FormListAsResponse(std::vector<std::wstring>* args, std::wstring* message)
{
	int i = 0;
	for(vector<wstring>::iterator it = args->begin() ; it != args->end(); it++)
	{
		if(i > 0)
		{
			message->append(COMMA);
		}

		wstring temp = *it;

		message->append(QUOTE);
		message->append(temp.c_str());
		message->append(QUOTE);
		i++;
	}

	return true;
}
