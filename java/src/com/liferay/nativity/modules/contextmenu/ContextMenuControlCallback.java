/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.nativity.modules.contextmenu;

import com.liferay.nativity.modules.contextmenu.model.ContextMenuItem;

import java.util.List;

/**
 * @author Dennis Ju
 */
public interface ContextMenuControlCallback {

	/**
	 * Called by the native service to request the menu items for a context
	 * menu popup
	 *
	 * @param paths The files selected for this context menu popup
	 *
	 * @return each ContextMenuItem instance in the list will appear at the
	 * context menu's top level
	 */
	public abstract List<ContextMenuItem> getContextMenuItems(String[] paths);

}