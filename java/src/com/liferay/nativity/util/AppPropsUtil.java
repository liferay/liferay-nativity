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

package com.liferay.nativity.util;

import java.io.InputStream;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class AppPropsUtil {

	public static String getProperty(String key) {
		if (_properties.isEmpty()) {
			try {
				InputStream in = ClassLoader.getSystemResourceAsStream(
					"test-config.properties");

				if (in != null) {
					_properties.load(in);
				}
				else {
					_logger.error("Unable to read test-config.properties");
				}
			}
			catch (Exception e) {
				_logger.error(e.getMessage(), e);
			}
		}

		return _properties.getProperty(key);
	}

	private static Logger _logger = LoggerFactory.getLogger(
		AppPropsUtil.class.getName());

	private static Properties _properties = new Properties();

}