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
#ifndef __HANDLERS_H__
#define __HANDLERS_H__

#include <glib-object.h>

G_BEGIN_DECLS

#define NAUTILUS_TYPE_LIFERAY (nautilus_liferay_get_type())
#define NAUTILUS_LIFERAY(o) (G_TYPE_CHECK_INSTANCE_CAST((o), NAUTILUS_TYPE_LIFERAY, NautilusLiferay))
#define NAUTILUS_IS_LIFERAY(o) (G_TYPE_CHECK_INSTANCE_TYPE((o), NAUTILUS_TYPE_LIFERAY))

typedef struct _NautilusLiferay NautilusLiferay;
typedef struct _NautilusLiferayClass NautilusLiferayClass;

struct _NautilusLiferay
{
	GObject __parent;
};

struct _NautilusLiferayClass
{
	GObjectClass __parent;
};

GType nautilus_liferay_get_type(void);
void registerHandlers(GTypeModule* module);

G_END_DECLS

#endif