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

package com.liferay.nativity.control.mac;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.listeners.SocketCloseListener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class AppleNativityControlImpl extends NativityControl {

	public boolean connect() {
		try {
			_serviceSocket = new Socket("127.0.0.1", _serviceSocketPort);

			_serviceBufferedReader = new BufferedReader(
				new InputStreamReader(
					_serviceSocket.getInputStream(), "UTF-8"));

			_serviceOutputStream = new DataOutputStream(
				_serviceSocket.getOutputStream());

			_callbackSocket = new Socket("127.0.0.1", _callbackSocketPort);

			_callbackBufferedReader = new BufferedReader(
				new InputStreamReader(
					_callbackSocket.getInputStream(), "UTF-8"));

			_callbackOutputStream = new DataOutputStream(
				_callbackSocket.getOutputStream());

			_callbackThread = new ReadThread(this);

			_callbackThread.start();

			return true;
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}

		return false;
	}

	public boolean disconnect() {
		try {
			_serviceSocket.close();

			return true;
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}

		return false;
	}

	@Override
	public boolean running() {
		boolean running = false;

		try {
			String grepCommand =
				"ps -e | grep com.liferay.FinderPluginHelper | grep -v grep";

			String[] cmd = { "/bin/sh", "-c", grepCommand };

			Process process = Runtime.getRuntime().exec(cmd);

			BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));

			while (bufferedReader.readLine() != null) {
				running = true;
			}

			bufferedReader.close();
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}

		_logger.trace("Finder plugin helper running: {}", running);

		return running;
	}

	@Override
	public String sendMessage(NativityMessage message) {
		try {
			_objectMapper.writeValue(_serviceOutputStream, message);

			_serviceOutputStream.write(_RETURN_NEW_LINE.getBytes("UTF-8"));

			String reply = _serviceBufferedReader.readLine();

			if (reply == null) {
				_serviceSocket.close();

				for (SocketCloseListener listener : socketCloseListeners) {
					listener.onSocketClose();
				}
			}

			return reply;
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);

			for (SocketCloseListener listener : socketCloseListeners) {
				listener.onSocketClose();
			}

			return "";
		}
	}

	@Override
	public void setSystemFolder(String folder) {
	}

	@Override
	public boolean startPlugin(String path) throws Exception {
		_logger.trace("Starting Finder plugin helper");

		Process process = Runtime.getRuntime().exec(new String[] { path });

		BufferedReader inputBufferedReader = new BufferedReader(
			new InputStreamReader(process.getInputStream()));

		BufferedReader errorBufferedReader = new BufferedReader(
			new InputStreamReader(process.getErrorStream()));

		String input = inputBufferedReader.readLine();

		while (input != null) {
			input = inputBufferedReader.readLine();
		}

		inputBufferedReader.close();

		String error = errorBufferedReader.readLine();

		if (error != null) {
			errorBufferedReader.close();

			_logger.trace(
				"Finder plugin helper failed to start. Error: {}", error);

			return false;
		}

		errorBufferedReader.close();

		process.waitFor();

		_logger.trace("Finder plugin helper successfully started");

		return true;
	}

	protected class ReadThread extends Thread {

		public ReadThread(AppleNativityControlImpl pluginControl) {
			_pluginControl = pluginControl;
		}

		@Override
		public void run() {
			_pluginControl._doCallbackLoop();
		}

		private AppleNativityControlImpl _pluginControl;

	}

	private void _doCallbackLoop() {
		while (_callbackSocket.isConnected()) {
			try {
				String data = _callbackBufferedReader.readLine();

				if (data == null) {
					_callbackSocket.close();

					for (SocketCloseListener listener : socketCloseListeners) {
						listener.onSocketClose();
					}

					break;
				}

				NativityMessage message = _objectMapper.readValue(
					data, NativityMessage.class);

				NativityMessage responseMessage = fireOnMessage(message);

				if (responseMessage != null) {
					_objectMapper.writeValue(
						_callbackOutputStream, responseMessage);
				}

				_callbackOutputStream.write(_RETURN_NEW_LINE.getBytes("UTF-8"));
			}
			catch (IOException ioe) {
				_logger.error(ioe.getMessage(), ioe);

				for (SocketCloseListener listener : socketCloseListeners) {
					listener.onSocketClose();
				}
			}
		}
	}

	private static final String _RETURN_NEW_LINE = "\r\n";

	private static int _callbackSocketPort = 33002;

	private static Logger _logger = LoggerFactory.getLogger(
		AppleNativityControlImpl.class.getName());

	private static ObjectMapper _objectMapper =
		new ObjectMapper().configure(
			JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

	private static int _serviceSocketPort = 33001;

	private BufferedReader _callbackBufferedReader;
	private DataOutputStream _callbackOutputStream;
	private Socket _callbackSocket;
	private ReadThread _callbackThread;
	private BufferedReader _serviceBufferedReader;
	private DataOutputStream _serviceOutputStream;
	private Socket _serviceSocket;

}