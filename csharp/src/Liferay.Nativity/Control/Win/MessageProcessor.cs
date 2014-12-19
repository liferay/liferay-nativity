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
using log4net;
using Newtonsoft.Json;

namespace Liferay.Nativity.Control.Win
{
	/**
	 * @author Gail Hernandez
	 * @author Patryk Strach - C# port
	 */
	public static class MessageProcessor
	{
		private readonly static ILog logger = LogManager.GetLogger(typeof(MessageProcessor));

		public static void ReadAndProcessMessage(TcpClient clientSocket, NativityControl nativityControl)
		{
			using (var commandStream = clientSocket.GetStream())
			using (var streamReader = new StreamReader(commandStream, new UnicodeEncoding(bigEndian: false, byteOrderMark: false)))
			using (var streamWriter = new StreamWriter(commandStream, new UnicodeEncoding(bigEndian: false, byteOrderMark: false)))
			{
				try
				{
					var message = streamReader.ReadToEnd();

					if (message != String.Empty)
					{
						MessageProcessor.Handle(nativityControl, streamWriter, message);
					}
				}
				catch(Exception e)
				{
					MessageProcessor.logger.Error(e);
				}
			}
		}

		private static void Handle(NativityControl nativityControl, TextWriter streamWriter, string receivedMessage)
		{
			try
			{
				var message = JsonConvert.DeserializeObject<NativityMessage>(receivedMessage);
				var responseMessage = nativityControl.FireMessage(message);

				if(responseMessage != null)
				{
					var responseMessageString = JsonConvert.SerializeObject(responseMessage);
					streamWriter.Write(responseMessageString);
					streamWriter.Write("\0");
				}
			}
			catch(IOException e)
			{
				logger.Error(e);
			}
		}
	}
}
