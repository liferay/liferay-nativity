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
import com.liferay.nativity.plugincontrol.NativityControl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis Ju
 */
public abstract class ContextMenuControlBase {

	public ContextMenuControlBase(NativityControl pluginControl) {
		this.pluginControl = pluginControl;

		_menuItemListeners = new ArrayList<MenuItemListener>();
	}

	public void addMenuItemListener(MenuItemListener listener) {
		_menuItemListeners.add(listener);
	}

	public void fireMenuItemListeners(
		int menuIndex, String menuText, String[] paths) {

		for (MenuItemListener menuItemListener : _menuItemListeners) {
			menuItemListener.onMenuItemSelected(menuIndex, menuText, paths);
		}
	}

	public abstract String[] getHelpItemsForMenus(String[] paths);

	public abstract String[] getMenuItems(String[] paths);

	public void removeAllMenuItemListeners() {
		_menuItemListeners.clear();
	}

	public void removeMenuItemListener(MenuItemListener menuItemListener) {
		_menuItemListeners.remove(menuItemListener);
	}

	public abstract void setContextMenuTitle(String title);

	protected List<MenuItemListener> _menuItemListeners;

	protected NativityControl pluginControl;

}