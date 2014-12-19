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
using System.IO;
using System.Runtime.InteropServices;

//import java.io.File;

using log4net;

namespace Liferay.Nativity.Util
{
	/**
	 * @author Dennis Ju, ported to C# by Andrew Rondeau
	 */
	public static class OSDetector
	{
		private static ILog logger = LogManager.GetLogger(typeof(OSDetector));

		public const double MAC_CHEETAH_10_0 = 10.0;
		
		public const double MAC_JAGUAR_10_2 = 10.2;
		
		public const double MAC_LEOPARD_10_5 = 10.5;
		
		public const double MAC_LION_10_7 = 10.7;
		
		public const double MAC_MOUNTAIN_LION_10_8 = 10.8;
		
		public const double MAC_PANTHER_10_3 = 10.3;
		
		public const double MAC_PUMA_10_1 = 10.1;
		
		public const double MAC_SNOW_LEOPARD_10_6 = 10.6;
		
		public const double MAC_TIGER_10_4 = 10.4;

		public const double WIN_7 = 6.1;
		
		public const double WIN_8 = 6.2;
		
		public const double WIN_2000 = 5.0;
		
		public const double WIN_SERVER_2003 = 5.2;
		
		public const double WIN_SERVER_2008 = 6.0;
		
		public const double WIN_SERVER_2012 = 6.2;
		
		public const double WIN_VISTA = 6.0;
		
		public const double WIN_XP_X64 = 5.2;

		public const double WIN_XP_X86 = 5.1;
		
		public static bool IsApple
		{
			get
			{
				// See http://mono.1490590.n4.nabble.com/Howto-detect-os-td1549244.html

				if (false == OSDetector.apple.HasValue)
				{
					var buf = Marshal.AllocHGlobal(8192); 
					try
					{ 
						// This is a hacktastic way of getting sysname from uname () 
						if (uname(buf) == 0)
						{ 
							var os = Marshal.PtrToStringAnsi(buf);
							OSDetector.apple = (os == "Darwin"); 
						} 
					} 
					catch 
					{
						OSDetector.apple = false;
					} 
					finally
					{ 
						Marshal.FreeHGlobal(buf); 
					} 
				}

				return OSDetector.apple.Value;
			}
		}
		private static bool? apple = null;

		[DllImport("libc")] 
		static extern int uname(IntPtr buf);
		
		/*public static boolean isMinimumAppleVersion(double minimumVersion) {
			if (!isApple()) {
				return false;
			}
			
			if (_version == null) {
				_version = System.getProperty("os.version");
			}
			
			String[] parts = _version.split("\\.");
			
			StringBuilder sb = new StringBuilder(3);
			
			sb.append(parts[0]);
			sb.append(".");
			sb.append(parts[1]);
			
			try {
				double version = Double.parseDouble(sb.toString());
				
				if (version >= minimumVersion) {
					return true;
				}
			}
			catch (Exception e) {
				_logger.error("Could not determine OS Version", e.getMessage());
			}
			
			return false;
		}*/

		public static bool IsWindows
		{
			get
			{
				return Environment.OSVersion.Platform == PlatformID.Win32Windows ||
				       Environment.OSVersion.Platform == PlatformID.Win32NT;
			}
		}

		public static bool IsMinimumWindowsVersion(double minimumVersion)
		{
			if(!IsWindows)
				return false;

			var minimumMajor = (int)minimumVersion;
			var minimumMinor = (int)(minimumVersion - minimumMajor) * 10;

			return Environment.OSVersion.Version.Major >= minimumMajor && Environment.OSVersion.Version.Minor >= minimumMinor;
		}

		public static bool IsLinux
		{
			get { return Environment.OSVersion.Platform == PlatformID.Unix; }
		}
	}
}