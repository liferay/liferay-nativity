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

import java.util.Map;

/**
 * @author Dennis Ju
 */
public interface FileIconControl extends FileIconControlCallback {

	/**
	 * Disables file icon overlays
	 */
	public void disableFileIcons();

	/**
	 * Enables file icon overlays
	 */
	public void enableFileIcons();

	/**
	 * Windows, Mac Finder Sync, and Mac Injector only
	 *
	 * Causes Explorer or Finder to refresh the file icons. This must be called
	 * when new icons are set to refresh stale windows.
	 *
	 * @param paths The array of file paths to refresh. This can be a null or
	 * empty set for Mac.
	 */
	public void refreshIcons(String[] paths);

	/**
	 * Windows only
	 *
	 * Causes Windows to refresh any open Explorer windows that are children of
	 * the given path; equivalent to pressing F5 on an Explorer window. This is
	 * useful for refreshing all icons and is significantly cheaper than
	 * blindly calling refreshIcons(paths) for many files.
	 *
	 * @param path The file path to refresh.
	 */
	public void refreshWindow(String path);

	/**
	 * Linux, Mac Finder Sync, and Mac Injector only
	 *
	 * Register an overlay icon with label and iconId
	 *
	 * @param path The path of the overlay icon to register
	 * @param label The label to show when icons are unavailable. Only used in
	 * Mac Finder Sync.
	 * @param iconId The unique iconId identifying this icon
	 */
	public void registerIconWithId(String path, String label, String iconId);

	/**
	 * Linux and Mac Finder Sync only
	 * Deprecated for Mac as of 1.2
	 *
	 * Set file icon overlay
	 *
	 * @param path The file path to set file icon overlays
	 *
	 * @param iconId The id of file icon overlay. A value of -1 will remove the
	 * overlay (same as calling removeFileIcon).
	 */
	public void setFileIcon(String path, int iconId);

	/**
	 * Linux and Mac Finder Sync only
	 * Deprecated for Mac as of 1.2
	 *
	 * Set file icon overlays
	 *
	 * @param fileIconsMap The map containing the paths and associated file
	 * icon overlay ids
	 */
	public void setFileIcons(Map<String, Integer> fileIconsMap);

}