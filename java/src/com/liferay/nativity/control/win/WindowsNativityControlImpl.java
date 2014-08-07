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

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.util.win.RegistryUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class WindowsNativityControlImpl extends NativityControl {

	@Override
	public boolean connect() {
		_logger.debug("Connecting...");

		boolean loaded = WindowsNativityUtil.load();

		_logger.debug("Loaded {}", loaded);

		_receive = new WindowsReceiveSocket(this);

		_receiveExecutor.execute(_receive);

		return loaded;
	}

	@Override
	public boolean disconnect() {
		return true;
	}

	@Override
	public boolean load() throws Exception {
		return true;
	}

	@Override
	public boolean loaded() {
		return true;
	}

	@Override
	public void refreshFiles(String[] paths) {
		if (paths == null) {
			return;
		}

		if (!WindowsNativityUtil.loaded()) {
			return;
		}

		try {
			for (String path : paths) {
				String temp = path.replace("/", "\\");

				WindowsNativityUtil.updateExplorer(temp);
			}
		}
		catch (UnsatisfiedLinkError e) {
			_logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void setFilterFolder(String folder) {
		String temp = folder.replace("/", "\\");

		RegistryUtil.writeRegistry(
			Constants.NATIVITY_REGISTRY_KEY,
			Constants.FILTER_FOLDER_REGISTRY_NAME, temp);
	}

	@Override
	public void setSystemFolder(String folder) {
		if (!WindowsNativityUtil.loaded()) {
			return;
		}

		try {
			WindowsNativityUtil.setSystemFolder(folder);
		}
		catch (UnsatisfiedLinkError e) {
			_logger.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean unload() throws Exception {
		return true;
	}

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsNativityControlImpl.class.getName());

	private WindowsReceiveSocket _receive;
	private ExecutorService _receiveExecutor =
		Executors.newSingleThreadExecutor();

}