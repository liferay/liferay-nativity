/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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
#ifndef __SOCKET_SERVER_H__
#define __SOCKET_SERVER_H__

#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <string>

struct ISocketCallback
{
	virtual void onStringReceived(int serverId, const std::string& text) = 0;
};

class SocketServer
{
	public:
		SocketServer(int id, unsigned short port, ISocketCallback* callback);
		SocketServer(int id, unsigned short port, ISocketCallback* callback, int timeoutSeconds, int timeoutMicroseconds);
		virtual ~SocketServer();

		void writeString(const std::string& data);
		bool readString(std::string& data);

		bool isConnected();
		void setTimeout(int seconds, int microseconds);

	private:
		int id_;

		pthread_t acceptThread_;
		pthread_t readThread_;

		int serverSocket_;
		int clientSocket_;

		ISocketCallback* callback_;
		unsigned short port_;

		int timeoutSeconds_;
		int timeoutMicroseconds_;

		void startListening();

		static void* acceptHandler(void* param);
		void doAcceptLoop();

		static void* readHandler(void* param);
		void doReadLoop();
};

#endif