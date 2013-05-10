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

		MessageListener getMenuListMessageListener = new MessageListener(
			Constants.GET_CONTEXT_MENU_LIST) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				@SuppressWarnings("unchecked")
				List<String> args = (List<String>)message.getValue();

				List<ContextMenuItem> menuItems = getMenuItem(
					args.toArray(new String[args.size()]));

				return new NativityMessage(
					Constants.GET_CONTEXT_MENU_LIST, menuItems);
			}
		};

		nativityControl.registerMessageListener(getMenuListMessageListener);

		MessageListener performActionMessageListener = new MessageListener(
			Constants.PERFORM_ACTION) {

			public NativityMessage onMessage(NativityMessage message) {
				String value = message.getValue().toString();

				_logger.debug("Nativity Message Value {}", value);

				try {
					ContextMenuAction contextMenuAction =
						_objectMapper.readValue(value, ContextMenuAction.class);

					fireAction(
						String.valueOf(contextMenuAction.getId()),
						contextMenuAction.getFiles());
				}
				catch (Exception e) {
					_logger.error(e.getMessage(), e);
				}

				return null;
			}
		};

		nativityControl.registerMessageListener(performActionMessageListener);
	}

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsContextMenuControlImpl.class.getName());

	private static ObjectMapper _objectMapper =
		new ObjectMapper().configure(
			JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

	class ContextMenuAction {

		public int getId() {
			return _id;
		}

		public String[] getFiles() {
			return _files;
		}

		public void setId(int id) {
			_id = id;
		}

		public void setFiles(String[] files) {
			_files = files;
		}

		private int _id;
		private String[] _files;

	}

}