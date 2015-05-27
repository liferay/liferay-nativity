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

package com.liferay.nativity.modules.contextmenu.unix;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.contextmenu.ContextMenuControlCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis Ju
 */
public class AppleContextMenuControlImpl
	extends UnixContextMenuControlBaseImpl {

	public AppleContextMenuControlImpl(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		super(nativityControl, contextMenuControlCallback);
	}

	@Override
	public void registerIcon(String path, String iconId) {
		Map<String, String> map = new HashMap<String, String>();

		map.put(Constants.PATH, path);
		map.put(Constants.ICON_ID, iconId);

		NativityMessage message = new NativityMessage(
			Constants.REGISTER_CONTEXT_MENU_ICON, map);

		nativityControl.sendMessage(message);
	}

}