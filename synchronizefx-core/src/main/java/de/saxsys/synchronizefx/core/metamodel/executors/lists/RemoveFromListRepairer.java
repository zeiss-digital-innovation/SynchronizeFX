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

package de.saxsys.synchronizefx.core.metamodel.executors.lists;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;

import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

/**
 * Repairs a remote {@link RemoveFromList} commands in relation to local {@link ListCommand}s and local
 * {@link RemoveFromList} commands in relation to remote {@link ListCommand}s.
 *
 * <p>
 * Repairing {@link RemoveFromList} command can result in more complex instructions thats semantics are not covered by a
 * single {@link RemoveFromList} command. Therefore in this class the a {@link List} of {@link RemoveFromList} commands
 * is returned which is guaranteed to contain at least one command.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class RemoveFromListRepairer {

    /**
     * Repairs a {@link RemoveFromList} in relation to an {@link AddToList} command.
     * 
     * @param toRepair
     *            The command to repair.
     * @param repairAgainst
     *            The command to repair against.
     * @return The repaired command.
     */
    public List<RemoveFromList> repairCommand(final RemoveFromList toRepair, final AddToList repairAgainst) {
        return repairAddOrReplace(toRepair, repairAgainst.getPosition());
    }

    /**
     * Repairs a {@link RemoveFromList} in relation to a {@link RemoveFromList} command.
     * 
     * @param toRepair
     *            The command to repair.
     * @param repairAgainst
     *            The command to repair against.
     * @return The repaired command.
     */
    public List<RemoveFromList> repairCommand(final RemoveFromList toRepair, final RemoveFromList repairAgainst) {
        if (toRepair.getStartPosition() + toRepair.getRemoveCount() <= repairAgainst.getStartPosition()) {
            return asList(toRepair);
        }
        if (toRepair.getStartPosition() >= repairAgainst.getStartPosition() + repairAgainst.getRemoveCount()) {
            return asList(createRepaired(toRepair, toRepair.getStartPosition() - repairAgainst.getRemoveCount(),
                    toRepair.getRemoveCount()));
        }
        final int startPosition = toRepair.getStartPosition() < repairAgainst.getStartPosition() ? toRepair
                .getStartPosition() : repairAgainst.getStartPosition();

        final int indicesBefore = repairAgainst.getStartPosition() - toRepair.getStartPosition();
        final int indicesAfter = (toRepair.getStartPosition() + toRepair.getRemoveCount())
                - (repairAgainst.getStartPosition() + repairAgainst.getRemoveCount());
        final int indicesBeforeAndAfter = max(indicesBefore, 0) + max(indicesAfter, 0);

        if (indicesBeforeAndAfter == 0) {
            return asList(createRepaired(toRepair, 0, 0));
        }

        final int removeCount = min(indicesBeforeAndAfter, toRepair.getRemoveCount());
        return asList(createRepaired(toRepair, startPosition, removeCount));
    }

    /**
     * Repairs a {@link RemoveFromList} in relation to a {@link ReplaceInList} command.
     * 
     * @param toRepair
     *            The command to repair.
     * @param repairAgainst
     *            The command to repair against.
     * @return The repaired command.
     */
    public List<RemoveFromList> repairCommand(final RemoveFromList toRepair, final ReplaceInList repairAgainst) {
        return repairAddOrReplace(toRepair, repairAgainst.getPosition());
    }

    private List<RemoveFromList> repairAddOrReplace(final RemoveFromList toRepair, final int position) {
        if (toRepair.getStartPosition() + toRepair.getRemoveCount() <= position) {
            return asList(toRepair);
        }
        if (toRepair.getStartPosition() >= position) {
            return asList(createRepaired(toRepair, toRepair.getStartPosition() + 1, toRepair.getRemoveCount()));
        }

        final int removeCountBefore = position - toRepair.getStartPosition();
        final int removeCountAfter = toRepair.getRemoveCount() - removeCountBefore;
        return asList(createRepaired(toRepair, toRepair.getStartPosition(), removeCountBefore),
                createRepaired(toRepair, position + 1, removeCountAfter));
    }

    private RemoveFromList createRepaired(final RemoveFromList toRepair, final int startPosition, //
            final int removeCount) {
        return new RemoveFromList(toRepair.getListId(), toRepair.getListVersionChange(), startPosition, removeCount);
    }
}
