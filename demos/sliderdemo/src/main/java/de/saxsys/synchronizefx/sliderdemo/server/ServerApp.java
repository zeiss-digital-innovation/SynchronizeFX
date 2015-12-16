package de.saxsys.synchronizefx.sliderdemo.server;

import java.util.Scanner;

import de.saxsys.synchronizefx.SynchronizeFxBuilder;
import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

/**
 * The Server application.
 * 
 * @author manuel.mauky
 * 
 */
public final class ServerApp {

    private ServerApp() {
    }

    /**
     * The main entry point to the server application.
     * 
     * @param args the CLI arguments.
     */
    public static void main(final String... args) {
        System.out.println("starting server");
        final Model model = new Model();

        final SynchronizeFxServer syncFxServer =
                SynchronizeFxBuilder.create().server().model(model).callback(new ServerCallback() {

                    @Override
                    public void onError(final SynchronizeFXException exception) {
                        System.err.println("Server Error:" + exception.getLocalizedMessage());
                    }

                    @Override
                    public void onClientConnectionError(final Object client, final SynchronizeFXException exception) {
                        System.err.println("An exception in the communication to a client occurred." + exception);
                    }

                }).build();

        syncFxServer.start();

        final Scanner console = new Scanner(System.in);

        boolean exit = false;

        while (!exit) {
            System.out.println("press 'q' for quit");

            final String input = console.next();

            if ("q".equals(input)) {
                exit = true;
            }
        }

        console.close();

        syncFxServer.shutdown();
    }
}
