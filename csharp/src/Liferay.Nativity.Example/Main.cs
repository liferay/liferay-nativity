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
using Liferay.Nativity.Util;

namespace Liferay.Nativity.Example
{
	class MainClass
	{
		private static readonly string started = DateTime.Now.ToShortTimeString();

		public static void Main (string[] args)
		{
			MainClass.ContextMenuExample();
			MainClass.FileIconsExample();
		}

		private static void FileIconsExample()
		{
			NativityControlUtil.NativityControl.Connect();

			// Disable filtering
			NativityControlUtil.NativityControl.SetFilterFolders("");

			// File Icons
			var testIconId = 3;
//			var testFilePath = "/Users/rondea/bar.txt";
			var testFilePath = "C:\\test.txt";

			// FileIconControlCallback not used on Linux
			Modules.FileIcon.FileIconControlCallback fileIconControlCallback = path =>
			{
				return path == testFilePath ? testIconId : -1;
			};
			
			var fileIconControl = FileIconControlUtil.GetFileIconControl(NativityControlUtil.NativityControl, fileIconControlCallback);

			fileIconControl.EnableFileIcons();

			if(!OSDetector.IsWindows)
			{
				testIconId = fileIconControl.RegisterIcon("/Users/rondea/git/client/x-platform/resources/cocoa/overlay_Check.icns");
			}

			// FileIconControl.setFileIcon() method only used by Mac and Linux
			fileIconControl.SetFileIcon(testFilePath, testIconId);
		}

		private static void ContextMenuExample()
		{
			NativityControlUtil.NativityControl.Connect();

			// Disable filtering
			NativityControlUtil.NativityControl.SetFilterFolders("");

			ContextMenuControlUtil.GetContextMenuControl(NativityControlUtil.NativityControl, MainClass.ContextMenuControlCallback);

			// This won't work unless the sample program starts the nativity plugin on Mac
			//NativityControlUtil.NativityControl.SocketClosed += MainClass.Connect;
		}

		private static IEnumerable<ContextMenuItem> ContextMenuControlCallback (IEnumerable<string> paths)
		{
			Console.WriteLine ("Investigating {0}", string.Join (" ,", paths.ToArray ()));

			var contextMenuItems = new List<ContextMenuItem> ();

			//if (paths.All (p => p == "/Users/sync/foo.txt"))
			//{

				var contextMenuItem = new ContextMenuItem("Nativity Test: " + started);
				contextMenuItem.Selected += MainClass.ContextMenuItem_HandleSelected;

				contextMenuItems.Add(contextMenuItem);

				contextMenuItem = new ContextMenuItem("Nativity SubMenu Test: " + started);
				contextMenuItem.Selected += MainClass.ContextMenuItem_HandleSelected;

				contextMenuItems.Add(contextMenuItem);

				for (var ctr = 1; ctr <= 3; ctr++)
				{
					if(ctr == 2)
						contextMenuItem.AddSeparator();

					var sub = new ContextMenuItem(String.Empty);
					sub.Selected += (s,p) => Console.WriteLine(sub.Title);
					sub.Title = ctr.ToString() + ": " + sub.Uuid.ToString();

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
