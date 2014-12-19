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

using System.IO;

namespace Liferay.Nativity.Control.Unix
{
	/**
	* @author Dennis Ju
	* @author Patryk Strach - C# port
	*/
	public class LinuxNativityControlImpl : UnixNativityControlBaseImpl
	{
		public override bool Load()
		{
			return false;
		}

		public override bool Loaded
		{
			get
			{
				return File.Exists("/usr/lib/nautilus/extensions-3.0/libliferaynativity.so") ||
				       File.Exists("/usr/lib64/nautilus/extensions-3.0/libliferaynativity.so");
			}
		}

		public override void RefreshFiles (System.Collections.Generic.IEnumerable<string> paths)
		{
		}

		public override void SetFilterFolders(params string[] folders)
		{
			if(folders.Length == 0)
				return;

			var message = new NativityMessage(Constants.SET_FILTER_PATH, folders[0]);

			SendMessage(message);
		}

		public override bool Unload()
		{
 			return false;
		}
	}
}
