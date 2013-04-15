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

static GObjectClass *parent_class;

extern "C" void commandExecuted (NautilusMenuItem *item,
		       gpointer          user_data)
{
	std::string cmdId("menuExec:");
	cmdId += boost::lexical_cast<std::string>((int)user_data);

	RequestManager::instance().menuExecuted(cmdId); 
}


extern "C" GList* nautilus_liferay_get_file_items (NautilusMenuProvider* provider, GtkWidget* window, GList* files)
{
	GList    *items = NULL;
	GList    *scan;

	if (files == NULL)
		return NULL;

	std::string cmd("menuQuery");
	for (scan = files; scan; scan = scan->next) {
		NautilusFileInfo* file = (NautilusFileInfo*)scan->data;
		char             *uri;

		uri = nautilus_file_info_get_uri (file);
		cmd += ":";
		cmd += g_filename_from_uri(uri,NULL,NULL);
		g_free (uri);
	}

	std::string answer(RequestManager::instance().queryMenuItems(cmd));
	if (answer.empty())
		return NULL;

	std::vector<std::string> itemsArray;
	boost::split(itemsArray, answer, boost::is_any_of(":"));

	if (itemsArray.empty())
		return NULL;

	writeLog("Items count: %d\n", itemsArray.size());

	NautilusMenuItem *item;
	item = nautilus_menu_item_new ("LiferayMenu",ContentManager::instance().getMenuTitle().c_str(),_(""),"drive-harddisk");

	items = g_list_append(items, item);

	NautilusMenu* menu = nautilus_menu_new();
	nautilus_menu_item_set_submenu(item, menu);

	for (int i=0;i<itemsArray.size();++i)
	{
		std::string itemTitle(itemsArray[i]);
		if (itemTitle == "_SEPARATOR_")
			continue;

		bool enabled(true);

		int pos = itemTitle.find(",");
		if (pos != itemTitle.npos)
		{
			enabled = itemTitle.substr(pos + 1,itemTitle.npos) == "true";
			itemTitle.erase(pos,itemTitle.npos);
		}

		NautilusMenuItem *childItem = nautilus_menu_item_new (itemTitle.c_str(),itemTitle.c_str(),_(""),"drive-harddisk");

		if (!enabled)
		{
			GValue sensitive = G_VALUE_INIT;
		    	g_value_init (&sensitive, G_TYPE_BOOLEAN);
			g_value_set_boolean (&sensitive, FALSE);
		  
		  	g_object_set_property (G_OBJECT(childItem), "sensitive", &sensitive);
		}
		else
		{
			g_signal_connect(childItem, "activate", G_CALLBACK(commandExecuted), (gpointer)i);
		}

		nautilus_menu_append_item(menu, childItem);
	}


	return items;
}

extern "C" NautilusOperationResult nautilus_liferay_extension_update_file_info(NautilusInfoProvider* provider, NautilusFileInfo* file, GClosure *update_complete, NautilusOperationHandle **handle)
{
	char             *uri;
	uri = nautilus_file_info_get_uri (file);

	nautilus_file_info_add_emblem(file, ContentManager::instance().getFileIconName(g_filename_from_uri(uri,NULL,NULL)).c_str());

	return NAUTILUS_OPERATION_COMPLETE;
}

extern "C" void nautilus_liferay_menu_provider_iface_init (NautilusMenuProviderIface *iface)
{
	iface->get_file_items = nautilus_liferay_get_file_items;
}

extern "C" void
nautilus_liferay_info_provider_iface_init (NautilusInfoProviderIface *iface)
{
	iface->update_file_info = nautilus_liferay_extension_update_file_info;
}


extern "C" void
nautilus_liferay_instance_init (NautilusLiferay *fr)
{
}


extern "C" void
nautilus_liferay_class_init (NautilusLiferayClass *clazz)
{
	parent_class = (GObjectClass*) g_type_class_peek_parent (clazz);
}


static GType liferay_type = 0;


extern "C" GType 
nautilus_liferay_get_type (void)
{
	return liferay_type;
}


extern "C" void registerHandlers(GTypeModule *module)
{
	writeLog("registerHandlers entered\n");

	static const GTypeInfo info = {
		sizeof (NautilusLiferayClass),
		(GBaseInitFunc) NULL,
		(GBaseFinalizeFunc) NULL,
		(GClassInitFunc) nautilus_liferay_class_init,
		NULL,
		NULL,
		sizeof (NautilusLiferay),
		0,
		(GInstanceInitFunc) nautilus_liferay_instance_init,
	};

	static const GInterfaceInfo menu_provider_iface_info = {
		(GInterfaceInitFunc) nautilus_liferay_menu_provider_iface_init,
		NULL,
		NULL
	};

	static const GInterfaceInfo info_provider_iface_info = {
		(GInterfaceInitFunc) nautilus_liferay_info_provider_iface_init,
		NULL,
		NULL
	};

	liferay_type = g_type_module_register_type (module, G_TYPE_OBJECT,"LiferayPlugin", &info,(GTypeFlags) 0);

	writeLog("g_type_module_register_type returned %d\n", liferay_type);

	g_type_module_add_interface (module,
				     liferay_type,
				     NAUTILUS_TYPE_MENU_PROVIDER,
				     &menu_provider_iface_info);

	g_type_module_add_interface (module,
	                             liferay_type,
	                             NAUTILUS_TYPE_INFO_PROVIDER,
	                             &info_provider_iface_info);
}

