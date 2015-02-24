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

import java.util.UUID;

import static java.util.UUID.randomUUID;

import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand.ListVersionChange;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests if {@link AddToListRepairer} works as expected.
 * 
 * @author Raik Bieniek
 */
public class AddToListRepairerTest {

    private static final UUID SOME_LIST = randomUUID();
    private static final ListVersionChange SOME_CHANGE = new ListVersionChange(randomUUID(), randomUUID());
    private static final Value SOME_VALUE = new Value("exemplary value");

    private final AddToListRepairer cut = new AddToListRepairer();

    /**
     * When the index of an {@link AddToList} command is the same or greater than that of the other {@link AddToList}
     * command, its index should be increased.
     */
    @Test
    public void shouldIncreaseIndexOfAddToListIfIndexIsSameOrGreaterThanOtherAddToListIndex() {
        final AddToList toRepair1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 3);
        final AddToList repairAgainst1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 2);

        final AddToList repaired1 = cut.repairCommand(toRepair1, repairAgainst1);

        assertThat(repaired1.getPosition()).isEqualTo(4);

        final AddToList toRepair2 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 53);
        final AddToList repairAgainst2 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 53);

        final AddToList repaired2 = cut.repairCommand(toRepair2, repairAgainst2);

        assertThat(repaired2.getPosition()).isEqualTo(54);
    }

    /**
     * When the index of an {@link AddToList} command is lesser than that of the other {@link AddToList} command, its
     * index should be left unchanged.
     */
    @Test
    public void shouldNotChangeIndexOfAddToListIfIndexIsLessThanOtherAddToListIndex() {
        final AddToList toRepair1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 1);
        final AddToList repairAgainst1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 2);

        final AddToList repaired1 = cut.repairCommand(toRepair1, repairAgainst1);

        assertThat(repaired1.getPosition()).isEqualTo(1);
    }

    /**
     * The index of an {@link AddToList} command should be decreased by as much as there are elements removed before the
     * index of the {@link AddToList} command by an {@link RemoveFromList} command.
     */
    @Test
    public void shouldDecreaseIndexOfAddToListByAsMuchElementsThatAreBeforOrAtTheIndexInARemoveFromListCommand() {
        final AddToList toRepair1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 5);
        final RemoveFromList repairAgainst1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 2);
        final AddToList repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1.getPosition()).isEqualTo(3);

        final AddToList toRepair2 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 65);
        final RemoveFromList repairAgainst2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 64, 3);
        final AddToList repaired2 = cut.repairCommand(toRepair2, repairAgainst2);
        assertThat(repaired2.getPosition()).isEqualTo(64);
    }

    /**
     * The index of an {@link AddToList} command should be left untouched when the indices of the elements that should
     * be removed by a {@link RemoveFromList} command are all higher than the index of the {@link AddToList} command.
     */
    @Test
    public void shouldNotChangeIndexOfAddToListIfIndexIsLesserThanOrEqualToTheIndicesOfRemoveFromListCommand() {
        final AddToList toRepair1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 5);
        final RemoveFromList repairAgainst1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 8, 92);
        final AddToList repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1.getPosition()).isEqualTo(5);

        final AddToList toRepair2 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 20);
        final RemoveFromList repairAgainst2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 20, 1);
        final AddToList repaired2 = cut.repairCommand(toRepair2, repairAgainst2);
        assertThat(repaired2.getPosition()).isEqualTo(20);
    }

    /**
     * When an {@link AddToList} command is repaired against an {@link ReplaceInList} command its index should be left
     * untouched.
     */
    @Test
    public void shouldNotChangeIndexOfAddToListIfOtherCommandIsReplaceInList() {
        final AddToList toRepair1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 5);
        final ReplaceInList repairAgainst1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 5);
        final AddToList repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1.getPosition()).isEqualTo(5);
    }

    /**
     * The list id and the list version of a repaired command should not be changed.
     */
    @Test
    public void shouldNotChangeListIdOrVersion() {
        final AddToList toRepair = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 31);

        final AddToList repairAgainst1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 31);
        final RemoveFromList repairAgainst2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 31, 1);
        final ReplaceInList repairAgainst3 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 31);

        final AddToList repaired1 = cut.repairCommand(toRepair, repairAgainst1);
        final AddToList repaired2 = cut.repairCommand(toRepair, repairAgainst2);
        final AddToList repaired3 = cut.repairCommand(toRepair, repairAgainst3);

        assertThat(repaired1.getListId()).isEqualTo(repaired2.getListId()).isEqualTo(repaired3.getListId())
                .isEqualTo(SOME_LIST);

        assertThat(repaired1.getListVersionChange()).isEqualTo(repaired2.getListVersionChange())
                .isEqualTo(repaired3.getListVersionChange()).isEqualTo(SOME_CHANGE);
    }
}
