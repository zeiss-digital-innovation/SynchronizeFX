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

package de.saxsys.synchronizefx.core.metamodel.concurrency;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import de.saxsys.synchronizefx.core.inmemorypeers.InMemoryClient;
import de.saxsys.synchronizefx.core.inmemorypeers.InMemoryServer;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.ReparingListPropertyCommandExecutorTest;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that concurrent modifications on a list property are eventually consistent.
 * 
 * <p>
 * This test checks if all classes necessary for list synchronization work correctly together. An exhaustive test for
 * all corner cases is done in {@link ReparingListPropertyCommandExecutorTest}.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class ConcurrentModificationListPropertyIT {

    /**
     * The time to wait for consistency in milliseconds.
     */
    private static final int WAIT_FOR_CONSITENCY_TIME = 100;

    private InMemoryServer<ExemplaryModel> server;

    private InMemoryClient<ExemplaryModel> client1;
    private InMemoryClient<ExemplaryModel> client2;

    /**
     * Sets up the test model and all peers.
     */
    @Before
    public void setUpPeersAndTestData() {
        final ExemplaryModel model = new ExemplaryModel();
        model.exemplaryProperty.add("initial 1");
        model.exemplaryProperty.add("initial 2");

        server = new InMemoryServer<>(model);
        client1 = new InMemoryClient<>(server);
        client2 = new InMemoryClient<>(server);

        server.startSynchronizeFxServer();
        client1.startSynchronizeFxClient();
        client2.startSynchronizeFxClient();

        client1.setDelaySending(true);
        client2.setDelaySending(true);
    }

    /**
     * Two clients adding elements to the same list should eventually result in correct ordered list on all clients.
     */
    @Test
    public void concurrentAddsShouldEventuallyBeInCorrectOrder() {
        client1.executeInClientThread(new Runnable() {
            @Override
            public void run() {
                // before "initial 1"
                client1.getModel().exemplaryProperty.add(0, "client 1 change");
            }
        });

        client2.executeInClientThread(new Runnable() {
            @Override
            public void run() {
                // before "initial 2"
                client2.getModel().exemplaryProperty.add(1, "client 2 change");
            }
        });

        flushCommandsOfClients();
        
        assertThat(server.getModel()).isEqualTo(client1.getModel()).isEqualTo(client2.getModel());
        assertThat(server.getModel().exemplaryProperty).containsExactly("client 1 change", "initial 1",
                "client 2 change", "initial 2");
    }

    /**
     * Two clients removing the same element concurrently should eventually result in synchronous lists.
     */
    @Test
    public void concurrentlyRemovingTheSameElementShouldWork() {
        client1.executeInClientThread(new Runnable() {
            @Override
            public void run() {
                // remove "initial 2"
                client1.getModel().exemplaryProperty.remove(1);
            }
        });

        client2.executeInClientThread(new Runnable() {
            @Override
            public void run() {
                // remove "initial 2"
                client2.getModel().exemplaryProperty.remove(1);
            }
        });

        flushCommandsOfClients();

        assertThat(server.getModel()).isEqualTo(client1.getModel()).isEqualTo(client2.getModel());
        assertThat(server.getModel().exemplaryProperty).containsExactly("initial 1");
    }

    private void flushCommandsOfClients() {
        waitShortly();
        client1.setDelaySending(false);
        client2.setDelaySending(false);
        waitShortly();
    }

    private void waitShortly() {
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
        private final ListProperty<String> exemplaryProperty = new SimpleListProperty<>(
                FXCollections.<String> observableArrayList());

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((exemplaryProperty == null) ? 0 : exemplaryProperty.hashCode());
            return result;
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
            ExemplaryModel other = (ExemplaryModel) obj;
            if (exemplaryProperty == null) {
                if (other.exemplaryProperty != null) {
                    return false;
                }
            } else if (!exemplaryProperty.equals(other.exemplaryProperty)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "ExemplaryModel [exemplaryProperty=" + exemplaryProperty + "]";
        }
    }
}
