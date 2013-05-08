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

package com.liferay.nativity.modules.fileicon.mac;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.fileicon.FileIconControlBase;
import com.liferay.nativity.modules.fileicon.FileIconControlCallback;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;

/**
 * @author Dennis Ju
 */
public class AppleFileIconControlImpl extends FileIconControlBase {

	/**
	 * @param nativityControl
	 */
	public AppleFileIconControlImpl(
		NativityControl nativityControl,
		FileIconControlCallback fileIconControlCallback) {

		super(nativityControl, fileIconControlCallback);
	}

	@Override
	public void disableFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.ENABLE_FILE_ICONS, Boolean.FALSE);

		nativityControl.sendMessage(message);
	}

	@Override
	public void enableFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.ENABLE_FILE_ICONS, Boolean.TRUE);

		nativityControl.sendMessage(message);
	}

	@Override
	public int registerIcon(String path) {
		NativityMessage message = new NativityMessage(
			Constants.REGISTER_ICON, path);

		String reply = nativityControl.sendMessage(message);

		if ((reply == null) || reply.isEmpty()) {
			return -1;
		}

		return Integer.parseInt(reply);
	}

	@Override
	public void removeAllFileIcons() {
	}

	public void removeFileIcon(String path) {
		NativityMessage message = new NativityMessage(
			Constants.REMOVE_FILE_ICONS, new String[] { path });

		nativityControl.sendMessage(message);
	}

	public void removeFileIcons(String[] paths) {
		NativityMessage message = new NativityMessage(
			Constants.REMOVE_FILE_ICONS, paths);

		nativityControl.sendMessage(message);
	}

	public void setFileIcon(String path, int iconId) {
		Map<String, Integer> map = new HashMap<String, Integer>(1);

		map.put(path, iconId);

		NativityMessage message = new NativityMessage(
			Constants.SET_FILE_ICONS, map);

		nativityControl.sendMessage(message);
	}

	public void setFileIcons(Map<String, Integer> fileIconsMap) {
		Map<String, Integer> map = new HashMap<String, Integer>(
			_messageBufferSize);

		int i = 0;

		for (Entry<String, Integer> entry : fileIconsMap.entrySet()) {
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
	public void setFilterPath(String folder) {
		NativityMessage message = new NativityMessage(
			Constants.SET_FILTER_PATH, folder);

		nativityControl.sendMessage(message);
	}

	@Override
	public void unregisterIcon(int id) {
		NativityMessage message = new NativityMessage(
			Constants.UNREGISTER_ICON, id);

		nativityControl.sendMessage(message);
	}

	private static int _messageBufferSize = 500;

}