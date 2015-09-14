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
#ifndef __REQUESTS_H__
#define __REQUESTS_H__

#include "socket.h"
#include <string>
#include <vector>
#include <json/json.h>

class RequestManager :
	public ISocketCallback
{
	public:
		static RequestManager& instance();

		std::string queryMenuItems(const std::string& request);
		void menuExecuted(const std::string& reply);

	protected:
		virtual void onStringReceived(int serverId, const std::string& text);

	private:
		SocketServer callbackSocket_;
		SocketServer commandSocket_;

		RequestManager();
		~RequestManager();

		void execSetFileIconsCmd(const Json::Value& jsonValue);
		void execRemoveFileIconsCmd(const Json::Value& jsonValue);
		void execRemoveAllFileIconsCmd(const Json::Value& jsonValue);
		void execEnableFileIconsCmd(const Json::Value& jsonValue);
		void execRegisterIconCmd(const Json::Value& jsonValue);
		void execRegisterIconWithIdCmd(const Json::Value& jsonValue);
		void execUnregisterIconCmd(const Json::Value& jsonValue);
		void execSetRootFolderCmd(const Json::Value& jsonValue);
};

#endif