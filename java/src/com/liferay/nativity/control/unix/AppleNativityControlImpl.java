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

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityMessage;
import com.liferay.nativity.util.mac.AppleUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class AppleNativityControlImpl extends UnixNativityControlBaseImpl {

	@Override
	public boolean load() throws Exception {
		_logger.trace("Loading Liferay Nativity");

		return AppleUtil.load();
	}

	@Override
	public boolean loaded() {
		return AppleUtil.loaded();
	}

	@Override
	public void setFilterFolder(String folder) {
		setFilterFolders(new String[] { folder });
	}

	@Override
	public void setFilterFolders(String[] folders) {
		NativityMessage message = new NativityMessage(
			Constants.SET_FILTER_PATHS, folders);

		sendMessage(message);
	}

	@Override
	public boolean unload() throws Exception {
		_logger.trace("Unloading Liferay Nativity");

		return AppleUtil.unload();
	}

	private static Logger _logger = LoggerFactory.getLogger(
		AppleNativityControlImpl.class.getName());

}