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

using System;
using System.Collections.Generic;
using System.Linq;

using Liferay.Nativity.Control;
using Liferay.Nativity.Modules.ContextMenu;
using Liferay.Nativity.Modules.ContextMenu.Model;
using Liferay.Nativity.Modules.FileIcon;

namespace Liferay.Nativity.Example
{
	class MainClass
	{
		private static readonly string started = DateTime.Now.ToShortTimeString();

		public static void Main (string[] args)
		{
			MainClass.Connect ();
		}

		private static void Connect()
		{
			NativityControlUtil.NativityControl.Connect();

			// File Icons
			var testIconId = -1;
			var testFilePath = "/Users/rondea/bar.txt";

			// FileIconControlCallback not used on Linux
			Modules.FileIcon.FileIconControlCallback fileIconControlCallback = path =>
			{
				return path == testFilePath ? testIconId : -1;
			};
			
			var fileIconControl = FileIconControlUtil.GetFileIconControl(NativityControlUtil.NativityControl, fileIconControlCallback);

			fileIconControl.EnableFileIcons();
			
			testIconId = fileIconControl.RegisterIcon("/Users/rondea/git/client/x-platform/resources/cocoa/overlay_Check.icns");

			// FileIconControl.setFileIcon() method only used by Mac and Linux
			fileIconControl.SetFileIcon(testFilePath, testIconId);

			// Context Menus
			ContextMenuControlUtil.GetContextMenuControl(NativityControlUtil.NativityControl, MainClass.ContextMenuControlCallback);

			// This won't work unless the sample program starts the nativity plugin on Mac
			//NativityControlUtil.NativityControl.SocketClosed += MainClass.Connect;
		}

		private static IEnumerable<ContextMenuItem> ContextMenuControlCallback (IEnumerable<string> paths)
		{
			Console.WriteLine ("Investigating {0}", string.Join (" ,", paths.ToArray ()));

			List<ContextMenuItem> contextMenuItems = new List<ContextMenuItem> ();

			//if (paths.All (p => p == "/Users/sync/foo.txt"))
			//{
				var contextMenuItem = new ContextMenuItem("Nativity Test: " + started);
				contextMenuItem.Selected += MainClass.ContextMenuItem_HandleSelected;

				contextMenuItems.Add(contextMenuItem);

				contextMenuItem.AddSeparator();

				for (var ctr = 0; ctr < 3; ctr++)
				{
					var sub = new ContextMenuItem(string.Empty);
					sub.Selected += (s,p) => Console.WriteLine(sub.Uuid);
					sub.Title = sub.Uuid.ToString();
					contextMenuItem.ContextMenuItems.Add(sub);
				}
			//}

			return contextMenuItems;
		}

		static void ContextMenuItem_HandleSelected (ContextMenuItem sender, IEnumerable<string> paths)
		{
			Console.WriteLine("{0} selected by {1} ({2})", string.Join(" ,", paths.ToArray()), sender, MainClass.started);
		}
	}
}
