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
#include "config.h"
#include <boost/lexical_cast.hpp>
#include <boost/algorithm/string.hpp>
#include <gio/gio.h>
#include <glib/gi18n-lib.h>
#include <glib-object.h>
#include <libnautilus-extension/nautilus-extension-types.h>
#include <libnautilus-extension/nautilus-file-info.h>
#include <libnautilus-extension/nautilus-info-provider.h>
#include <libnautilus-extension/nautilus-menu-provider.h>
#include "content.h"
#include "logger.h"

ContentManager::ContentManager() :
	lastIconId_(0),
	overlaysEnabled_(false)
{
}

ContentManager::~ContentManager()
{
}

ContentManager& ContentManager::instance()
{
	static ContentManager inst;

	return inst;
}

std::string ContentManager::getFileIconName(const std::string& fileName) const
{
	std::map<std::string, std::string>::const_iterator itIcon = iconsForFiles_.find(fileName);

	if (itIcon == iconsForFiles_.end())
	{
		return "";
	}

	std::map<std::string, std::string>::const_iterator itName = icons_.find(itIcon->second);

	if (itName == icons_.end())
	{
		return "";
	}

	std::string folder(itName->second);
	std::string icon(itName->second);

	size_t pos = folder.find_last_of("/");

	if (pos != folder.npos)
	{
		folder.erase(pos, folder.npos);
		icon.erase(0, pos + 1);
	}

	if (!folder.empty())
	{
		if (registeredFolders_.find(folder) == registeredFolders_.end())
		{
			GtkIconTheme* theme = gtk_icon_theme_get_default();

			writeLog("add folder to gtk theme (%x) paths: %s", theme, folder.c_str());
			gtk_icon_theme_append_search_path(theme, folder.c_str());

			registeredFolders_.insert(folder);
		}
	}

	pos = icon.find_last_of(".");

	if (pos != icon.npos)
	{
		icon.erase(pos, icon.npos);
	}

	return icon;
}

void ContentManager::setFileIcon(const std::string& fileName, int iconId)
{
	std::string rootFolder = ContentManager::instance().getRootFolder();

	if (!rootFolder.empty() && !boost::starts_with(fileName, rootFolder))
	{
		return;
	}

	std::string iconIdString = boost::lexical_cast<std::string>(iconId);

	if (iconIdString == "-1")
	{
		iconsForFiles_.erase(fileName);
	}
	else
	{
		iconsForFiles_[fileName] = iconIdString;
	}
}

void ContentManager::removeFileIcon(const std::string& fileName)
{
	iconsForFiles_.erase(fileName);
}

void ContentManager::removeAllFileIcons()
{
	iconsForFiles_.clear();
}

int ContentManager::registerIcon(const std::string& fileName)
{
	lastIconId_++;
	icons_[boost::lexical_cast<std::string>(lastIconId_)] = fileName;

	return lastIconId_;
}

void ContentManager::registerIconWithId(const std::string& fileName, const std::string& iconId)
{
	icons_[iconId] = fileName;
}

void ContentManager::unregisterIcon(int iconId)
{
	icons_.erase(boost::lexical_cast<std::string>(iconId));
}

void ContentManager::enableFileIcons(bool enable)
{
	overlaysEnabled_ = enable;
}

bool ContentManager::isOverlaysEnabled()
{
	return overlaysEnabled_;
}

void ContentManager::setRootFolder(const std::string& rootFolder)
{
	rootFolder_ = rootFolder;
}

const std::string& ContentManager::getRootFolder() const
{
	return rootFolder_;
}