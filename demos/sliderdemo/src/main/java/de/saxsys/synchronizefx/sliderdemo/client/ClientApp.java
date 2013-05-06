package de.saxsys.synchronizefx.sliderdemo.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import de.saxsys.synchronizefx.SynchronizeFxBuilder;
import de.saxsys.synchronizefx.core.clientserver.ClientCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.sliderdemo.server.Model;

/**
 * Client application's entry point.
 * 
 * @author manuel.mauky
 * 
 */
public class ClientApp extends Application {

    private static final String SERVER = "localhost";

    private Model model;

    private SynchronizeFxClient client;

    /**
     * Main entry point for the client application.
     * 
     * @param args
     *            CLI arguments.
     */
    public static void main(final String... args) {
        Application.launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        stage.setTitle("SynchronizeFX Example Client");
        final View view = new View();
        stage.setScene(new Scene(view, 400, 200));

        client = SynchronizeFxBuilder.create().buildClient(SERVER,
                new ClientCallback() {
                    @Override
                    public void modelReady(final Object object) {
                        model = (Model) object;

                        view.sliderValue().bindBidirectional(
                                model.sliderValueProperty());
                    }

                    @Override
                    public void onError(final SynchronizeFXException exception) {
                        System.out.println("Client Error: "
                                + exception.getLocalizedMessage());
                    }

                    @Override
                    public void onServerDisconnect() {
                        System.out.println("Server disconnected");
                    }
                });

        client.connect();

        stage.show();
    }

    /**
     * Disconnect the client when the application is closed.
     */
    @Override
    public void stop() {
        System.out.print("Stopping the client...");
        client.disconnect();
        System.out.println("done");
    }
}
