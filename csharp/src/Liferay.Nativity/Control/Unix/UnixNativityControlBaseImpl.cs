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
using System.Net.Sockets;
using System.Text;
using System.Threading;

using Newtonsoft.Json;
using log4net;
using System.Collections.Generic;

namespace Liferay.Nativity.Control.Unix
{
	/**
	* @author Dennis Ju, ported to C# by Andrew Rondeau
	*/
	public class UnixNativityControlBaseImpl : NativityControl
	{
		private readonly static ILog logger = LogManager.GetLogger(typeof(UnixNativityControlBaseImpl));

		/// <summary>
		/// Time spent on polling
		/// </summary>
		private static readonly TimeSpan SOCKETCONNECTED_POLL_TIME = TimeSpan.FromMilliseconds(100);

		private static readonly TimeSpan infiniteTimeSpan  = new TimeSpan(-1);
		private static Timer timer = null;

		private const string RETURN_NEW_LINE = "\r\n";

		private const int CALLBACK_SOCKET_PORT = 33002;
		private const int COMMAND_SOCKET_PORT = 33001;

		private StreamReader callbackBufferedReader;
		private StreamWriter callbackOutputStream;
		private TcpClient callbackSocket;
		private Thread callbackThread;
		private StreamReader commandBufferedReader;
		private StreamWriter commandOutputStream;
		private TcpClient commandSocket;
		private object sync = new object();

		private bool connected = false;

		public override bool Connect ()
		{
			lock (sync)
			{
				if (this.connected) 
				{
					logger.Debug ("connected!");
					return this.connected;
				}

				try
				{
					this.commandSocket = new TcpClient("127.0.0.1", UnixNativityControlBaseImpl.COMMAND_SOCKET_PORT);

					var commandStream = this.commandSocket.GetStream();
					this.commandBufferedReader = new StreamReader(commandStream, Encoding.UTF8);
					this.commandOutputStream = new StreamWriter(commandStream, new UTF8Encoding(false));
					this.commandOutputStream.NewLine = UnixNativityControlBaseImpl.RETURN_NEW_LINE;
					this.commandOutputStream.AutoFlush = true;

					this.callbackSocket = new TcpClient("127.0.0.1", UnixNativityControlBaseImpl.CALLBACK_SOCKET_PORT);

					var callbackStream = this.callbackSocket.GetStream();
					this.callbackBufferedReader = new StreamReader(callbackStream, Encoding.UTF8);
					this.callbackOutputStream = new StreamWriter(callbackStream, new UTF8Encoding(false));
					this.callbackOutputStream.NewLine = UnixNativityControlBaseImpl.RETURN_NEW_LINE;
					this.callbackOutputStream.AutoFlush = true;

					this.connected = true;

					this.callbackThread = new Thread(this.DoCallbackLoop);
					this.callbackThread.Start();

					logger.DebugFormat(
						"Successfully connected to command socket: {0}",
						UnixNativityControlBaseImpl.COMMAND_SOCKET_PORT);

					logger.DebugFormat(
						"Successfully connected to service socket: {0}",
						UnixNativityControlBaseImpl.CALLBACK_SOCKET_PORT);

					if(UnixNativityControlBaseImpl.timer == null)
					{
						//Start in a suspended state
						UnixNativityControlBaseImpl.timer = new Timer( CheckSocketConnection, null, UnixNativityControlBaseImpl.infiniteTimeSpan, UnixNativityControlBaseImpl.infiniteTimeSpan );
					}
					this.StartSocketConnectionCheck();
					return true;
				}
				catch (Exception e)  // IOException???
				{
					logger.Error("Connect Exception", e);
					this.Disconnect();
					this.OnSocketRestart ();
					this.connected = false;
				}
			}
			return this.connected;
		}

		public override void StartSocketConnectionCheck()
		{
			logger.Debug ("StartSocketConnectionCheck");
			if (UnixNativityControlBaseImpl.timer != null)
			{
				UnixNativityControlBaseImpl.timer.Change (TimeSpan.Zero, UnixNativityControlBaseImpl.infiniteTimeSpan);
			}
		}

		public override void StopSocketConnectionCheck()
		{
			logger.Debug ("StopSocketConnectionCheck");
			if (UnixNativityControlBaseImpl.timer != null)
			{
				UnixNativityControlBaseImpl.timer.Dispose ();
				UnixNativityControlBaseImpl.timer = null;
			}
		}

		public override void CheckSocketConnection(object state)
		{
			try
			{
				if((this.commandSocket.Available == 0 && commandSocket.Client.Poll((int)SOCKETCONNECTED_POLL_TIME.TotalMilliseconds, SelectMode.SelectRead)) ||
					this.callbackSocket.Available == 0 && callbackSocket.Client.Poll((int)SOCKETCONNECTED_POLL_TIME.TotalMilliseconds, SelectMode.SelectRead))
				{
					logger.Error("CheckConnection failed restarting connection.");
					this.Disconnect();
					this.OnSocketRestart ();
					return;
				}
				else
				{
					var message = new NativityMessage(Constants.CHECK_SOCKET_CONNECTION, string.Empty);
					var checkMessageString =  JsonConvert.SerializeObject(message);
					this.callbackOutputStream.WriteLine(checkMessageString);
				}
				UnixNativityControlBaseImpl.timer.Change (this.checkSocketConnectionInterval, UnixNativityControlBaseImpl.infiniteTimeSpan);
			}
			catch(Exception ex)
			{
				logger.Error("CheckConnection failed restarting connection", ex);
				this.Disconnect();
				this.OnSocketRestart ();
			}
			return;
		}

