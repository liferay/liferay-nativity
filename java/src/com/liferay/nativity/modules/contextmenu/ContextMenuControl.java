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

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.contextmenu.listeners.MenuItemListener;
import com.liferay.nativity.modules.contextmenu.mac.AppleContextMenuControlImpl;
import com.liferay.nativity.modules.contextmenu.win.WindowsContextMenuControlImpl;
import com.liferay.nativity.util.OSDetector;

/**
 * @author Dennis Ju
 */
public abstract class ContextMenuControl {

	public ContextMenuControl(NativityControl pluginControl) {
		_pluginControl = pluginControl;

		if (_contextMenuControlBaseDelegate == null) {
			if (OSDetector.isApple()) {
				_contextMenuControlBaseDelegate =
					createAppleContextMenuControlBase();
			}
			else if (OSDetector.isWindows()) {
				_contextMenuControlBaseDelegate =
					createWindowsContextMenuControlBase();
			}
		}
	}

	/**
	 * Adds a MenuItemListener to respond to menu item selections.
	 * Multiple listeners can be added.
	 *
	 * @param listener to respond to menu item selections
	 */
	public void addMenuItemListener(MenuItemListener menuItemListener) {
		_contextMenuControlBaseDelegate.addMenuItemListener(menuItemListener);
	}

	public abstract String[] getHelpItemsForMenus(String[] files);

	/**
	 * Called by the native service when the user requests a context menu popup
	 *
	 * @param the files selected for this context menu popup
	 *
	 * @return array of menu item titles to populate the context menu
	 */
	public abstract String[] getMenuItems(String[] paths);

	/**
	 * Removes all MenuItemListeners
	 */
	public void removeAllMenuItemListeners() {
		_contextMenuControlBaseDelegate.removeAllMenuItemListeners();
	}

	/**
	 * Removes a MenuItemListener
	 *
	 * @param the MenuItemListener to remove
	 */
	public void removeMenuItemListener(MenuItemListener menuItemListener) {
		_contextMenuControlBaseDelegate.removeMenuItemListener(
			menuItemListener);
	}

	/**
	 * Set title of root context menu item, all other items will be added as
	 * children of it
	 *
	 * @param title of context menu
	 */
	public void setContextMenuTitle(String title) {
		NativityMessage message = new NativityMessage(
			Constants.SET_MENU_TITLE, title);

		_pluginControl.sendMessage(message);
	}

	protected ContextMenuControlBase createAppleContextMenuControlBase() {
		return new AppleContextMenuControlImpl(_pluginControl) {

			@Override
			public String[] getMenuItems(String[] paths) {
				return ContextMenuControl.this.getMenuItems(paths);
			}

			@Override
			public String[] getHelpItemsForMenus(String[] paths) {
				return ContextMenuControl.this.getMenuItems(paths);
			}
		};
	}

	protected ContextMenuControlBase createWindowsContextMenuControlBase() {
		return new WindowsContextMenuControlImpl(_pluginControl) {

			@Override
			public String[] getMenuItems(String[] paths) {

				return ContextMenuControl.this.getMenuItems(paths);
			}

			@Override
			public String[] getHelpItemsForMenus(String[] paths) {
				return ContextMenuControl.this.getMenuItems(paths);
			}
		};
	}

	private ContextMenuControlBase _contextMenuControlBaseDelegate;
	private NativityControl _pluginControl;

}