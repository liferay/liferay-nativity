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

using log4net;

using Liferay.Nativity.Listeners;

/**
 * @author Dennis Ju, ported to C# by Andrew Rondeau
 */
namespace Liferay.Nativity.Control
{
	public abstract class NativityControl : IDisposable
	{
		private static ILog logger = LogManager.GetLogger(typeof(NativityControl));

		private Dictionary<String, MessageListener> commandMap = new Dictionary<string, MessageListener>();

		/// <summary>
		/// Initialize connection with native service
		/// </summary>
		/// <returns>true if connection is successful</returns>
		public abstract bool Connect();
		
		/// <summary>
		/// Initialize disconnection with native service
		/// </summary>
		/// <returns>true if disconnection is successful</returns>
		public abstract bool Disconnect();

		public void Dispose ()
		{
			this.Disconnect ();
		}
		
		/// <summary>
		/// Triggers the appropriate registered MessageListener when messages are
		/// received from the native service.
		/// </summary>
		/// <returns>NativityMessage to send back to the native service. Returns null
		/// if no registered MessageListener is found or if no response
		/// needs to be sent back to the native service.</returns>
		/// <param name="message">NativityMessage received from the native service</param>
		public NativityMessage FireMessage (NativityMessage message)
		{
			// This causes too much SPAM in the log
			//logger.DebugFormat("Firing message: {0}", message.Command);

			MessageListener messageListener;
			lock (this.commandMap)
			{
				if (!this.commandMap.TryGetValue (message.Command, out messageListener))
				{
					logger.WarnFormat ("Can not handle message: {0}", message.Command);
					return null;
				}
			}

			return messageListener (message);
		}
		
		/// <summary>
		/// Mac only
		///
		/// Loads Liferay Nativity into Finder.
		/// </summary>
		/// <returns>true if successfully loaded</returns>
		public abstract bool Load();
		
		/// <summary>
		/// Mac only
		///
		/// Check if Liferay Nativity is loaded in Finder.
		/// </summary>
		/// <returns>true if loaded</returns>
		public abstract bool Loaded { get; }

		/// <summary>
		/// Mac only
		///
		/// Check if Socket connection Interval
		/// </summary>
		/// <returns>TimeSpan</returns>
		public abstract TimeSpan CheckSocketConnectionInterval { get; set; }

		/// <summary>
		/// Mac only
		///
		/// Start Socket Connection Check
		/// </summary>
		/// <returns>void</returns>
		public abstract void StartSocketConnectionCheck();

		/// <summary>
		/// Mac only
		///
		/// Stop Socket Connection Check
		/// </summary>
		/// <returns>void</returns>
		public abstract void StopSocketConnectionCheck();
		
		/// <summary>
		/// Windows only
		/// 
		/// Causes Explorer to refresh the display of the file in explorer
		/// </summary>
		/// <param name="paths">files to refresh</param>
		public abstract void RefreshFiles(IEnumerable<string> paths);
		
		/// <summary>
		/// Used by modules to register a MessageListener that will respond to
		/// messages received from the native service. Each registered
		/// MessageListener instance must have a unique "command" parameter.
		/// Registering an instance with the same "command" parameter will replace
		/// previously registered instances.
		/// </summary>
		/// <param name="command">The command to listen for</param>
		/// <param name="messageListener">MessageListener to register</param>
		public void RegisterMessageListener (string command, MessageListener messageListener)
		{
			lock (this.commandMap)
			{
				this.commandMap [command] = messageListener;
			}
		}

		/// <summary>
		/// Mac only
		/// 
		/// Used by modules to send messages to the native service.
		/// </summary>
		/// <returns>response from the native service</returns>
		/// <param name="message">NativityMessage to send to the native service</param>
		public virtual string SendMessage(NativityMessage message)
		{
			return string.Empty;
		}
		
		/// <summary>
		/// Optionally set the root folder filter path for requests made
		/// to the native service. For example, setting a value of "/test/folder"
		/// indicates that any requests for files that are not a child of
		/// "/test/folder" will be ignored. This can improve native performance.
		/// </summary>
		/// <param name="folder">root folder path to filter by (inclusive)</param>
		public abstract void SetFilterFolder(string folder);
		
		/// <summary>
		/// Windows only
		/// 
		/// Marks the specified folder as a system folder so that Desktop.ini values
		/// will take effect.
		/// </summary>
		/// <param name="folder">folder to set as a system folder</param>
		public abstract void SetSystemFolder(string folder);

		/// <summary>
		/// MacOnly only
		/// CheckSocketConnection
		/// 
		/// </summary>
		public abstract void CheckSocketConnection(object state);
		
		/// <summary>
		///  Mac only
		///
		/// Unloads Liferay Nativity from Finder.
		/// </summary>
		/// <returns>true if successfully unloaded</returns>
		public abstract bool Unload();

		/// <summary>
		/// Triggered when the socket connection to the native service is closed
		/// </summary>
		public event SocketCloseListener SocketClosed;

		/// <summary>
		/// Triggered when the socket connection to the native service is to be restarted
		/// </summary>
		public event SocketRestartListener RestartSocketConnection;

		protected void OnSocketClosed()
		{
			var socketClosed = this.SocketClosed;
			if (null != socketClosed)
			{
				socketClosed();
			}
		}

		protected void OnSocketRestart()
		{
			var socketRestart = this.RestartSocketConnection;
			if (null != socketRestart)
			{
				socketRestart();
			}
		}
	}
}