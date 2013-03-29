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

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.listeners.SocketCloseListener;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.ObjectBinder;
import flexjson.ObjectFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.lang.reflect.Type;

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
				new InputStreamReader(_serviceSocket.getInputStream()));

			_serviceOutputStream = new DataOutputStream(
				_serviceSocket.getOutputStream());

			_callbackSocket = new Socket("127.0.0.1", _callbackSocketPort);

			_callbackBufferedReader = new BufferedReader(
				new InputStreamReader(_callbackSocket.getInputStream()));

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
		String command = _jsonSerializer.exclude("*.class").deepSerialize(
			message);

		try {
			command += "\r\n";

			_serviceOutputStream.writeBytes(command);

			String reply = _serviceBufferedReader.readLine();

			if (reply == null) {
				_serviceSocket.close();

				if (_socketCloseListener != null) {
					_socketCloseListener.onSocketClose();
				}
			}

			return reply;
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);

			return "";
		}
	}

	@Override
	public void setRootFolder(String folder) {
		NativityMessage message = new NativityMessage(
			Constants.SET_ROOT_FOLDER, folder);

		sendMessage(message);
	}

	public void setSocketCloseListener(
		SocketCloseListener socketCloseListener) {

		_socketCloseListener = socketCloseListener;
	}

	@Override
	public void setSystemFolder(String folder) {
	}

	@Override
	public boolean startPlugin(String path) throws Exception {
		_logger.trace("Starting Finder plugin helper");

		Process process = Runtime.getRuntime().exec(path);

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

					if (_socketCloseListener != null) {
						_socketCloseListener.onSocketClose();
					}

					break;
				}

				ObjectFactory objectFactory = new ObjectFactory() {
					@Override
					public Object instantiate(
						ObjectBinder context, Object value, Type targetType,
						Class targetClass) {

						return value;
					}
				};

				JSONDeserializer<NativityMessage> _messageJSONDeserializer =
					new JSONDeserializer<NativityMessage>().use(
						"value", objectFactory);

				NativityMessage message = _messageJSONDeserializer.deserialize(
					data, NativityMessage.class);

				NativityMessage responseMessage = fireOnMessage(message);

				String response;

				if (responseMessage == null) {
					response = "\r\n";
				}
				else {
					response = _jsonSerializer.exclude("*.class")
						.deepSerialize(responseMessage) + "\r\n";
				}

				_callbackOutputStream.writeBytes(response);
			}
			catch (IOException ioe) {
				_logger.error(ioe.getMessage(), ioe);
			}
		}
	}

	private static int _callbackSocketPort = 33002;
	private static JSONSerializer _jsonSerializer = new JSONSerializer();

	private static Logger _logger = LoggerFactory.getLogger(
		AppleNativityControlImpl.class.getName());

	private static int _serviceSocketPort = 33001;

	private BufferedReader _callbackBufferedReader;
	private DataOutputStream _callbackOutputStream;
	private Socket _callbackSocket;
	private ReadThread _callbackThread;
	private BufferedReader _serviceBufferedReader;
	private DataOutputStream _serviceOutputStream;
	private Socket _serviceSocket;
	private SocketCloseListener _socketCloseListener;

}