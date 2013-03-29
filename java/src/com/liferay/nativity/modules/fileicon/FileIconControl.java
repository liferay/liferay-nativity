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

	public void disableFileIcons();

	public void enableFileIcons();

	public int registerIcon(String path);

	public void removeAllFileIcons();

	public void removeFileIcon(String path);

	public void removeFileIcons(String[] paths);

	public void setFileIcon(String path, int iconId);

	public void setFileIcons(Map<String, Integer> fileIconsMap);

	public void unregisterIcon(int id);

}