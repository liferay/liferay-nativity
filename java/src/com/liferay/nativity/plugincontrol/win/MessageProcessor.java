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

import com.liferay.nativity.plugincontrol.NativityMessage;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;

import java.nio.charset.Charset;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class MessageProcessor implements Runnable {

	public MessageProcessor(
		Socket clientSocket, WindowsNativityControlImpl plugIn) {

		_clientSocket = clientSocket;
		_plugIn = plugIn;
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

		JSONDeserializer<NativityMessage> jsonDeserializer =
			new JSONDeserializer<NativityMessage>();

		jsonDeserializer.use("value", ArrayList.class);

		try {
			NativityMessage message = jsonDeserializer.deserialize(
				receivedMessage, NativityMessage.class);

			NativityMessage responseMessage = _plugIn.fireOnMessage(message);

			if (responseMessage == null) {
				_logger.debug("Response Null");

				_returnEmpty();
			}
			else {
				String response =
					_jsonSerializer.exclude("*.class")
						.deepSerialize(responseMessage);

				_logger.debug("Response {}",response);

				_outputStreamWriter.write(response);
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

	private static JSONSerializer _jsonSerializer = new JSONSerializer();

	private static Logger _logger = LoggerFactory.getLogger(
		MessageProcessor.class.getName());

	private Socket _clientSocket;
	private InputStreamReader _inputStreamReader;
	private OutputStreamWriter _outputStreamWriter;
	private WindowsNativityControlImpl _plugIn;

}