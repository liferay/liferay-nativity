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

import com.liferay.nativity.plugincontrol.NativityPluginControl;

import java.util.Map;

/**
 * @author Dennis Ju
 */
public abstract class FileIconControlBase {

	public FileIconControlBase(NativityPluginControl pluginControl) {
		this.pluginControl = pluginControl;
	}

	public abstract void disableFileIcons();

	public abstract void enableFileIcons();

	public abstract int getIconForFile(String path);

	public abstract int registerIcon(String path);

	public abstract void removeAllFileIcons();

	public abstract void removeFileIcon(String fileName);

	public abstract void removeFileIcons(String[] fileNames);

	public abstract void setIconForFile(String fileName, int iconId);

	public abstract void setIconsForFiles(Map<String, Integer> fileIconsMap);

	public abstract void unregisterIcon(int id);

	protected NativityPluginControl pluginControl;

}