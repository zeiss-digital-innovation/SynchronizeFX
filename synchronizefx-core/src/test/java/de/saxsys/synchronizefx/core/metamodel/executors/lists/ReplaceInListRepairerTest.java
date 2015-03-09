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

import de.saxsys.synchronizefx.core.metamodel.Optional;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand.ListVersionChange;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks if {@link ReplaceInListRepairer} works as expected.
 * 
 * @author Raik Bieniek
 */
public class ReplaceInListRepairerTest {

    private static final UUID SOME_LIST = randomUUID();
    private static final ListVersionChange SOME_CHANGE = new ListVersionChange(randomUUID(), randomUUID());
    private static final Value SOME_VALUE = new Value("some value");

    private final ReplaceInListRepairer cut = new ReplaceInListRepairer();

    // ///////////////
    // / AddToList ///
    // ///////////////

    /**
     * If the index of an {@link AddToList} command is less or equal than the index of the {@link ReplaceInList} command
     * that should be repaired, the index should be increased.
     */
    @Test
    public void shouldIncreaseIndexIfIndexOfAddToListIsLessOrEqual() {
        final ReplaceInList toRepair1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 6);
        final AddToList repairAgainst1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 3);
        final ReplaceInList repaired1 = cut.repairCommand(toRepair1, repairAgainst1);

        assertThat(repaired1.getPosition()).isEqualTo(7);

        final ReplaceInList toRepair2 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 9);
        final AddToList repairAgainst2 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 9);
        final ReplaceInList repaired2 = cut.repairCommand(toRepair2, repairAgainst2);

        assertThat(repaired2.getPosition()).isEqualTo(10);
    }

    /**
     * If the index of an {@link AddToList} command is greater than index of the {@link ReplaceInList} command that
     * should be repaired, the index should be left untouched.
     */
    @Test
    public void shouldNotChangeIndexIfIndexOfAddToListIsGreater() {
        final ReplaceInList toRepair1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 3);
        final AddToList repairAgainst1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 6);
        final ReplaceInList repaired1 = cut.repairCommand(toRepair1, repairAgainst1);

        assertThat(repaired1.getPosition()).isEqualTo(3);
    }

    // ////////////////////
    // / RemoveFromList ///
    // ////////////////////

    /**
     * The index of an {@link ReplaceInList} command should be decreased by as much as there are elements removed before
     * the index of the {@link ReplaceInList} command by an {@link RemoveFromList} command.
     */
    @Test
    public void shouldDecreaseIndexOfAddToListByAsMuchElementsThatAreBeforOrAtTheIndexInARemoveFromListCommand() {
        final ReplaceInList toRepair1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 5);
        final RemoveFromList repairAgainst1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 2, 2);
        final ListCommand repaired1 = cut.repairCommand(toRepair1, repairAgainst1);

        assertThat(repaired1).isInstanceOf(ReplaceInList.class);
        assertThat(((ReplaceInList) repaired1).getPosition()).isEqualTo(3);

        final ReplaceInList toRepair2 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 20);
        final RemoveFromList repairAgainst2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 18, 2);
        final ListCommand repaired2 = cut.repairCommand(toRepair2, repairAgainst2);

        assertThat(repaired2).isInstanceOf(ReplaceInList.class);
        assertThat(((ReplaceInList) repaired2).getPosition()).isEqualTo(18);
    }

    /**
     * When the index of {@link ReplaceInList} command was removed by a {@link RemoveFromList} command it should be
     * converted to an {@link AddToList} command.
     */
    @Test
    public void shouldReturnAddToListWithDecreasedIndexWhenIndexOfReplaceInListWasRemoved() {
        final ReplaceInList toRepair1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 65);
        final RemoveFromList repairAgainst1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 64, 3);
        final ListCommand repaired1 = cut.repairCommand(toRepair1, repairAgainst1);

        assertThat(repaired1).isInstanceOf(AddToList.class);
        assertThat(((AddToList) repaired1).getPosition()).isEqualTo(64);

    }

    /**
     * The index of an {@link ReplaceInList} command should be left untouched when the indices of the elements that
     * should be removed by a {@link RemoveFromList} command are all higher than the index of the {@link ReplaceInList}
     * command.
     */
    @Test
    public void shouldNotChangeIndexOfAddToListIfIndexIsLesserThanToTheIndicesOfRemoveFromListCommand() {
        final ReplaceInList toRepair1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 5);
        final RemoveFromList repairAgainst1 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 8, 92);
        final ListCommand repaired1 = cut.repairCommand(toRepair1, repairAgainst1);

        assertThat(repaired1).isInstanceOf(ReplaceInList.class);
        assertThat(((ReplaceInList) repaired1).getPosition()).isEqualTo(5);
    }

    // ///////////////////
    // / ReplaceInList ///
    // ///////////////////

    /**
     * When the index of a {@link ReplaceInList} command differs from the {@link ReplaceInList} command that should be
     * returned, its index should not be changed.
     */
    @Test
    public void shouldNotChangeIndexIfTwoReplaceInListCommandsReplaceDifferentIndizes() {
        final ReplaceInList toRepair1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 93);
        final ReplaceInList repairAgainst1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 94);
        final Optional<ReplaceInList> repaired1 = cut.repairLocalCommand(toRepair1, repairAgainst1);
        assertThat(repaired1.isPresent()).isTrue();
        assertThat(repaired1.get().getPosition()).isEqualTo(93);

        final ReplaceInList toRepair2 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 39);
        final ReplaceInList repairAgainst2 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 152);
        final ReplaceInList repairRemoteCommand2 = cut.repairRemoteCommand(toRepair2, repairAgainst2);
        assertThat(repairRemoteCommand2.getPosition()).isEqualTo(39);
    }

    /**
     * When the index of a local {@link ReplaceInList} command which should be repaired is the same as the index of a
     * remote {@link ReplaceInList} repaired against, the local command should be tropped.
     */
    @Test
    public void shouldDropLocalCommandWhenIndicesAreEqual() {
        final ReplaceInList toRepair1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 4);
        final ReplaceInList repairAgainst1 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 4);
        final Optional<ReplaceInList> repaired1 = cut.repairLocalCommand(toRepair1, repairAgainst1);
        assertThat(repaired1.isPresent()).isFalse();
    }

    /**
     * The index of a remote {@link ReplaceInList} command should never be changed, even when the index is equal to the
     * index of a local {@link ReplaceInList} it is repaired against.
     */
    @Test
    public void shouldNotChangeRemoteCommandEvenWhenIndicesAreEqual() {
        final ReplaceInList toRepair2 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 86);
        final ReplaceInList repairAgainst2 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 86);
        final ReplaceInList repairRemoteCommand2 = cut.repairRemoteCommand(toRepair2, repairAgainst2);
        assertThat(repairRemoteCommand2.getPosition()).isEqualTo(86);
    }

    // ///////////
    // / Other ///
    // ///////////

    /**
     * The list id and the list version of a repaired command should not be changed.
     */
    @Test
    public void shouldNotChangeListIdOrVersion() {
        final ReplaceInList toRepair = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 6);

        final AddToList repairAgainst1 = new AddToList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 3);
        final RemoveFromList repairAgainst2 = new RemoveFromList(SOME_LIST, SOME_CHANGE, 3, 92);
        final ReplaceInList repairAgainst3 = new ReplaceInList(SOME_LIST, SOME_CHANGE, SOME_VALUE, 8);

        final ReplaceInList repaired1 = cut.repairCommand(toRepair, repairAgainst1);
        final ListCommand repaired2 = cut.repairCommand(toRepair, repairAgainst2);
        final ReplaceInList repaired3 = cut.repairLocalCommand(toRepair, repairAgainst3).get();
        final ReplaceInList repaired4 = cut.repairRemoteCommand(toRepair, repairAgainst3);

        assertThat(repaired1.getListId()).isEqualTo(repaired2.getListId()).isEqualTo(repaired3.getListId())
                .isEqualTo(repaired4.getListId()).isEqualTo(SOME_LIST);

        assertThat(repaired1.getListVersionChange()).isEqualTo(repaired2.getListVersionChange())
                .isEqualTo(repaired3.getListVersionChange()).isEqualTo(repaired4.getListVersionChange())
                .isEqualTo(SOME_CHANGE);
    }
}
