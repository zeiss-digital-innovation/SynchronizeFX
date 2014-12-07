/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
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

package de.saxsys.synchronizefx.netty.base;

import de.saxsys.synchronizefx.core.clientserver.Serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPipeline;

/**
 * A codec that collects all data of a message created by a {@link Serializer} and passes it to the next pipeline
 * stage.
 * 
 * @author Raik Bieniek
 */
public interface Codec {

    /**
     * Adds the neccessary handler to the pipeline that ensure that coherent messages created by {@link Serializer}
     * are passed as on unit to the next pipeline stage.
     * 
     * <p>
     * Data recieved over the network enteres the handlers as it comes in in the form of {@link ByteBuf}s. Usually a
     * large message will be recieved in multiple small chunks. A chunk can also contain data of multiple messages.
     * The handlers added to this pipeline must ensure that further handlers recieve coherent {@link Serializer}
     * messages in form of {@link ByteBuf}s.
     * </p>
     * 
     * @param pipeline The pipeline to add the handler to.
     */
    void addToPipeline(ChannelPipeline pipeline);
}
