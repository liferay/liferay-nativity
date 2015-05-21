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

package com.liferay.nativity.modules.contextmenu;

import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.modules.contextmenu.fs.FSContextMenuControlImpl;
import com.liferay.nativity.modules.contextmenu.unix.AppleContextMenuControlImpl;
import com.liferay.nativity.modules.contextmenu.unix.LinuxContextMenuControlImpl;
import com.liferay.nativity.modules.contextmenu.win.WindowsContextMenuControlImpl;
import com.liferay.nativity.util.OSDetector;

/**
 * @author Dennis Ju
 */
public class ContextMenuControlUtil {

	public static ContextMenuControl getContextMenuControl(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		ContextMenuControlUtil contextMenuControlUtil =
			new ContextMenuControlUtil(
				nativityControl, contextMenuControlCallback);

		if (OSDetector.isApple()) {
			if (OSDetector.isMinimumAppleVersion(
					OSDetector.MAC_YOSEMITE_10_10)) {

				return contextMenuControlUtil.createFSContextMenuControl();
			}
			else {
				return contextMenuControlUtil.createAppleContextMenuControl();
			}
		}
		else if (OSDetector.isWindows()) {
			return contextMenuControlUtil.createWindowsContextMenuControl();
		}
		else if (OSDetector.isLinux()) {
			return contextMenuControlUtil.createLinuxContextMenuControl();
		}

		return null;
	}

	protected ContextMenuControl createAppleContextMenuControl() {
		return new AppleContextMenuControlImpl(
			_nativityControl, _contextMenuControlCallback);
	}

	protected ContextMenuControl createFSContextMenuControl() {
		return new FSContextMenuControlImpl(
			_nativityControl, _contextMenuControlCallback);
	}

	protected ContextMenuControl createLinuxContextMenuControl() {
		return new LinuxContextMenuControlImpl(
			_nativityControl, _contextMenuControlCallback);
	}

	protected ContextMenuControl createWindowsContextMenuControl() {
		return new WindowsContextMenuControlImpl(
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