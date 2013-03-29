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

package com.liferay.nativity.modules.fileicon;

import com.liferay.nativity.modules.fileicon.mac.AppleFileIconControlImpl;
import com.liferay.nativity.modules.fileicon.win.WindowsFileIconControlImpl;
import com.liferay.nativity.plugincontrol.NativityControl;
import com.liferay.nativity.util.OSDetector;

/**
 * @author Dennis Ju
 */
public class FileIconControlFactory {

	public FileIconControlFactory(
		NativityControl nativityPluginControl,
		FileIconControlCallback fileIconControlCallback) {

		_nativityControl = nativityPluginControl;
		_fileIconControlCallback = fileIconControlCallback;
	}

	public FileIconControl getFileIconControl() {
		FileIconControl fileIconControl = null;
		if (OSDetector.isApple()) {
			fileIconControl = createAppleFileIconControl();
		}
		else if (OSDetector.isWindows()) {
			fileIconControl = createWindowsFileIconControl();
		}

		return fileIconControl;
	}

	protected FileIconControl createAppleFileIconControl() {
		return new AppleFileIconControlImpl(
			_nativityControl, _fileIconControlCallback);
	}

	protected FileIconControl createWindowsFileIconControl() {
		return new WindowsFileIconControlImpl(
			_nativityControl, _fileIconControlCallback);
	}

	private FileIconControlCallback _fileIconControlCallback;

	private NativityControl _nativityControl;

}