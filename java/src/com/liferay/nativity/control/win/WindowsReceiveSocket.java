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

import java.io.IOException;

import java.net.Socket;
import java.net.SocketException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class WindowsReceiveSocket extends WindowsSocketBase {

	public WindowsReceiveSocket(WindowsNativityControlImpl nativityControl) {
		super(33001);

		_nativityControl = nativityControl;
	}

	protected void handleConnection() {
		try {
			Socket clientSocket = getServerSocket().accept();

			_messageProcessor.execute(
				new MessageProcessor(clientSocket, _nativityControl));
		}
		catch (SocketException se) {

			// Expected when socket is closed

		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsReceiveSocket.class.getName());

	private Executor _messageProcessor = Executors.newSingleThreadExecutor();
	private WindowsNativityControlImpl _nativityControl;

}