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

namespace Liferay.Nativity.Modules.FileIcon
{
	/**
	 * @author Dennis Ju, ported to C# by Andrew Rondeau. Support for icons added by Ivan Burlakov
	 */
	public interface IFileIconControl
	{
		/// <summary>
		/// Disables file icon overlays
		/// </summary>
		void DisableFileIcons();
		
		/// <summary>
		/// Enables file icon overlays
		/// </summary>
		void EnableFileIcons();

		/// <summary>
		/// Mac only
		/// 
		/// Register an overlay icon
		/// </summary>
		/// <returns>overlay icon id. -1 if the icon failed ot register.</returns>
		/// <param name="path">path to the overlay icon</param>
		int RegisterIcon(string path);

        /// <summary>
        /// Mac only
        /// 
        /// Registers the menu icon and resize it to the size of context menu text.
        /// </summary>
        /// <returns>The menu icon id. -1 if the icon failed ot register.</returns>
        /// <param name="path">Path to the menu icon</param>
        int RegisterMenuIcon(string path);
		
		/// <summary>
		/// Mac and Linux only
		/// 
		/// Removes all file icon overlays on Linux, redraws all Finder windows on Mac
		/// </summary>
		void RemoveAllFileIcons();
		
		/// <summary>
		/// Mac and Linux only
		/// 
		/// Removes file icon overlay on Linux, redraws all Finder windows on Mac
		/// </summary>
		/// <param name="path">file path to remove the file icon overlay</param>
		void RemoveFileIcon(string path);
		
		/// <summary>
		/// Mac and Linux only
		/// 
		/// Removes file icon overlays on Linux, redraws all Finder windows on Mac
		/// </summary>
		/// <param name="paths">file paths to remove file icon overlays</param>
		void RemoveFileIcons(IEnumerable<string> paths);
		
		/// <summary>
		/// Mac and Linux only
		/// 
		/// Set file icon overlay on Linux, redraws all Finder windows on Mac
		/// </summary>
		/// <param name="path">file path to set file icon overlays</param>
		/// <param name="iconId">id of file icon overlay. Value of -1 will remove the overlay
		/// (same as calling removeFileIcon).</param>
		void SetFileIcon(string path, int iconId);
		
		/// <summary>
		/// Mac and Linux only
		/// 
		/// Set file icon overlays on Linux, redraws all Finder windows on Mac
		/// </summary>
		/// <param name="fileIconsMap">map containing paths and file icon overlay ids</param>
		void SetFileIcons(IDictionary<string, int> fileIconsMap);
		
		/// <summary>
		/// Mac only
		/// 
		/// Unregister an overlay icon
		/// </summary>
		/// <param name="id">overlay icon id</param>
		void UnregisterIcon(int id);
		
		/// <summary>
		/// Windows only
		/// 
		/// Called by the native service to request the icon overlay id for the
		/// specified file
		/// </summary>
		/// <returns>icon overlay id</returns>
		/// <param name="path">file path requesting the overlay icon</param>
		int GetIconForFile(string path);
	}
}
