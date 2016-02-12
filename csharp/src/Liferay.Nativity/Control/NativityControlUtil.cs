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
using Liferay.Nativity.Control.Unix;
using Liferay.Nativity.Control.Win;
using log4net;

using Liferay.Nativity.Util;

/**
 * @author Dennis Ju, ported to C# by Andrew Rondeau
 */
namespace Liferay.Nativity.Control
{
	public static class NativityControlUtil
	{
		private static ILog logger = LogManager.GetLogger(typeof(NativityControlUtil));

		static NativityControlUtil ()
		{
			if (OSDetector.IsApple) 
			{
				NativityControlUtil.nativityControl = new AppleNativityControlImpl();
			}
			else if (OSDetector.IsWindows) 
			{
				NativityControlUtil.nativityControl = new WindowsNativityControlImpl();
			}
			else if (OSDetector.IsLinux) 
			{
				NativityControlUtil.nativityControl = new LinuxNativityControlImpl();
			}
			else
			{
				logger.ErrorFormat("{0} is not supported", System.Environment.OSVersion.Platform);
				nativityControl = null;
			}
		}

		/// <summary>
		/// Gets an instance of NativityControl based on the user's operating system. Returns null for unsupported operating systems.
		/// </summary>
		public static NativityControl NativityControl
		{
			get { return NativityControlUtil.nativityControl; }
		}
		private static NativityControl nativityControl;
	}
}
