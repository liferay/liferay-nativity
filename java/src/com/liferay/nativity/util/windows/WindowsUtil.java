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

package com.liferay.nativity.util.windows;

import com.liferay.nativity.util.AppPropsKeys;
import com.liferay.nativity.util.AppPropsUtil;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class WindowsUtil {

	public static boolean isLoaded() {
		if (!_loaded) {
			_load();
		}

		return _loaded;
	}

	public static boolean setRootFolder(String rootFolderPath) {
		rootFolderPath = _fixPath(rootFolderPath);

		if (!isLoaded()) {
			return false;
		}

		return _setRootFolder(rootFolderPath);
	}

	public static boolean updateExplorer(
		String filePath, ExplorerConstants eventType) {

		filePath = _fixPath(filePath);

		if (!isLoaded()) {
			return false;
		}

		return _updateExplorer(filePath, eventType.getValue());
	}

	public static boolean updateRenameExplorer(
		String oldPath, String filePath, ExplorerConstants eventType) {

		oldPath = _fixPath(oldPath);
		filePath = _fixPath(filePath);

		if (!isLoaded()) {
			return false;
		}

		return _updateRenameExplorer(oldPath, filePath, eventType.getValue());
	}

	private static String _fixPath(String name) {
		if (name == null) {
			return "";
		}

		return name.replace("\\", "/");
	}

	private static String _getFilePath(String parentPath, String name) {
		StringBuilder sb = new StringBuilder(3);

		sb.append(parentPath);
		sb.append(File.separator);
		sb.append(name);

		return _fixPath(sb.toString());
	}

	private static String _getParentPath(String path) throws IOException {
		path = _fixPath(path);

		int index = path.lastIndexOf("/");

		if (index < 1) {
			throw new IOException("Invalid file path " + path);
		}

		String parentPath = path.substring(0, index);

		return parentPath;
	}

	private static void _load() {
		_loaded = false;

		String path = AppPropsUtil.getProperty(
			AppPropsKeys.NVTY_APPLICATION_PATH);

		if (path.contains(".exe")) {
			try {
				path = _getParentPath(path);
			}
			catch (Exception e) {
				_logger.error(e.getMessage(), e);
			}
		}

		String liferayUtil64 = AppPropsUtil.getProperty(
			AppPropsKeys.NVTY_LIFERAY_NATIVITY_UTIL_64);

		String path64 = _getFilePath(path, liferayUtil64);

		String liferayUtil86 = AppPropsUtil.getProperty(
				AppPropsKeys.NVTY_LIFERAY_NATIVITY_UTIL_86);

		String path86 = _getFilePath(path, liferayUtil86);

		String liferayUtilDefault = AppPropsUtil.getProperty(
				AppPropsKeys.NVTY_LIFERAY_NATIVITY_UTIL_DEFAULT);

		if (_loadLibrary(true, path64)) {
			return;
		}
		else if (_loadLibrary(true, path86)) {
			return;
		}
		else if (_loadLibrary(false, liferayUtil64)) {
			return;
		}
		else if (_loadLibrary(false, liferayUtil86)) {
			return;
		}
		else if (_loadLibrary(false, liferayUtilDefault)) {
			return;
		}

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
		}
		catch (Exception e) {
			_logger.error("Failed to load {}", path);
		}

		return _loaded;
	}

	private static native boolean _setRootFolder(String rootFolderPath);

	private static native boolean _updateExplorer(
		String filePath, int eventType);

	private static native boolean _updateRenameExplorer(
	String oldPath, String filePath, int eventType);

	private static boolean _loaded = false;

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsUtil.class.getName());

}