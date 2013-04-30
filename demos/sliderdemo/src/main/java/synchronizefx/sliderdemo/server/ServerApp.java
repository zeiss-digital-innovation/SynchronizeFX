package synchronizefx.sliderdemo.server;

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
public class ServerApp {

	public static void main(final String... args) {
		System.out.println("starting server");
		final Model model = new Model();

		final SynchronizeFxServer syncFxServer = SynchronizeFxBuilder.create().buildServer(model,
				new ServerCallback() {

					@Override
					public void onError(final SynchronizeFXException exception) {
						System.out.println("Server Error:" + exception.getLocalizedMessage());
					}

				});

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
