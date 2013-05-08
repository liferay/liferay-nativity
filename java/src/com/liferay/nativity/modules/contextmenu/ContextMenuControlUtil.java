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

package com.liferay.nativity.modules.contextmenu;

import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.modules.contextmenu.mac.AppleContextMenuControlImpl;
import com.liferay.nativity.modules.contextmenu.win.WindowsContextMenuControlImpl;
import com.liferay.nativity.util.OSDetector;

/**
 * @author Dennis Ju
 */
public class ContextMenuControlUtil {

	public static void registerContextMenuControlCallback(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		ContextMenuControlUtil contextMenuControlUtil =
			new ContextMenuControlUtil(
				nativityControl, contextMenuControlCallback);

		if (OSDetector.isApple()) {
			contextMenuControlUtil.createAppleContextMenuControl();
		}
		else if (OSDetector.isWindows()) {
			contextMenuControlUtil.createWindowsContextMenuControl();
		}
		else if (OSDetector.isLinux()) {
			contextMenuControlUtil.createLinuxContextMenuControl();
		}
	}

	protected void createAppleContextMenuControl() {
		new AppleContextMenuControlImpl(
			_nativityControl, _contextMenuControlCallback);
	}

	protected void createLinuxContextMenuControl() {
		new AppleContextMenuControlImpl(
			_nativityControl, _contextMenuControlCallback);
	}

	protected void createWindowsContextMenuControl() {
		new WindowsContextMenuControlImpl(
			_nativityControl, _contextMenuControlCallback);
	}

	private ContextMenuControlUtil(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		_nativityControl = nativityControl;
		_contextMenuControlCallback = contextMenuControlCallback;
	}

	private ContextMenuControlCallback _contextMenuControlCallback;
	private NativityControl _nativityControl;

}