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

package de.saxsys.synchronizefx.core.metamodel;


/**
 * Changes an Observable without notifying listeners of these changes.
 */
public class SilentChangeExecutor {

    private Listeners listeners;

    /**
     * Initializes an instance with its dependencies.
     */
    public SilentChangeExecutor() {
    }

    /**
     * Executes a change to an observable of the users domain model without generating change commands for this change.
     * 
     * <p>
     * Any changes done to the users domain model are executed over the model change executor passed in the constructor.
     * </p>
     * 
     * @param observable
     *            The observable that is changed.
     * @param change
     *            The change that is done to the observable.
     */
    public void execute(final Object observable, final Runnable change) {
        listeners.disableFor(observable);
        change.run();
        listeners.enableFor(observable);
    }

    /**
     * Registers the listener that should be suspended while modifying observables.
     * 
     * @param listeners
     *            the listeners
     */
    public void registerListenersToSilence(final Listeners listeners) {
        this.listeners = listeners;
    }
}
