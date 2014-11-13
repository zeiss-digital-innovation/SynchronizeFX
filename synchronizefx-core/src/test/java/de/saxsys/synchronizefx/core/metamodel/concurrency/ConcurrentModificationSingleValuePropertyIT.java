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

package de.saxsys.synchronizefx.core.metamodel.concurrency;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import de.saxsys.synchronizefx.core.inmemorypeers.InMemoryClient;
import de.saxsys.synchronizefx.core.inmemorypeers.InMemoryServer;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that concurrent modifications on a single value property are eventually consistent.
 * 
 * @author Raik Bieniek
 */
public class ConcurrentModificationSingleValuePropertyIT {

    /**
     * The time to wait for consistency in milliseconds.
     */
    private static final int WAIT_FOR_CONSITENCY_TIME = 100;

    private static final int INITIAL_VALUE = 0;
    private static final int CLIENT_1_CHANGE = 1;
    private static final int CLIENT_2_CHANGE = 2;

    private final InMemoryServer<ExemplaryModel> server = new InMemoryServer<>(new ExemplaryModel());

    private final InMemoryClient<ExemplaryModel> client1 = new InMemoryClient<>(server);
    private final InMemoryClient<ExemplaryModel> client2 = new InMemoryClient<>(server);

    /**
     * Connect the clients to the server.
     */
    @Before
    public void setUpServerAndClients() {
        server.startSynchronizeFxServer();
        client1.startSynchronizeFxClient();
        client2.startSynchronizeFxClient();
    }

    /**
     * Checks that a property that is changed on multiple clients at nearly the same time is eventually synchron on
     * all clients and the server.
     */
    @Test
    public void valueOfSingleValuePropertyShouldBeEventuallyConsistent() {
        client1.executeInClientThread(new Runnable() {
            @Override
            public void run() {
                client1.getModel().exemplaryProperty.set(CLIENT_1_CHANGE);
            }
        });

        client2.executeInClientThread(new Runnable() {
            @Override
            public void run() {
                client2.getModel().exemplaryProperty.set(CLIENT_2_CHANGE);
            }
        });

        waitForConsistency();

        final ExemplaryModel unchangedModel = new ExemplaryModel();

        assertThat(server.getModel()).isEqualTo(client1.getModel()).isEqualTo(client2.getModel())
                .isNotEqualTo(unchangedModel);
    }

    private void waitForConsistency() {
        try {
            Thread.sleep(WAIT_FOR_CONSITENCY_TIME);
        } catch (final InterruptedException e) {
            throw new RuntimeException("Could not wait for consistency. ", e);
        }
    }

    /**
     * An exemplary model that is used in the tests.
     */
    public static final class ExemplaryModel {
        private final IntegerProperty exemplaryProperty = new SimpleIntegerProperty(INITIAL_VALUE);

        @Override
        public int hashCode() {
            return exemplaryProperty.get();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ExemplaryModel other = (ExemplaryModel) obj;
            return exemplaryProperty.get() == other.exemplaryProperty.get();
        }

        @Override
        public String toString() {
            return "ExemplaryModel [exemplaryProperty=" + exemplaryProperty.get() + "]";
        }
    }
}
