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

		_contextMenuItems = new ArrayList<ContextMenuItem>();
	}

	public void fireAction(String uuid, String[] paths) {
		for (ContextMenuItem contextMenuItem : _contextMenuItems) {
			if (contextMenuItem.getUuid().equals(uuid)) {
				_logger.debug("Firing action uuid: {}, for: {}", uuid, paths);

				contextMenuItem.fireActions(paths);

				break;
			}
		}
	}

	@Override
	public List<ContextMenuItem> getMenuItem(String[] paths) {
		List<ContextMenuItem> contextMenuItems =
			contextMenuControlCallback.getMenuItem(paths);

		_contextMenuItems.clear();

		if (contextMenuItems == null) {
			return null;
		}

		for (ContextMenuItem contextMenuItem : contextMenuItems) {
			_contextMenuItems.addAll(contextMenuItem.getAllContextMenuItems());
		}

		return contextMenuItems;
	}

	protected ContextMenuControlCallback contextMenuControlCallback;
	protected NativityControl nativityControl;

	private static Logger _logger = LoggerFactory.getLogger(
		ContextMenuControl.class.getName());

	private List<ContextMenuItem> _contextMenuItems;

}