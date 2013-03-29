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
public class ContextMenuControlFactory {

	public static ContextMenuControl getContextMenuControl(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		if (_contextMenuControl == null) {
			ContextMenuControlFactory contextMenuControlFactory =
				new ContextMenuControlFactory(
					nativityControl, contextMenuControlCallback);

			if (OSDetector.isApple()) {
				_contextMenuControl =
					contextMenuControlFactory.createAppleContextMenuControl();
			}
			else if (OSDetector.isWindows()) {
				_contextMenuControl =
					contextMenuControlFactory.createWindowsContextMenuControl();
			}
		}

		return _contextMenuControl;
	}

	protected ContextMenuControl createAppleContextMenuControl() {
		return new AppleContextMenuControlImpl(
			_nativityControl, _contextMenuControlCallback);
	}

	protected ContextMenuControl createWindowsContextMenuControl() {
		return new WindowsContextMenuControlImpl(
			_nativityControl, _contextMenuControlCallback);
	}

	private ContextMenuControlFactory(
		NativityControl nativityControl,
		ContextMenuControlCallback contextMenuControlCallback) {

		_nativityControl = nativityControl;
		_contextMenuControlCallback = contextMenuControlCallback;
	}

	private static ContextMenuControl _contextMenuControl;

	private ContextMenuControlCallback _contextMenuControlCallback;
	private NativityControl _nativityControl;

}