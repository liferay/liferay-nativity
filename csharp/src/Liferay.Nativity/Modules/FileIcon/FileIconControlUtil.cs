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

using Liferay.Nativity.Control;
using Liferay.Nativity.Util;

namespace Liferay.Nativity.Modules.FileIcon
{
	/**
	 * @author Dennis Ju, ported to C# by Andrew Rondeau
	 */
	public static class FileIconControlUtil
	{
		public static IFileIconControl GetFileIconControl(
			NativityControl nativityControl,
			FileIconControlCallback fileIconControlCallback) {

			if (OSDetector.IsApple)
			{
				return new Unix.AppleFileIconControlImpl(nativityControl, fileIconControlCallback);
			}
			/*else if (OSDetector.isWindows()) {
				return new WindowsFileIconControlImpl(nativityControl, fileIconControlCallback);
			}
			else if (OSDetector.isLinux()) {
				return new LinuxFileIconControlImpl(nativityControl, fileIconControlCallback);
			}*/
			
			return null;
		}

		/*protected FileIconControl createLinuxFileIconControl() {
		}
		
		protected FileIconControl createWindowsFileIconControl() {
			return new WindowsFileIconControlImpl(
				_nativityControl, _fileIconControlCallback);
		}*/
	}
}
