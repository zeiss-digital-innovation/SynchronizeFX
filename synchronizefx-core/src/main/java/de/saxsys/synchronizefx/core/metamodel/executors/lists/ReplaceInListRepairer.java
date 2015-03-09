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

import de.saxsys.synchronizefx.core.metamodel.Optional;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

/**
 * Repairs a remote {@link ReplaceInList} commands in relation to local {@link ListCommand}s and local
 * {@link ReplaceInList} commands in relation to remote {@link ListCommand}s.
 *
 *
 * @author Raik Bieniek
 */
class ReplaceInListRepairer {

    /**
     * Repairs a {@link ReplaceInList} in relation to an {@link AddToList} command.
     * 
     * @param toRepair
     *            The command to repair.
     * @param repairAgainst
     *            The command to repair against.
     * @return The repaired command.
     */
    public ReplaceInList repairCommand(final ReplaceInList toRepair, final AddToList repairAgainst) {
        if (repairAgainst.getPosition() > toRepair.getPosition()) {
            return toRepair;
        }
        return new ReplaceInList(toRepair.getListId(), toRepair.getListVersionChange(), toRepair.getValue(),
                toRepair.getPosition() + 1);
    }

    /**
     * Repairs a {@link ReplaceInList} in relation to an {@link RemoveFromList} command.
     * 
     * <p>
     * Repairing a {@link ReplaceInList} command can result in an {@link AddToList} command in the case that the element
     * that should be replaced was removed. Therefore this methods return a {@link ListCommand} which is either a
     * {@link ReplaceInList} or {@link AddToList}.
     * </p>
     * 
     * @param toRepair
     *            The command to repair.
     * @param repairAgainst
     *            The command to repair against.
     * @return The repaired command.
     */
    public ListCommand repairCommand(final ReplaceInList toRepair, final RemoveFromList repairAgainst) {
        final int indicesBefore = toRepair.getPosition() - repairAgainst.getStartPosition();
        if (indicesBefore <= 0) {
            return toRepair;
        }
        final int indicesToDecrese = indicesBefore < repairAgainst.getRemoveCount() ? indicesBefore : repairAgainst
                .getRemoveCount();

        if (repairAgainst.getStartPosition() + repairAgainst.getRemoveCount() <= toRepair.getPosition()) {
            return new ReplaceInList(toRepair.getListId(), toRepair.getListVersionChange(), toRepair.getValue(),
                    toRepair.getPosition() - indicesToDecrese);
        }
        return new AddToList(toRepair.getListId(), toRepair.getListVersionChange(), toRepair.getValue(),
                toRepair.getPosition() - indicesToDecrese);
    }

    /**
     * Repairs a local {@link ReplaceInList} in relation to a remote {@link ReplaceInList} command.
     * 
     * @param toRepair
     *            The local command to repair.
     * @param repairAgainst
     *            The remote command to repair against.
     * @return The repaired command or an empty optional if repairing results in droping the command.
     */
    public Optional<ReplaceInList> repairLocalCommand(final ReplaceInList toRepair, final ReplaceInList repairAgainst) {
        if (toRepair.getPosition() == repairAgainst.getPosition()) {
            return Optional.empty();
        }
        return Optional.of(toRepair);
    }

    /**
     * Repairs a remote {@link ReplaceInList} in relation to a local {@link ReplaceInList} command.
     * 
     * @param toRepair
     *            The remote command to repair.
     * @param repairAgainst
     *            The local command to repair against.
     * @return The repaired command.
     */
    public ReplaceInList repairRemoteCommand(final ReplaceInList toRepair, final ReplaceInList repairAgainst) {
        return toRepair;
    }
}
