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

import java.io.IOException;
import java.io.InputStream;

import java.text.MessageFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author Dennis Ju
 */
public class AppleScriptUtil {

	public static Object executeScript(String script) throws ScriptException {
		if (_appleScriptEngine == null) {
			_scriptEngineManager = new ScriptEngineManager();

			_appleScriptEngine = _scriptEngineManager.getEngineByName(
				"AppleScriptEngine");

			if (_appleScriptEngine == null) {
				_appleScriptEngine = _scriptEngineManager.getEngineByName(
					"AppleScript");

				if (_appleScriptEngine == null) {
					throw new ScriptException(
						"AppleScriptEngine not available");
				}
			}
		}

		return _appleScriptEngine.eval(script, _appleScriptEngine.getContext());
	}

	public static String getScript(String scriptName) throws IOException {
		if (_loadedScripts.containsKey(scriptName)) {
			return _loadedScripts.get(scriptName);
		}

		InputStream is = AppleScriptUtil.class.getResourceAsStream(
			"/resources/osax/" + scriptName);

		if (is == null) {
			return null;
		}

		String script = getString(is);

		_loadedScripts.put(scriptName, script);

		return script;
	}

	public static String getScript(String scriptName, Object... arguments)
		throws IOException {

		String script = getScript(scriptName);

		return MessageFormat.format(script, arguments);
	}

	private static String getString(InputStream is) {
		Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");

		if (scanner.hasNext()) {
			return scanner.next();
		}
		else {
			return "";
		}
	}

	private static ScriptEngine _appleScriptEngine;
	private static Map<String, String> _loadedScripts =
		new HashMap<String, String>();
	private static ScriptEngineManager _scriptEngineManager;

}