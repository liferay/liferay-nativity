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
#include <boost/algorithm/string.hpp>
#include <boost/lexical_cast.hpp>
#include <json/json.h>
#include "content.h"
#include "logger.h"
#include "requests.h"

RequestManager::RequestManager() :
	callbackSocket_(2, 33002, NULL, 0, 100000),
	commandSocket_(1, 33001, this)
{
}

RequestManager::~RequestManager()
{
}

RequestManager& RequestManager::instance()
{
	static RequestManager inst;

	return inst;
}

std::string RequestManager::queryMenuItems(const std::string& request)
{
	if (!callbackSocket_.isConnected())
	{
		return std::string();
	}

	callbackSocket_.writeString(request);

	std::string result;

	if (callbackSocket_.readString(result))
	{
		return result;
	}
	else
	{
		return std::string();
	}
}

void RequestManager::menuExecuted(const std::string& reply)
{
	if (callbackSocket_.isConnected())
	{
		callbackSocket_.writeString(reply);
	}
}

void RequestManager::onStringReceived(int serverId, const std::string& text)
{
	if (serverId == 1)
	{
		Json::Value jsonRoot;
		Json::Reader jsonReader;

		jsonReader.parse(text, jsonRoot);

		std::string command = jsonRoot.get("command", "").asString();

		if (command.empty())
		{
			return;
		}

		writeLog("Command received: %s\n", command.c_str());

		Json::Value jsonValue = jsonRoot.get("value", "");

		if (command == "enableFileIcons")
		{
			execEnableFileIconsCmd(jsonValue);
		}
		else if (command == "removeFileIcons")
		{
			execRemoveFileIconsCmd(jsonValue);
		}
		else if (command == "registerIcon")
		{
			execRegisterIconCmd(jsonValue);
		}
		else if (command == "registerIconWithId")
		{
			execRegisterIconCmd(jsonValue);
		}
		else if (command == "removeAllFileIcons")
		{
			execRemoveAllFileIconsCmd(jsonValue);
		}
		else if (command == "setFileIcons")
		{
			execSetFileIconsCmd(jsonValue);
		}
		else if (command == "setFilterPath")
		{
			execSetRootFolderCmd(jsonValue);
		}
		else if (command == "unregisterIcon")
		{
			execUnregisterIconCmd(jsonValue);
		}
		else
		{
			commandSocket_.writeString("-1");
		}
	}
}

void RequestManager::execSetFileIconsCmd(const Json::Value& jsonValue)
{
	std::vector<std::string> paths = jsonValue.getMemberNames();

	for (int i = 0; i < paths.size(); i++)
	{
		ContentManager::instance().setFileIcon(paths[i], jsonValue.get(paths[i], "").asInt());
	}

	commandSocket_.writeString("1");
}

void RequestManager::execRemoveFileIconsCmd(const Json::Value& jsonValue)
{
	for (int i = 0; i < jsonValue.size(); i++)
	{
		ContentManager::instance().removeFileIcon(jsonValue[i].asString());
	}

	commandSocket_.writeString("1");
}

void RequestManager::execRemoveAllFileIconsCmd(const Json::Value& jsonValue)
{
	for (int i = 0; i < jsonValue.size(); i++)
	{
		ContentManager::instance().removeFileIcon(jsonValue[i].asString());
	}

	commandSocket_.writeString("1");
}

void RequestManager::execEnableFileIconsCmd(const Json::Value& jsonValue)
{
	ContentManager::instance().enableFileIcons(jsonValue.asBool());

	commandSocket_.writeString("1");
}

void RequestManager::execRegisterIconCmd(const Json::Value& jsonValue)
{
	int index = ContentManager::instance().registerIcon(jsonValue.asString());

	commandSocket_.writeString(boost::lexical_cast<std::string>(index));
}

void RequestManager::execRegisterIconWithIdCmd(const Json::Value& jsonValue)
{
	ContentManager::instance().registerIconWithId(jsonValue["path"].asString(), jsonValue["iconId"].asString());

	commandSocket_.writeString("1");
}

void RequestManager::execUnregisterIconCmd(const Json::Value& jsonValue)
{
	ContentManager::instance().unregisterIcon(jsonValue.asInt());

	commandSocket_.writeString("1");
}

void RequestManager::execSetRootFolderCmd(const Json::Value& jsonValue)
{
	ContentManager::instance().setRootFolder(jsonValue.asString());

	commandSocket_.writeString("1");
}