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

package com.liferay.nativity.control.win;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.nativity.control.NativityMessage;

import java.io.IOException;

import java.net.Socket;
import java.net.SocketException;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class WindowsSendSocket extends WindowsSocketBase {

	public WindowsSendSocket() {
		super(33002);
	}

	public void send(NativityMessage message) {
		try {
			String command = _objectMapper.writeValueAsString(message);

			_commands.add(command);
		}
		catch (JsonProcessingException jpe) {
			_logger.error("Failed to serialize message", jpe);
		}
	}

	@Override
	protected void handleConnection() {
		try {
			if (_commands.isEmpty()) {
				return;
			}

			_logger.trace("Waiting for connection");

			Socket clientSocket = getServerSocket().accept();

			_logger.trace("Got connection, sending...{}", _commands.size());

			String[] array = null;

			synchronized(this) {
				array = new String[_commands.size()];
				_commands.toArray(array);
				_commands.clear();
			}

			if (array != null) {
				_logger.debug("firing event {}", array.length);

				_messageSender.execute(new MessageSender(clientSocket, array));
			}

			if (clientSocket.isOutputShutdown()) {
				_logger.debug("Output shutdown.");
			}
			else {
				_logger.debug("Output not shutdown");
			}

			_logger.trace("Done sending!");
		}
		catch (SocketException se) {
			_logger.error(se.getMessage());
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static ConcurrentLinkedQueue<String> _commands =
		new ConcurrentLinkedQueue<String>();

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsSendSocket.class.getName());

	private static ObjectMapper _objectMapper =
		new ObjectMapper().configure(
			JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

	private ExecutorService _messageSender =
		Executors.newSingleThreadExecutor();

}