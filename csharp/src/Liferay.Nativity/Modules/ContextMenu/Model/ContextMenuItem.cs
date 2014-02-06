/**
 * Syncplicity, LLC © 2014 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * If you would like a copy of source code for this product, EMC will provide a
 * copy of the source code that is required to be made available in accordance
 * with the applicable open source license.  EMC may charge reasonable shipping
 * and handling charges for such distribution.  Please direct requests in writing
 * to EMC Legal, 176 South St., Hopkinton, MA 01748, ATTN: Open Source Program
 * Office.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */

/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

using System;
using System.Collections.Generic;
using System.Linq;

using Newtonsoft.Json;

namespace Liferay.Nativity.Modules.ContextMenu.Model
{
	/**
	* @author Dennis Ju, ported to C# by Andrew Rondeau. Support for icons added by Ivan Burlakov
	*/
	/// <summary>
	/// Sample: {"contextMenuItems":[],"uuid":"9b4f28ef-1026-43de-a499-a1e979146ded","enabled":true,"helpText":null,"title":"Nativity Test: 2014/01/27 15:40:34"}
	/// </summary>
	[JsonObject(MemberSerialization.OptIn)]
	public class ContextMenuItem
	{
		private const string SEPARATOR = "_SEPARATOR_";

        public ContextMenuItem (string title) : this(title, -1)
		{

		}

        public ContextMenuItem(string title, int icon)
        {
            this.title = title;
            this.icon = icon;
        }

		[JsonProperty("enabled")]
		public bool Enabled
		{
			get { return this.enabled; }
			set { this.enabled = value; }
		}
		private bool enabled = true;

		[JsonProperty("helpText")]
		public string HelpText
		{
			get { return this.helpText; }
			set { this.helpText = value; }
		}
		private string helpText;

		[JsonProperty("title")]
		public string Title
		{
			get { return this.title; }
			set { this.title = value; }
		}
		private string title;

        [JsonProperty("icon")]
        public int Icon
        {
            get { return this.icon; }
            set { this.icon = value; }
        }
        private int icon;

		[JsonProperty("contextMenuItems")]
		public List<ContextMenuItem> ContextMenuItems
		{
			get { return this.contextMenuItems; }
		}
		private List<ContextMenuItem> contextMenuItems = new List<ContextMenuItem>();

		[JsonProperty("uuid")]
		public Guid Uuid
		{
			get { return this.uuid; }
		}
		private Guid uuid = Guid.NewGuid();

		//@JsonIgnore
		public IEnumerable<ContextMenuItem> GetAllContextMenuItems()
		{
			yield return this;

			foreach (var child in this.contextMenuItems.SelectMany(cmi => cmi.GetAllContextMenuItems()))
			{
				yield return child;
			}
		}
		
		public void AddSeparator() 
		{
			this.contextMenuItems.Add(new ContextMenuItem(ContextMenuItem.SEPARATOR));
		}
		
		public void AddSeparator(int index) 
		{
			this.contextMenuItems.Insert(index, new ContextMenuItem(ContextMenuItem.SEPARATOR));
		}

		public void TriggerSelected (IEnumerable<string> paths)
		{
			var contextMenuSelected = this.Selected;
			if (null != contextMenuSelected)
			{
				contextMenuSelected(this, paths);
			}
		}

		public event ContextMenuAction Selected;

		public override int GetHashCode ()
		{
			return this.uuid.GetHashCode();
		}

		public override string ToString ()
		{
			return string.Format ("{0} ({1}) {2}", this.title, this.uuid, this.contextMenuItems.Count);
		}
	}
}