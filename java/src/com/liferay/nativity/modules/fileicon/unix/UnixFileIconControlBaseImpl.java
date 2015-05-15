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

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.fileicon.FileIconControlBase;
import com.liferay.nativity.modules.fileicon.FileIconControlCallback;

/**
 * @author Dennis Ju
 */
public abstract class UnixFileIconControlBaseImpl extends FileIconControlBase {

	public UnixFileIconControlBaseImpl(
		NativityControl nativityControl,
		FileIconControlCallback fileIconControlCallback) {

		super(nativityControl, fileIconControlCallback);
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
	public void registerIconWithId(String path, String label, int id) {
	}

	@Override
	public void removeAllFileIcons() {
		NativityMessage message = new NativityMessage(
			Constants.REMOVE_ALL_FILE_ICONS, "");

		nativityControl.sendMessage(message);
	}

	@Override
	public void unregisterIcon(int id) {
		NativityMessage message = new NativityMessage(
			Constants.UNREGISTER_ICON, id);

		nativityControl.sendMessage(message);
	}

}