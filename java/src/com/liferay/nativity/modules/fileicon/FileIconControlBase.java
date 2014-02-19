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

package com.liferay.nativity.modules.fileicon;

import java.util.List;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.MessageListener;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;

/**
* @author Michael Young
*/
public abstract class FileIconControlBase implements FileIconControl {

	public FileIconControlBase(
			NativityControl nativityControl,
			FileIconControlCallback fileIconControlCallback) {

		this.nativityControl = nativityControl;
		this.fileIconControlCallback = fileIconControlCallback;

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
	public int getIconForFile(String path) {
		return fileIconControlCallback.getIconForFile(path);
	}

	protected FileIconControlCallback fileIconControlCallback;
	protected NativityControl nativityControl;

}