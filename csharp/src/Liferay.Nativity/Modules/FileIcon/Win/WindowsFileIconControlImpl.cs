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
using System.Text;
using Liferay.Nativity.Control;
using Microsoft.Win32;

namespace Liferay.Nativity.Modules.FileIcon.Win
{
	/**
	* @author Dennis Ju
	* @author Patryk Strach - C# port
	*/
	public class WindowsFileIconControlImpl : FileIconControlBase
	{
		public WindowsFileIconControlImpl(NativityControl nativityControl, FileIconControlCallback fileIconControlCallback)
			: base(nativityControl, fileIconControlCallback)
		{
		}

		public override void DisableFileIcons()
		{
			Registry.SetValue(Constants.NATIVITY_REGISTRY_KEY, Constants.ENABLE_OVERLAY_REGISTRY_NAME, "0");
		}

		public override void EnableFileIcons()
		{
			Registry.SetValue(Constants.NATIVITY_REGISTRY_KEY, Constants.ENABLE_OVERLAY_REGISTRY_NAME, "1");
		}

		public override void RefreshIcons()
		{
		}

		public override int RegisterIcon(string path)
		{
			return 0;
		}

		public override int RegisterMenuIcon(string path)
		{
			return 0;
		}

		public override void RemoveAllFileIcons()
		{
		}

		public override void RemoveFileIcon(string path)
		{
		}

		public override void RemoveFileIcons(IEnumerable<string> paths)
		{
		}

		public override void SetFileIcon(string path, int iconId)
		{
		}

		public override void SetFileIcons(IDictionary<string, int> fileIconsMap)
		{
		}

		public override void UnregisterIcon(int id)
		{
		}
	}
}
