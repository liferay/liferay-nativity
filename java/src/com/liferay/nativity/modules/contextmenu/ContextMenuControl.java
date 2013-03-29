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

import com.liferay.nativity.modules.contextmenu.listeners.MenuItemListener;

/**
 * @author Dennis Ju
 */
public interface ContextMenuControl extends ContextMenuControlCallback {

	/**
	 * Adds a MenuItemListener to respond to menu item selections.
	 * Multiple listeners can be added.
	 *
	 * @param listener to respond to menu item selections
	 */
	public void addMenuItemListener(MenuItemListener menuItemListener);

	/**
	 * Notifies all MenuItemListener instances when a menu item is selected
	 *
	 * @param index value of the selected menu item
	 *
	 * @param text value of the selected menu item
	 *
	 * @param array of selected file paths
	 */
	public void fireMenuItemListeners(String menuText, String[] paths);

	/**
	 * Removes all MenuItemListeners
	 */
	public void removeAllMenuItemListeners();

	/**
	 * Removes a MenuItemListener
	 *
	 * @param the MenuItemListener to remove
	 */
	public void removeMenuItemListener(MenuItemListener menuItemListener);

	/**
	 * Set title of root context menu item, all other items will be added as
	 * children of it
	 *
	 * @param title of context menu
	 */
	public void setContextMenuTitle(String title);

}