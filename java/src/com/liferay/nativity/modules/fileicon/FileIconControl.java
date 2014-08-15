/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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
	 * Mac only
	 *
	 * Refresh icons. When using callbacks for setting the file icons, this
	 * must be called when new icons are set to refresh stale windows. Finder
	 * will not request new icons from the client unless there is interaction
	 * with the Finder window.
	 */
	public void refreshIcons();

	/**
	 * Mac and Linux only
	 *
	 * Register an overlay icon
	 *
	 * @param path The path of the overlay icon to register
	 *
	 * @return overlay icon id. -1 if the icon failed ot register.
	 */
	public int registerIcon(String path);

	/**
	 * Linux only
	 * Deprecated for Mac as of 1.2
	 *
	 * Removes all file icon overlays
	 */
	public void removeAllFileIcons();

	/**
	 * Linux only
	 * Deprecated for Mac as of 1.2
	 *
	 * Removes file icon overlay
	 *
	 * @param path The file path to remove the overlay
	 */
	public void removeFileIcon(String path);

	/**
	 * Linux only
	 * Deprecated for Mac as of 1.2
	 *
	 * Removes file icon overlays
	 *
	 * @param paths The file paths to remove file icon overlays
	 */
	public void removeFileIcons(String[] paths);

	/**
	 * Linux only
	 * Deprecated for Mac as of 1.2
	 *
	 * Set file icon overlay
	 *
	 * @param path The file path to set file icon overlays
	 *
	 * @param iconId The id of file icon overlay. Value of -1 will remove the
	 * overlay (same as calling removeFileIcon).
	 */
	public void setFileIcon(String path, int iconId);

	/**
	 * Linux only
	 * Deprecated for Mac as of 1.2
	 *
	 * Set file icon overlays
	 *
	 * @param fileIconsMap The map containing the paths and associated file
	 * icon overlay ids
	 */
	public void setFileIcons(Map<String, Integer> fileIconsMap);

	/**
	 * Mac only
	 *
	 * Unregister an overlay icon
	 *
	 * @param id The id of the icon to unregister
	 */
	public void unregisterIcon(int id);

}