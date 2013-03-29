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

package com.liferay.nativity.control;

import com.liferay.nativity.control.linux.LinuxNativityControlImpl;
import com.liferay.nativity.control.mac.AppleNativityControlImpl;
import com.liferay.nativity.control.win.WindowsNativityControlImpl;
import com.liferay.nativity.util.OSDetector;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public abstract class NativityControl {

	/**
	 * Factory method to get an instance of NativityPluginControl
	 *
	 * @return implementation of NativityPluginControl instance based on the
	 * user's operating system. Returns null for unsupported operating systems.
	 */
	public static NativityControl getNativityControl() {
		if (_nativityControl == null) {
			if (OSDetector.isApple()) {
				_nativityControl = new AppleNativityControlImpl();
			}
			else if (OSDetector.isWindows()) {
				_nativityControl = new WindowsNativityControlImpl();
			}
			else if (OSDetector.isLinux()) {
				_nativityControl = new LinuxNativityControlImpl();
			}
			else {
				_logger.error(
					"NativityControl does not support {}",
					System.getProperty("os.name"));

				_nativityControl = null;
			}
		}

		return _nativityControl;
	}

	public NativityControl() {
		_commandMap = new HashMap<String, MessageListener>();
	}

	/**
	 * Initialize connection with native service
	 *
	 * @return true if connection is successful
	 */
	public abstract boolean connect();

	/**
	 * Initialize disconnection with native service
	 *
	 * @return true if disconnection is successful
	 */
	public abstract boolean disconnect();

	/**
	 * Triggers the appropriate registered MessageListener when messages are
	 * received from the native service.
	 *
	 * @param NativityMessage received from the native service
	 *
	 * @return NativityMessage to send back to the native service. Returns null
	 * if no registered MessageListener is found or if no response
	 * needs to be sent back to the native service.
	 */
	public NativityMessage fireOnMessage(NativityMessage message) {
		MessageListener messageListener = _commandMap.get(message.getCommand());

		if (messageListener == null) {
			return null;
		}

		return messageListener.onMessage(message);
	}

	/**
	 * Used by modules to register a MessageListener that will respond to
	 * messages received from the native service. Each registered
	 * MessageListener instance must have a unique "command" parameter.
	 * Registering an instance with the same "command" parameter will replace
	 * previously registered instances.
	 *
	 * @param MessageListener to register
	 */
	public void registerMessageListener(MessageListener messageListener) {
		_commandMap.put(messageListener.getCommand(), messageListener);
	}

	/**
	 * Checks if the native service is running
	 *
	 * @return true if native service is running
	 */
	public abstract boolean running();

	/**
	 * Used by modules to send messages to the native service.
	 *
	 * @param NativityMessage to send to the native service
	 *
	 * @return response from the native service
	 */
	public abstract String sendMessage(NativityMessage message);

	/**
	 * Optionally set the root folder filter path for requests made
	 * to the native service. For example, setting a value of "/test/folder"
	 * indicates that any requests (like custom context menus) for files that
	 * are not a child of "/test/folder" will be ignored. This can improve
	 * native performance.
	 *
	 * @param root folder path to filter by (inclusive)
	 */
	public abstract void setRootFolder(String folder);

	/**
	 * Windows only
	 *
	 * Marks the specified folder as a system folder so that Desktop.ini values
	 * will take effect.
	 *
	 * @param folder to set as a system folder
	 */
	public abstract void setSystemFolder(String folder);

	/**
	 * Mac only
	 *
	 * Starts the native service.
	 *
	 * @param
	 *
	 * @return true if the service successfully started
	 */
	public abstract boolean startPlugin(String path) throws Exception;

	private static Logger _logger = LoggerFactory.getLogger(
		NativityControl.class.getName());

	private static NativityControl _nativityControl;

	private Map<String, MessageListener> _commandMap;

}