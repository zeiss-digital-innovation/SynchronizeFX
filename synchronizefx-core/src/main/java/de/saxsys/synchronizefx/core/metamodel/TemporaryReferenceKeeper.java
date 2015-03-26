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

import java.util.Timer;
import java.util.TimerTask;

/**
 * Keeps hard references to arbitrary objects for 1 minute to prevent them from being garbage-collected to early.
 * 
 * @author Raik Bieniek
 */
public class TemporaryReferenceKeeper {

    /**
     * The time to keep hard references in milliseconds.
     */
    private static final long REFERENCE_KEEPING_TIME = 60000;
    private final Timer timer = new Timer();

    /**
     * Keeps a hard reference to the passed object for 1 minute.
     * 
     * @param object
     *            The object to keep a hard reference to.
     */
    public void keepReferenceTo(final Object object) {
        timer.schedule(new HardReferenceTask(object), REFERENCE_KEEPING_TIME);
    }

    /**
     * A task that just keeps a hard reference to an object.
     */
    private static class HardReferenceTask extends TimerTask {

        @SuppressWarnings("unused")
        // Just to keep the reference.
        private final Object referenceToKeep;

        public HardReferenceTask(final Object referenceToKeep) {
            this.referenceToKeep = referenceToKeep;
        }

        @Override
        public void run() {
            // Nothing needs to be done. The task must just stop existing.
        }
    }
}
