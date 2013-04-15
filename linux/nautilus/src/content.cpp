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
#include "content.h"
#include <glib-object.h>
#include <glib/gi18n-lib.h>
#include <gio/gio.h>
#include <libnautilus-extension/nautilus-extension-types.h>
#include <libnautilus-extension/nautilus-file-info.h>
#include <libnautilus-extension/nautilus-menu-provider.h>
#include <libnautilus-extension/nautilus-info-provider.h>
#include "logger.h"

ContentManager::ContentManager() :
	lastIconId_(0),
	overlaysEnabled_(false),
	menuTitle_("Liferay")
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
	std::map<std::string, int>::const_iterator itIcon = iconsForFiles_.find(fileName);
	if (itIcon == iconsForFiles_.end())
		return "";

	std::map<int, std::string>::const_iterator itName = icons_.find(itIcon->second);
	if (itName == icons_.end())
		return "";

	std::string folder(itName->second);
	std::string icon(itName->second);

	size_t pos = folder.find_last_of("/");
	if (pos != folder.npos)
	{
		folder.erase(pos,folder.npos);
		icon.erase(0, pos + 1);
	}

	if (!folder.empty())
	{
		if (registeredFolders_.find(folder) == registeredFolders_.end())
		{
			GtkIconTheme *theme = gtk_icon_theme_get_default();

			writeLog("add folder to gtk theme (%x) paths: %s", theme, folder.c_str());
			gtk_icon_theme_append_search_path(theme, folder.c_str());

			registeredFolders_.insert(folder);
		}
	}

	pos = icon.find_last_of(".");
	if (pos != icon.npos)
		icon.erase(pos, icon.npos);


	return icon;
}

void ContentManager::setIconForFile(const std::string& fileName, int icon)
{
	iconsForFiles_[fileName] = icon;
}

void ContentManager::removeFileIcon(const std::string& fileName)
{
	iconsForFiles_.erase(fileName);
}

int ContentManager::registerIcon(const std::string& fileName)
{
	lastIconId_++;
	icons_[lastIconId_] = fileName;

	return lastIconId_;
}

void ContentManager::unregisterIcon(int iconId)
{
	icons_.erase(iconId);
}

void ContentManager::enableOverlays(bool enable)
{
	overlaysEnabled_ = enable;
}

bool ContentManager::isOverlaysEnabled()
{
	return overlaysEnabled_;
}

void ContentManager::setMenuTitle(const std::string& title)
{
	menuTitle_ = title;
}

const std::string& ContentManager::getMenuTitle() const
{
	return menuTitle_;
}

