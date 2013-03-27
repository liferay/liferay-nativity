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
import com.liferay.nativity.plugincontrol.NativityPluginControl;
import com.liferay.nativity.util.OSDetector;

import java.util.Map;

/**
 * @author Dennis Ju
 */
public abstract class FileIconControl {

	public FileIconControl(NativityPluginControl pluginControl) {
		_pluginControl = pluginControl;

		if (_fileIconControlBaseDelegate == null) {
			if (OSDetector.isApple()) {
				_fileIconControlBaseDelegate = createAppleFileIconControlBase();
			}
			else if (OSDetector.isWindows()) {
				_fileIconControlBaseDelegate =
					createWindowsFileIconControlBase();
			}
		}
	}

	public void disableFileIcons() {
		_fileIconControlBaseDelegate.disableFileIcons();
	}

	public void enableFileIcons() {
		_fileIconControlBaseDelegate.enableFileIcons();
	}

	// Windows only

	public abstract int getIconForFile(String path);

	public int registerIcon(String path) {
		return _fileIconControlBaseDelegate.registerIcon(path);
	}

	public void removeAllFileIcons() {
		_fileIconControlBaseDelegate.removeAllFileIcons();
	}

	public void removeFileIcon(String fileName) {
		_fileIconControlBaseDelegate.removeFileIcon(fileName);
	}

	public void removeFileIcons(String[] fileNames) {
		_fileIconControlBaseDelegate.removeFileIcons(fileNames);
	}

	public void setIconForFile(String fileName, int iconId) {
		_fileIconControlBaseDelegate.setIconForFile(fileName, iconId);
	}

	public void setIconsForFiles(Map<String, Integer> fileIconsMap) {
		_fileIconControlBaseDelegate.setIconsForFiles(fileIconsMap);
	}

	public void unregisterIcon(int id) {
		_fileIconControlBaseDelegate.unregisterIcon(id);
	}

	protected FileIconControlBase createAppleFileIconControlBase() {
		return new AppleFileIconControlImpl(_pluginControl) {
			@Override
			public int getIconForFile(String path) {
				return FileIconControl.this.getIconForFile(path);
			}
		};
	}

	protected FileIconControlBase createWindowsFileIconControlBase() {
		return new WindowsFileIconControlImpl(_pluginControl) {
			@Override
			public int getIconForFile(String path) {
				return FileIconControl.this.getIconForFile(path);
			}
		};
	}

	private FileIconControlBase _fileIconControlBaseDelegate;
	private NativityPluginControl _pluginControl;

}