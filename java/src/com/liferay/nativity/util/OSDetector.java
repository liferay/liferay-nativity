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

package com.liferay.nativity.util;

import java.io.File;

/**
 * @author Dennis Ju
 */
public class OSDetector {

	public static final String MAC_CHEETAH_10_0 = "10.0";

	public static final String MAC_EL_CAPITAN_10_11 = "10.11";

	public static final String MAC_JAGUAR_10_2 = "10.2";

	public static final String MAC_LEOPARD_10_5 = "10.5";

	public static final String MAC_LION_10_7 = "10.7";

	public static final String MAC_MAVERICKS_10_9 = "10.9";

	public static final String MAC_MOUNTAIN_LION_10_8 = "10.8";

	public static final String MAC_PANTHER_10_3 = "10.3";

	public static final String MAC_PUMA_10_1 = "10.1";

	public static final String MAC_SIERRA_10_12 = "10.12";

	public static final String MAC_SNOW_LEOPARD_10_6 = "10.6";

	public static final String MAC_TIGER_10_4 = "10.4";

	public static final String MAC_YOSEMITE_10_10 = "10.10";

	public static final String WIN_7 = "6.1";

	public static final String WIN_8 = "6.2";

	public static final String WIN_8_1 = "6.3";

	public static final String WIN_10 = "10.0";

	public static final String WIN_2000 = "5.0";

	public static final String WIN_SERVER_2003 = "5.2";

	public static final String WIN_SERVER_2008 = "6.0";

	public static final String WIN_SERVER_2012 = "6.2";

	public static final String WIN_SERVER_2012_R2 = "6.3";

	public static final String WIN_SERVER_2016 = "10.0";

	public static final String WIN_VISTA = "6.0";

	public static final String WIN_XP_X64 = "5.2";

	public static final String WIN_XP_X86 = "5.1";

	public static boolean isAIX() {
		if (_aix != null) {
			return _aix.booleanValue();
		}

		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.equals("aix")) {
			_aix = Boolean.TRUE;
		}
		else {
			_aix = Boolean.FALSE;
		}

		return _aix.booleanValue();
	}

	public static boolean isApple() {
		if (_apple != null) {
			return _apple.booleanValue();
		}

		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("mac")) {
			_apple = Boolean.TRUE;
		}
		else {
			_apple = Boolean.FALSE;
		}

		return _apple.booleanValue();
	}

	public static boolean isLinux() {
		if (_linux != null) {
			return _linux.booleanValue();
		}

		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("linux")) {
			_linux = Boolean.TRUE;
		}
		else {
			_linux = Boolean.FALSE;
		}

		return _linux.booleanValue();
	}

	public static boolean isMinimumAppleVersion(String minimumVersion) {
		if (!isApple()) {
			return false;
		}

		if (_version == null) {
			_version = System.getProperty("os.version");
		}

		int compare = VersionUtil.compare(_version, minimumVersion);

		if (compare >= 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public static boolean isMinimumWindowsVersion(String minimumVersion) {
		if (!isWindows()) {
			return false;
		}

		if (_version == null) {
			_version = System.getProperty("os.version");
		}

		int compare = VersionUtil.compare(_version, minimumVersion);

		if (compare >= 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public static boolean isUnix() {
		if (_unix != null) {
			return _unix.booleanValue();
		}

		if (File.pathSeparator.equals(":")) {
			_unix = Boolean.TRUE;
		}
		else {
			_unix = Boolean.FALSE;
		}

		return _unix.booleanValue();
	}

	public static boolean isWindows() {
		if (_windows != null) {
			return _windows.booleanValue();
		}

		if (File.pathSeparator.equals(";")) {
			_windows = Boolean.TRUE;
		}
		else {
			_windows = Boolean.FALSE;
		}

		return _windows.booleanValue();
	}

	private static Boolean _aix;
	private static Boolean _apple;
	private static Boolean _linux;
	private static Boolean _unix;
	private static String _version;
	private static Boolean _windows;

}