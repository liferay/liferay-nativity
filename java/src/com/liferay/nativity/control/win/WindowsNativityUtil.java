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

package com.liferay.nativity.control.win;

import com.liferay.nativity.util.OSDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 * @author Dennis Ju
 */
public class WindowsNativityUtil {

	public static native boolean addFavoritesPath(String folder);

	public static boolean load() {
		if (!_loaded) {
			_load();
		}

		return _loaded;
	}

	public static boolean loaded() {
		return _loaded;
	}

	public static native boolean refreshExplorer(String filePath);

	public static native boolean removeFavoritesPath(String folder);

	public static native boolean setSystemFolder(String folder);

	public static native boolean updateExplorer(String filePath);

	private static void _load() {
		if (!OSDetector.isMinimumWindowsVersion(OSDetector.WIN_VISTA)) {
			_logger.error(
				"Liferay Nativity is not compatible on Windows Vista or lower");

			return;
		}

		_logger.trace("Loading WindowsNativityUtil DLL");

		String nativityDllName = _NATIVITY_DLL_NAME;

		if (System.getenv("ProgramFiles(x86)") != null) {
			nativityDllName = nativityDllName + "_x64";
		}
		else {
			nativityDllName = nativityDllName + "_x86";
		}

		try {
			System.loadLibrary(nativityDllName);

			_loaded = true;

			_logger.trace("Loaded WindowsNativityUtil DLL");
		}
		catch (UnsatisfiedLinkError ule) {
			_logger.error("Failed to load WindowsNativityUtil DLL");
		}
	}

	private static final String _NATIVITY_DLL_NAME =
		"LiferayNativityWindowsUtil";

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsNativityUtil.class.getName());

	private static boolean _loaded = false;

}