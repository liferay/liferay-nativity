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

package com.liferay.nativity.plugincontrol;

import com.liferay.nativity.plugincontrol.linux.LinuxNativityPluginControlImpl;
import com.liferay.nativity.plugincontrol.mac.AppleNativityPluginControlImpl;
import com.liferay.nativity.plugincontrol.win.WindowsNativityPluginControlImpl;
import com.liferay.nativity.util.OSDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class NativityPluginControlUtil {

	/**
	 * Static method to get an instance of NativityPluginControl
	 *
	 * @return implementation of NativityPluginControl instance based on the
	 * users operating system. Returns null for unsupported operating systems.
	 */
	public static NativityPluginControl getNativityPluginControl() {
		if (_nativityPluginControl == null) {
			if (OSDetector.isApple()) {
				_nativityPluginControl = new AppleNativityPluginControlImpl();
			}
			else if (OSDetector.isWindows()) {
				_nativityPluginControl = new WindowsNativityPluginControlImpl();
			}
			else if (OSDetector.isLinux()) {
				_nativityPluginControl = new LinuxNativityPluginControlImpl();
			}
			else {
				_logger.error(
					"NativityPluginControl does not support {}",
					System.getProperty("os.name"));

				_nativityPluginControl = null;
			}
		}

		return _nativityPluginControl;
	}

	private static Logger _logger = LoggerFactory.getLogger(
		NativityPluginControlUtil.class.getName());

	private static NativityPluginControl _nativityPluginControl;

}