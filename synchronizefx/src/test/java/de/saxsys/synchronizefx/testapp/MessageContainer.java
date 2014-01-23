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

package de.saxsys.synchronizefx.testapp;

import java.util.List;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

/**
 * Represents the container, which contains several messages.
 * 
 * @author ragna-diana.steglich
 * 
 */
public class MessageContainer {

    private ListProperty<Message> messages = new SimpleListProperty<>(FXCollections.<Message> observableArrayList());

    /**
     * @return the messages, which are managed by the server
     */
    public List<Message> getMessages() {
        return messages.get();
    }

    /**
     * @see MessageContainer##getMessages()
     * @return the property
     */
    public ListProperty<Message> messageProperty() {
        return messages;
    }

}
