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
#include <libnautilus-extension/nautilus-extension-types.h>
#include <libnautilus-extension/nautilus-column-provider.h>
#include <glib/gi18n-lib.h>
#include "handlers.h"
#include "logger.h"
#include "requests.h"

// Hooks for Nautilus

extern "C" void nautilus_module_initialize(GTypeModule* module)
{
	RequestManager::instance();

	writeLog("nautilus_module_initialize\n");
	registerHandlers(module);
}

extern "C" void nautilus_module_shutdown(void)
{
	writeLog("nautilus_module_shutdown\n");
}

extern "C" void nautilus_module_list_types(const GType** types, int* num_types)
{
	writeLog("nautilus_module_list_types\n");

	static GType type_list[1];

	type_list[0] = NAUTILUS_TYPE_LIFERAY;
	*types = type_list;
	*num_types = 1;
}


// Hooks for Nemo (Nautilus fork)

extern "C" void nemo_module_initialize(GTypeModule* module)
{
	nautilus_module_initialize(module);
}

extern "C" void nemo_module_shutdown(void)
{
	nautilus_module_shutdown();
}

extern "C" void nemo_module_list_types(const GType** types, int* num_types)
{
	nautilus_module_list_types(types, num_types);
}
