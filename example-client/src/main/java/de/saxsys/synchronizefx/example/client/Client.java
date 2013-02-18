package de.saxsys.synchronizefx.example.client;

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
import de.saxsys.synchronizefx.core.SynchronizeFXException;
import de.saxsys.synchronizefx.core.clientserver.DomainModelClient;
import de.saxsys.synchronizefx.core.clientserver.UserCallbackClient;
import de.saxsys.synchronizefx.example.server.domain.Board;
import de.saxsys.synchronizefx.example.server.domain.Note;
import de.saxsys.synchronizefx.example.server.domain.Position2D;
import de.saxsys.synchronizefx.netty.KryoNetClient;

/**
 * Provides a client that shows notes on a board
 * 
 * The movement of these notes is synchronized over the network so that other instances of this class in other JVMs see
 * the movement of the notes live.
 * 
 * @author raik.bieniek
 * 
 */
public final class Client extends Application implements UserCallbackClient {

    private static final int NOTE_WIDTH = 200;
    private static final int NOTE_HEIGHT = 50;
    private Pane root;
    private Map<Note, Pane> notes = new HashMap<>();
    private DomainModelClient client;

    @Override
    public void start(final Stage stage) {
        stage.setTitle("Example Client");

        root = new Pane();

        KryoNetClient kryoClient = new KryoNetClient("localhost", 5000);
        try {
            client = new DomainModelClient(kryoClient, this);
        } catch (SynchronizeFXException error) {
            onError(error);
        }

        stage.setScene(new Scene(root));
        stage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent arg0) {
                client.disconnect();
                System.exit(0);
            }
        });
        stage.show();
    }

    /**
     * This method starts the client application.
     * 
     * @param args The arguments are ignored.
     */
    public static void main(final String... args) {
        launch();
    }

    @Override
    public void modelReady(final Object model) {
        createGui((Board) model);

    }

    private void createGui(final Board model) {
        for (Note note : model.getNotes()) {
            addNote(note);
        }
        model.notesProperty().addListener(new ListChangeListener<Note>() {
            @Override
            public void onChanged(final javafx.collections.ListChangeListener.Change<? extends Note> event) {
                event.reset();
                while (event.next()) {
                    if (event.wasAdded()) {
                        for (Note note : event.getAddedSubList()) {
                            addNote(note);
                        }
                    } else if (event.wasRemoved()) {
                        for (Note note : event.getRemoved()) {
                            removeNote(note);
                        }
                    }
                }
                event.reset();
            }
        });
    }

    private void addNote(final Note note) {
        final Pane newNote = new Pane();
        final Position2D position = note.getPosition();

        newNote.getChildren().add(new Label(note.getText()));
        newNote.styleProperty().set("-fx-background-color: red;");
        newNote.setPrefSize(NOTE_WIDTH, NOTE_HEIGHT);

        newNote.layoutXProperty().bind(position.xProperty().multiply(root.widthProperty()));
        newNote.layoutYProperty().bind(position.yProperty().multiply(root.heightProperty()));

        newNote.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                position.setX(event.getSceneX() / root.getWidth());
                position.setY(event.getSceneY() / root.getHeight());

                event.consume();
            }
        });

        root.getChildren().add(newNote);

        notes.put(note, newNote);
    }

    private void removeNote(final Note note) {
        root.getChildren().remove(notes.get(note));
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        error.printStackTrace();
        System.exit(-1);
    }
}
