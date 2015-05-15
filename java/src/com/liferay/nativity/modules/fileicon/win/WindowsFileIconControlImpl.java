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

package com.liferay.nativity.modules.fileicon.win;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.modules.fileicon.FileIconControlBase;
import com.liferay.nativity.modules.fileicon.FileIconControlCallback;
import com.liferay.nativity.util.win.RegistryUtil;

import java.util.Map;

/**
* @author Dennis Ju
*/
public class WindowsFileIconControlImpl extends FileIconControlBase {

	public WindowsFileIconControlImpl(
		NativityControl nativityControl,
		FileIconControlCallback fileIconControlCallback) {

		super(nativityControl, fileIconControlCallback);
	}

	@Override
	public void disableFileIcons() {
		RegistryUtil.writeRegistry(
			Constants.NATIVITY_REGISTRY_KEY,
			Constants.ENABLE_OVERLAY_REGISTRY_NAME, 0);
	}

	@Override
	public void enableFileIcons() {
		RegistryUtil.writeRegistry(
			Constants.NATIVITY_REGISTRY_KEY,
			Constants.ENABLE_OVERLAY_REGISTRY_NAME, 1);
	}

	@Override
	public void refreshIcons() {
	}

	@Override
	public int registerIcon(String path) {
		return 0;
	}

	@Override
	public void registerIconWithId(String path, String label, int id) {
	}

	@Override
	public void removeAllFileIcons() {
	}

	@Override
	public void removeFileIcon(String path) {
	}

	@Override
	public void removeFileIcons(String[] paths) {
	}

	@Override
	public void setFileIcon(String path, int iconId) {
	}

	@Override
	public void setFileIcons(Map<String, Integer> fileIconsMap) {
	}

	@Override
	public void unregisterIcon(int id) {
	}

}