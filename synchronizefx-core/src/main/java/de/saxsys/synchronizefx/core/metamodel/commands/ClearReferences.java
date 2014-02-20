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

package de.saxsys.synchronizefx.core.metamodel.commands;

import javafx.beans.property.Property;

/**
 * Tells the other side, that hard references that where created during the assembling of the objects can now be
 * deleted.
 * 
 * The commands to create the values of {@link Property} fields in domain object are in some cases send before the
 * command to create the domain object. Until the values are attached to the domain object there would normally only
 * weak references to these values which could mean that they are already garbage collected before they are attached to
 * the domain obejct. To prevent this all created object are put into a set to have a hard reference. To prevent memory
 * leaks this set has to be emptied to make the objects garbage collectible when they are no longer used by the user.
 * This is done by this command.
 * 
 * @author raik.bieniek
 * 
 */
public class ClearReferences {

    @Override
    public String toString() {
        return "ClearReferences []";
    }
}
