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
import com.liferay.nativity.modules.fileicon.FileIconControlBase;
import com.liferay.nativity.plugincontrol.NativityMessage;
import com.liferay.nativity.plugincontrol.NativityPluginControl;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;

/**
 * @author Dennis Ju
 */
public abstract class AppleFileIconControlImpl extends FileIconControlBase {

	/**
	 * @param pluginControl
	 */
	public AppleFileIconControlImpl(NativityPluginControl pluginControl) {
		super(pluginControl);

		// TODO Auto-generated constructor stub

	}

	@Override
	public void disableFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.ENABLE_OVERLAYS, Boolean.FALSE);

		pluginControl.sendMessage(message);
	}

	@Override
	public void enableFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.ENABLE_OVERLAYS, Boolean.TRUE);

		pluginControl.sendMessage(message);
	}

	@Override
	public int registerIcon(String path) {
		NativityMessage message = new NativityMessage(
			Constants.REGISTER_ICON, path);

		String reply = pluginControl.sendMessage(message);

		return Integer.parseInt(reply);
	}

	/* (non-Javadoc)
	 * @see com.liferay.nativity.modules.fileicon.FileIconControlBase#removeAllFileIcons()
	 */
	@Override
	public void removeAllFileIcons() {

		// TODO Auto-generated method stub

	}

	public void removeFileIcon(String fileName) {
		NativityMessage message = new NativityMessage(
			Constants.REMOVE_FILE_ICONS, new String[] { fileName });

		pluginControl.sendMessage(message);
	}

	public void removeFileIcons(String[] fileNames) {
		NativityMessage message = new NativityMessage(
			Constants.REMOVE_FILE_ICONS, fileNames);

		pluginControl.sendMessage(message);
	}

	public void setIconForFile(String fileName, int iconId) {
		Map<String, Integer> map = new HashMap<String, Integer>(1);

		map.put(fileName, iconId);

		NativityMessage message = new NativityMessage(
			Constants.SET_FILE_ICONS, map);

		pluginControl.sendMessage(message);
	}

	public void setIconsForFiles(Map<String, Integer> fileIconsMap) {
		Map<String, Integer> map = new HashMap<String, Integer>(
			_messageBufferSize);

		int i = 0;

		for (Entry<String, Integer> entry : fileIconsMap.entrySet()) {
			map.put(entry.getKey(), entry.getValue());

			i++;

			if (i == _messageBufferSize) {
				NativityMessage message = new NativityMessage(
					Constants.SET_FILE_ICONS, map);

				pluginControl.sendMessage(message);

				map.clear();
				i = 0;
			}
		}

		if (i > 0) {
			NativityMessage message = new NativityMessage(
				Constants.SET_FILE_ICONS, map);

			pluginControl.sendMessage(message);
		}
	}

	@Override
	public void unregisterIcon(int id) {
		NativityMessage message = new NativityMessage(
			Constants.UNREGISTER_ICON, id);

		pluginControl.sendMessage(message);
	}

	private static int _messageBufferSize = 500;

}