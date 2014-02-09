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

import java.util.LinkedList;
import java.util.List;

import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

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
 * Encodes and decodes list of SynchronizeFX commands to web socket frames using a user supplied serializer.
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 * 
 */
class CommandToWebSocketFrameCodec extends MessageToMessageCodec<WebSocketFrame, List<Object>> {

    private static final Logger LOG = LoggerFactory.getLogger(CommandToWebSocketFrameCodec.class);

    private final List<ByteBuf> fragments = new LinkedList<>();

    private final Serializer serializer;

    /**
     * Initializes the codec.
     * 
     * @param serializer The implementation for serializing and deserializing <code>byte[]</code> to SynchronizeFX
     *            commands.
     */
    public CommandToWebSocketFrameCodec(final Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final List<Object> msg, final List<Object> out)
        throws Exception {
        out.add(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(serializer.serialize(msg))));
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final WebSocketFrame msg, final List<Object> out)
        throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            byte[] commandData = collectCommandData((BinaryWebSocketFrame) msg);
            if (commandData != null) {
                out.add(decode(commandData));
            }
        } else if (msg instanceof TextWebSocketFrame) {
            LOG.warn("Recieved a Websocket text frame. This was not expected. Ignoring it.");
        }
    }

    /**
     * Collects data of {@link BinaryWebSocketFrame}s until enough data is available to start deserializing it.
     * 
     * <p>
     * Serialized data for commands that where serialized together may be transfered in multiple chunks. Therefore
     * this method collects this chunks until all have arrived.
     * </p>
     * 
     * @param msg An incoming frame
     * @return all bytes that where created by a single {@link Serializer#serialize(List)} call if enough bytes have
     *         arrived or <code>null</code> if not.
     */
    private byte[] collectCommandData(final BinaryWebSocketFrame msg) {
        fragments.add(msg.content());

        if (!msg.isFinalFragment()) {
            return null;
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
        fragments.clear();
        return buffer;
    }

    private List<Object> decode(final byte[] commandData) throws SynchronizeFXException {
        return serializer.deserialize(commandData);
    }
}
