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

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class WindowsNativityControlImpl extends NativityControl {

	@Override
	public boolean connect() {
		if (_connected) {
			return true;
		}

		boolean loaded = WindowsNativityUtil.load();

		if (!loaded) {
			_logger.debug("WindowsNativityUtil failed to load");

			return false;
		}

		if (_serverSocket == null) {
			try {
				_serverSocket = new ServerSocket(_port);

				_connected = true;
			}
			catch (IOException ioe) {
				_logger.error(ioe.getMessage(), ioe);

				return false;
			}
		}

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (_connected) {
					handleConnection();
				}
			}
		};

		_executor.execute(runnable);

		return true;
	}

	@Override
	public boolean disconnect() {
		if (!_connected) {
			return true;
		}

		try {
			_serverSocket.close();
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}

		_connected = false;

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
		if ((paths == null) || (paths.length == 0)) {
			return;
		}

		if (!WindowsNativityUtil.loaded()) {
			return;
		}

		try {
			for (String path : paths) {
				WindowsNativityUtil.updateExplorer(path);
			}
		}
		catch (UnsatisfiedLinkError ule) {
			_logger.error(ule.getMessage(), ule);
		}
	}

	@Override
	public void setFilterFolder(String folder) {
		RegistryUtil.writeRegistry(
			Constants.NATIVITY_REGISTRY_KEY,
			Constants.FILTER_FOLDER_REGISTRY_NAME, folder);
	}

	@Override
	public void setSystemFolder(String folder) {
		if (!WindowsNativityUtil.loaded()) {
			return;
		}

		try {
			WindowsNativityUtil.setSystemFolder(folder);
		}
		catch (UnsatisfiedLinkError ule) {
			_logger.error(ule.getMessage(), ule);
		}
	}

	@Override
	public boolean unload() throws Exception {
		return true;
	}

	protected void handleConnection() {
		try {
			Socket clientSocket = _serverSocket.accept();

			_executor.execute(new MessageProcessor(clientSocket, this));
		}
		catch (SocketException se) {

			// Expected when socket is closed

		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsNativityControlImpl.class.getName());

	private static int _port = 33001;

	private boolean _connected = false;
	private Executor _executor = Executors.newCachedThreadPool();
	private ServerSocket _serverSocket;
}