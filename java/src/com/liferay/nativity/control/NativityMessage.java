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

/**
 * @author Dennis Ju
 */
public class NativityMessage {

	public NativityMessage() {
	}

	public NativityMessage(String command, Object value) {
		_command = command;
		_value = value;
	}

	public String getCommand() {
		return _command;
	}

	public Object getValue() {
		return _value;
	}

	public void setCommand(String command) {
		_command = command;
	}

	public void setValue(Object value) {
		_value = value;
	}

	private String _command;
	private Object _value;

}