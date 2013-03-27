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
import com.liferay.nativity.modules.contextmenu.listeners.ExecuteMenuItemListener;
import com.liferay.nativity.modules.contextmenu.mac.AppleContextMenuControlImpl;
import com.liferay.nativity.modules.contextmenu.win.WindowsContextMenuControlImpl;
import com.liferay.nativity.plugincontrol.NativityMessage;
import com.liferay.nativity.plugincontrol.NativityPluginControl;
import com.liferay.nativity.util.OSDetector;

/**
 * @author Dennis Ju
 */
public abstract class ContextMenuControl {

	public ContextMenuControl(NativityPluginControl pluginControl) {
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

	public void addExecuteMenuItemListener(
		ExecuteMenuItemListener executeMenuItemListener) {

		_contextMenuControlBaseDelegate.addExecuteMenuItemListener(
			executeMenuItemListener);
	}

	/**
	 * Set the listener that triggers when a context menu opens
	 *
	 * @param a
	 *            MenuItemListener
	 */

	public abstract String[] getHelpItemsForMenus(String[] files);

	public abstract String[] getMenuItems(String[] paths);

	public void removeAllExecuteMenuItemListeners() {

		_contextMenuControlBaseDelegate.removeAllExecuteMenuItemListeners();
	}

	public void removeExecuteMenuItemListener(
		ExecuteMenuItemListener executeMenuItemListener) {

		_contextMenuControlBaseDelegate.removeExecuteMenuItemListener(
			executeMenuItemListener);
	}

	/**
	 * Set title of root context menu item, all other items will be added as
	 * children of it
	 *
	 * @param new title of item
	 */
	public void setContextMenuTitle(String title) {
		NativityMessage message = new NativityMessage(
			Constants.SET_MENU_TITLE, title);

		_pluginControl.sendMessage(message);
	}

	/**
	 * @return
	 */
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

	/**
	 * @return
	 */
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
	private NativityPluginControl _pluginControl;

}