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

package de.saxsys.synchronizefx;

/**
 * This is a Step Builder Pattern implementation to create client and server instances for SynchronizeFX.
 * 
 * @author manuel.mauky
 * 
 */
public final class SynchronizeFxStepBuilder {
    private SynchronizeFxStepBuilder() {
    }

    /**
     * Initial step to create the builder instance.
     * 
     * @return the builder.
     */
    public static InitialStep create() {
        return new Builder();
    }

    /**
     * The default implementation of {@link InitialStep}.
     * 
     * @see InitialStep
     */
    private static class Builder implements InitialStep {
        @Override
        public ServerModelStep server() {
            return new ServerBuilder();
        }

        @Override
        public ClientCallbackStep client() {
            return new ClientBuilder();
        }
    }
}
