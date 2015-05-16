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

package com.liferay.nativity.modules.contextmenu.unix;

import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.modules.contextmenu.ContextMenuControlCallback;
import com.liferay.nativity.modules.contextmenu.model.ContextMenuItem;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dennis Ju
 */
public class LinuxContextMenuControlImpl
	extends UnixContextMenuControlBaseImpl {

	public LinuxContextMenuControlImpl(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		super(nativityControl, contextMenuControlCallback);
	}

	@Override
	public List<ContextMenuItem> getContextMenuItems(String[] paths) {
		List<ContextMenuItem> newContextMenuItems =
			contextMenuControlCallback.getContextMenuItems(paths);

		if (!Arrays.equals(paths, _currentPaths)) {
			contextMenuItems.clear();

			_currentPaths = paths;
		}

		if (newContextMenuItems == null) {
			return null;
		}

		for (ContextMenuItem contextMenuItem : newContextMenuItems) {
			contextMenuItems.addAll(contextMenuItem.getAllContextMenuItems());
		}

		return newContextMenuItems;
	}

	private String[] _currentPaths;

}