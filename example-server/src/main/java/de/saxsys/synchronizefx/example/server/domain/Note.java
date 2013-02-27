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

package de.saxsys.synchronizefx.example.server.domain;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a single note on the board.
 * 
 * @author raik.bieniek
 */
public class Note {
    private StringProperty text = new SimpleStringProperty();
    private ObjectProperty<Position2D> position = new SimpleObjectProperty<>(new Position2D(0, 0));

    /**
     * 
     * @return the text that is written on the note.
     */
    public String getText() {
        return text.get();
    }

    /**
     * @see Note#getText()
     * @param text the new text.
     */
    public void setText(final String text) {
        this.text.set(text);
    }
    
    /**
     * @see Note#getText()
     * @return the property
     */
    public StringProperty textProperty() {
        return text;
    }

    /**
     * 
     * @return the relative position of the note on the board.
     */
    public Position2D getPosition() {
        return position.get();
    }

    /**
     * @see Note#getPosition()
     * @param position the new position.
     */
    public void setPosition(final Position2D position) {
        this.position.set(position);
    }

    /**
     * @see Note#getPosition()
     * @return the property
     */
    public ObjectProperty<Position2D> positionProperty() {
        return position;
    }
}
