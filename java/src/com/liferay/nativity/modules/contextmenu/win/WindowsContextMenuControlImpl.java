/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.nativity.modules.contextmenu.win;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.MessageListener;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.contextmenu.ContextMenuControl;
import com.liferay.nativity.modules.contextmenu.ContextMenuControlCallback;
import com.liferay.nativity.modules.contextmenu.model.ContextMenuItem;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class WindowsContextMenuControlImpl extends ContextMenuControl {

	public WindowsContextMenuControlImpl(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		super(nativityControl, contextMenuControlCallback);

		MessageListener getContextMenuItemsMessageListener =
			new MessageListener(Constants.GET_CONTEXT_MENU_ITEMS) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				@SuppressWarnings("unchecked")
				List<String> args = (List<String>)message.getValue();

				List<ContextMenuItem> contextMenuItems = getContextMenuItems(
					args.toArray(new String[args.size()]));

				return new NativityMessage(
					Constants.GET_CONTEXT_MENU_ITEMS, contextMenuItems);
			}
		};

		nativityControl.registerMessageListener(
			getContextMenuItemsMessageListener);

		MessageListener fireContextMenuActionMessageListener =
			new MessageListener(Constants.FIRE_CONTEXT_MENU_ACTION) {

			public NativityMessage onMessage(NativityMessage message) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map =
					(Map<String, Object>)message.getValue();

				String uuid = (String)map.get(Constants.UUID);

				@SuppressWarnings("unchecked")
				List<String> files = (List<String>)map.get(Constants.FILES);

				String[] filesArray = (String[])files.toArray(
					new String[files.size()]);

				fireContextMenuAction(uuid, filesArray);

				return null;
			}
		};

		nativityControl.registerMessageListener(
			fireContextMenuActionMessageListener);
	}

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsContextMenuControlImpl.class.getName());

	private static ObjectMapper _objectMapper = new ObjectMapper().configure(
		JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

}