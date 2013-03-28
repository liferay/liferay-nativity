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

package com.liferay.nativity.test;

import com.liferay.nativity.modules.contextmenu.ContextMenuControl;
import com.liferay.nativity.plugincontrol.NativityPluginControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gail Hernandez
 */
public class TestContextMenuControl extends ContextMenuControl {

	public TestContextMenuControl(NativityPluginControl pluginControl) {
		super(pluginControl);

		_random = new Random();
	}

	@Override
	public String[] getHelpItemsForMenus(String[] paths) {
		_logger.debug("getHelpItemsForMenus {}", paths);

		int count = _random.nextInt(20) + 3;

		List<String> items = new ArrayList<String>();

		for (int i = 0; i < count; i++) {
			items.add("Help " + i);
		}

		return items.toArray(new String[0]);
	}

	@Override
	public String[] getMenuItems(String[] paths) {
		_logger.debug("getMenuItems {}", paths);

		int count = _random.nextInt(20) + 3;

		List<String> items = new ArrayList<String>();

		for (int i = 0; i < count; i++) {
			items.add("Menu " + i);
		}

		return items.toArray(new String[0]);
	}

	private static Logger _logger = LoggerFactory.getLogger(
		TestContextMenuControl.class.getName());

	private Random _random;

}