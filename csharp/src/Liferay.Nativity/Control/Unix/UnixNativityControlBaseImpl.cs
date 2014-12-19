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

namespace Liferay.Nativity.Control.Unix
{
	/**
	* @author Dennis Ju, ported to C# by Andrew Rondeau
	*/
	public abstract class UnixNativityControlBaseImpl : NativityControl
	{
		private readonly static ILog logger = LogManager.GetLogger(typeof(UnixNativityControlBaseImpl));

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

		private bool connected = false;

		public override bool Connect ()
		{
			if (this.connected)
			{
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

				return true;
			}
			catch (Exception e)  // IOException???
			{
				logger.Error(e);
				this.connected = false;
			}
			
			return this.connected;
		}

		public override bool Disconnect ()
		{
			try 
			{
				this.commandSocket.Close();
				this.callbackSocket.Close();
				
				this.connected = false;
				
				logger.Debug("Successfully disconnected");

				return true;
			}
			catch (Exception e)
			{
				logger.Error(e);
				this.connected = true;
				
				return false;
			}
		}
	
		public override string SendMessage(NativityMessage message)
		{
			if (false == this.connected)
			{
				logger.Warn("LiferayNativity is not connected");
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
				logger.Error(e);
				this.OnSocketClosed();

				return string.Empty;
			}
		}

		// Windows only
		public override void SetSystemFolder(string folder)
		{
		}

		private void DoCallbackLoop() 
		{
			if (false == this.connected) 
			{
				logger.Debug("LiferayNativity is not connected");
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
						this.Dispose();
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
						logger.Error(e);
					}
				}
				catch (IOException ioe) 
				{
					logger.Error(ioe);

					this.Dispose();
					this.OnSocketClosed();
				}
			}
		}
	}
}