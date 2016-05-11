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
#include <string.h>
#include <glib/gi18n-lib.h>
#include <gio/gio.h>
#include <libnautilus-extension/nautilus-extension-types.h>
#include <libnautilus-extension/nautilus-file-info.h>
#include <libnautilus-extension/nautilus-menu-provider.h>
#include <libnautilus-extension/nautilus-info-provider.h>
#include "handlers.h"
#include "logger.h"
#include "requests.h"
#include <boost/algorithm/string.hpp>
#include <boost/lexical_cast.hpp>
#include <vector>
#include "content.h"
#include <json/json.h>

static GObjectClass* parent_class;

extern "C" void commandExecuted(NautilusMenuItem* item, gpointer user_data)
{
	GList* files = g_list_first((GList*)g_object_get_data(G_OBJECT(item), "nativity::files"));
	GList* scan;

	Json::Value jsonRoot;

	jsonRoot["command"] = "contextMenuAction";

	Json::Value jsonFiles(Json::arrayValue);

	for (scan = files; scan; scan = scan->next)
	{
		NautilusFileInfo* file = (NautilusFileInfo*)scan->data;

		char* uri = nautilus_file_info_get_uri(file);
		gchar* filename = g_filename_from_uri(uri, NULL, NULL);
		g_free(uri);

		if (filename == NULL)
		{
			continue;
		}

		std::string filePath(filename);
		g_free(filename);

		try
		{
			jsonFiles.append(Json::Value(filePath));
		}
		catch (boost::bad_lexical_cast)
		{
			writeLog("boost::bad_lexical_cast\n");
		}
	}

	gchar* title;
	g_object_get(G_OBJECT(item), "label", &title, NULL);
	std::string menuTitle(title);
	g_free(title);

	gchar* uuid = (gchar*)g_object_get_data(G_OBJECT(item), "nativity::uuid");
	std::string menuUuid(uuid);

	Json::Value jsonValue;

	jsonValue["title"] = menuTitle;
	jsonValue["uuid"] = menuUuid;
	jsonValue["files"] = jsonFiles;

	jsonRoot["value"] = jsonValue;

	Json::FastWriter jsonWriter;

	RequestManager::instance().menuExecuted(jsonWriter.write(jsonRoot));
}

extern "C" void addMenuItems(NautilusMenuItem* parentMenuItem, Json::Value jsonMenuItems, GList* files, int depth)
{
	NautilusMenu* menu = nautilus_menu_new();

	nautilus_menu_item_set_submenu(parentMenuItem, menu);

	for (int i = 0; i < jsonMenuItems.size(); i++)
	{
		Json::Value jsonMenuItem = jsonMenuItems[i];

		std::string uuid = jsonMenuItem.get("uuid", "").asCString();
		std::string itemTitle = jsonMenuItem.get("title", "").asCString();

		if (itemTitle == "_SEPARATOR_")
		{
			continue;
		}

		std::string identifier = "nativity" + boost::lexical_cast<std::string>(depth) + boost::lexical_cast<std::string>(i);
		NautilusMenuItem* menuItem = nautilus_menu_item_new(identifier.c_str(), itemTitle.c_str(), _(""), "drive-harddisk");
		nautilus_menu_append_item(menu, menuItem);

		Json::Value jsonSubMenuItems = jsonMenuItem.get("contextMenuItems", Json::arrayValue);

		if (jsonSubMenuItems.size() != 0)
		{
			addMenuItems(menuItem, jsonSubMenuItems, files, depth++);
		}
		else
		{
			if (jsonMenuItem.get("enabled", true).asBool())
			{
				std::string uuidString = jsonMenuItem.get("uuid", "").asString();
				const gchar* uuid = uuidString.c_str();

				g_signal_connect(menuItem, "activate", G_CALLBACK(commandExecuted), NULL);

				g_object_set_data_full(G_OBJECT(menuItem), "nativity::files", nautilus_file_info_list_copy(files), (GDestroyNotify)nautilus_file_info_list_free);
				g_object_set_data_full(G_OBJECT(menuItem), "nativity::uuid", g_strdup(uuid), g_free);
			}
			else
			{
				GValue sensitive = G_VALUE_INIT;
				g_value_init(&sensitive, G_TYPE_BOOLEAN);
				g_value_set_boolean(&sensitive, FALSE);

				g_object_set_property(G_OBJECT(menuItem), "sensitive", &sensitive);
			}
		}
	}
}