		public override bool Disconnect ()
		{
			try 
			{
				if(this.connected == false)
				{
					logger.Info("Disconnected already!");
				}

				this.commandSocket.Close();
				this.callbackSocket.Close();
				
				this.connected = false;
				this.StopSocketConnectionCheck();

				logger.Debug("Successfully disconnected");

				return true;
			}
			catch (Exception e)
			{
				logger.Error("Disconnected exception", e);
				this.connected = true;
				this.StopSocketConnectionCheck();
				return false;
			}
		}
	
		public override string SendMessage(NativityMessage message)
		{
			if (false == this.connected)
			{
				logger.Warn("SendMessage : LiferayNativity is not connected");
				this.Disconnect();
				this.OnSocketRestart ();
				return string.Empty;
			}
			
			try 
			{
				var messageString = JsonConvert.SerializeObject(message);
				this.commandOutputStream.WriteLine(messageString);

				/* log4net doesn't support trace
				if (_logger.isTraceEnabled()) 
				{
					_logger.trace(
						"Sent on socket {}: {}", _commandSocketPort, messageString);
				}*/
				
				var reply = this.commandBufferedReader.ReadLine();
				
				/* log4net doesn't support trace
				if (_logger.isTraceEnabled()) 
				{
					_logger.trace(
						"Received on socket {}: {}", _commandSocketPort, reply);
				}*/

				if (reply == null) 
				{
					this.commandSocket.Close();
					this.OnSocketClosed();
				}
				
				return reply;
			}
			catch (IOException e) 
			{
				logger.Error("SendMessage : LiferayNativity is not connected", e);
				this.connected = false;
				this.OnSocketClosed();

				return string.Empty;
			}
		}

		public override bool Load ()
		{
			throw new NotImplementedException ();
		}
		public override void RefreshFiles (System.Collections.Generic.IEnumerable<string> paths)
		{
			throw new NotImplementedException ();
		}
		public override void SetFilterFolder (string folder)
		{
			throw new NotImplementedException ();
		}
		public override void SetSystemFolder (string folder)
		{
			throw new NotImplementedException ();
		}
		public override bool Unload ()
		{
			throw new NotImplementedException ();
		}

		public override bool Loaded 
		{
			get 
			{
				throw new NotImplementedException ();
			}
		}

		public override TimeSpan CheckSocketConnectionInterval
		{
			get
			{
				return this.checkSocketConnectionInterval;
			}

			set
			{
				this.checkSocketConnectionInterval = value;
				if (UnixNativityControlBaseImpl.timer != null)
				{
					UnixNativityControlBaseImpl.timer.Change (this.checkSocketConnectionInterval, UnixNativityControlBaseImpl.infiniteTimeSpan);
				}
			}
		}
		private TimeSpan checkSocketConnectionInterval = UnixNativityControlBaseImpl.infiniteTimeSpan;

		private void DoCallbackLoop() 
		{
			if (false == this.connected) 
			{
				logger.Info("DoCallbackLoop : LiferayNativity is not connected");
				this.Disconnect();
				this.OnSocketRestart ();
				return;
			}
			
			while (this.connected)
			{
				try
				{
					string data = this.callbackBufferedReader.ReadLine();

					// log4net doesn't have trace
					/*if (_logger.isTraceEnabled())
					{
						_logger.trace(
							"Received on socket {}: {}", _callbackSocketPort, data);
					}*/
					
					if (data == null) 
					{
						this.Disconnect();
						this.OnSocketClosed();

						break;
					}
					
					if (string.Empty == data) 
					{
						continue;
					}

					try
					{
						var message = JsonConvert.DeserializeObject<NativityMessage>(data);
						var responseMessage = this.FireMessage(message);
						
						if (responseMessage != null)
						{
							var responseMessageString =  JsonConvert.SerializeObject(responseMessage);
							this.callbackOutputStream.WriteLine(responseMessageString);

							// log4net doesn't have trace
							/*if (_logger.isTraceEnabled())
							{
								_logger.trace(
									"Sent on socket {}: {}", _callbackSocketPort,
									responseMessageString);
							}*/
						}
						// Use to diagnose missing messages
						//else
						//{
						//	logger.WarnFormat("No response to {0}", message.Command);
						//}
					}
					catch (Exception e)
					{
						this.connected = false;
						logger.Error("DoCallbackLoop : LiferayNativity is not connected", e);
						this.Disconnect();
						this.OnSocketClosed();
					}
				}
				catch (IOException ioe) 
				{
					this.connected = false;
					logger.Error(ioe);

					this.Disconnect();
					this.OnSocketClosed();
				}
			}
		}
	}
}