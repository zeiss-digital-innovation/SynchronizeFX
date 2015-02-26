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
 * Checks if {@link RemoveFromListRepairer} works as expected.
 * 
 * @author Raik Bieniek
 */
public class RemoveFromListRepairerTest {

    private static final UUID SOME_LIST = randomUUID();
    private static final ListVersionChange SOME_CHANGE = new ListVersionChange(randomUUID(), randomUUID());
    private static final Value SOME_VALUE = new Value("exemplary value");

    private final RemoveFromListRepairer cut = new RemoveFromListRepairer();

    // ///////////////
    // / AddToList ///
    // ///////////////

    /**
     * When the indices that should be removed by an {@link RemoveFromList} command are all before an {@link AddToList}
     * command, the indices to remove should be left untouched.
     */
    @Test
    public void shouldNotChangeIndicesWhenTheyAreAllBeforeIndexOfAddToListCommand() {
        final RemoveFromList toRepair1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 5, 1);
        final AddToList repairAgainst1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 6);
        final List<RemoveFromList> repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1).hasSize(1);
        positionAndCountEquals(repaired1.get(0), 5, 1);

        final RemoveFromList toRepair2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 40, 15);
        final AddToList repairAgainst2 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 96);
        final List<RemoveFromList> repaired2 = cut.repairCommand(toRepair2, repairAgainst2);
        assertThat(repaired2).hasSize(1);
        positionAndCountEquals(repaired2.get(0), 40, 15);
    }

    /**
     * When the indices that should be removed by an {@link RemoveFromList} command include the index of an
     * {@link AddToList} command, two {@link RemoveFromList} commands should be created skipping the index of the
     * {@link AddToList} command but removing the same amount of elements.
     */
    @Test
    public void shouldSkipIndexOfAddToListCommandButRemoveSameAmountOfElements() {
        final RemoveFromList toRepair1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 8, 10);
        final AddToList repairAgainst1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 12);
        final List<RemoveFromList> repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1).hasSize(2);
        positionAndCountEquals(repaired1.get(0), 8, 4);
        positionAndCountEquals(repaired1.get(1), 13, 6);

        final RemoveFromList toRepair2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 12, 6);
        final AddToList repairAgainst2 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 12);
        final List<RemoveFromList> repaired2 = cut.repairCommand(toRepair2, repairAgainst2);
        assertThat(repaired2).hasSize(1);
        positionAndCountEquals(repaired2.get(0), 13, 6);

        final RemoveFromList toRepair3 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 4);
        final AddToList repairAgainst3 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 5);
        final List<RemoveFromList> repaired3 = cut.repairCommand(toRepair3, repairAgainst3);
        assertThat(repaired3).hasSize(2);
        positionAndCountEquals(repaired3.get(0), 2, 3);
        positionAndCountEquals(repaired3.get(1), 6, 1);
    }

    /**
     * When the indices that should be removed by an {@link RemoveFromList} command are after the index of an
     * {@link AddToList} command, the <code>startPosition</code> should be increased.
     */
    @Test
    public void shouldIncreaseStartIndexWhenAllIndicesAreAfterAddToListCommand() {
        final RemoveFromList toRepair1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 23, 28);
        final AddToList repairAgainst1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 16);
        final List<RemoveFromList> repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1).hasSize(1);
        positionAndCountEquals(repaired1.get(0), 24, 28);
    }

    // ////////////////////
    // / RemoveFromList ///
    // ////////////////////

    /**
     * When the indices of a {@link RemoveFromList} command are all before the indices of an other
     * {@link RemoveFromList} command, the indices should not be changed.
     */
    @Test
    public void shouldNotChangeIndicesWhenTheyAreAllBeforeIndicesOfOtherRemoveFromListCommand() {
        final RemoveFromList toRepair1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 5, 10);
        final RemoveFromList repairAgainst1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 16, 8);
        final List<RemoveFromList> repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1).hasSize(1);
        positionAndCountEquals(repaired1.get(0), 5, 10);
    }

    /**
     * When a {@link RemoveFromList} command removes some of the indices an other {@link RemoveFromList} command removes
     * too, these indices should not be removed.
     */
    @Test
    public void shouldNotRemoveIndicesAlreadyRemovedByOtherRemoveFromListCommand() {
        final RemoveFromList toRepair1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 4);
        final RemoveFromList repairAgainst1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 4, 10);
        final List<RemoveFromList> repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1).hasSize(1);
        positionAndCountEquals(repaired1.get(0), 2, 2);

        final RemoveFromList toRepair2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 4);
        final RemoveFromList repairAgainst2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 4, 2);
        final List<RemoveFromList> repaired2 = cut.repairCommand(toRepair2, repairAgainst2);
        assertThat(repaired2).hasSize(1);
        positionAndCountEquals(repaired2.get(0), 2, 2);

        final RemoveFromList toRepair3 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 4);
        final RemoveFromList repairAgainst3 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 4);
        final List<RemoveFromList> repaired3 = cut.repairCommand(toRepair3, repairAgainst3);
        assertThat(repaired3).hasSize(0);

        final RemoveFromList toRepair4 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 4);
        final RemoveFromList repairAgainst4 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 0, 20);
        final List<RemoveFromList> repaired4 = cut.repairCommand(toRepair4, repairAgainst4);
        assertThat(repaired4).hasSize(0);

        final RemoveFromList toRepair5 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 0, 20);
        final RemoveFromList repairAgainst5 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 4, 2);
        final List<RemoveFromList> repaired5 = cut.repairCommand(toRepair5, repairAgainst5);
        assertThat(repaired5).hasSize(1);
        positionAndCountEquals(repaired5.get(0), 0, 18);
    }

    /**
     * When an other {@link RemoveFromList} command removes indices before the {@link RemoveFromList} that should be
     * repaired, the start position of the {@link RemoveFromList} command should be decreased by the amount of indices
     * removed by the other command.
     */
    @Test
    public void shouldDecreaseStartPositionWhenAllIndicesOfOtherRemoveFromListCommandAreBefore() {
        final RemoveFromList toRepair1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 10, 5);
        final RemoveFromList repairAgainst1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 3, 4);
        final List<RemoveFromList> repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1).hasSize(1);
        positionAndCountEquals(repaired1.get(0), 6, 5);

        final RemoveFromList toRepair2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 3, 4);
        final RemoveFromList repairAgainst2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 0, 4);
        final List<RemoveFromList> repaired2 = cut.repairCommand(toRepair2, repairAgainst2);
        assertThat(repaired2).hasSize(1);
        positionAndCountEquals(repaired2.get(0), 0, 3);

        final RemoveFromList toRepair3 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 20, 10);
        final RemoveFromList repairAgainst3 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 20, 3);
        final List<RemoveFromList> repaired3 = cut.repairCommand(toRepair3, repairAgainst3);
        assertThat(repaired3).hasSize(1);
        positionAndCountEquals(repaired3.get(0), 20, 7);

        final RemoveFromList toRepair4 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 10, 10);
        final RemoveFromList repairAgainst4 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 5, 10);
        final List<RemoveFromList> repaired4 = cut.repairCommand(toRepair4, repairAgainst4);
        assertThat(repaired4).hasSize(1);
        positionAndCountEquals(repaired4.get(0), 5, 5);
    }

    // ///////////////////
    // / ReplaceInList ///
    // ///////////////////

    // ReplaceInList commands are handles exactly like AddToList commands because repairing a ReplaceInList command
    // against an RemoveFromList command transforms it into an AddToList command if neccessary.

    /**
     * When the indices that should be removed by an {@link RemoveFromList} command are all before an
     * {@link ReplaceInList} command, the indices to remove should be left untouched.
     */
    @Test
    public void shouldNotChangeIndicesWhenTheyAreAllBeforeIndexOfReplaceInListCommand() {
        final RemoveFromList toRepair1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 5, 1);
        final ReplaceInList repairAgainst1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 6);
        final List<RemoveFromList> repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1).hasSize(1);
        positionAndCountEquals(repaired1.get(0), 5, 1);

        final RemoveFromList toRepair2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 40, 15);
        final ReplaceInList repairAgainst2 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 96);
        final List<RemoveFromList> repaired2 = cut.repairCommand(toRepair2, repairAgainst2);
        assertThat(repaired2).hasSize(1);
        positionAndCountEquals(repaired2.get(0), 40, 15);
    }

    /**
     * When the indices that should be removed by an {@link RemoveFromList} command include the index of an
     * {@link ReplaceInList} command, two {@link RemoveFromList} commands should be created skipping the index of the
     * {@link ReplaceInList} command but removing the same amount of elements.
     */
    @Test
    public void shouldSkipIndexOfReplaceInListCommandButRemoveSameAmountOfElements() {
        final RemoveFromList toRepair1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 8, 10);
        final ReplaceInList repairAgainst1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 12);
        final List<RemoveFromList> repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1).hasSize(2);
        positionAndCountEquals(repaired1.get(0), 8, 4);
        positionAndCountEquals(repaired1.get(1), 13, 6);

        final RemoveFromList toRepair2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 12, 6);
        final ReplaceInList repairAgainst2 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 12);
        final List<RemoveFromList> repaired2 = cut.repairCommand(toRepair2, repairAgainst2);
        assertThat(repaired2).hasSize(1);
        positionAndCountEquals(repaired2.get(0), 13, 6);

        final RemoveFromList toRepair3 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 4);
        final ReplaceInList repairAgainst3 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 5);
        final List<RemoveFromList> repaired3 = cut.repairCommand(toRepair3, repairAgainst3);
        assertThat(repaired3).hasSize(2);
        positionAndCountEquals(repaired3.get(0), 2, 3);
        positionAndCountEquals(repaired3.get(1), 6, 1);
    }

    /**
     * When the indices that should be removed by an {@link RemoveFromList} command are after the index of an
     * {@link ReplaceInList} command, the <code>startPosition</code> should be increased.
     */
    @Test
    public void shouldIncreaseStartIndexWhenAllIndicesAreAfterReplaceInListCommand() {
        final RemoveFromList toRepair1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 23, 28);
        final ReplaceInList repairAgainst1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 16);
        final List<RemoveFromList> repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1).hasSize(1);
        positionAndCountEquals(repaired1.get(0), 24, 28);
    }

    // ///////////
    // / Other ///
    // ///////////

    /**
     * The list id and the list version of a repaired command should not be changed.
     */
    @Test
    public void shouldNotChangeListIdOrVersion() {
        final RemoveFromList toRepair2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 12, 6);
        final AddToList repairAgainst2 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 12);
        final List<RemoveFromList> repaired2 = cut.repairCommand(toRepair2, repairAgainst2);
        assertThat(repaired2).hasSize(1);
        assertThat(repaired2.get(0).getListId()).isEqualTo(SOME_LIST);
        assertThat(repaired2.get(0).getListVersionChange()).isEqualTo(SOME_CHANGE);

        final RemoveFromList toRepair1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 4);
        final RemoveFromList repairAgainst1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 4, 10);
        final List<RemoveFromList> repaired1 = cut.repairCommand(toRepair1, repairAgainst1);
        assertThat(repaired1).hasSize(1);
        assertThat(repaired1.get(0).getListId()).isEqualTo(SOME_LIST);
        assertThat(repaired1.get(0).getListVersionChange()).isEqualTo(SOME_CHANGE);

        final RemoveFromList toRepair3 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 4);
        final ReplaceInList repairAgainst3 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 5);
        final List<RemoveFromList> repaired3 = cut.repairCommand(toRepair3, repairAgainst3);
        assertThat(repaired3).hasSize(2);
        assertThat(repaired3.get(0).getListId()).isEqualTo(SOME_LIST);
        assertThat(repaired3.get(0).getListVersionChange()).isEqualTo(SOME_CHANGE);
        assertThat(repaired3.get(1).getListId()).isEqualTo(SOME_LIST);
        assertThat(repaired3.get(1).getListVersionChange()).isEqualTo(SOME_CHANGE);
    }

    private void positionAndCountEquals(final RemoveFromList removeFromList, final int startPosition, final int count) {
        assertThat(removeFromList.getStartPosition()).isEqualTo(startPosition);
        assertThat(removeFromList.getRemoveCount()).isEqualTo(count);
    }
}
