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

using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using log4net;
using Microsoft.Win32;
using Newtonsoft.Json;

namespace Liferay.Nativity.Control.Win
{
	/**
	 * @author Gail Hernandez
	 * @author Dennis Ju
	 * @author Patryk Strach - C# port
	 */
	public class WindowsNativityControlImpl : NativityControl
	{
		private readonly static ILog logger = LogManager.GetLogger(typeof(WindowsNativityControlImpl));

		private const int PORT = 33001;

		private bool connected = false;
		private TcpListener serverSocket = new TcpListener(IPAddress.Loopback, PORT);

		public override bool Connect()
		{
			if (this.connected)
				return true;

			bool loaded = WindowsNativityUtil.Load();
			if (!loaded)
			{
				logger.Debug("WindowsNativityUtil failed to load");

				return false;
			}

			try
			{
				this.serverSocket.Start();
				
				this.connected = true;
			}
			catch(SocketException e)
			{
				logger.Error(e);
				return false;
			}

			var thread = new Thread(HandleConnection);
			thread.Start();

			return true;
		}

		public override bool Disconnect()
		{
			if(!this.connected)
				return true;

			try
			{
				this.serverSocket.Stop();
			}
			catch (SocketException e)
			{
				logger.Error(e);
			}

			this.connected = false;

			return true;
		}

		public override bool Load()
		{
			return true;
		}

		public override bool Loaded
		{
			get { return true; }
		}

		public override void RefreshFiles(IEnumerable<string> paths)
		{
			if (paths == null || !WindowsNativityUtil.Loaded)
				return;

			foreach (var path in paths)
			{
				WindowsNativityUtil.UpdateExplorer(path);
			}
		}

		public override void SetFilterFolders(params string[] folders)
		{
			var foldersJson = JsonConvert.SerializeObject(folders);

			Registry.SetValue(Constants.NATIVITY_REGISTRY_KEY, Constants.FILTER_FOLDERS_REGISTRY_NAME, foldersJson);
		}

		public override void SetSystemFolder(string folder)
		{
			if (!WindowsNativityUtil.Loaded)
				return;

			WindowsNativityUtil.SetSystemFolder(folder);
		}

		public override bool Unload()
		{
			return true;
		}

		protected void HandleConnection()
		{
			while(this.connected)
			{
				try
				{
					var clientSocket = this.serverSocket.AcceptTcpClient();

					ThreadPool.QueueUserWorkItem(x => MessageProcessor.ReadAndProcessMessage(clientSocket, this));
				}
				catch(SocketException e)
				{
					logger.Error(e);

					OnSocketClosed();
				}
			}
		}
	}
}
