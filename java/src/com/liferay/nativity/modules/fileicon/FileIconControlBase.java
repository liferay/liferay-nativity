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

package com.liferay.nativity.modules.fileicon;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.MessageListener;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.util.StringUtil;

import java.util.List;

/**
* @author Michael Young
*/
public abstract class FileIconControlBase implements FileIconControl {

	public FileIconControlBase(
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

				int icon = getIconForFile(filePath);

				return new NativityMessage(Constants.GET_FILE_ICON_ID, icon);
			}
		};

		nativityControl.registerMessageListener(messageListener);
	}

	@Override
	public int getIconForFile(String path) {
		path = StringUtil.normalize(path);

		return fileIconControlCallback.getIconForFile(path);
	}

	protected FileIconControlCallback fileIconControlCallback;
	protected NativityControl nativityControl;

}