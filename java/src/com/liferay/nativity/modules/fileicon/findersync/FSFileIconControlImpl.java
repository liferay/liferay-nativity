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

package com.liferay.nativity.modules.fileicon.findersync;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.MessageListener;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.fileicon.FileIconControl;
import com.liferay.nativity.modules.fileicon.FileIconControlCallback;
import com.liferay.nativity.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dennis Ju
 */
public class FSFileIconControlImpl implements FileIconControl {

	public FSFileIconControlImpl(
		FileIconControlCallback fileIconControlCallback,
		NativityControl nativityControl) {

		this.fileIconControlCallback = fileIconControlCallback;
		this.nativityControl = nativityControl;

		MessageListener messageListener = new MessageListener(
			Constants.GET_FILE_ICON_ID) {

			@Override
			public NativityMessage onMessage(NativityMessage message) {
				String filePath = null;

				if (message.getValue() instanceof List<?>) {
					List<?> args = (List<?>)message.getValue();

					if (args.size() > 0) {
						filePath = args.get(0).toString();
					}
				}
				else if (message.getValue() != null) {
					filePath = message.getValue().toString();
				}

				if (filePath == null) {
					return null;
				}

				int iconId = getIconForFile(filePath);

				Map<String, Integer> map = new HashMap<String, Integer>(1);

				map.put(filePath, iconId);

				return new NativityMessage(Constants.SET_FILE_ICONS, map);
			}
		};

		nativityControl.registerMessageListener(messageListener);
	}

	@Override
	public void disableFileIcons() {
	}

	@Override
	public void enableFileIcons() {
	}

	@Override
	public int getIconForFile(String path) {
		path = StringUtil.normalize(path);

		return fileIconControlCallback.getIconForFile(path);
	}

	@Override
	public void refreshIcons() {
		refreshIcons(null);
	}

	@Override
	public void refreshIcons(String[] paths) {
		NativityMessage message = new NativityMessage(
			Constants.REFRESH_ICONS, "");

		nativityControl.sendMessage(message);
	}

	@Override
	public void refreshWindow(String path) {
	}

	@Override
	public int registerIcon(String path) {
		return -1;
	}

	@Override
	public void registerIconWithId(String path, String label, String iconId) {
		Map<String, String> map = new HashMap<String, String>(3);

		map.put(Constants.PATH, path);
		map.put(Constants.LABEL, label);
		map.put(Constants.ICON_ID, iconId);

		NativityMessage message = new NativityMessage(
			Constants.REGISTER_ICON_WITH_ID, map);

		nativityControl.sendMessage(message);
	}

	@Override
	public void removeAllFileIcons() {
	}

	@Override
	public void removeFileIcon(String path) {
	}

	@Override
	public void removeFileIcons(String[] paths) {
	}

	@Override
	public void setFileIcon(String path, int iconId) {
		Map<String, Integer> map = new HashMap<String, Integer>(1);

		map.put(path, iconId);

		setFileIcons(map);
	}

	@Override
	public void setFileIcons(Map<String, Integer> fileIconsMap) {
		Map<String, Integer> map = new HashMap<String, Integer>(
			_messageBufferSize);

		int i = 0;

		for (Map.Entry<String, Integer> entry : fileIconsMap.entrySet()) {
			map.put(entry.getKey(), entry.getValue());

			i++;

			if (i == _messageBufferSize) {
				NativityMessage message = new NativityMessage(
					Constants.SET_FILE_ICONS, map);

				nativityControl.sendMessage(message);

				map.clear();

				i = 0;
			}
		}

		if (i > 0) {
			NativityMessage message = new NativityMessage(
				Constants.SET_FILE_ICONS, map);

			nativityControl.sendMessage(message);
		}
	}

	@Override
	public void unregisterIcon(int id) {
	}

	protected FileIconControlCallback fileIconControlCallback;
	protected NativityControl nativityControl;

	private static int _messageBufferSize = 500;

}