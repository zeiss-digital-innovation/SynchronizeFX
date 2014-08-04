/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
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

package de.saxsys.synchronizefx.netty.websockets;

import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A codec that collects coherent messages as {@link ByteBuf}s by using {@link BinaryWebSocketFrame}s.
 * 
 * @author Raik Bieniek
 */
class ByteBufToWebSocketFrameCodec extends MessageToMessageCodec<WebSocketFrame, ByteBuf> {

    private static final ByteBuf[] BYTE_BUF_TYPE = new ByteBuf[0];
    private static final Logger LOG = LoggerFactory.getLogger(ByteBufToWebSocketFrameCodec.class);

    private final List<ByteBuf> fragments = new LinkedList<>();

    /**
     * Initializes the codec.
     */
    public ByteBufToWebSocketFrameCodec() {
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf msg, final List<Object> out) throws Exception {
        out.add(new BinaryWebSocketFrame(msg));
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final WebSocketFrame msg, final List<Object> out)
        throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            ByteBuf content = msg.content();
            // the content is passed to other handlers so they need to be retained.
            content.retain();
            fragments.add(content);
            if (msg.isFinalFragment()) {
                if (fragments.size() == 1) {
                    out.add(fragments.get(0));
                } else {
                    ByteBuf[] array = fragments.toArray(BYTE_BUF_TYPE);
                    out.add(Unpooled.wrappedBuffer(array));
                }
                fragments.clear();
            }
        } else if (msg instanceof TextWebSocketFrame) {
            LOG.warn("Recieved a Websocket text frame. This was not expected. Ignoring it.");
        }
    }
}
