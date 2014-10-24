/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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

package de.saxsys.synchronizefx.pinboarddemo.client;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.SynchronizeFxBuilder;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.clientserver.ClientCallback;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.pinboarddemo.domain.Story;
import de.saxsys.synchronizefx.pinboarddemo.domain.Task;
import de.saxsys.synchronizefx.pinboarddemo.domain.TaskGuiData;

/**
 * Provides a client that shows tasks of a story on a scurm board
 * 
 * The movement of these tasks is synchronized over the network so that other instances of this class in other JVMs
 * see the movement of the tasks live.
 * 
 * @author Raik Bieniek
 */
public final class Client extends Application implements ClientCallback {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private static final int TASK_WIDTH = 200;
    private static final int TASK_HEIGHT = 50;

    private static final String SERVER = "localhost";

    private Pane root;
    private Map<Task, Pane> tasks = new HashMap<>();
    private SynchronizeFxClient client;

    @Override
    public void start(final Stage stage) {
        stage.setTitle("Example Scrum Board");
        stage.setWidth(600);
        stage.setHeight(480);
        root = new Pane();

        stage.setScene(new Scene(root));
        stage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent arg0) {
                client.disconnect();
                System.exit(0);
            }
        });
        stage.show();

        startSynchronizeFx();
    }

    private void startSynchronizeFx() {
        client = SynchronizeFxBuilder.create().client().address(SERVER).callback(this).build();
        client.connect();
    }

    @Override
    public void modelReady(final Object model) {
        createGui((Story) model);

    }

    @Override
    public void onServerDisconnect() {
        LOG.warn("The server closed the connection.");
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        LOG.error("A SynchronizeFX error occured. Terminating the client now.", error);
        System.exit(-1);
    }

    private void createGui(final Story model) {
        for (Task note : model.getTasks()) {
            addNote(note);
        }
        model.tasksProperty().addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(final javafx.collections.ListChangeListener.Change<? extends Task> event) {
                event.reset();
                while (event.next()) {
                    if (event.wasAdded()) {
                        for (Task note : event.getAddedSubList()) {
                            addNote(note);
                        }
                    } else if (event.wasRemoved()) {
                        for (Task note : event.getRemoved()) {
                            removeTask(note);
                        }
                    }
                }
                event.reset();
            }
        });
    }

    private void addNote(final Task task) {
        final Pane taskPane = new Pane();
        final TaskGuiData position = task.getGuiData();

        taskPane.getChildren().add(new Label(task.getTitle()));
        taskPane.styleProperty().set("-fx-background-color: red;");
        taskPane.setPrefSize(TASK_WIDTH, TASK_HEIGHT);

        taskPane.layoutXProperty().bind(position.xProperty().multiply(root.widthProperty()));
        taskPane.layoutYProperty().bind(position.yProperty().multiply(root.heightProperty()));

        taskPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                position.setX(event.getSceneX() / root.getWidth());
                position.setY(event.getSceneY() / root.getHeight());

                event.consume();
            }
        });

        root.getChildren().add(taskPane);

        tasks.put(task, taskPane);
    }

    private void removeTask(final Task task) {
        root.getChildren().remove(tasks.get(task));
    }

    /**
     * This method starts the client application.
     * 
     * @param args The arguments are ignored.
     */
    public static void main(final String... args) {
        launch();
    }
}
