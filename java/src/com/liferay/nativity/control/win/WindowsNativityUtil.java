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

	private static boolean _createFolder(String destinationFolder) {
		_logger.trace("Creating folder {} ", destinationFolder);

		File file = new File(destinationFolder);

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

	private static void _extractDLL(String destination64, String path64) {
		_logger.trace("Extracting {} {}", destination64, path64);
		try {
			InputStream in = WindowsNativityUtil.class.getResourceAsStream(
				path64);

			File outFile = new File(destination64);

			FileUtils.copyInputStreamToFile(in, outFile);
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static String _extractDLLFromJar() {
		_logger.trace("Extracting dll from jar ");

		String jarPath = "/";

		String path64 = _createPath(
			jarPath, _LIFERAY_NATIVITY_WINDOWS_UTIL_x64);

		path64+= _DLL;

		String path86 = _createPath(
			jarPath, _LIFERAY_NATIVITY_WINDOWS_UTIL_x86);

		path86+= _DLL;

		String dataFolder = System.getenv("APPDATA");

		String destinationFolder = _createPath(dataFolder, "Liferay");

		if (!_createFolder(destinationFolder)) {
			return null;
		}

		String destination64 = _createPath(
			destinationFolder, _LIFERAY_NATIVITY_WINDOWS_UTIL_x64);

		destination64 += _DLL;

		String destination86 = _createPath(
			destinationFolder, _LIFERAY_NATIVITY_WINDOWS_UTIL_x86);

		destination86 += _DLL;

		if (!_needExtractFromDLL(destination64, destination86)) {
			_logger.trace("DLL's exist, no need to extract from jar");

			return destinationFolder;
		}

		_logger.trace("Trying {} {}", destination64, path64);

		_extractDLL(destination64, path64);

		_logger.trace("Trying {} {}", destination86, path86);

		_extractDLL(destination86, path86);

		return destinationFolder;
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

		if (_loadLibrary(false, _LIFERAY_NATIVITY_WINDOWS_UTIL_x64)) {
			_logger.trace("Successfully loaded x64 libraray from lib.path");
			return;
		}
		else if (_loadLibrary(false, _LIFERAY_NATIVITY_WINDOWS_UTIL_x86)) {
			_logger.trace("Successfully loaded x86 libraray from lib.path");
			return;
		}

		_logger.trace("Unable to load dll from path, loading from jar");

		String path = _extractDLLFromJar();

		if (path == null) {
			_logger.error("Unable to extrct dll from jar");
			return;
		}

		_logger.trace("Extracted dll from jar {} ", path);

		String path64 = _createPath(path, _LIFERAY_NATIVITY_WINDOWS_UTIL_x64);
		path64 += _DLL;

		String path86 = _createPath(path, _LIFERAY_NATIVITY_WINDOWS_UTIL_x86);
		path86 += _DLL;

		if (_loadLibrary(true, path64)) {
			_logger.trace("Loaded {} ", path64);
			return;
		}
		else if (_loadLibrary(true, path86)) {
			_logger.trace("Loaded {} ", path86);
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

	private static boolean _needExtractFromDLL(
		String destination64, String destination86) {

		File file64 = new File(destination64);
		File file86 = new File(destination86);

		if (!file64.exists() || !file86.exists()) {
			return true;
		}

		return false;
	}

	private static final String _DLL = ".dll";

	private static final String _LIFERAY_NATIVITY_WINDOWS_UTIL_x64 =
		"LiferayNativityWindowsUtil_x64";
	private static final String _LIFERAY_NATIVITY_WINDOWS_UTIL_x86 =
		"LiferayNativityWindowsUtil_x86";

	private static boolean _load = true;
	private static boolean _loaded;
	private static Logger _logger = LoggerFactory.getLogger(
		WindowsNativityUtil.class.getName());

}