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

package com.liferay.nativity.test;

import com.liferay.nativity.modules.contextmenu.ContextMenuControlCallback;
import com.liferay.nativity.modules.contextmenu.model.ContextMenuAction;
import com.liferay.nativity.modules.contextmenu.model.ContextMenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class TestContextMenuControlCallback
	implements ContextMenuControlCallback {

	@Override
	public List<ContextMenuItem> getContextMenuItems(String[] paths) {
		_logger.debug("getMenuItems {}", paths);

		int count = _random.nextInt(20) + 3;

		ContextMenuItem parentMenuItem = new ContextMenuItem("Parent Menu");

		for (int i = 0; i < count; i++) {
			ContextMenuItem childMenu = new ContextMenuItem(
				"Menu " + i, parentMenuItem);

			childMenu.setHelpText("Help " + i);

			ContextMenuAction action = new ContextMenuAction() {
				@Override
				public void onSelection(String[] paths) {
					_logger.info("item clicked");
				}
			};

			childMenu.setContextMenuAction(action);

			if ((i % 2) == 1) {
				childMenu.setEnabled(false);
			}

			if ((i > 0) && ((i % 3) == 0)) {
				parentMenuItem.addSeparator();
			}
		}

		List<ContextMenuItem> contextMenuItems =
			new ArrayList<ContextMenuItem>();

		contextMenuItems.add(parentMenuItem);

		return contextMenuItems;
	}

	private static Logger _logger = LoggerFactory.getLogger(
		TestContextMenuControlCallback.class.getName());

	private Random _random = new Random();

}