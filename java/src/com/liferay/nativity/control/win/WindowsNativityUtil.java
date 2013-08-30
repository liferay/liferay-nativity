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

package com.liferay.nativity.control.win;

import com.liferay.nativity.util.OSDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class WindowsNativityUtil {

	public static boolean load() {
		if (!_loaded) {
			_load();
		}

		return _loaded;
	}

	public static native boolean setSystemFolder(String folder);

	public static native boolean updateExplorer(String filePath);

	private static void _load() {
		_loaded = false;

		if (!_load) {
			_logger.trace("Do not load");

			return;
		}

		if (!OSDetector.isMinimumWindowsVersion(OSDetector.WIN_VISTA)) {
			_load = false;

			return;
		}
		
		if (_loadLibrary(false, _LIFERAY_NATIVITY_WINDOWS_UTIL_x64)) {
			return;
		}
		else if (_loadLibrary(false, _LIFERAY_NATIVITY_WINDOWS_UTIL_x86)) {
			return;
		}

		_logger.error("Path : {}", System.getProperty("java.library.path"));
		_logger.error("Unable to load library");
	}

	private static boolean _loadLibrary(boolean fullPath, String path) {
		try {
			if (fullPath) {
				System.load(path);
			}
			else {
				System.loadLibrary(path);
			}

			_loaded = true;

			_logger.trace("Loaded library {}", path);
		}
		catch (UnsatisfiedLinkError e) {
			_logger.error("Failed to load {}", path);
			_logger.error(e.getMessage(), e);
		}
		catch (Exception e) {
			_logger.error("Failed to load {}", path);
			_logger.error(e.getMessage(), e);
		}

		return _loaded;
	}

	private static final String _LIFERAY_NATIVITY_WINDOWS_UTIL_x64 =
		"LiferayNativityWindowsUtil_x64";
	private static final String _LIFERAY_NATIVITY_WINDOWS_UTIL_x86 =
		"LiferayNativityWindowsUtil_x86";

	private static boolean _load = true;
	private static boolean _loaded;
	private static Logger _logger = LoggerFactory.getLogger(
		WindowsNativityUtil.class.getName());

}