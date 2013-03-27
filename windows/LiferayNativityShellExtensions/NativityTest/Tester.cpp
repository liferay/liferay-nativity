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

#include "Tester.h"
#include "TestData.h"

#include "ContextMenuUtil.h"
#include "CommunicationSocket.h"
#include "CommunicationProcessor.h"
#include <iostream>

using namespace std;

Tester::Tester()
{
}

Tester::~Tester()
{
}

bool Tester::TestNativityUtil()
{
	cout<<"Beginning test nativity util"<<endl;
	
	CommunicationSocket* fakePlugInToServer = new CommunicationSocket(SERVER_TO_JAVA_RECEIVE_SOCKET);
	CommunicationSocket* fakeServerToPlugIn = new CommunicationSocket(SERVER_TO_JAVA_SEND_SOCKET);
	
	for(int i = 0; i < 20; i++)
	{
		wstring* message = new wstring();
		if(fakePlugInToServer->ReceiveResponseOnly(message))
		{
			wcout<<"Received Message "<<endl;

			map<wstring*, vector<wstring*>*>* messages = new map<wstring*, vector<wstring*>*>();

			if(CommunicationProcessor::ProcessMessage(message, messages))
			{
				wcout<<"Processed message"<<endl;
			}
			else
			{
				wcout<<"ERROR UNable to Process message"<<endl;
			}

			messages->erase(messages->begin(), messages->end());

			delete messages;
		}
		else
		{
			wcout<<"Unable to receive message"<<endl;
		}

		delete message;

		//if(fakeServerToPlugIn->SendMessageOnly(L"menuExec"))
		//{
		//	wcout<<"Sent message to plug in"<<endl;
		//}
		//else
		//{
		//	wcout<<"Unable to send message"<<endl;
		//}

		::Sleep(2000);
	}

	cout<<"Done!!!"<<endl;

	delete fakePlugInToServer;
	cout<<"Deleted 2"<<endl;
	delete fakeServerToPlugIn;
	cout<<"Deleted 3"<<endl;

	return true;
}

bool Tester::TestContextMenuUtil()
{
	cout<<"Beginning test context menu util"<<endl;
	ContextMenuUtil* contextMenuUtil = new ContextMenuUtil();

	wstring* file = new wstring(IN_FILE_1);
    if(!contextMenuUtil->AddFile(file))
	{
		cout<<"Unable to add file"<<endl;
		return false;
	}

	cout<<"Added file"<<endl;

	if(!contextMenuUtil->InitMenus())
	{
		cout<<"Unable to init menus"<<endl;
		return false;
	}

	cout<<"Init Menus"<<endl;

	if(!contextMenuUtil->PerformAction(3))
	{
		cout<<"Unable to perform action"<<endl;
		return false;
	}
}
 