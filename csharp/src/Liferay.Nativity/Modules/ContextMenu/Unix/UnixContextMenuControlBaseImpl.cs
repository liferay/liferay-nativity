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
using System.Collections;
using System.Collections.Generic;
using System.Linq;

using log4net;
using Newtonsoft.Json.Linq;

using Liferay.Nativity.Control;

namespace Liferay.Nativity.Modules.ContextMenu.Unix
{
	/**
	 * @author Dennis Ju, ported to C# by Andrew Rondeau
	 */
	public abstract class UnixContextMenuControlBaseImpl : ContextMenuControl
	{
		private ILog log = LogManager.GetLogger(typeof(UnixContextMenuControlBaseImpl));

		public UnixContextMenuControlBaseImpl(NativityControl nativityControl, ContextMenuControlCallback contextMenuControlCallback)
			: base(nativityControl, contextMenuControlCallback)
		{
			nativityControl.RegisterMessageListener(Constants.GET_CONTEXT_MENU_ITEMS, this.GetContextMenuItems);
			nativityControl.RegisterMessageListener(Constants.FIRE_CONTEXT_MENU_ACTION, this.RaiseContextMenuItem_Selected);
		}

		public NativityMessage GetContextMenuItems (NativityMessage message)
		{
			var currentFilesJArray = message.Value as JArray;

			var currentFiles = currentFilesJArray.Cast<string>();
			
			var contextMenuItems = this.GetContextMenuItems(currentFiles);
			return new NativityMessage(Constants.MENU_ITEMS, contextMenuItems);
		}

		public NativityMessage RaiseContextMenuItem_Selected (NativityMessage nativityMessage)
		{
			var message = nativityMessage.Value as JObject;

			if (null == message)
			{
				return null;
			}

			JToken uuidToken;
			if (message.TryGetValue("uuid", out uuidToken))
			{
				Guid uuid;
				try
				{
					uuid = (Guid)uuidToken;
				}
				catch
				{
					log.ErrorFormat("{0}, ({1}), is not a valid Guid", uuidToken, uuidToken.GetType());
					return null;
				}

				JToken filesToken;
				if (message.TryGetValue("files", out filesToken))
				{
					if (filesToken is JArray)
					{
						var files = ((JArray)filesToken).Cast<string>();
						this.RaiseContextMenu_Selected(uuid, files);
					}
				}
			}

			return null;
		}
	}
}
