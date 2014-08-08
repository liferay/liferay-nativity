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
#include "socket.h"
#include "logger.h"
#include <unistd.h>
#include <errno.h>

SocketServer::SocketServer(int id, unsigned short port, ISocketCallback* callback) :
	id_(id),
	port_(port),
	callback_(callback),
	clientSocket_(0),
	serverSocket_(0)
{
	startListening();
}

SocketServer::SocketServer(int id, unsigned short port, ISocketCallback* callback, int timeoutSeconds, int timeoutMicroseconds) :
	id_(id),
	port_(port),
	callback_(callback),
	clientSocket_(0),
	serverSocket_(0),
	timeoutSeconds_(timeoutSeconds),
	timeoutMicroseconds_(timeoutMicroseconds)
{
	startListening();
}

SocketServer::~SocketServer()
{
}

void SocketServer::startListening()
{
	pthread_create(&acceptThread_, NULL, acceptHandler, (void*)this);
}

void* SocketServer::acceptHandler(void* param)
{
	SocketServer* instance = (SocketServer*)param;

	instance->doAcceptLoop();
}

void SocketServer::doAcceptLoop()
{
	struct sockaddr_in server, client;

	serverSocket_ = socket(AF_INET, SOCK_STREAM, 0);

	if (serverSocket_ == -1)
	{
		writeLog("Could not create socket\n");

		return;
	}

	writeLog("Socket created\n");

	server.sin_family = AF_INET;
	server.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
	server.sin_port = htons(port_);

	if (bind(serverSocket_, (struct sockaddr*)&server, sizeof(server)) < 0)
	{
		writeLog("bind failed\n");

		return;
	}

	listen(serverSocket_, 3);

	writeLog("Waiting for incoming connections...\n");
	socklen_t size = sizeof(struct sockaddr_in);

	int client_sock(0);

	while ((client_sock = accept(serverSocket_, (struct sockaddr*)&client, &size)))
	{
		if (clientSocket_)
		{
			close(clientSocket_);
			pthread_join(readThread_, NULL);
			writeLog("Previous connection closed\n");
		}

		clientSocket_ = client_sock;

		writeLog("Connection accepted socket = %d\n", clientSocket_);

		struct timeval timeoutTimeval;

		timeoutTimeval.tv_sec = timeoutSeconds_;
		timeoutTimeval.tv_usec = timeoutMicroseconds_;

		setsockopt(clientSocket_, SOL_SOCKET, SO_RCVTIMEO, (struct timeval*)&timeoutTimeval, sizeof(struct timeval));

		if (callback_)
		{
			if (pthread_create(&readThread_, NULL, readHandler, (void*)this) < 0)
			{
				writeLog("could not create thread\n");
				continue;
			}
		}
	}
}

void* SocketServer::readHandler(void* param)
{
	SocketServer* instance = (SocketServer*)param;

	instance->doReadLoop();
}

void SocketServer::doReadLoop()
{
	char buffer[2048];

	while (1)
	{
		std::string data;

		if (!readString(data))
		{
			break;
		}

		writeLog("String data read: %s\n", data.c_str());

		callback_->onStringReceived(id_, data);
	}
}

bool SocketServer::isConnected()
{
	return clientSocket_ != 0;
}

void SocketServer::writeString(const std::string& data)
{
	send(clientSocket_, data.c_str(), data.size(), 0);
	send(clientSocket_, "\r\n", 2, 0);
}

bool SocketServer::readString(std::string& data)
{
	data = std::string();
	char buffer;

	while (1)
	{
		int read_size = recv(clientSocket_, &buffer, 1, 0);

		if (read_size == 0)
		{
			clientSocket_ = 0;

			return false;
		}
		else if (read_size < 0)
		{
			int err_code;
			socklen_t len = sizeof(err_code);

			if (getsockopt(clientSocket_, SOL_SOCKET, SO_ERROR, &err_code, &len) != 0)
			{
				clientSocket_ = 0;
			}
			else if (err_code == EAGAIN || err_code == EWOULDBLOCK)
			{
				writeLog("Context menu request timed out\n");
			}

			return false;
		}

		if (buffer == '\r')
		{
			continue;
		}
		if (buffer == '\n')
		{
			break;
		}

//		writeLog("Received data: %s\n", data.c_str());

		data += buffer;
	}

	return true;
}

void SocketServer::setTimeout(int seconds, int microseconds)
{
	timeoutSeconds_ = seconds;
	timeoutMicroseconds_ = microseconds;
}
