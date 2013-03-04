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

package de.saxsys.synchronizefx.example.domain;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * A relative 2 dimensional position.
 *  
 * @author raik.bieniek
 *
 */
public class Position2D {
    private DoubleProperty x = new SimpleDoubleProperty();
    private DoubleProperty y = new SimpleDoubleProperty();

    /**
     * Initializes a relative position that points to the upper left corner.
     * 
     * The coordinates will have the value 0.
     */
    public Position2D() {
    }

    /**
     * Initializes a relative position with given coordinates.
     * 
     * @param x the x coordinate, @see {@link Position2D#getX()}
     * @param y the y coordinate, @see {@link Position2D#getY()}
     */
    public Position2D(final int x, final int y) {
        this.x.set(x);
        this.y.set(y);
    }

    /**
     * @see Position2D#getX()
     * @return the property
     */
    public DoubleProperty xProperty() {
        return x;
    }

    /**
     * @see Position2D#getY()
     * @return the property
     */
    public DoubleProperty yProperty() {
        return y;
    }

    /**
     * 
     * @return the relative x value. This must be a value between 0 (as left as possible) and 1 (as right as possible).
     */
    public double getX() {
        return x.get();
    }

    /**
     * @see Position2D#getX()
     * @param x the new x coordinate
     */
    public void setX(final double x) {
        this.x.set(x);
    }

    /**
     * 
     * @return the relative y value. This must be a value between 0 (as up as possible) and 1 (as down as possible).
     */
    public double getY() {
        return y.get();
    }

    /**
     * @see Position2D#getY()
     * @param y the new y coordinate
     */
    public void setY(final double y) {
        this.y.set(y);
    }
}
