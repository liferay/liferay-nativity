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

package com.liferay.nativity.control.linux;

import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;

/**
 * @author Dennis Ju
 */
public class LinuxNativityControlImpl extends NativityControl {

	@Override
	public boolean connect() {
		return false;
	}

	@Override
	public boolean disconnect() {
		return false;
	}

	@Override
	public boolean running() {
		return false;
	}

	@Override
	public String sendMessage(NativityMessage message) {
		return null;
	}

	@Override
	public void setRootFolder(String folder) {
	}

	@Override
	public void setSystemFolder(String folder) {
	}

	@Override
	public boolean startPlugin(String path) throws Exception {
		return false;
	}

}