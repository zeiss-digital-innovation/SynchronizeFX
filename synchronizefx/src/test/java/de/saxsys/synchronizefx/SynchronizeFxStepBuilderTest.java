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

package de.saxsys.synchronizefx;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.kryo.Serializer;

import de.saxsys.synchronizefx.core.clientserver.ClientCallback;
import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;

/**
 * This test is used to demonstrate the use of the {@link SynchronizeFxBuilder}.
 * 
 */
public class SynchronizeFxStepBuilderTest {
    private ClientCallback clientCallback;

    private ServerCallback serverCallback;

    private Object modelObject;

    private Serializer<Double> doubleSerializer;
    private Serializer<Integer> integerSerializer;

    /**
     * Mainly initializes mocks for callbacks and serializers.
     */
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        clientCallback = mock(ClientCallback.class);

        serverCallback = mock(ServerCallback.class);

        modelObject = new Object();

        doubleSerializer = mock(Serializer.class);

        integerSerializer = mock(Serializer.class);
    }

    /**
     * Checks if a client is build for the least possible count of method calls.
     */
    @Test
    public void testSimplestPossibleClient() {
        final SynchronizeFxClient client =
                SynchronizeFxBuilder.create().client().address("localhost").callback(clientCallback).build();
        Assert.assertNotNull(client);
    }

    /**
     * Checks if a client is build when all possible values in the builder are set.
     */
    @Test
    public void testClientWithAllPossibleValues() {
        final SynchronizeFxClient client =
                SynchronizeFxBuilder.create().client().address("192.168.0.1").callback(clientCallback).port(16789)
                        .customSerializer(Double.class, doubleSerializer)
                        .customSerializer(Integer.class, integerSerializer).build();
        Assert.assertNotNull(client);
    }

    /**
     * Checks if a server is build for the least possible count of method calls.
     */
    @Test
    public void testSimplestPossibleServer() {
        final SynchronizeFxServer server =
                SynchronizeFxBuilder.create().server().model(modelObject).callback(serverCallback).build();
        Assert.assertNotNull(server);
    }

    /**
     * Checks if a server is build when all possible values in the builder are set.
     */
    @Test
    public void testServerWithAllPossibleValues() {
        final SynchronizeFxServer server =
                SynchronizeFxBuilder.create().server().model(modelObject).callback(serverCallback)
                        .customSerializer(Double.class, doubleSerializer).port(16789)
                        .customSerializer(Integer.class, integerSerializer).build();
        Assert.assertNotNull(server);
    }
}
