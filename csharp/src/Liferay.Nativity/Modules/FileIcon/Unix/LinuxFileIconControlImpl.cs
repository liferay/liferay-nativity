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

using Liferay.Nativity.Control;

namespace Liferay.Nativity.Modules.FileIcon.Unix
{
	/**
	 * @author Dennis Ju, ported to C# by Andrew Rondeau. Support for icons added by Ivan Burlakov
	 */
	public abstract class LinuxFileIconControlBaseImpl : FileIconControlBase
	{
		private const int MESSAGE_BUFFER_SIZE = 500;
		
		public LinuxFileIconControlBaseImpl(
			NativityControl nativityControl,
			FileIconControlCallback fileIconControlCallback)
			: base(nativityControl, fileIconControlCallback)
		{
		}
		
		public override void DisableFileIcons() 
		{
			var message = new NativityMessage(Constants.ENABLE_FILE_ICONS, false);
			this.nativityControl.SendMessage(message);
		}
		
		public override void EnableFileIcons()
		{
			var message = new NativityMessage(Constants.ENABLE_FILE_ICONS, true);
			this.nativityControl.SendMessage(message);
		}

		public override void RemoveFileIcon(string path)
		{
			var message = new NativityMessage(Constants.REMOVE_FILE_ICONS, new string[] { path });
			this.nativityControl.SendMessage(message);
		}
		
		public override void RemoveFileIcons (IEnumerable<string> paths)
		{
			var list = new List<string> (LinuxFileIconControlBaseImpl.MESSAGE_BUFFER_SIZE);
			
			foreach (var path in paths)
			{
				list.Add (path);
				
				if (list.Count >= LinuxFileIconControlBaseImpl.MESSAGE_BUFFER_SIZE)
				{
					var message = new NativityMessage (Constants.REMOVE_FILE_ICONS, list);
					this.nativityControl.SendMessage (message);
					
					list.Clear ();
				}
			}
			
			if (list.Count > 0)
			{
				var message = new NativityMessage (Constants.REMOVE_FILE_ICONS, list);
				this.nativityControl.SendMessage (message);
			}
		}
		
		public override void SetFileIcon(string path, int iconId)
		{
			var map = new Dictionary<string, int>(1);
			
			map[path] = iconId;
			
			var message = new NativityMessage(Constants.SET_FILE_ICONS, map);
			this.nativityControl.SendMessage(message);
		}
		
		public override void SetFileIcons(IDictionary<string, int> fileIconsMap)
		{
			var map = new Dictionary<string, int>(LinuxFileIconControlBaseImpl.MESSAGE_BUFFER_SIZE);
			
			foreach (var entry in fileIconsMap)
			{
				map[entry.Key] = entry.Value;
				
				if (map.Count >= LinuxFileIconControlBaseImpl.MESSAGE_BUFFER_SIZE)
				{
					var message = new NativityMessage(Constants.SET_FILE_ICONS, map);
					this.nativityControl.SendMessage(message);
					
					map.Clear();
				}
			}
			
			if (map.Count > 0)
			{
				var message = new NativityMessage(Constants.SET_FILE_ICONS, map);
				this.nativityControl.SendMessage(message);
			}
		}
	}
}
