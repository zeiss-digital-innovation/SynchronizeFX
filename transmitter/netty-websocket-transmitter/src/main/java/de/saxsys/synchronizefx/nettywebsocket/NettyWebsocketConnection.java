/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

package de.saxsys.synchronizefx.nettywebsocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;
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
class NettyWebsocketConnection extends ChannelInboundMessageHandlerAdapter<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(NettyWebsocketConnection.class);

    private static final String PROTOCOL = "v1.websocket.synchronizefx.saxsys.de";

    private WebSocketClientHandshaker wsHandshaker;
    private ChannelPromise wsHandshakeFuture;
    private NettyWebsocketClient parent;

    private List<ByteBuf> fragments = new LinkedList<>();

    /**
     * Inititalizes the connection.
     * 
     * @param uri the URI of the server to connect to
     * @param parent the Instance that setup this connection. It is informed on incoming messages or errors.
     */
    public NettyWebsocketConnection(final URI uri, final NettyWebsocketClient parent) {
        this.wsHandshaker =
                WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, PROTOCOL, false, null,
                        Integer.MAX_VALUE);
        this.parent = parent;
    }

    /**
     * Waits for the handshake to finish.
     * 
     * @throws InterruptedException When the waiting failed.
     */
    void waitForHandshakeFinished() throws InterruptedException {
        wsHandshakeFuture.sync();
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        wsHandshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        wsHandshaker.handshake(ctx.channel());
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!wsHandshaker.isHandshakeComplete()) {
            wsHandshaker.finishHandshake(ch, (FullHttpResponse) msg);
            LOG.info("Websocket handshake completed successfully");
            wsHandshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof BinaryWebSocketFrame) {
            handleFrame((BinaryWebSocketFrame) msg);
        } else if (msg instanceof CloseWebSocketFrame) {
            parent.onServerDisconnect();
        } else if (msg instanceof TextWebSocketFrame) {
            LOG.warn("Recieved a Websocket text frame. This was not expected. Ignoring it.");
        } else if (msg instanceof PongWebSocketFrame) {
            LOG.warn("Recieved a Websocket Pong frame. This was not expected. Ignoring it.");
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
        if (!wsHandshakeFuture.isDone()) {
            wsHandshakeFuture.setFailure(cause);
        }

        ctx.close();
        parent.onError(cause);
    }
}
