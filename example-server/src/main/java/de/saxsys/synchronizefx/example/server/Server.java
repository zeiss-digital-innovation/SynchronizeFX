/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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

package de.saxsys.synchronizefx.example.server;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.SynchronizeFxBuilder;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.core.clientserver.UserCallbackServer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.example.domain.Board;
import de.saxsys.synchronizefx.example.domain.Note;

/**
 * A server that serves notes and their relative positions on a board to multiple clients.
 * 
 * @author raik.bieniek
 * 
 */
public final class Server implements UserCallbackServer {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private Board board;
    private Random random = new Random();
    private SynchronizeFxServer server;

    private Server() {
        createBoard();

        startSynchronizeFx();

        shutdownServerOnExit();

        userInputLoop();
    }

    private void startSynchronizeFx() {
        server = new SynchronizeFxBuilder().createServer(this.board, this);
        server.start();
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        LOG.error("A SynchronizeFX error occured. Terminating the server now.", error);
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
        this.board = new Board();

        addRandomNote();
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
                addRandomNote();
            } else if (board.getNotes().size() != 0 && key == 'r') {
                removeRandomNote();

            } else {
                continue;
            }
            System.out.println("press " + add + (board.getNotes().size() > 0 ? " or " + remove : ""));
        }
    }

    private void removeRandomNote() {
        List<Note> notes = this.board.getNotes();
        int size = notes.size();
        if (size <= 0) {
            return;
        }
        notes.remove(random.nextInt(size));
    }

    private void addRandomNote() {
        Note note = new Note();
        note.getPosition().setX(random.nextDouble());
        note.getPosition().setY(random.nextDouble());
        note.setText("Sample Note " + random.nextInt(1000));

        this.board.getNotes().add(note);
    }

    /**
     * Starts the server.
     * 
     * @param args Arguments are ignored
     */
    public static void main(final String... args) {
        new Server();
    }
}
