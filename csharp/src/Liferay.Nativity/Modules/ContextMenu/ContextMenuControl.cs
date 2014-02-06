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

using log4net;

using Liferay.Nativity.Control;

namespace Liferay.Nativity.Modules.ContextMenu
{
	/**
	 * @author Dennis Ju, ported to C# by Andrew Rondeau
	 */
	public abstract class ContextMenuControl
	{
		private static readonly ILog log = LogManager.GetLogger(typeof(ContextMenuControl));

		protected ContextMenuControlCallback contextMenuControlCallback;

		protected Dictionary<Guid, Model.ContextMenuItem> contextMenuItems = new Dictionary<Guid, Model.ContextMenuItem>();

		protected NativityControl nativityControl;

		public ContextMenuControl(NativityControl nativityControl, ContextMenuControlCallback contextMenuControlCallback)
		{
			this.nativityControl = nativityControl;
			this.contextMenuControlCallback = contextMenuControlCallback;
		}
		
		public void RaiseContextMenu_Selected (Guid uuid, IEnumerable<string> paths)
		{
			Model.ContextMenuItem contextMenuItem;
			if (this.contextMenuItems.TryGetValue (uuid, out contextMenuItem))
			{
				contextMenuItem.TriggerSelected (paths);
			}
			else
			{
				var registeredMenuItems = string.Join(", ", this.contextMenuItems.Values.Select(cmi => string.Format("{0}:{1}", cmi.Title, cmi.Uuid)).ToArray());
				log.WarnFormat ("No registered handler for uuid {0}. Registered menu items: {1}", uuid, registeredMenuItems);
			}
		}
		
		/// <summary>
		/// Called by the native service to request the menu items for a context
		/// menu popup
		/// </summary>
		/// <returns>each ContextMenuItem instance in the list will appear at the
		/// context menu's top level</returns>
		/// <param name="paths">the files selected for this context menu popup</param>
		public IEnumerable<Model.ContextMenuItem> GetContextMenuItems(IEnumerable<string> paths) 
		{
			var newContextMenuItems = this.contextMenuControlCallback(paths);

			// TODO: There's a potential memory leak here
			// TODO: There appears to be no message sent when the context menu is closed. When it's closed, this.contextMenuItems should be cleared
			// TODO: The consequences of this is that, if someone leaves a context menu open for a long time, it will hold references to objects and keep those references alive

			if (newContextMenuItems == null)
			{
				this.contextMenuItems.Clear();
				return null;
			}

			newContextMenuItems = newContextMenuItems.ToArray();

			this.contextMenuItems = newContextMenuItems.SelectMany(cmi => cmi.GetAllContextMenuItems()).ToDictionary(cmi => cmi.Uuid);
			return newContextMenuItems;
		}
	}
}
