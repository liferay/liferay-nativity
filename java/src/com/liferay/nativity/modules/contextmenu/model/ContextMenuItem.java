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

package com.liferay.nativity.modules.contextmenu.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Dennis Ju
 */
public class ContextMenuItem {

	public ContextMenuItem(String title) {
		_title = title;
		_enabled = true;
		_uuid = UUID.randomUUID().toString();
		_actions = new ArrayList<Action>();
		_contextMenuItems = new ArrayList<ContextMenuItem>();
	}

	public ContextMenuItem(
		String title, ContextMenuItem parentContextMenuItem) {

		this(title);

		parentContextMenuItem.addContextMenuItem(this);
	}

	public void addAction(Action action) {
		_actions.add(action);
	}

	public boolean addContextMenuItem(ContextMenuItem menuItem) {
		return _contextMenuItems.add(menuItem);
	}

	public void addContextMenuItem(ContextMenuItem menuItem, int index) {
		_contextMenuItems.add(index, menuItem);
	}

	public void addSeparator(int index) {
		_contextMenuItems.add(index, _getSeparator());
	}

	public boolean addSeparator() {
		return _contextMenuItems.add(_getSeparator());
	}

	public void fireActions(String[] paths) {
		for (Action action : _actions) {
			action.onSelection(paths);
		}
	}

	@JsonIgnore
	public List<ContextMenuItem> getAllContextMenuItems() {
		List<ContextMenuItem> contextMenuItems =
			new ArrayList<ContextMenuItem>();

		contextMenuItems.add(this);

		_addChildren(this, contextMenuItems);

		return contextMenuItems;
	}

	public List<ContextMenuItem> getContextMenuItems() {
		return _contextMenuItems;
	}

	public boolean getEnabled() {
		return _enabled;
	}

	public String getHelpText() {
		return _helpText;
	}

	public String getUuid() {
		return _uuid;
	}

	public String getTitle() {
		return _title;
	}

	public boolean removeContextMenuItem(ContextMenuItem menuItem) {
		return _contextMenuItems.remove(menuItem);
	}

	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}

	public void setHelpText(String helpText) {
		_helpText = helpText;
	}

	public void setTitle(String title) {
		_title = title;
	}

	public String toString() {
		return _title + " (" + _uuid + ") " + _contextMenuItems.size();
	}

	private static ContextMenuItem _getSeparator() {
		return new ContextMenuItem(_SEPARATOR);
	}

	private void _addChildren(
		ContextMenuItem contextMenuItem,
		List<ContextMenuItem> contextMenuItems) {

		for (ContextMenuItem childContextMenuItem :
				contextMenuItem.getContextMenuItems()) {

			contextMenuItems.add(childContextMenuItem);

			_addChildren(childContextMenuItem, contextMenuItems);
		}
	}

	private static final String _SEPARATOR = "_SEPARATOR_";

	private List<Action> _actions;
	private List<ContextMenuItem> _contextMenuItems;
	private boolean _enabled;
	private String _helpText;
	private String _uuid;
	private String _title;

}