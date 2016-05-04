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

import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.modules.contextmenu.model.ContextMenuItem;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public abstract class ContextMenuControl implements ContextMenuControlCallback {

	public ContextMenuControl(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		this.nativityControl = nativityControl;
		this.contextMenuControlCallback = contextMenuControlCallback;

		contextMenuItems = new ArrayList<ContextMenuItem>();
	}

	public void fireContextMenuAction(String uuid, String[] paths) {
		for (ContextMenuItem contextMenuItem : contextMenuItems) {
			if (contextMenuItem.getUuid().equals(uuid)) {
				_logger.trace("Firing action uuid: {} for: {}", uuid, paths);

				contextMenuItem.fireContextMenuAction(paths);

				break;
			}
		}
	}

	@Override
	public List<ContextMenuItem> getContextMenuItems(String[] paths) {
		List<ContextMenuItem> newContextMenuItems =
			contextMenuControlCallback.getContextMenuItems(paths);

		contextMenuItems.clear();

		if (newContextMenuItems == null) {
			return null;
		}

		for (ContextMenuItem contextMenuItem : newContextMenuItems) {
			contextMenuItems.addAll(contextMenuItem.getAllContextMenuItems());
		}

		return newContextMenuItems;
	}

	/**
	 * Mac Finder Sync and Mac Injector only
	 *
	 * Register a context menu icon
	 *
	 * @param path The path of the context menu icon to register
	 * @param iconId The unique icon id used for this image
	 *
	 * @deprecated as of 1.5. Set the path of the icon per ContextMenuItem
	 * instance via ContextMenuItem.setIconPath(iconPath)
	 */
	@Deprecated
	public abstract void registerIcon(String path, String iconId);

	protected ContextMenuControlCallback contextMenuControlCallback;
	protected List<ContextMenuItem> contextMenuItems;
	protected NativityControl nativityControl;

	private static Logger _logger = LoggerFactory.getLogger(
		ContextMenuControl.class.getName());

}