extern "C" GList* nautilus_liferay_get_file_items(NautilusMenuProvider* provider, GtkWidget* window, GList* files)
{
	GList* menuItems = NULL;
	GList* scan;

	if (files == NULL)
	{
		return NULL;
	}

	Json::Value jsonRoot;
	Json::Value jsonValue(Json::arrayValue);

	std::string rootFolder = ContentManager::instance().getRootFolder();

	for (scan = files; scan; scan = scan->next)
	{
		NautilusFileInfo* file = (NautilusFileInfo*)scan->data;

		char* uri = nautilus_file_info_get_uri(file);
		gchar* filename = g_filename_from_uri(uri, NULL, NULL);
		g_free(uri);

		if (filename == NULL)
		{
			continue;
		}

		std::string filePath(filename);
		g_free(filename);

		try
		{
			if (!rootFolder.empty() && !boost::starts_with(filePath, rootFolder))
			{
				return NULL;
			}

			jsonValue.append(Json::Value(filePath));
		}
		catch (boost::bad_lexical_cast)
		{
			writeLog("boost::bad_lexical_cast\n");
		}
	}

	jsonRoot["command"] = "getContextMenuList";
	jsonRoot["value"] = jsonValue;

	Json::FastWriter jsonWriter;

	std::string answer(RequestManager::instance().queryMenuItems(jsonWriter.write(jsonRoot)));

	if (answer.empty())
	{
		return NULL;
	}

	Json::Value jsonAnswer;
	Json::Reader jsonReader;

	jsonReader.parse(answer, jsonAnswer);

	Json::Value jsonMenuItems = jsonAnswer.get("value", Json::arrayValue);

	if (jsonMenuItems.empty())
	{
		return NULL;
	}

	for (int i = 0; i < jsonMenuItems.size(); i++)
	{
		Json::Value jsonMenuItem = jsonMenuItems[i];

		std::string identifier = "nativity" + boost::lexical_cast<std::string>(i);
		std::string itemTitle = jsonMenuItem.get("title", "").asCString();

		NautilusMenuItem* menuItem = nautilus_menu_item_new(identifier.c_str(), itemTitle.c_str(), _(""), "drive-harddisk");

		menuItems = g_list_append(menuItems, menuItem);

		Json::Value jsonSubMenuItems = jsonMenuItem.get("contextMenuItems", Json::arrayValue);

		if (jsonSubMenuItems.size() != 0)
		{
			addMenuItems(menuItem, jsonSubMenuItems, files, 0);
		}
		else
		{
			if (jsonMenuItem.get("enabled", true).asBool())
			{
				std::string uuidString = jsonMenuItem.get("uuid", "").asString();
				const gchar* uuid = uuidString.c_str();

				g_signal_connect(menuItem, "activate", G_CALLBACK(commandExecuted), NULL);

				g_object_set_data_full(G_OBJECT(menuItem), "nativity::files", nautilus_file_info_list_copy(files), (GDestroyNotify)nautilus_file_info_list_free);
				g_object_set_data_full(G_OBJECT(menuItem), "nativity::uuid", g_strdup(uuid), g_free);
			}
			else
			{
				GValue sensitive = G_VALUE_INIT;
				g_value_init(&sensitive, G_TYPE_BOOLEAN);
				g_value_set_boolean(&sensitive, FALSE);

				g_object_set_property(G_OBJECT(menuItem), "sensitive", &sensitive);
			}
		}
	}

	return menuItems;
}

extern "C" NautilusOperationResult nautilus_liferay_extension_update_file_info(NautilusInfoProvider* provider, NautilusFileInfo* file, GClosure* update_complete, NautilusOperationHandle** handle)
{
	char* uri = nautilus_file_info_get_uri(file);

	gchar* filename = g_filename_from_uri(uri, NULL, NULL);

	if (filename != NULL)
	{
		nautilus_file_info_add_emblem(file, ContentManager::instance().getFileIconName(filename).c_str());

		g_free(filename);
	}

	return NAUTILUS_OPERATION_COMPLETE;
}

extern "C" void nautilus_liferay_menu_provider_iface_init(NautilusMenuProviderIface* iface)
{
	iface->get_file_items = nautilus_liferay_get_file_items;
}

extern "C" void nautilus_liferay_info_provider_iface_init(NautilusInfoProviderIface* iface)
{
	iface->update_file_info = nautilus_liferay_extension_update_file_info;
}

extern "C" void nautilus_liferay_instance_init(NautilusLiferay* fr)
{
}

extern "C" void nautilus_liferay_class_init(NautilusLiferayClass* clazz)
{
	parent_class = (GObjectClass*)g_type_class_peek_parent(clazz);
}

static GType liferay_type = 0;

extern "C" GType nautilus_liferay_get_type(void)
{
	return liferay_type;
}

extern "C" void registerHandlers(GTypeModule* module)
{
	writeLog("registerHandlers entered\n");

	static const GTypeInfo info =
	{
		sizeof(NautilusLiferayClass),
		(GBaseInitFunc)NULL,
		(GBaseFinalizeFunc)NULL,
		(GClassInitFunc)nautilus_liferay_class_init,
		NULL,
		NULL,
		sizeof(NautilusLiferay),
		0,
		(GInstanceInitFunc)nautilus_liferay_instance_init,
	};

	static const GInterfaceInfo menu_provider_iface_info =
	{
		(GInterfaceInitFunc)nautilus_liferay_menu_provider_iface_init,
		NULL,
		NULL
	};

	static const GInterfaceInfo info_provider_iface_info =
	{
		(GInterfaceInitFunc)nautilus_liferay_info_provider_iface_init,
		NULL,
		NULL
	};

	liferay_type = g_type_module_register_type(module, G_TYPE_OBJECT, "LiferayNativity", &info, (GTypeFlags)0);

	writeLog("g_type_module_register_type returned %d\n", liferay_type);

	g_type_module_add_interface(module, liferay_type, NAUTILUS_TYPE_MENU_PROVIDER, &menu_provider_iface_info);

	g_type_module_add_interface(module, liferay_type, NAUTILUS_TYPE_INFO_PROVIDER, &info_provider_iface_info);
}