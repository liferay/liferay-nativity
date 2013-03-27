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
import com.liferay.nativity.plugincontrol.MessageListener;
import com.liferay.nativity.plugincontrol.NativityMessage;
import com.liferay.nativity.plugincontrol.NativityPluginControl;

import java.util.List;
import java.util.Map;

/**
 * @author Dennis Ju
 */
public abstract class WindowsFileIconControlImpl extends FileIconControlBase {

	public WindowsFileIconControlImpl(NativityPluginControl pluginControl) {
		super(pluginControl);

		MessageListener getFileOverlayIdMessageListener = new MessageListener(
			Constants.GET_FILE_OVERLAY_ID) {

			@Override
			public NativityMessage onMessageReceived(NativityMessage message) {
				List<String> args = (List<String>)message.getValue();

				if (args.size() > 0) {
					String arg1 = args.get(0);

					int icon = getIconForFile(arg1);

					return new NativityMessage(
						Constants.GET_FILE_OVERLAY_ID, icon);
				}
				else {
					return null;
				}
			}
		};

		pluginControl.registerMessageListener(getFileOverlayIdMessageListener);
	}

	@Override
	public void disableFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.ENABLE_FILE_ICONS, String.valueOf(false));

		pluginControl.sendMessage(message);
	}

	@Override
	public void enableFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.ENABLE_FILE_ICONS, String.valueOf(true));

		pluginControl.sendMessage(message);
	}

	@Override
	public int registerIcon(String path) {
		return 0;
	}

	@Override
	public void removeAllFileIcons() {
		//TODO
	}

	@Override
	public void removeFileIcon(String fileName) {
		NativityMessage message = new NativityMessage(
			Constants.CLEAR_FILE_ICON, fileName);

		pluginControl.sendMessage(message);
	}

	@Override
	public void removeFileIcons(String[] fileNames) {
		NativityMessage message = new NativityMessage(
			Constants.CLEAR_FILE_ICON, fileNames);

		pluginControl.sendMessage(message);
	}

	@Override
	public void setIconForFile(String fileName, int iconId) {
		NativityMessage message = new NativityMessage(
			Constants.UPDATE_FILE_ICON, fileName);

		pluginControl.sendMessage(message);
	}

	@Override
	public void setIconsForFiles(Map<String, Integer> fileIconsMap) {
		NativityMessage message = new NativityMessage(
			Constants.UPDATE_FILE_ICON, fileIconsMap.keySet());

		pluginControl.sendMessage(message);
	}

	@Override
	public void unregisterIcon(int id) {
	}

}