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

package com.liferay.nativity.test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityControlUtil;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.modules.contextmenu.ContextMenuControlUtil;
import com.liferay.nativity.modules.fileicon.FileIconControl;
import com.liferay.nativity.modules.fileicon.FileIconControlUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.xml.DOMConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class TestDriver {

	public static void main(String[] args) {
		_intitializeLogging();

		List<String> items = new ArrayList<String>();

		items.add("ONE");

		NativityMessage message = new NativityMessage("BLAH", items);

		try {
			_logger.debug(_objectMapper.writeValueAsString(message));
		}
		catch (JsonProcessingException jpe) {
			_logger.error(jpe.getMessage(), jpe);
		}

		_logger.debug("main");

		NativityControl nativityControl =
			NativityControlUtil.getNativityControl();

		FileIconControl fileIconControl =
			FileIconControlUtil.getFileIconControl(
				nativityControl, new TestFileIconControlCallback());

		ContextMenuControlUtil.getContextMenuControl(
			nativityControl, new TestContextMenuControlCallback());

		BufferedReader bufferedReader = new BufferedReader(
			new InputStreamReader(System.in));

		nativityControl.connect();

		String read = "";
		boolean stop = false;

		try {
			while (!stop) {
				_list = !_list;

				_logger.debug("Loop start...");

				_logger.debug("_enableFileIcons");
				_enableFileIcons(fileIconControl);

				_logger.debug("_registerFileIcon");
				_registerFileIcon(fileIconControl);

				_logger.debug("_setFilterPath");
				_setFilterPath(nativityControl);

				_logger.debug("_setSystemFolder");
				_setSystemFolder(nativityControl);

				_logger.debug("_updateFileIcon");
				_updateFileIcon(fileIconControl);

				_logger.debug("_clearFileIcon");
				_clearFileIcon(fileIconControl);

				_logger.debug("Ready?");

				if (bufferedReader.ready()) {
					_logger.debug("Reading...");

					read = bufferedReader.readLine();

					_logger.debug("Read {}", read);

					if (read.length() > 0) {
						stop = true;
					}

					_logger.debug("Stopping {}", stop);
				}
			}
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}

		_logger.debug("Done");
	}

	private static void _clearFileIcon(FileIconControl fileIconControl) {
		if (_list) {
			String[] paths = new String[] { _testFolder, _testFile };

			fileIconControl.removeFileIcons(paths);
		}
		else {
			fileIconControl.removeFileIcon(_testFolder);
		}

		try {
			Thread.sleep(_waitTime);
		}
		catch (InterruptedException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static void _enableFileIcons(FileIconControl fileIconControl) {
		fileIconControl.enableFileIcons();

		try {
			Thread.sleep(_waitTime);
		}
		catch (InterruptedException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static void _intitializeLogging() {
		File file = new File("java/nativity-log4j.xml");

		if (file.exists()) {
			DOMConfigurator.configure(file.getPath());
		}
	}

	private static void _registerFileIcon(FileIconControl fileIconControl) {
		fileIconControl.registerIconWithId(_fileIconPath, "", Integer.toString(_fileIconId));

		try {
			Thread.sleep(_waitTime);
		}
		catch (InterruptedException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static void _setFilterPath(NativityControl nativityControl) {
		nativityControl.setFilterFolder(_testRootFolder);

		try {
			Thread.sleep(_waitTime);
		}
		catch (InterruptedException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static void _setSystemFolder(NativityControl nativityControl) {
		nativityControl.setSystemFolder(_testRootFolder);

		try {
			Thread.sleep(_waitTime);
		}
		catch (InterruptedException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static void _updateFileIcon(FileIconControl fileIconControl) {
		if (_list) {
			Map<String, Integer> map = new HashMap<String, Integer>();

			map.put(_testFolder, _fileIconId);
			map.put(_testFile, _fileIconId);

			fileIconControl.setFileIcons(map);
		}
		else {
			fileIconControl.setFileIcon(_testFolder, _fileIconId);
		}

		try {
			Thread.sleep(_waitTime);
		}
		catch (InterruptedException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static Logger _logger = LoggerFactory.getLogger(
		TestDriver.class.getName());

	private static int _fileIconId = -1;
	private static String _fileIconPath = "/Users/liferay/Desktop/test.icns";
	private static boolean _list = false;
	private static ObjectMapper _objectMapper = new ObjectMapper().configure(
		JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
	private static String _testFile =
		"C:/Users/liferay/Documents/liferay-sync/Sync.pptx";
	private static String _testFolder =
		"C:/Users/liferay/Documents/liferay-sync/My Documents (test)/temp";
	private static String _testRootFolder =
		"C:/Users/liferay/Documents/liferay-sync";
	private static int _waitTime = 1000;

}