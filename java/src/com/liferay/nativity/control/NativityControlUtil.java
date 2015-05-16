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

import com.liferay.nativity.control.findersync.FSNativityControlImpl;
import com.liferay.nativity.control.unix.AppleNativityControlImpl;
import com.liferay.nativity.control.unix.LinuxNativityControlImpl;
import com.liferay.nativity.control.win.WindowsNativityControlImpl;
import com.liferay.nativity.util.OSDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class NativityControlUtil {

	/**
	 * Gets an instance of NativityControl
	 *
	 * @return implementation of NativityControl instance based on the
	 * user's operating system. Returns null for unsupported operating systems.
	 */
	public static NativityControl getNativityControl() {
		if (_nativityControl == null) {
			if (OSDetector.isApple()) {
				if (OSDetector.isMinimumAppleVersion(
						OSDetector.MAC_YOSEMITE_10_10)) {

					return new FSNativityControlImpl();
				}
				else {
					_nativityControl = new AppleNativityControlImpl();
				}
			}
			else if (OSDetector.isWindows()) {
				_nativityControl = new WindowsNativityControlImpl();
			}
			else if (OSDetector.isLinux()) {
				_nativityControl = new LinuxNativityControlImpl();
			}
			else {
				_logger.error(
					"{} is not supported", System.getProperty("os.name"));

				_nativityControl = null;
			}
		}

		return _nativityControl;
	}

	private static Logger _logger = LoggerFactory.getLogger(
		NativityControlUtil.class.getName());

	private static NativityControl _nativityControl;

}