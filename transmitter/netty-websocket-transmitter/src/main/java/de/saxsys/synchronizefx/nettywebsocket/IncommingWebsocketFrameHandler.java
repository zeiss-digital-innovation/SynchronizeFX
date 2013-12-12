/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

package de.saxsys.synchronizefx.nettywebsocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the websocket connection to the server.
 * 
 * For the protocol used see the class Javadoc of de.saxsys.synchronizefx.tomcat.SynchronizeFXTomcatServlet in the
 * module tomcat-transmitter.
 */
class IncommingWebsocketFrameHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(IncommingWebsocketFrameHandler.class);

    private NettyWebsocketClient parent;

    private List<ByteBuf> fragments = new LinkedList<>();

    /**
     * Inititalizes the connection.
     * 
     * @param parent
     *            the Instance that setup this connection. It is informed on incoming messages or errors.
     */
    public IncommingWebsocketFrameHandler(final NettyWebsocketClient parent) {
        this.parent = parent;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            handleFrame((BinaryWebSocketFrame) msg);
        } else if (msg instanceof CloseWebSocketFrame) {
            parent.onServerDisconnect();
        } else if (msg instanceof TextWebSocketFrame) {
            LOG.warn("Recieved a Websocket text frame. This was not expected. Ignoring it.");
        }
    }

    private void handleFrame(final BinaryWebSocketFrame msg) {
        fragments.add(msg.content());

        if (!msg.isFinalFragment()) {
            return;
        }

        int size = 0;
        for (ByteBuf fragment : fragments) {
            size += fragment.readableBytes();
        }

        byte[] buffer = new byte[size];
        int position = 0;
        for (ByteBuf fragment : fragments) {
            size = fragment.readableBytes();
            fragment.readBytes(buffer, position, size);
            position += size;
        }
        parent.onMessageRecived(buffer);
        fragments.clear();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ctx.close();
        parent.onError(cause);
    }
}
