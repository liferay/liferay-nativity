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

using Liferay.Nativity.Control;
using Liferay.Nativity.Modules.FileIcon;

namespace Liferay.Nativity.Modules.FileIcon.Unix
{
	public class AppleFileIconControlImpl : UnixFileIconControlBaseImpl
	{
		public AppleFileIconControlImpl(
			NativityControl nativityControl,
			FileIconControlCallback fileIconControlCallback)
			: base(nativityControl, fileIconControlCallback)
		{
		}
		
		public override void DisableFileIcons() 
		{
			var message = new NativityMessage(Constants.ENABLE_FILE_ICONS_WITH_CALLBACK, false);
			this.nativityControl.SendMessage(message);
		}
		
		public override void EnableFileIcons()
		{
			var message = new NativityMessage(Constants.ENABLE_FILE_ICONS_WITH_CALLBACK, true);
			this.nativityControl.SendMessage(message);
		}

		public override void RemoveFileIcon(string path)
		{
			var message = new NativityMessage(Constants.REPAINT_ALL_ICONS, string.Empty);
			this.nativityControl.SendMessage(message);
		}
		
		public override void RemoveFileIcons (IEnumerable<string> paths)
		{
			var message = new NativityMessage (Constants.REPAINT_ALL_ICONS, string.Empty);
			this.nativityControl.SendMessage (message);
		}
		
		public override void SetFileIcon(string path, int iconId)
		{
			var message = new NativityMessage(Constants.REPAINT_ALL_ICONS, string.Empty);
			this.nativityControl.SendMessage(message);
		}
		
		public override void SetFileIcons(IDictionary<string, int> fileIconsMap)
		{
			var message = new NativityMessage(Constants.REPAINT_ALL_ICONS, string.Empty);
			this.nativityControl.SendMessage(message);
		}
	}
}

