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

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.MessageListener;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.contextmenu.ContextMenuControl;
import com.liferay.nativity.modules.contextmenu.ContextMenuControlCallback;
import com.liferay.nativity.modules.contextmenu.model.ContextMenuItem;
import com.liferay.nativity.util.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Dennis Ju
 */
public abstract class UnixContextMenuControlBaseImpl
	extends ContextMenuControl {

	public UnixContextMenuControlBaseImpl(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		super(nativityControl, contextMenuControlCallback);

		MessageListener getContextMenuItemsMessageListener =
			new MessageListener(Constants.GET_CONTEXT_MENU_ITEMS) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				@SuppressWarnings("unchecked")
				List<String> files = (List<String>)message.getValue();

				String[] filesArray = files.toArray(new String[files.size()]);

				filesArray = StringUtil.normalize(filesArray);

				List<ContextMenuItem> contextMenuItems = getContextMenuItems(
					filesArray);

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
				@SuppressWarnings("unchecked")
				Map<String, Object> map =
					(Map<String, Object>)message.getValue();

				String uuid = (String)map.get(Constants.UUID);

				@SuppressWarnings("unchecked")
				List<String> files = (List<String>)map.get(Constants.FILES);

				String[] filesArray = files.toArray(new String[files.size()]);

				filesArray = StringUtil.normalize(filesArray);

				fireContextMenuAction(uuid, filesArray);

				return null;
			}
		};

		nativityControl.registerMessageListener(
			fireContextMenuActionMessageListener);
	}

	@Override
	public void registerIcon(String path, String iconId) {
	}

}