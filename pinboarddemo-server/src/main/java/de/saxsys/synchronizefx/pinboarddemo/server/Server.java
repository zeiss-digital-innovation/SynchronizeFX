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

package de.saxsys.synchronizefx.pinboarddemo.server;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.SynchronizeFxBuilder;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.pinboarddemo.domain.Story;
import de.saxsys.synchronizefx.pinboarddemo.domain.Task;

/**
 * A server that serves a scrum story with its tasks and their relative
 * positions on a scrum board to multiple clients.
 * 
 * @author Raik Bieniek
 */
public final class Server implements ServerCallback {

	private static final Logger LOG = LoggerFactory.getLogger(Server.class);

	private Story story;
	private Random random = new Random();
	private SynchronizeFxServer server;

	private Server() {
		createBoard();

		startSynchronizeFx();

		shutdownServerOnExit();

		userInputLoop();
	}

	private void startSynchronizeFx() {
		server = SynchronizeFxBuilder.create().server().model(this.story)
				.callback(this).build();
		server.start();
	}

	@Override
	public void onError(final SynchronizeFXException error) {
		LOG.error("A SynchronizeFX error occured. Terminating the server now.",
				error);
		System.exit(-1);
	}

	private void shutdownServerOnExit() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				server.shutdown();
			}
		});
	}

	private void createBoard() {
		this.story = new Story();

		addRandomTask();
	}

	private void userInputLoop() {
		String add = "'a' to add a note";
		String remove = "'r' to remove a note";

		System.out.println("press " + add + " or " + remove);

		while (true) {
			char key;
			try {
				key = (char) System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			if (key == '\n' || key == '\r') {
				continue;
			}

			if (key == 'a') {
				addRandomTask();
			} else if (story.getTasks().size() != 0 && key == 'r') {
				removeRandomTask();

			} else {
				continue;
			}
			System.out.println("press " + add
					+ (story.getTasks().size() > 0 ? " or " + remove : ""));
		}
	}

	private void removeRandomTask() {
		List<Task> tasks = this.story.getTasks();
		int size = tasks.size();
		if (size <= 0) {
			return;
		}
		tasks.remove(random.nextInt(size));
	}

	private void addRandomTask() {
		Task task = new Task();
		task.getGuiData().setX(random.nextDouble());
		task.getGuiData().setY(random.nextDouble());
		task.setTitle("Sample Task " + random.nextInt(1000));

		this.story.getTasks().add(task);
	}

	/**
	 * Starts the server.
	 * 
	 * @param args
	 *            Arguments are ignored
	 */
	public static void main(final String... args) {
		new Server();
	}
}
