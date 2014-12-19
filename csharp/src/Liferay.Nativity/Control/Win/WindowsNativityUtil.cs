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
using System.Runtime.InteropServices;
using Liferay.Nativity.Util;
using log4net;

namespace Liferay.Nativity.Control.Win
{
	/**
	 * @author Gail Hernandez
	 * @author Dennis Ju
	 * @author Patryk Strach - C# port
	 */
	public static class WindowsNativityUtil
	{
		private readonly static ILog logger = LogManager.GetLogger(typeof(WindowsNativityUtil));

		private const string NATIVITY_DLL_NAME_X86 = "LiferayNativityWindowsUtil_x86";
		private const string NATIVITY_DLL_NAME_X64 = "LiferayNativityWindowsUtil_x64";

		public static bool Load()
		{
			if(Loaded)
				return true;

			if(!OSDetector.IsMinimumWindowsVersion(OSDetector.WIN_VISTA))
			{
				logger.Error("Liferay Nativity is not compatible on Windows Vista or lower");

				return false;
			}

			Loaded = true;

			return true;
		}

		public static bool Loaded
		{
			get;
			private set;
		}

		private static bool Is64Bit
		{
			get { return IntPtr.Size == 8; }
		}

		public static bool SetSystemFolder(string folder)
		{
			return Is64Bit ? SetSystemFolder_x64(folder) : SetSystemFolder_x86(folder);
		}

		public static bool UpdateExplorer(string filePath)
		{
			return Is64Bit ? UpdateExplorer_x64(filePath) : UpdateExplorer_x86(filePath);
		}

		[DllImport(NATIVITY_DLL_NAME_X86, EntryPoint="SetSystemFolder")]
		private static extern bool SetSystemFolder_x86([MarshalAs(UnmanagedType.LPWStr)] string folder);

		[DllImport(NATIVITY_DLL_NAME_X86, EntryPoint="UpdateExplorer")]
		private static extern bool UpdateExplorer_x86([MarshalAs(UnmanagedType.LPWStr)] string folder);

		[DllImport(NATIVITY_DLL_NAME_X64, EntryPoint="SetSystemFolder")]
		private static extern bool SetSystemFolder_x64([MarshalAs(UnmanagedType.LPWStr)] string folder);

		[DllImport(NATIVITY_DLL_NAME_X64, EntryPoint="UpdateExplorer")]
		private static extern bool UpdateExplorer_x64([MarshalAs(UnmanagedType.LPWStr)] string folder);
	}
}
