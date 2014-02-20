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

package de.saxsys.synchronizefx.testapp;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Represents a dummy, which is used for starting the JavaFX-Thread.
 * 
 * @author ragna-diana.steglich
 * 
 */
public class DummyApplication extends Application {

    private static boolean running;

    @Override
    public void start(final Stage stage) throws Exception {
        running = true;
    }

    /**
     * Returns whether an application is running.
     * 
     * @return <code>true</code>, if the instance is running, otherwise <code>false</code>
     */
    public static boolean isRunning() {
        return running;
    }

}
