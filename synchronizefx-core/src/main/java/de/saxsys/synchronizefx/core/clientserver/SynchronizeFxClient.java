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

package de.saxsys.synchronizefx.core.clientserver;

import javafx.beans.property.Property;

/**
 * This class implements a client that accesses a JavaFX model made available over the network by a
 * {@link SynchronizeFxServer}. All fields of the model that implement the {@link Property} interface will be
 * synchronized between all clients.
 * 
 * @author raik.bieniek
 * 
 */
public class SynchronizeFxClient {

    private DomainModelClient impl;

    /**
     * Sets up everything that is needed to to get the domain model instance from the server.
     * 
     * This method doesn't connect to the server. Use {@link SynchronizeFxClient#connect()} for that.
     * 
     * @param networkLayer An object that does the serialization and the network transfer of the data generated to
     *            keep models synchron.
     * @param listener Used to inform the user of this class on errors and when the initial transfer of the domain
     *            model is ready. The methods in the callback are not called before you call
     *            {@link SynchronizeFxClient#connect()}
     */
    public SynchronizeFxClient(final MessageTransferClient networkLayer, final ClientCallback listener) {
        impl = new DomainModelClient(networkLayer, listener);
    }

    /**
     * Connects to the server and requests the domain model from it.
     * 
     * When the model is transfered completly it is returned by invoking the
     * {@link ClientCallback#modelReady(Object)} method on the instance supplied by the user in the constructor.
     */
    public void connect() {
        impl.connect();
    }

    /**
     * Terminates the connection to the server.
     */
    public void disconnect() {
        impl.disconnect();
    }
}
