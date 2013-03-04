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

package de.saxsys.synchronizefx.core.testutils;

import static org.junit.Assert.fail;

import java.util.List;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.TopologyLayerCallback;

/**
 * An implementation that saves the parameters of the callback function so that they can be evaluated in tests.
 */
public class SaveParameterCallback implements TopologyLayerCallback {
    private Object root;
    private List<Object> commands;

    @Override
    public void sendCommands(final List<Object> commands) {
        this.commands = commands;
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        fail("exception occured: " + error.getMessage());
    }

    @Override
    public void domainModelChanged(final Object root) {
        this.root = root;
    }

    /**
     * The list with commands that was received last via {@link TopologyLayerCallback#sendCommands(List)}.
     * 
     * @return The list or null if no commands where received yet.
     */
    public List<Object> getCommands() {
        return commands;
    }

    /**
     * The last root object for a domain model that was received via
     * {@link TopologyLayerCallback#domainModelChanged(Object)}.
     * 
     * @return The root object or null if {@link TopologyLayerCallback#domainModelChanged(Object)} was not called yet.
     */
    public Object getRoot() {
        return root;
    }
}