/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.nativity.control.findersync;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.nativity.Constants;
import com.liferay.nativity.control.NativityControl;
import com.liferay.nativity.control.NativityMessage;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.File;
import java.io.PrintWriter;

import java.net.InetSocketAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Ju
 */
public class FSNativityControlImpl extends NativityControl {

	@Override
	public void addFavoritesPath(String path) {
		NativityMessage message = new NativityMessage(
			Constants.ADD_FAVORITES_PATH, path);

		sendMessage(message);
	}

	@Override
	public boolean connect() {
		if (_connected) {
			return true;
		}

		_childEventLoopGroup = new NioEventLoopGroup();
		_parentEventLoopGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();

			serverBootstrap.group(_parentEventLoopGroup, _childEventLoopGroup);

			serverBootstrap.channel(NioServerSocketChannel.class);

			ChannelInitializer channelInitializer =
				new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel socketChannel)
						throws Exception {

						DelimiterBasedFrameDecoder messageDecoder =
							new DelimiterBasedFrameDecoder(
								Integer.MAX_VALUE, Delimiters.lineDelimiter());

						FinderSyncChannelHandler finderSyncChannelHandler =
							new FinderSyncChannelHandler();

						socketChannel.pipeline().addLast(
							messageDecoder, finderSyncChannelHandler);
					}
				};

			serverBootstrap.childHandler(channelInitializer);

			serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture channelFuture = serverBootstrap.bind(0).sync();

			InetSocketAddress inetSocketAddress =
				(InetSocketAddress)channelFuture.channel().localAddress();

			_writePortToFile(inetSocketAddress.getPort());
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);

			_connected = false;

			return false;
		}

		_connected = true;

		return true;
	}

	@Override
	public boolean disconnect() {
		if (!_connected) {
			return true;
		}

		_childEventLoopGroup.shutdownGracefully();
		_parentEventLoopGroup.shutdownGracefully();

		_connected = false;

		return true;
	}

	public Set<String> getAllObservedFolders() {
		Set<String> observedFolders = new HashSet<String>();

		for (FinderSyncChannelHandler finderSyncChannelHandler :
				_finderSyncChannelHandlers) {

			observedFolders.addAll(
				finderSyncChannelHandler.getObservedFolders());
		}

		return observedFolders;
	}

	@Override
	public boolean load() throws Exception {
		return true;
	}

	@Override
	public boolean loaded() {
		return true;
	}

	@Override
	public void refreshFiles(String[] paths) {
	}

	@Override
	public void removeFavoritesPath(String path) {
		NativityMessage message = new NativityMessage(
			Constants.REMOVE_FAVORITES_PATH, path);

		sendMessage(message);
	}

	@Override
	public String sendMessage(NativityMessage message) {
		try {
			byte[] messageBytes = _objectMapper.writeValueAsBytes(message);

			ByteBuf byteBuf = Unpooled.wrappedBuffer(
				messageBytes, _RETURN_NEW_LINE);

			_channelGroup.writeAndFlush(byteBuf);
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}

		return "";
	}

	@Override
	public void setFilterFolder(String folder) {
		setFilterFolders(new String[] {folder});
	}

	@Override
	public void setFilterFolders(String[] folders) {
		NativityMessage message = new NativityMessage(
			Constants.SET_FILTER_PATHS, folders);

		sendMessage(message);
	}

	@Override
	public void setPortFilePath(String path) {
		_portFilePath = path;
	}

	@Override
	public void setSystemFolder(String folder) {
	}

	@Override
	public boolean unload() throws Exception {
		return false;
	}

	private void _writePortToFile(int port) {
		String path = null;

		if (_portFilePath == null) {
			path = System.getProperty("user.home") + "/.liferay-nativity/port";
		}
		else {
			path = _portFilePath;
		}

		final File file = new File(path);

		PrintWriter writer = null;

		try {
			file.getParentFile().mkdirs();

			file.createNewFile();

			writer = new PrintWriter(path);
		}
		catch (Exception e) {
			_logger.error(e.getMessage(), e);

			return;
		}

		writer.println(String.valueOf(port));

		writer.close();

		Thread thread = new Thread() {
			public void run() {
				try {
					file.delete();
				}
				catch (Exception e) {
					_logger.error(e.getMessage(), e);
				}
			}
		};

		Runtime.getRuntime().addShutdownHook(thread);
	}

	private static final byte[] _RETURN_NEW_LINE = "\r\n".getBytes();

	private static Logger _logger = LoggerFactory.getLogger(
		FSNativityControlImpl.class.getName());

	private static ObjectMapper _objectMapper = new ObjectMapper().configure(
		JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

	private ChannelGroup _channelGroup = new DefaultChannelGroup(
		GlobalEventExecutor.INSTANCE);
	private EventLoopGroup _childEventLoopGroup;
	private boolean _connected;
	private List<FinderSyncChannelHandler> _finderSyncChannelHandlers =
		new ArrayList<FinderSyncChannelHandler>();
	private EventLoopGroup _parentEventLoopGroup;
	private String _portFilePath;

	private class FinderSyncChannelHandler
		extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext channelHandlerContext)
			throws Exception {

			super.channelActive(channelHandlerContext);

			_finderSyncChannelHandlers.add(this);

			_channelGroup.add(channelHandlerContext.channel());

			fireSocketOpenListeners();
		}

		@Override
		public void channelInactive(ChannelHandlerContext channelHandlerContext)
			throws Exception {

			super.channelInactive(channelHandlerContext);

			_finderSyncChannelHandlers.remove(this);

			_channelGroup.remove(channelHandlerContext.channel());

			fireSocketCloseListeners();
		}

		@Override
		public void channelRead(
			ChannelHandlerContext channelHandlerContext, Object messageObj) {

			ByteBuf readByteBuf = (ByteBuf)messageObj;

			try {
				String messageStr = readByteBuf.toString(CharsetUtil.UTF_8);

				if (messageStr.isEmpty()) {
					return;
				}

				try {
					NativityMessage nativityMessage = _objectMapper.readValue(
						messageStr, NativityMessage.class);

					if (nativityMessage.getCommand().equals(
							Constants.START_OBSERVING_FOLDER)) {

						_observedFolders.add(
							nativityMessage.getValue().toString());

						return;
					}
					else if (nativityMessage.getCommand().equals(
								Constants.END_OBSERVING_FOLDER)) {

						_observedFolders.remove(
							nativityMessage.getValue().toString());

						return;
					}

					NativityMessage responseMessage = fireMessage(
						nativityMessage);

					if (responseMessage != null) {
						byte[] responseMessageBytes =
							_objectMapper.writeValueAsBytes(responseMessage);

						ByteBuf writeByteBuf = Unpooled.wrappedBuffer(
							responseMessageBytes, _RETURN_NEW_LINE);

						channelHandlerContext.writeAndFlush(writeByteBuf);
					}
				}
				catch (Exception e) {
					_logger.error(e.getMessage(), e);
				}
			}
			finally {
				readByteBuf.release();
			}
		}

		@Override
		public void exceptionCaught(
			ChannelHandlerContext channelHandlerContext, Throwable throwable) {

			_logger.error(throwable.getMessage(), throwable);

			channelHandlerContext.close();

			fireSocketCloseListeners();
		}

		public Set<String> getObservedFolders() {
			return _observedFolders;
		}

		private Set<String> _observedFolders = Collections.newSetFromMap(
			new ConcurrentHashMap<String, Boolean>());

	}

}