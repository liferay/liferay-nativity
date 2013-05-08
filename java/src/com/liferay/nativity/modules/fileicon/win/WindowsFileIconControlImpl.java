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
import com.liferay.nativity.control.MessageListener;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.fileicon.FileIconControlBase;
import com.liferay.nativity.modules.fileicon.FileIconControlCallback;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Dennis Ju
*/
public class WindowsFileIconControlImpl extends FileIconControlBase {

	public WindowsFileIconControlImpl(
		NativityControl nativityControl,
		FileIconControlCallback fileIconControlCallback) {

		super(nativityControl, fileIconControlCallback);

		MessageListener messageListener = new MessageListener(
				Constants.GET_FILE_ICON_ID) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				_logger.debug(message.getValue().toString());

				String filePath = null;

				if (message.getValue() instanceof List<?>) {

					List<?> args = (List<?>)message.getValue();

					if (args.size() > 0) {
						filePath = args.get(0).toString();
					}
				}
				else {
					filePath = message.getValue().toString();
				}

				int icon = getIconForFile(filePath);

				return new NativityMessage(Constants.GET_FILE_ICON_ID, icon);
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
	public void setFilterPath(String folder) {
		NativityMessage message = new NativityMessage(
			Constants.SET_FILTER_PATH, folder);

		nativityControl.sendMessage(message);
	}

	@Override
	public void unregisterIcon(int id) {
		return;
	}

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsFileIconControlImpl.class.getName());

}