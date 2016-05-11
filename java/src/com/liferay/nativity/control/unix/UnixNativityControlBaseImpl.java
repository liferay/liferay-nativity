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

package com.liferay.nativity.control.unix;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.Socket;
import java.net.SocketException;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public abstract class UnixNativityControlBaseImpl extends NativityControl {

	@Override
	public void addFavoritesPath(String path) {
	}

	@Override
	public boolean connect() {
		try {
			_commandSocket = new Socket("127.0.0.1", _commandSocketPort);

			_commandBufferedReader = new BufferedReader(
				new InputStreamReader(
					_commandSocket.getInputStream(), "UTF-8"));

			_commandOutputStream = new DataOutputStream(
				_commandSocket.getOutputStream());

			_callbackSocket = new Socket("127.0.0.1", _callbackSocketPort);

			_callbackBufferedReader = new BufferedReader(
				new InputStreamReader(
					_callbackSocket.getInputStream(), "UTF-8"));

			_callbackOutputStream = new DataOutputStream(
				_callbackSocket.getOutputStream());

			_callbackThread = new ReadThread(this);

			_callbackThread.start();

			_connected = true;

			if (_logger.isDebugEnabled()) {
				_logger.debug(
					"Successfully connected to command socket: {}",
					_commandSocketPort);

				_logger.debug(
					"Successfully connected to service socket: {}",
					_callbackSocketPort);
			}

			return true;
		}
		catch (IOException e) {
			_logger.error(e.getMessage());
		}

		_connected = false;

		return false;
	}

	public boolean disconnect() {
		try {
			_commandSocket.close();
			_callbackSocket.close();

			_connected = false;

			if (_logger.isDebugEnabled()) {
				_logger.debug("Successfully disconnected");
			}

			return true;
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}

		_connected = true;

		return false;
	}

	@Override
	public Set<String> getAllObservedFolders() {
		return null;
	}

	@Override
	public void refreshFiles(String[] paths) {
	}

	@Override
	public void removeFavoritesPath(String path) {
	}

	@Override
	public String sendMessage(NativityMessage message) {
		if (!_connected) {
			_logger.debug("LiferayNativity is not connected");

			return "";
		}

		try {
			String messageString = _objectMapper.writeValueAsString(message);

			_commandOutputStream.write(messageString.getBytes("UTF-8"));
			_commandOutputStream.write(_RETURN_NEW_LINE.getBytes("UTF-8"));

			if (_logger.isTraceEnabled()) {
				_logger.trace(
					"Sent on socket {}: {}", _commandSocketPort, messageString);
			}

			String reply = _commandBufferedReader.readLine();

			if (_logger.isTraceEnabled()) {
				_logger.trace(
					"Received on socket {}: {}", _commandSocketPort, reply);
			}

			if (reply == null) {
				_commandSocket.close();

				fireSocketCloseListeners();
			}

			return reply;
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);

			fireSocketCloseListeners();

			return "";
		}
	}

	@Override
	public void setPortFilePath(String path) {
	}

	@Override
	public void setSystemFolder(String folder) {
	}

	protected class ReadThread extends Thread {

		public ReadThread(UnixNativityControlBaseImpl pluginControl) {
			_pluginControl = pluginControl;
		}

		@Override
		public void run() {
			_pluginControl._doCallbackLoop();
		}

		private UnixNativityControlBaseImpl _pluginControl;

	}

	private void _doCallbackLoop() {
		if (!_connected) {
			_logger.debug("LiferayNativity is not connected");

			return;
		}

		while (_connected) {
			try {
				String data = _callbackBufferedReader.readLine();

				if (_logger.isTraceEnabled()) {
					_logger.trace(
						"Received on socket {}: {}", _callbackSocketPort, data);
				}

				if (data == null) {
					disconnect();

					fireSocketCloseListeners();

					break;
				}

				if (data.isEmpty()) {
					continue;
				}

				NativityMessage message = _objectMapper.readValue(
					data, NativityMessage.class);

				NativityMessage responseMessage = fireMessage(message);

				if (responseMessage != null) {
					String responseMessageString =
						_objectMapper.writeValueAsString(responseMessage);

					_callbackOutputStream.write(
						responseMessageString.getBytes("UTF-8"));
					_callbackOutputStream.write(
						_RETURN_NEW_LINE.getBytes("UTF-8"));

					if (_logger.isTraceEnabled()) {
						_logger.trace(
							"Sent on socket {}: {}", _callbackSocketPort,
							responseMessageString);
					}
				}
			}
			catch (IOException ioe) {
				if (!(ioe instanceof SocketException)) {
					_logger.error(ioe.getMessage(), ioe);
				}

				disconnect();

				fireSocketCloseListeners();
			}
		}
	}

	private static final String _RETURN_NEW_LINE = "\r\n";

	private static Logger _logger = LoggerFactory.getLogger(
		UnixNativityControlBaseImpl.class.getName());

	private static int _callbackSocketPort = 33002;
	private static int _commandSocketPort = 33001;
	private static ObjectMapper _objectMapper = new ObjectMapper().configure(
		JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

	private BufferedReader _callbackBufferedReader;
	private DataOutputStream _callbackOutputStream;
	private Socket _callbackSocket;
	private ReadThread _callbackThread;
	private BufferedReader _commandBufferedReader;
	private DataOutputStream _commandOutputStream;
	private Socket _commandSocket;
	private boolean _connected = false;

}