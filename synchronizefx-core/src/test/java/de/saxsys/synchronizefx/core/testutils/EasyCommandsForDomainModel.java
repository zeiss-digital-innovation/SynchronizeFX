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

package de.saxsys.synchronizefx.core.testutils;

import java.util.List;

import de.saxsys.synchronizefx.core.metamodel.CommandsForDomainModelCallback;
import de.saxsys.synchronizefx.core.metamodel.MetaModel;

/**
 * A tool to simplify
 * {@link MetaModel#commandsForDomainModel(de.saxsys.synchronizefx.core.metamodel.CommandsForDomainModelCallback)} for
 * test that can ensure thread safety in other ways.
 */
public final class EasyCommandsForDomainModel {
    private List<Object> commands;
    private MetaModel model;

    private EasyCommandsForDomainModel(final MetaModel model) {
        this.model = model;
    }
    
    private List<Object> commandsForDomainModel() {
        model.commandsForDomainModel(new CommandsForDomainModelCallback() {
            
            @Override
            public void commandsReady(final List<Object> initialCommands) {
                commands = initialCommands;
            }
        });
        return commands;
    }

    /**
     * Creates the commands necessary to reproduce a domain model.
     * 
     * Changes that are done to the domain model while this method has not returned can be lost.
     * 
     * @param model the MetaModel for the domain model that the commands should be created.
     * @return The commands
     */
    public static List<Object> commandsForDomainModel(final MetaModel model) {
        return new EasyCommandsForDomainModel(model).commandsForDomainModel();
    }
}
