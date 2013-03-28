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

	/**
	 * Disables file icon overlays
	 */
	public void disableFileIcons() {
		_fileIconControlBaseDelegate.disableFileIcons();
	}

	/**
	 * Enables file icon overlays
	 */
	public void enableFileIcons() {
		_fileIconControlBaseDelegate.enableFileIcons();
	}

	/**
	 * Windows only
	 *
	 * Called by the native service to request the icon overlay id for the
	 * specified file
	 *
	 * @param file path requesting the overlay icon
	 *
	 * @return icon overlay id
	 */
	public abstract int getIconForFile(String path);

	/**
	 * Mac only
	 *
	 * Register an overlay icon
	 *
	 * @param path to the overlay icon
	 *
	 * @return overlay icon id
	 */
	public int registerIcon(String path) {
		return _fileIconControlBaseDelegate.registerIcon(path);
	}

	/**
	 * Mac only
	 *
	 * Removes all file icon overlays
	 */
	public void removeAllFileIcons() {
		_fileIconControlBaseDelegate.removeAllFileIcons();
	}

	/**
	 * Removes file icon overlay
	 *
	 * @param file path to remove the file icon overlay
	 */
	public void removeFileIcon(String path) {
		_fileIconControlBaseDelegate.removeFileIcon(path);
	}

	/**
	 * Removes file icon overlays
	 *
	 * @param file paths to remove file icon overlays
	 */
	public void removeFileIcons(String[] paths) {
		_fileIconControlBaseDelegate.removeFileIcons(paths);
	}

	/**
	 * Mac only
	 *
	 * Set file icon overlay
	 *
	 * @param file path to set file icon overlays
	 *
	 * @param id of file icon overlay
	 */
	public void setFileIcon(String path, int iconId) {
		_fileIconControlBaseDelegate.setFileIcon(path, iconId);
	}

	/**
	 * Mac only
	 *
	 * Set file icon overlays
	 *
	 * @param map containing paths and file icon overlay ids
	 */
	public void setFileIcons(Map<String, Integer> fileIconsMap) {
		_fileIconControlBaseDelegate.setFileIcons(fileIconsMap);
	}

	/**
	 * Mac only
	 *
	 * Unregister an overlay icon
	 *
	 * @param overlay icon id
	 */
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