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

package com.liferay.nativity.plugincontrol;

import com.liferay.nativity.modules.contextmenu.ContextMenuControl;
import com.liferay.nativity.modules.fileicon.FileIconControl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis Ju
 */
public abstract class NativityPluginControl {

	public NativityPluginControl() {
		_commandMap = new HashMap<String, MessageListener>();
	}

	public abstract void connect();

	public abstract void disconnect();

	public NativityMessage fireMessageListener(NativityMessage message) {
		MessageListener messageListener = _commandMap.get(message.getCommand());

		if (messageListener == null) {
			return null;
		}

		return messageListener.onMessageReceived(message);
	}

	public void registerMessageListener(MessageListener messageListener) {
		_commandMap.put(messageListener.getCommand(), messageListener);
	}

	public abstract boolean running();

	public abstract String sendMessage(NativityMessage message);

	public abstract void setRootFolder(String folder);

	public abstract void setSystemFolder(String folder);

	public abstract boolean startPlugin(String path) throws Exception;

	protected ContextMenuControl contextMenuControl;

	protected FileIconControl fileIconControl;

	private Map<String, MessageListener> _commandMap;

}