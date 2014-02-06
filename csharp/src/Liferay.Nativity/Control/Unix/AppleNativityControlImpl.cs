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

using log4net;

//import com.liferay.nativity.util.mac.AppleUtil;

namespace Liferay.Nativity.Control.Unix
{
	/**
	 * @author Dennis Ju, ported to C# by Andrew Rondeau
	 */
	public class AppleNativityControlImpl : UnixNativityControlBaseImpl
	{
		private static readonly ILog logger = LogManager.GetLogger(typeof(AppleNativityControlImpl));

		public override bool Load ()
		{
			logger.Info("Loading Liferay Nativity");

			throw new System.NotImplementedException ("return AppleUtil.load();");
		}

		public override bool Loaded
		{
			get
			{
				throw new System.NotImplementedException ("return AppleUtil.loaded();");
			}
		}

		// This is windows-only, no-op on Mac
		public override void RefreshFiles (System.Collections.Generic.IEnumerable<string> paths)
		{
		}

		public override bool Unload ()
		{
			logger.Info("Unloading Liferay Nativity");

			throw new System.NotImplementedException ("return AppleUtil.unload();");
		}
	}
}
