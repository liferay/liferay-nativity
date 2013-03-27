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

import java.io.OutputStreamWriter;

import java.net.Socket;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class MessageSender implements Runnable {

	public MessageSender(Socket clientSocket, String[] array) {
		_clientSocket = clientSocket;
		_commands = array;
	}

	@Override
	public void run() {
		_logger.debug("Running sender...");

		if (_commands.length == 0) {
			return;
		}

		_logger.debug("Starting write...");

		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
				_clientSocket.getOutputStream(), Charset.forName("UTF-16LE"));

			for (String command : _commands) {
				outputStreamWriter.write(command);
			}

			outputStreamWriter.write("\0");

			_logger.debug("Finished write...");

			outputStreamWriter.close();

			_logger.debug("Closing write socket");
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static Logger _logger = LoggerFactory.getLogger(
		MessageSender.class.getName());

	private Socket _clientSocket;
	private String[] _commands;

}