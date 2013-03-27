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

package com.liferay.nativity.plugincontrol.win;

import java.io.IOException;

import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public abstract class WindowsSocketBase implements Runnable {

	public WindowsSocketBase(int port) {
		_port = port;
	}

	public void disconnect() {
		synchronized(this) {
			if (!_running) {
				return;
			}

			_running = false;
		}

		try {
			_serverSocket.close();
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	public boolean isConnected() {
		return _running;
	}

	@Override
	public void run() {
		synchronized(this) {
			if (_running) {
				return;
			}

			_running = true;
		}

		_logger.trace("Running listener");

		if (_serverSocket == null) {
			try {

				_logger.debug("New server socket");

				_serverSocket = new ServerSocket(_port);
			}
			catch (IOException ioe) {
				_logger.error(ioe.getMessage(), ioe);

				return;
			}
		}

		while (_running) {
			handleConnection();
		}

		_logger.trace("Completed");
	}

	protected ServerSocket getServerSocket() {
		return _serverSocket;
	}

	protected abstract void handleConnection();

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsSocketBase.class.getName());

	private int _port;

	private boolean _running = false;

	private ServerSocket _serverSocket;

}