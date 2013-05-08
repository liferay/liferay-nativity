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

package com.liferay.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brian Wing Shun Chan
 * @author Dennis Ju
 */
public class OSDetector {

	public static final double MAC_CHEETAH_10_0 = 10.0;

	public static final double MAC_JAGUAR_10_2 = 10.2;

	public static final double MAC_LEOPARD_10_5 = 10.5;

	public static final double MAC_LION_10_7 = 10.7;

	public static final double MAC_MOUNTAIN_LION_10_8 = 10.8;

	public static final double MAC_PANTHER_10_3 = 10.3;

	public static final double MAC_PUMA_10_1 = 10.1;

	public static final double MAC_SNOW_LEOPARD_10_6 = 10.6;

	public static final double MAC_TIGER_10_4 = 10.4;

	public static final double WIN_2000 = 5.0;

	public static final double WIN_7 = 6.1;

	public static final double WIN_8 = 6.2;

	public static final double WIN_SERVER_2003 = 5.2;

	public static final double WIN_SERVER_2008 = 6.0;

	public static final double WIN_SERVER_2012 = 6.2;

	public static final double WIN_VISTA = 6.0;

	public static final double WIN_XP_X64 = 5.2;

	public static final double WIN_XP_X86 = 5.1;

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

	public static boolean isMinimumAppleVersion(double minimumVersion) {
		if (!isApple()) {
			return false;
		}

		if (_version == null) {
			_version = System.getProperty("os.version");
		}

		String[] parts = _version.split("\\.");

		StringBuilder sb = new StringBuilder(3);

		sb.append(parts[0]);
		sb.append(".");
		sb.append(parts[1]);

		try {
			double version = Double.parseDouble(sb.toString());

			if (version >= minimumVersion) {
				return true;
			}
		}
		catch (Exception e) {
			_logger.error("Could not determine OS Version. {}", e.getMessage());
		}

		return false;
	}

	public static boolean isMinimumWindowsVersion(double minimumVersion) {
		if (!isWindows()) {
			return false;
		}

		if (_version == null) {
			_version = System.getProperty("os.version");
		}

		try {
			double version = Double.parseDouble(_version);

			if (version >= minimumVersion) {
				return true;
			}
		}
		catch (Exception e) {
			_logger.error("Could not determine OS Version. {}", e.getMessage());
		}

		return false;
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
	private static Logger _logger = LoggerFactory.getLogger(
		OSDetector.class.getName());
	private static Boolean _unix;
	private static String _version;
	private static Boolean _windows;

}