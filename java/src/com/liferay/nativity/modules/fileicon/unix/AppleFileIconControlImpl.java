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

package com.liferay.nativity.modules.fileicon.unix;

import java.util.Map;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.fileicon.FileIconControlCallback;

/**
 * @author Dennis Ju
 */
public class AppleFileIconControlImpl extends UnixFileIconControlBaseImpl {

	public AppleFileIconControlImpl(
		NativityControl nativityControl,
		FileIconControlCallback fileIconControlCallback) {

		super(nativityControl, fileIconControlCallback);
	}

	@Override
	public void disableFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.ENABLE_FILE_ICONS_WITH_CALLBACK, Boolean.FALSE);

		nativityControl.sendMessage(message);
	}

	@Override
	public void enableFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.ENABLE_FILE_ICONS_WITH_CALLBACK, Boolean.TRUE);

		nativityControl.sendMessage(message);
	}

	public void removeFileIcon(String path) {
		NativityMessage message = new NativityMessage(
			Constants.REPAINT_ALL_ICONS, "");

		nativityControl.sendMessage(message);
	}

	public void removeFileIcons(String[] paths) {
		NativityMessage message = new NativityMessage(
				Constants.REPAINT_ALL_ICONS, "");

		nativityControl.sendMessage(message);
	}

	public void setFileIcon(String path, int iconId) {
		NativityMessage message = new NativityMessage(
				Constants.REPAINT_ALL_ICONS, "");

		nativityControl.sendMessage(message);
	}

	public void setFileIcons(Map<String, Integer> fileIconsMap) {
		NativityMessage message = new NativityMessage(
				Constants.REPAINT_ALL_ICONS, "");

		nativityControl.sendMessage(message);
	}
}