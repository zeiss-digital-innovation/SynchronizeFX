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

import java.util.ArrayList;
import java.util.List;

import de.saxsys.synchronizefx.core.testutils.ComplexDomainModel;
import de.saxsys.synchronizefx.core.testutils.ComplexDomainModel.Sprint;
import de.saxsys.synchronizefx.core.testutils.ComplexDomainModel.Story;
import de.saxsys.synchronizefx.core.testutils.ComplexDomainModel.Task;
import de.saxsys.synchronizefx.core.testutils.DirectExecutor;
import de.saxsys.synchronizefx.core.testutils.EasyCommandsForDomainModel;
import de.saxsys.synchronizefx.core.testutils.SaveParameterCallback;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This tests whether objects that are moved between lists are handled correctly by the meta model. It should test
 * that listeners for creating meta model commands are added only once per object, even when they are moved between
 * lists.
 * 
 * @author michael.thiele
 * 
 */
public class MultipleListenersAreAddedForMovedObjectsTest {

    private ComplexDomainModel originalDomainModel;
    private SaveParameterCallback originalCb;
    private MetaModel originalMeta;

    private ComplexDomainModel copyDomainModel;
    private SaveParameterCallback copyCb;
    private MetaModel copyMeta;

    /**
     * Test setup.
     */
    @Before
    public void setUp() {
        this.originalDomainModel = new ComplexDomainModel();
        this.originalCb = new SaveParameterCallback();
        this.originalMeta = new MetaModel(this.originalCb, originalDomainModel, new DirectExecutor());

        this.copyCb = new SaveParameterCallback();
        this.copyMeta = new MetaModel(copyCb, new DirectExecutor());
        copyMeta.execute(EasyCommandsForDomainModel.commandsForDomainModel(originalMeta));
        this.copyDomainModel = (ComplexDomainModel) copyCb.getRoot();
    }

    /**
     * @see ETEO-934: comment #4.
     */
    @Test
    public void moveTaskToNewlyCreatedStory() {
        final Task taskA = new Task();
        taskA.setName("taskA");

        final Story storyA = new Story();
        storyA.getTasks().add(taskA);

        final Sprint sprintA = new Sprint();
        sprintA.getStories().add(storyA);

        final Sprint sprintB = new Sprint();
        
        final List<Object> commands = new ArrayList<>();
        originalDomainModel.getSprints().add(sprintA);
        commands.addAll(originalCb.getCommands());
        originalDomainModel.getSprints().add(sprintB);
        commands.addAll(originalCb.getCommands());
        assertEquals(0, copyDomainModel.getSprints().size());

        copyMeta.execute(commands);
        commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

        final Story storyB = new Story();
        sprintA.getStories().add(storyB);

        copyMeta.execute(originalCb.getCommands());
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

        final Story removedStory = sprintA.getStories().remove(1);
        commands.addAll(originalCb.getCommands());
        sprintB.getStories().add(removedStory);
        commands.addAll(originalCb.getCommands());
        copyMeta.execute(commands);
        commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

        final Task removedTasks = storyA.getTasks().remove(0);
        commands.addAll(originalCb.getCommands());
        storyB.getTasks().add(removedTasks);
        commands.addAll(originalCb.getCommands());
        // remove, add, cleanReferences
        assertEquals(3, commands.size());
        copyMeta.execute(commands);
        commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

    }

    @Test
    public void moveExistingStoryToNewSprintAndCreateNewTask() {
        final Sprint sprintA = new Sprint();
        final Story storyA = new Story();
        sprintA.getStories().add(storyA);

        originalDomainModel.getSprints().add(sprintA);
        assertEquals(0, copyDomainModel.getSprints().size());

        copyMeta.execute(originalCb.getCommands());
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

        final Sprint sprintB = new Sprint();
        originalDomainModel.getSprints().add(sprintB);

        copyMeta.execute(originalCb.getCommands());
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());


        final List<Object> commands = new ArrayList<>();
        final Story copyStoryA = copyDomainModel.getSprints().get(0).getStories().get(0);
        final Story removedStory = sprintA.getStories().remove(0);
        commands.addAll(originalCb.getCommands());
        sprintB.getStories().add(removedStory);
        commands.addAll(originalCb.getCommands());
        assertEquals(3, commands.size());
        copyMeta.execute(commands);
        commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());
        assertTrue(copyStoryA == copyDomainModel.getSprints().get(1).getStories().get(0));

        final Task taskA = new Task();
        taskA.setName("taskA");
        storyA.getTasks().add(taskA);
        // create, set, addToList, clearReferences
        assertEquals(4, originalCb.getCommands().size());
        copyMeta.execute(originalCb.getCommands());
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());
    }
}
