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

package de.saxsys.synchronizefx.pinboarddemo.domain;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Contains the data of a {@link Task} that describes its apperance in the GUI.
 * 
 * @author Raik Bieniek
 */
public class TaskGuiData {
	private DoubleProperty x = new SimpleDoubleProperty();
	private DoubleProperty y = new SimpleDoubleProperty();

	/**
	 * Initializes this GUI data with a position that points to the upper left
	 * corner.
	 * 
	 * The coordinates will have the value 0.
	 */
	public TaskGuiData() {
	}

	/**
	 * Initializes a relative position with given coordinates.
	 * 
	 * @param x
	 *            the position on the x-Axis, @see {@link TaskGuiData#getX()}
	 * @param y
	 *            the position on the x-Axis, @see {@link TaskGuiData#getY()}
	 */
	public TaskGuiData(final int x, final int y) {
		this.x.set(x);
		this.y.set(y);
	}

	/**
	 * @see TaskGuiData#getX()
	 * @return the property
	 */
	public DoubleProperty xProperty() {
		return x;
	}

	/**
	 * @see TaskGuiData#getY()
	 * @return the property
	 */
	public DoubleProperty yProperty() {
		return y;
	}

	/**
	 * The relative position of the {@link Task} on the x-axis.
	 * 
	 * <p>
	 * This is a value between 0 (as left as possible) and 1 (as right as
	 * possible).
	 * </p>
	 * 
	 * @return the relative x position
	 */
	public double getX() {
		return x.get();
	}

	/**
	 * @see TaskGuiData#getX()
	 * @param x
	 *            the new x coordinate
	 */
	public void setX(final double x) {
		this.x.set(x);
	}

	/**
	 * The relative position of the {@link Task} on the y-axis.
	 * 
	 * <p>
	 * This must be a value between 0 (as up as possible) and 1 (as down as
	 * possible).
	 * </p>
	 * 
	 * @return the relative y position
	 */
	public double getY() {
		return y.get();
	}

	/**
	 * @see TaskGuiData#getY()
	 * @param y
	 *            the new y coordinate
	 */
	public void setY(final double y) {
		this.y.set(y);
	}
}
