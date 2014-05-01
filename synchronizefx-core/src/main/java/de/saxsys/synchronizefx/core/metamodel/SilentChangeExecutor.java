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

package de.saxsys.synchronizefx.core.metamodel;

/**
 * Changes an Observable without notifying listeners of these changes.
 */
class SilentChangeExecutor {

    private final Listeners listeners;
    private final ModelChangeExecutor executor;

    /**
     * Initializes an instance with its dependencies.
     * 
     * @param listeners
     *            used to disable and re-enable change notification while modifying observables.
     * @param changeExecutor
     *            used for execute any changes to the users domain model.
     */
    public SilentChangeExecutor(final Listeners listeners, final ModelChangeExecutor changeExecutor) {
        this.listeners = listeners;
        this.executor = changeExecutor;
    }

    /**
     * Executes a change to an observable of the users domain model without generating change commands for this change.
     * 
     * <p>
     * Any changes done to the users domain model are executed over the {@link ModelChangeExecutor} passed in the
     * constructor.
     * </p>
     * 
     * @param observable
     *            The observable that is changed.
     * @param change
     *            The change that is done to the observable.
     */
    public void execute(final Object observable, final Runnable change) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                listeners.disableFor(observable);
                change.run();
                listeners.enableFor(observable);
            }
        });
    }

    /**
     * An executor for changes to the users domain model.
     */
    public interface ModelChangeExecutor {

        /**
         * Executes a change to the users domain model.
         * 
         * @param change
         *            The change to execute.
         */
        void execute(Runnable change);
    }
}
