/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

package com.liferay.nativity.util.win;

import java.lang.reflect.Method;

import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class RegistryUtil {

	public static boolean writeRegistry(String key, String name, int value) {
		return writeRegistry(key, name, String.valueOf(value));
	}

	public static boolean writeRegistry(String key, String name, String value) {
		try {
			_init();

			boolean result = _regCreateKeyEx(key);

			if (!result) {
				return false;
			}

			int handle = _regOpenKeyToWrite(key);

			if (handle == 0) {
				return false;
			}

			boolean success = _regSetStringValueEx(handle, name, value);

			if (!_regCloseKey(handle)) {
				return false;
			}

			return success;
		} 
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}

		return false;
	}

	private static void _init() {
		if (_userRoot == null) {
			_userRoot = Preferences.userRoot();
		}

		if (_clazz == null) {
			_clazz = _userRoot.getClass();
		}
	}

	private static boolean _regCloseKey(int handle) throws Exception {
		Method regCloseKey = _clazz.getDeclaredMethod(
			WINDOWS_REG_CLOSE_KEY, new Class[] {int.class});

		regCloseKey.setAccessible(true);

		regCloseKey.invoke(_userRoot, new Object[] { handle });

		return true;
	}

	private static boolean _regCreateKeyEx(String key) throws Exception {
		Method regCreateKeyEx = _clazz.getDeclaredMethod(
			WINDOWS_REG_CREATE_KEY_EX, new Class[] {int.class, byte[].class});

		regCreateKeyEx.setAccessible(true);

		Object result = regCreateKeyEx.invoke(
			_userRoot, new Object[] {
				HKEY_CURRENT_USER, _stringToByteArray(key)});

		if (result == null) {
			return false;
		}

		if (result instanceof int[]) {
			int[] handle = (int[])result;

			if (handle.length == 0) {
				return false;
			}

			return _regCloseKey(handle[0]);
		}

		return false;
	}

	private static int _regOpenKeyToWrite(String key) throws Exception {
		Method regOpenKey = _clazz.getDeclaredMethod(
			WINDOWS_REG_OPEN_KEY,
			new Class[] {int.class, byte[].class, int.class});

		regOpenKey.setAccessible(true);

		Object result = regOpenKey.invoke(
			_userRoot, 
			new Object[] {HKEY_CURRENT_USER, _stringToByteArray(key), 
				KEY_WRITE});

		if (result == null) {
			return 0;
		}

		if (result instanceof int[]) {
			int[] hResult = (int[])result;

			if (hResult.length == 0) {
				return 0;
			}

			return hResult[0];
		}

		return 0;
	}

	private static boolean _regSetStringValueEx(
			int handle, String name, String value) 
		throws Exception {

		Method regSetValueEx = _clazz.getDeclaredMethod(
			WINDOWS_REG_SET_VALUE_EX, 
			new Class[] {
				int.class, byte[].class, byte[].class});

		regSetValueEx.setAccessible(true);

		Object hResult = regSetValueEx.invoke(
			_userRoot, 
			new Object[] {handle, _stringToByteArray(name), 
				_stringToByteArray(value)});

		if (hResult instanceof Integer) {
			int result = ((Integer) hResult).intValue();

			if (result == 0) {
				return true;
			} 
			else {
				_logger.error("Unable to set registry value {} {}", name,
					result);
			}
		}

		return false;
	}

	private static byte[] _stringToByteArray(String str) {
		byte[] result = new byte[str.length() + 1];

		for (int i = 0; i < str.length(); i++) {
			result[i] = (byte) str.charAt(i);
		}

		result[str.length()] = 0;

		return result;
	}

	private static final int HKEY_CURRENT_USER = 0x80000001;

	private static final int KEY_WRITE = 0x20006;

	private static final String WINDOWS_REG_CREATE_KEY_EX = 
		"WindowsRegCreateKeyEx";

	private static final String WINDOWS_REG_OPEN_KEY = 
		"WindowsRegOpenKey";

	private static final String WINDOWS_REG_SET_VALUE_EX = 
		"WindowsRegSetValueEx";

	private static Logger _logger = LoggerFactory.getLogger(
		RegistryUtil.class.getName());
	
	private static final String WINDOWS_REG_CLOSE_KEY = 
		"WindowsRegCloseKey";

	private static Class<? extends Preferences> _clazz = null;
	private static Preferences _userRoot = null;

}