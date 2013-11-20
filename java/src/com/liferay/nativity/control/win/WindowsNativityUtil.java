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

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class WindowsNativityUtil {

	public static boolean load() {
		_logger.trace("Loading nativity");

		if (!_loaded) {
			_load();
		}

		return _loaded;
	}

	public static native boolean setSystemFolder(String folder);

	public static native boolean updateExplorer(String filePath);

	private static boolean _createFolder(String folder) {
		_logger.trace("Creating folder {} ", folder);

		File file = new File(folder);

		if (!file.exists()) {
			return file.mkdirs();
		}

		return true;
	}

	private static String _createPath(String path, String dllName) {
		_logger.trace("Creating path {} {} ", path, dllName);

		StringBuilder sb = new StringBuilder();

		sb.append(path);

		if (!path.endsWith("/") && !path.endsWith("\\")) {
			sb.append("/");
		}

		sb.append(dllName);

		_logger.trace("Created path {} ", sb.toString());

		return sb.toString();
	}

	private static void _extractDLL(String src, String dest) {
		_logger.trace("Extracting {} {}", src, dest);

		try {
			InputStream in = WindowsNativityUtil.class.getResourceAsStream(src);

			File out = new File(dest);

			FileUtils.copyInputStreamToFile(in, out);
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static String _extractDLLFromJar() {
		_logger.trace("Extracting dll from jar");

		String jarPath = "/";

		String src64 = _createPath(jarPath, _NATIVITY_LIB_x64);

		String src86 = _createPath(jarPath, _NATIVITY_LIB_x86);

		String dataFolder = System.getenv("APPDATA");

		String destFolder = _createPath(dataFolder, "Liferay");

		if (!_createFolder(destFolder)) {
			return null;
		}

		String dest64 = _createPath(destFolder, _NATIVITY_LIB_x64);

		String dest86 = _createPath(destFolder, _NATIVITY_LIB_x86);

		if (dllsExist(dest64, dest86)) {
			_logger.trace("DLL's exist, no need to extract from jar");

			return destFolder;
		}

		_logger.trace("Trying {} {}", src64, dest64);

		_extractDLL(src64, dest64);

		_logger.trace("Trying {} {}", src86, dest86);

		_extractDLL(src86, dest86);

		return destFolder;
	}

	private static void _load() {
		_loaded = false;

		if (!_load) {
			_logger.trace("Do not load");

			return;
		}

		_logger.trace("Starting to load dlls");

		if (!OSDetector.isMinimumWindowsVersion(OSDetector.WIN_VISTA)) {
			_load = false;

			_logger.error("Unable to load dll, below minimum OS version");

			return;
		}

		if (_loadLibrary(false, _NATIVITY_LIB_x64)) {
			_logger.trace("Successfully loaded x64 libraray from lib.path");

			return;
		}
		else if (_loadLibrary(false, _NATIVITY_LIB_x86)) {
			_logger.trace("Successfully loaded x86 libraray from lib.path");

			return;
		}

		_logger.trace("Unable to load dll from path, loading from jar");

		String path = _extractDLLFromJar();

		if (path == null) {
			_logger.error("Unable to extract dll from jar");

			return;
		}

		_logger.trace("Extracted dll from jar {} ", path);

		String dllPath64 = _createPath(path, _NATIVITY_LIB_x64);

		String dllPath86 = _createPath(path, _NATIVITY_LIB_x86);

		if (_loadLibrary(true, dllPath64)) {
			_logger.trace("Loaded {} ", dllPath64);

			return;
		}
		else if (_loadLibrary(true, dllPath86)) {
			_logger.trace("Loaded {} ", dllPath86);

			return;
		}

		_logger.error("Path : {}", System.getProperty("java.library.path"));
		_logger.error("Unable to load library");
	}

	private static boolean _loadLibrary(boolean fullPath, String path) {
		_logger.trace("Trying to load {} {}", fullPath, path);

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
			_logger.error("Failed to load {} ()", e.getMessage(), path);
		}
		catch (Exception e) {
			_logger.error("Failed to load {}", path);
			_logger.error(e.getMessage(), e);
		}

		return _loaded;
	}

	private static boolean dllsExist(String dllPath64, String dllPath86) {
		File dllFile64 = new File(dllPath64);
		File dllFile86 = new File(dllPath86);

		if (dllFile64.exists() && dllFile86.exists()) {
			return true;
		}

		return false;
	}

	private static final String _NATIVITY_LIB_x64 =
		"LiferayNativityWindowsUtil_x64.dll";
	private static final String _NATIVITY_LIB_x86 =
		"LiferayNativityWindowsUtil_x86.dll";

	private static boolean _load = true;
	private static boolean _loaded;
	private static Logger _logger = LoggerFactory.getLogger(
		WindowsNativityUtil.class.getName());

}