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

package com.liferay.nativity.modules.contextmenu.linux;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.MessageListener;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.contextmenu.ContextMenuControl;
import com.liferay.nativity.modules.contextmenu.ContextMenuControlCallback;
import com.liferay.nativity.modules.contextmenu.model.ContextMenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class LinuxContextMenuControlImpl extends ContextMenuControl {

	public LinuxContextMenuControlImpl(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		super(nativityControl, contextMenuControlCallback);

		MessageListener getContextMenuItemsMessageListener =
			new MessageListener(Constants.GET_CONTEXT_MENU_ITEMS) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				List<String> files = (List<String>)message.getValue();

				String[] currentFilesArray = (String[])files.toArray(
					new String[files.size()]);

				List<ContextMenuItem> contextMenuItems = getContextMenuItems(
					currentFilesArray);

				return new NativityMessage(
					Constants.MENU_ITEMS, contextMenuItems);
			}
		};

		nativityControl.registerMessageListener(
			getContextMenuItemsMessageListener);

		MessageListener fireContextMenuActionMessageListener =
			new MessageListener(Constants.FIRE_CONTEXT_MENU_ACTION) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				Map<String, Object> map =
					(Map<String, Object>)message.getValue();

				String uuid = (String)map.get("uuid");

				List<String> files = (List<String>)map.get("files");

				String[] filesArray = (String[])files.toArray(
					new String[files.size()]);

				fireContextMenuAction(uuid, filesArray);

				return null;
			}
		};

		nativityControl.registerMessageListener(
			fireContextMenuActionMessageListener);
	}

	@Override
	public List<ContextMenuItem> getContextMenuItems(String[] paths) {
		List<ContextMenuItem> newContextMenuItems = 
			contextMenuControlCallback.getContextMenuItems(paths);

		if (!Arrays.equals(paths, currentPaths)) {
			contextMenuItems.clear();

			currentPaths = paths;
		}

		if (newContextMenuItems == null) {
			return null;
		}

		for (ContextMenuItem contextMenuItem : newContextMenuItems) {
			contextMenuItems.addAll(contextMenuItem.getAllContextMenuItems());
		}

		return newContextMenuItems;
	}
	
	private String[] currentPaths;

}