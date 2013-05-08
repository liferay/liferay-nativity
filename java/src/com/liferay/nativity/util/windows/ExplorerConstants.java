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

package com.liferay.nativity.util.windows;

/**
 * @author Gail Hernandez
 */
public enum ExplorerConstants {

	SHCNE_CREATE(0), SHCNE_DELETE(1), SHCNE_MKDIR(2), SHCNE_RENAMEFOLDER(3),
	SHCNE_RENAMEITEM(4), SHCNE_RMDIR(5), SHCNE_UPDATEDIR(6),
	SHCNE_UPDATEITEM(7);

	ExplorerConstants(int value) {
		_value = value;
	}

	public int getValue() {
		return _value;
	}

	private final int _value;

}