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

package de.saxsys.synchronizefx.core.metamodel.executors;

import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;

/**
 * Executes incoming change events on single value properties.
 * 
 * This executor handles the following change command:
 * 
 * <ul>
 * <li>{@link SetPropertyValue}</li>
 * </ul>
 * 
 * @author Raik Bieniek
 */
public class SingleValuePropertyCommandExecutor {

    /**
     * Logs a {@link SetPropertyValue} command that was send locally to the server.
     * 
     * @param command The command to log.
     */
    void logLocalCommand(final SetPropertyValue command) {

    }
}
