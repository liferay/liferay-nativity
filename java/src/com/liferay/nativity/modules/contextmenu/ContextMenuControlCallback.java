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

package com.liferay.nativity.modules.contextmenu;

/**
 * @author Dennis Ju
 */
public interface ContextMenuControlCallback {

	/**
	 * Windows only
	 *
	 * Called by the native service to request the help text for menu items
	 *
	 * @param the files selected for the help text
	 *
	 * @return array of help text titles corresponding to the menu items
	 */
	public abstract String[] getHelpItemsForMenus(String[] paths);

	/**
	 * Called by the native service to request the menu items for a context
	 * menu popup
	 *
	 * @param the files selected for this context menu popup
	 *
	 * @return array of menu item titles to populate the context menu
	 */
	public abstract String[] getMenuItems(String[] paths);

}