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

package com.liferay.nativity.util.mac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class AppleUtil {

	public static String getBundleVersion(String bundlePath) {
		try {
			String script = AppleScriptUtil.getScript(
				"getbundleversion.applescript", bundlePath);

			return (String)AppleScriptUtil.executeScript(script);
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}

		return "";
	}

	public static String getInstalledBundleVersion() {
		try {
			String script = AppleScriptUtil.getScript(
				"getbundleversion.applescript",
				"/Library/ScriptingAdditions/LiferayNativity.osax");

			return (String)AppleScriptUtil.executeScript(script);
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}

		return "";
	}

	public static void installScripts(String sourcePath) {
		try {
			String script = AppleScriptUtil.getScript(
				"installscripts.applescript", sourcePath);

			AppleScriptUtil.executeScript(script);

			Thread.sleep(1000);

			reloadScripts();
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}
	}

	public static boolean load() {
		try {
			String script = AppleScriptUtil.getScript("load.applescript");

			AppleScriptUtil.executeScript(script);

			return true;
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}

		return false;
	}

	public static boolean loaded() {
		try {
			String script = AppleScriptUtil.getScript("loaded.applescript");

			Long result = (Long)AppleScriptUtil.executeScript(script);

			if ((result == null) || (result != 0)) {
				return false;
			}
			else {
				return true;
			}
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}

		return false;
	}

	public static void reloadScripts() {
		try {
			String script = AppleScriptUtil.getScript(
				"reloadscripts.applescript");

			AppleScriptUtil.executeScript(script);
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}
	}

	public static void uninstallScripts() {
		try {
			String script = AppleScriptUtil.getScript(
				"uninstallscripts.applescript");

			AppleScriptUtil.executeScript(script);
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}
	}

	public static boolean unload() {
		try {
			String script = AppleScriptUtil.getScript("unload.applescript");

			AppleScriptUtil.executeScript(script);

			return true;
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}

		return false;
	}

	private static Logger _logger = LoggerFactory.getLogger(
		AppleUtil.class.getName());

}