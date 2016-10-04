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

package com.liferay.nativity.control;

import com.liferay.nativity.listeners.SocketCloseListener;
import com.liferay.nativity.listeners.SocketOpenListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public abstract class NativityControl {

	public NativityControl() {
		_commandMap = new HashMap<String, MessageListener>();
		socketCloseListeners = new ArrayList<SocketCloseListener>();
		socketOpenListeners = new ArrayList<SocketOpenListener>();
	}

	/**
	 * Adds the given path to the favorites list in Finder or Explorer
	 *
	 * @param path The full path of the file or folder to add to the favorites
	 * list
	 */
	public abstract void addFavoritesPath(String path);

	/**
	 * Adds a SocketCloseListener that will be triggered when the socket
	 * connection to the native service is closed
	 *
	 * @param socketCloseListener The SocketCloseListener instance to add
	 */
	public void addSocketCloseListener(
		SocketCloseListener socketCloseListener) {

		socketCloseListeners.add(socketCloseListener);
	}

	/**
	 * Adds a SocketOpenListener that will be triggered when the socket
	 * connection to the native service is closed
	 *
	 * @param socketOpenListener The SocketOpenListener instance to add
	 */
	public void addSocketOpenListener(SocketOpenListener socketOpenListener) {
		socketOpenListeners.add(socketOpenListener);
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
	 * @param message The NativityMessage instance received from the native
	 * service
	 *
	 * @return NativityMessage to send back to the native service. Returns null
	 * if no registered MessageListener is found or if no response
	 * needs to be sent back to the native service.
	 */
	public NativityMessage fireMessage(NativityMessage message) {
		_logger.trace("Firing message: {}", message.getCommand());

		MessageListener messageListener = _commandMap.get(message.getCommand());

		if (messageListener == null) {
			return null;
		}

		return messageListener.onMessage(message);
	}

	/**
	 * Fires all SocketCloseListeners
	 */
	public void fireSocketCloseListeners() {
		for (SocketCloseListener listener : socketCloseListeners) {
			listener.onSocketClose();
		}
	}

	/**
	 * Fires all SocketOpenListeners
	 */
	public void fireSocketOpenListeners() {
		for (SocketOpenListener listener : socketOpenListeners) {
			listener.onSocketOpen();
		}
	}

	/**
	 * Mac Finder Sync only
	 *
	 * Gets all currently observed folders
	 *
	 * @return set of observed folder paths
	 */
	public abstract Set<String> getAllObservedFolders();

	/**
	 * Mac Injector only
	 *
	 * Loads Liferay Nativity into Finder.
	 *
	 * @return true if successfully loaded
	 */
	public abstract boolean load() throws Exception;

	/**
	 * Mac Injector only
	 *
	 * Check if Liferay Nativity is loaded in Finder.
	 *
	 * @return true if loaded
	 */
	public abstract boolean loaded();

	/**
	 * Windows only
	 *
	 * Causes Explorer to refresh the display of the file in explorer
	 *
	 * @param paths The array of file paths to refresh
	 *
	 * @deprecated as of 1.4. Use FileIconControl.refreshIcons(paths).
	 */
	@Deprecated
	public abstract void refreshFiles(String[] paths);

	/**
	 * Used by modules to register a MessageListener that will respond to
	 * messages received from the native service. Each registered
	 * MessageListener instance must have a unique "command" parameter.
	 * Registering an instance with the same "command" parameter will replace
	 * previously registered instances.
	 *
	 * @param messageListener The MessageListener instance to register
	 */
	public void registerMessageListener(MessageListener messageListener) {
		_commandMap.put(messageListener.getCommand(), messageListener);
	}

	/**
	 * Removes the given path to the favorites list in Finder or Explorer
	 *
	 * @param path The full path of the file or folder to remove from the
	 * favorites list
	 */
	public abstract void removeFavoritesPath(String path);

	/**
	 * Removes a previously added SocketCloseListener instance
	 *
	 * @param socketCloseListener The SocketCloseListener instance to remove
	 */
	public void removeSocketCloseListener(
		SocketCloseListener socketCloseListener) {

		socketCloseListeners.remove(socketCloseListener);
	}

	/**
	 * Removes a previously added SocketOpenListener instance
	 *
	 * @param socketOpenListener The SocketOpenListener instance to remove
	 */
	public void removeSocketOpenListener(
		SocketOpenListener socketOpenListener) {

		socketOpenListeners.remove(socketOpenListener);
	}

	/**
	 * Mac Finder Sync and Injector only
	 *
	 * Used by modules to send messages to the native service.
	 *
	 * @param nativityMessage The NativityMessage instance to send to the native
	 * service
	 *
	 * @return response from the native service
	 */
	public abstract String sendMessage(NativityMessage nativityMessage);

	/**
	 * Convenience method for calling setFilterFolders with one folder.
	 *
	 * @param folder The folder path to filter by (inclusive)
	 */
	public abstract void setFilterFolder(String folder);

	/**
	 * Required for Mac Finder Sync. Optional for others.
	 *
	 * Set the root folder filter path for requests made
	 * to the native service. For example, setting a value of "/test/folder"
	 * indicates that any requests for files that are not a child of
	 * "/test/folder" will be ignored. This can improve native performance.
	 *
	 * @param folders The folder paths to filter by (inclusive)
	 */
	public abstract void setFilterFolders(String[] folders);

	/**
	 * Mac Finder Sync only
	 *
	 * Set the file path that Nativity will write the port number to
	 * upon successfully opening the port. The file will be automatically
	 * deleted upon graceful shutdown. The Finder Sync plugin should read from
	 * this same path to determine what port number to connect to. If this
	 * value is not set, Nativity will default to the file path
	 * {user.home}/.liferay-nativity/port
	 *
	 * @param path The path of the file containing the port number
	 */
	public abstract void setPortFilePath(String path);

	/**
	 * Windows only
	 *
	 * Marks the specified folder as a system folder so that Desktop.ini values
	 * will take effect.
	 *
	 * @param folder The path of the folder to set as a system folder
	 *
	 * @deprecated as of 1.4. Use Java 7's DosFileAttributeView class or Java 6
	 * with Runtime.exec(...) with attrib system commands.
	 */
	@Deprecated
	public abstract void setSystemFolder(String folder);

	/**
	 * Mac only
	 *
	 * Unloads Liferay Nativity from Finder.
	 *
	 * @return true if successfully unloaded
	 */
	public abstract boolean unload() throws Exception;

	protected List<SocketCloseListener> socketCloseListeners;
	protected List<SocketOpenListener> socketOpenListeners;

	private static Logger _logger = LoggerFactory.getLogger(
		NativityControl.class.getName());

	private Map<String, MessageListener> _commandMap;

}