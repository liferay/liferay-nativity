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
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.nativity.control.NativityMessage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class MessageProcessor implements Runnable {

	public MessageProcessor(
		Socket clientSocket, WindowsNativityControlImpl nativityControl) {

		_clientSocket = clientSocket;
		_nativityControl = nativityControl;
	}

	@Override
	public void run() {
		_init();

		try {
			StringBuilder sb = new StringBuilder();

			boolean end = false;

			while (!end) {
				int item = _inputStreamReader.read();

				if (item == -1) {
					end = true;
				}
				else {
					char letter = (char)item;
					sb.append(letter);
				}
			}

			String message = sb.toString();

			message = message.replace("\\", "/");
			message = message.replace("/\"", "\\\"");

			if (message.isEmpty()) {
				_returnEmpty();
			}
			else {
				_handle(message);
			}
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private void _handle(String receivedMessage) throws IOException {
		_logger.debug("Message {}", receivedMessage);

		if (receivedMessage.charAt(0) != '{')
		{
			_logger.error("Invalid message {}", receivedMessage);
			return;
		}

		try {
			NativityMessage message = _objectMapper.readValue(
				receivedMessage, NativityMessage.class);

			NativityMessage responseMessage = _nativityControl.fireOnMessage(
				message);

			if (responseMessage == null) {
				_returnEmpty();
			}
			else {
				_logger.debug(
					"Response {}", responseMessage.getValue().toString());

				_objectMapper.writeValue(_outputStreamWriter, responseMessage);
				_outputStreamWriter.write("\0");
			}
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}
		finally {

			// Windows expects null terminated string.

			if (!_clientSocket.isOutputShutdown()) {
				_outputStreamWriter.close();
			}
		}
	}

	private void _init() {
		try {
			_inputStreamReader = new InputStreamReader(
				_clientSocket.getInputStream(), Charset.forName("UTF-16LE"));

			_outputStreamWriter = new OutputStreamWriter(
				_clientSocket.getOutputStream(), Charset.forName("UTF-16LE"));
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private void _returnEmpty() {
		try {
			_outputStreamWriter.close();
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	private static Logger _logger = LoggerFactory.getLogger(
		MessageProcessor.class.getName());

	private static ObjectMapper _objectMapper =
		new ObjectMapper().configure(
			JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

	private Socket _clientSocket;
	private InputStreamReader _inputStreamReader;
	private WindowsNativityControlImpl _nativityControl;
	private OutputStreamWriter _outputStreamWriter;

}