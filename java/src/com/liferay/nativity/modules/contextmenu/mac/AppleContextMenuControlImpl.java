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
import com.liferay.nativity.modules.contextmenu.ContextMenuControlBase;
import com.liferay.nativity.modules.contextmenu.ContextMenuControlCallback;

import java.util.List;

/**
 * @author Dennis Ju
 */
public class AppleContextMenuControlImpl extends ContextMenuControlBase {

	public AppleContextMenuControlImpl(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		super(nativityControl, contextMenuControlCallback);

		MessageListener menuQueryMessageListener = new MessageListener(
			Constants.MENU_QUERY) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				_currentFiles = (List<String>)message.getValue();

				String[] currentFilesArray =
					(String[])_currentFiles.toArray(
						new String[_currentFiles.size()]);

				String[] items = getMenuItems(currentFilesArray);

				return new NativityMessage(Constants.MENU_ITEMS, items);
			}
		};

		nativityControl.registerMessageListener(menuQueryMessageListener);

		MessageListener menuExecMessageListener = new MessageListener(
			Constants.MENU_EXEC) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				String menuText = (String)message.getValue();

				String[] currentFiles =
					(String[])_currentFiles.toArray(
						new String[_currentFiles.size()]);

				fireMenuItemListeners(menuText, currentFiles);

				return null;
			}
		};

		nativityControl.registerMessageListener(menuExecMessageListener);
	}

	@Override
	public void setContextMenuTitle(String title) {
		NativityMessage message = new NativityMessage(
			Constants.SET_MENU_TITLE, title);

		nativityControl.sendMessage(message);
	}

	private List<String> _currentFiles;

}