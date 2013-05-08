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

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class WindowsNativityControlImpl extends NativityControl {

	@Override
	public boolean connect() {
		_logger.debug("Connecting...");

		if (running()) {
			return true;
		}

		_receive = new WindowsReceiveSocket(this);

		_receiveExecutor.execute(_receive);
		_sendExecutor.execute(_send);

		_logger.debug("Done connecting");

		return true;
	}

	@Override
	public boolean disconnect() {
		return true;
	}

	@Override
	public boolean running() {
		return _send.isConnected();
	}

	@Override
	public String sendMessage(NativityMessage message) {
		_send.send(message);

		return "";
	}

	@Override
	public void setSystemFolder(String folder) {
		NativityMessage message = new NativityMessage(
			Constants.SET_SYSTEM_FOLDER, folder);

		sendMessage(message);
	}

	@Override
	public boolean startPlugin(String path) throws Exception {
		return true;
	}

	private static Logger _logger = LoggerFactory.getLogger(
		WindowsNativityControlImpl.class.getName());

	private WindowsReceiveSocket _receive;

	private ExecutorService _receiveExecutor =
		Executors.newSingleThreadExecutor();

	private WindowsSendSocket _send = new WindowsSendSocket();

	private ExecutorService _sendExecutor = Executors.newSingleThreadExecutor();

}