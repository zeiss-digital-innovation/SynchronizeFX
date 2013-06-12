package de.saxsys.synchronizefx.core.metamodel;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.testutils.ComplexDomainModel;
import de.saxsys.synchronizefx.core.testutils.ComplexDomainModel.Sprint;
import de.saxsys.synchronizefx.core.testutils.ComplexDomainModel.Story;
import de.saxsys.synchronizefx.core.testutils.ComplexDomainModel.Task;

/**
 * This tests whether objects that are moved between lists are handled correctly by the meta model. It should test
 * that listeners for creating meta model commands are added only once per object, even when they are moved between
 * lists.
 * 
 * @author michael.thiele
 * 
 */
public class MultipleListenersAreAddedForMovedObjects {

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
        this.originalMeta = new MetaModel(this.originalCb, originalDomainModel);

        this.copyCb = new SaveParameterCallback();
        this.copyMeta = new MetaModel(copyCb);
        copyMeta.execute(commandsForDomainModel(originalMeta));
        this.copyDomainModel = (ComplexDomainModel) copyCb.root;
    }

    /**
     * @see ETEO-934: comment #4.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void moveTaskToNewlyCreatedStory() {
        final Task taskA = new Task();
        taskA.setName("taskA");

        final Story storyA = new Story();
        storyA.getTasks().add(taskA);

        final Sprint sprintA = new Sprint();
        sprintA.getStories().add(storyA);

        final Sprint sprintB = new Sprint();

        originalDomainModel.getSprints().add(sprintA);
        originalDomainModel.getSprints().add(sprintB);
        assertEquals(0, copyDomainModel.getSprints().size());

        copyMeta.execute(originalCb.commands);
        originalCb.commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

        final Story storyB = new Story();
        sprintA.getStories().add(storyB);

        copyMeta.execute(originalCb.commands);
        originalCb.commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

        sprintB.getStories().add(sprintA.getStories().remove(1));
        copyMeta.execute(originalCb.commands);
        originalCb.commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

        storyB.getTasks().add(storyA.getTasks().remove(0));
        // remove, add, cleanReferences
        assertEquals(3, originalCb.commands.size());
        copyMeta.execute(originalCb.commands);
        originalCb.commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

    }

    @SuppressWarnings("deprecation")
    @Test
    public void moveExistingStoryToNewSprintAndCreateNewTask() {
        final Sprint sprintA = new Sprint();
        final Story storyA = new Story();
        sprintA.getStories().add(storyA);

        originalDomainModel.getSprints().add(sprintA);
        assertEquals(0, copyDomainModel.getSprints().size());

        copyMeta.execute(originalCb.commands);
        originalCb.commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

        final Sprint sprintB = new Sprint();
        originalDomainModel.getSprints().add(sprintB);

        copyMeta.execute(originalCb.commands);
        originalCb.commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());

        final Story copyStoryA = copyDomainModel.getSprints().get(0).getStories().get(0);
        sprintB.getStories().add(sprintA.getStories().remove(0));
        assertEquals(3, originalCb.commands.size());
        copyMeta.execute(originalCb.commands);
        originalCb.commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());
        assertTrue(copyStoryA == copyDomainModel.getSprints().get(1).getStories().get(0));

        final Task taskA = new Task();
        taskA.setName("taskA");
        storyA.getTasks().add(taskA);
        // create, set, addToList, clearReferences
        assertEquals(4, originalCb.commands.size());
        copyMeta.execute(originalCb.commands);
        originalCb.commands.clear();
        assertEquals(originalDomainModel.getSprints(), copyDomainModel.getSprints());
    }

    private List<Object> commandsForDomainModel(final MetaModel model) {
        final CommandsStore store = new CommandsStore();
        model.commandsForDomainModel(new CommandsForDomainModelCallback() {

            @Override
            public void commandsReady(final List<Object> initialCommands) {
                store.commands = initialCommands;
            }
        });
        return store.commands;
    }

    private static class SaveParameterCallback implements TopologyLayerCallback {
        private Object root;
        private final List<Object> commands = new LinkedList<>();

        @Override
        public void sendCommands(final List<Object> commands) {
            this.commands.addAll(commands);
        }

        @Override
        public void onError(final SynchronizeFXException error) {
            fail("exception occured: " + error.getMessage());
        }

        @Override
        public void domainModelChanged(final Object root) {
            this.root = root;
        }

    }

    private static final class CommandsStore {
        protected List<Object> commands;
    }
}
