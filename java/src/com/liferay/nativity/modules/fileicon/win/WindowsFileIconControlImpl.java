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

package com.liferay.nativity.modules.fileicon.win;

import com.liferay.nativity.Constants;
import com.liferay.nativity.modules.fileicon.FileIconControlBase;
import com.liferay.nativity.modules.fileicon.FileIconControlCallback;
import com.liferay.nativity.plugincontrol.MessageListener;
import com.liferay.nativity.plugincontrol.NativityControl;
import com.liferay.nativity.plugincontrol.NativityMessage;

import java.util.List;
import java.util.Map;

/**
 * @author Dennis Ju
 */
public class WindowsFileIconControlImpl extends FileIconControlBase {

	public WindowsFileIconControlImpl(
		NativityControl nativityControl,
		FileIconControlCallback fileIconControlCallback) {

		super(nativityControl, fileIconControlCallback);

		MessageListener messageListener = new MessageListener(
			Constants.GET_FILE_OVERLAY_ID) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				List<String> args = (List<String>)message.getValue();

				if (args.size() > 0) {
					String arg = args.get(0);

					int icon = getIconForFile(arg);

					return new NativityMessage(
						Constants.GET_FILE_OVERLAY_ID, icon);
				}
				else {
					return null;
				}
			}
		};

		nativityControl.registerMessageListener(messageListener);
	}

	@Override
	public void disableFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.ENABLE_FILE_ICONS, String.valueOf(false));

		nativityControl.sendMessage(message);
	}

	@Override
	public void enableFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.ENABLE_FILE_ICONS, String.valueOf(true));

		nativityControl.sendMessage(message);
	}

	@Override
	public int registerIcon(String path) {
		return 0;
	}

	@Override
	public void removeAllFileIcons() {
	}

	@Override
	public void removeFileIcon(String path) {
		NativityMessage message = new NativityMessage(
			Constants.CLEAR_FILE_ICON, path);

		nativityControl.sendMessage(message);
	}

	@Override
	public void removeFileIcons(String[] paths) {
		NativityMessage message = new NativityMessage(
			Constants.CLEAR_FILE_ICON, paths);

		nativityControl.sendMessage(message);
	}

	@Override
	public void setFileIcon(String path, int iconId) {
		NativityMessage message = new NativityMessage(
			Constants.UPDATE_FILE_ICON, path);

		nativityControl.sendMessage(message);
	}

	@Override
	public void setFileIcons(Map<String, Integer> fileIconsMap) {
		NativityMessage message = new NativityMessage(
			Constants.UPDATE_FILE_ICON, fileIconsMap.keySet());

		nativityControl.sendMessage(message);
	}

	@Override
	public void unregisterIcon(int id) {
		return;
	}

}