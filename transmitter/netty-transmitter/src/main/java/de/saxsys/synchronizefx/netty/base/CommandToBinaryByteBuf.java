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

package de.saxsys.synchronizefx.netty.base;

import java.util.List;

import de.saxsys.synchronizefx.core.clientserver.Serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

/**
 * Translates Netty {@link ByteBuf}s to {@link List}s of SynchronizeFX commands using a {@link Serializer}.
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 */
public class CommandToBinaryByteBuf extends MessageToMessageCodec<ByteBuf, List<Object>> {

    private final Serializer serializer;

    /**
     * Initializes the codec.
     * 
     * @param serializer The implementation for serializing and deserializing <code>byte[]</code> to SynchronizeFX
     *            commands.
     */
    public CommandToBinaryByteBuf(final Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf msg, final List<Object> out) throws Exception {
        byte[] data;
        if (msg.hasArray()) {
            data = msg.array();
        } else {
            data = new byte[msg.readableBytes()];
            msg.readBytes(data);
        }

        out.add(serializer.deserialize(data));
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final List<Object> msg, final List<Object> out)
        throws Exception {
        final ByteBuf buffer = Unpooled.wrappedBuffer(serializer.serialize(msg));
        buffer.retain();
        out.add(buffer);
    }
}
