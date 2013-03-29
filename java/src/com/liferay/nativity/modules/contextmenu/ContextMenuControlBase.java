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

import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.modules.contextmenu.listeners.MenuItemListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis Ju
 */
public abstract class ContextMenuControlBase implements ContextMenuControl {

	public ContextMenuControlBase(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		this.nativityControl = nativityControl;
		this.contextMenuControlCallback = contextMenuControlCallback;

		_menuItemListeners = new ArrayList<MenuItemListener>();
	}

	@Override
	public void addMenuItemListener(MenuItemListener listener) {
		_menuItemListeners.add(listener);
	}

	@Override
	public void fireMenuItemListeners(String menuText, String[] paths) {
		for (MenuItemListener menuItemListener : _menuItemListeners) {
			menuItemListener.onMenuItemSelected(menuText, paths);
		}
	}

	@Override
	public String[] getHelpItemsForMenus(String[] paths) {
		return contextMenuControlCallback.getHelpItemsForMenus(paths);
	}

	@Override
	public String[] getMenuItems(String[] paths) {
		return contextMenuControlCallback.getMenuItems(paths);
	}

	@Override
	public void removeAllMenuItemListeners() {
		_menuItemListeners.clear();
	}

	@Override
	public void removeMenuItemListener(MenuItemListener menuItemListener) {
		_menuItemListeners.remove(menuItemListener);
	}

	protected ContextMenuControlCallback contextMenuControlCallback;
	protected NativityControl nativityControl;

	private List<MenuItemListener> _menuItemListeners;

}