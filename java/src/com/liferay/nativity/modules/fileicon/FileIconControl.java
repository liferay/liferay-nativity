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
	 * Register an overlay icon
	 *
	 * @param path to the overlay icon
	 *
	 * @return overlay icon id. -1 if the icon failed ot register.
	 */
	public int registerIcon(String path);

	/**
	 * Mac only
	 *
	 * Removes all file icon overlays
	 */
	public void removeAllFileIcons();

	/**
	 * Removes file icon overlay
	 *
	 * @param file path to remove the file icon overlay
	 */
	public void removeFileIcon(String path);

	/**
	 * Removes file icon overlays
	 *
	 * @param file paths to remove file icon overlays
	 */
	public void removeFileIcons(String[] paths);

	/**
	 * Mac only
	 *
	 * Set file icon overlay
	 *
	 * @param file path to set file icon overlays
	 *
	 * @param id of file icon overlay
	 */
	public void setFileIcon(String path, int iconId);

	/**
	 * Mac only
	 *
	 * Set file icon overlays
	 *
	 * @param map containing paths and file icon overlay ids
	 */
	public void setFileIcons(Map<String, Integer> fileIconsMap);

	/**
	 * Optionally set the root folder filter path for requests made
	 * to the native service. For example, setting a value of "/test/folder"
	 * indicates that any requests for files that are not a child of
	 * "/test/folder" will be ignored. This can improve native performance.
	 *
	 * @param root folder path to filter by (inclusive)
	 */
	public abstract void setFilterPath(String folder);

	/**
	 * Mac only
	 *
	 * Unregister an overlay icon
	 *
	 * @param overlay icon id
	 */
	public void unregisterIcon(int id);

}