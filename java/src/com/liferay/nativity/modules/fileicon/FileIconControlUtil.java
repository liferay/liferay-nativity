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

import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.modules.fileicon.mac.AppleFileIconControlImpl;
import com.liferay.nativity.modules.fileicon.win.WindowsFileIconControlImpl;
import com.liferay.nativity.util.OSDetector;

/**
 * @author Dennis Ju
 */
public class FileIconControlUtil {

	public static FileIconControl getFileIconControl(
		NativityControl nativityControl,
		FileIconControlCallback fileIconControlCallback) {

		if (_fileIconControl == null) {
			FileIconControlUtil fileIconControlUtil =
				new FileIconControlUtil(
					nativityControl, fileIconControlCallback);

			if (OSDetector.isApple()) {
				_fileIconControl =
					fileIconControlUtil.createAppleFileIconControl();
			}
			else if (OSDetector.isWindows()) {
				_fileIconControl =
					fileIconControlUtil.createWindowsFileIconControl();
			}
			else if (OSDetector.isLinux()) {
				_fileIconControl =
					fileIconControlUtil.createLinuxFileIconControl();
			}
		}

		return _fileIconControl;
	}

	protected FileIconControl createAppleFileIconControl() {
		return new AppleFileIconControlImpl(
			_nativityControl, _fileIconControlCallback);
	}

	protected FileIconControl createLinuxFileIconControl() {
		return new AppleFileIconControlImpl(
			_nativityControl, _fileIconControlCallback);
	}

	protected FileIconControl createWindowsFileIconControl() {
		return new WindowsFileIconControlImpl(
			_nativityControl, _fileIconControlCallback);
	}

	private FileIconControlUtil(
		NativityControl nativityControl,
		FileIconControlCallback fileIconControlCallback) {

		_nativityControl = nativityControl;
		_fileIconControlCallback = fileIconControlCallback;
	}

	private static FileIconControl _fileIconControl;

	private FileIconControlCallback _fileIconControlCallback;
	private NativityControl _nativityControl;

}