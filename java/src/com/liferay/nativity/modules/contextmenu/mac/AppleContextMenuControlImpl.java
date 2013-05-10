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

package com.liferay.nativity.modules.contextmenu.mac;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.MessageListener;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.contextmenu.ContextMenuControl;
import com.liferay.nativity.modules.contextmenu.ContextMenuControlCallback;
import com.liferay.nativity.modules.contextmenu.model.ContextMenuItem;

import java.util.List;
import java.util.Map;

/**
 * @author Dennis Ju
 */
public class AppleContextMenuControlImpl extends ContextMenuControl {

	public AppleContextMenuControlImpl(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		super(nativityControl, contextMenuControlCallback);

		MessageListener menuQueryMessageListener = new MessageListener(
			Constants.MENU_QUERY) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				List<String> files = (List<String>)message.getValue();

				String[] currentFilesArray = (String[])files.toArray(
					new String[files.size()]);

				List<ContextMenuItem> items = getMenuItem(currentFilesArray);

				return new NativityMessage(Constants.MENU_ITEMS, items);
			}
		};

		nativityControl.registerMessageListener(menuQueryMessageListener);

		MessageListener menuExecMessageListener = new MessageListener(
			Constants.MENU_EXEC) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				Map<String, Object> map =
					(Map<String, Object>)message.getValue();

				String uuid = (String)map.get("uuid");

				List<String> files = (List<String>)map.get("files");

				String[] filesArray = (String[])files.toArray(
					new String[files.size()]);

				fireAction(uuid, filesArray);

				return null;
			}
		};

		nativityControl.registerMessageListener(menuExecMessageListener);
	}

}