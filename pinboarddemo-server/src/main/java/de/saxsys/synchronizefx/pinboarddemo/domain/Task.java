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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a single task on the board.
 * 
 * @author Raik Bieniek
 */
public class Task {
	private StringProperty title = new SimpleStringProperty();
	private ObjectProperty<TaskGuiData> guiData = new SimpleObjectProperty<>(
			new TaskGuiData(0, 0));

	/**
	 * The title of the task.
	 * 
	 * @return The title
	 */
	public String getTitle() {
		return title.get();
	}

	/**
	 * @see Task#getTitle()
	 * @param title
	 *            the new title
	 */
	public void setTitle(final String title) {
		this.title.set(title);
	}

	/**
	 * @see Task#getTitle()
	 * @return the property
	 */
	public StringProperty titleProperty() {
		return title;
	}

	/**
	 * The data describing the visual appearance of this task in the GUI.
	 * 
	 * @return The GUI data.
	 */
	public TaskGuiData getGuiData() {
		return guiData.get();
	}

	/**
	 * @see Task#getGuiData()
	 * @param guiData
	 *            the new GUI data
	 */
	public void setPosition(final TaskGuiData guiData) {
		this.guiData.set(guiData);
	}

	/**
	 * @see Task#getGuiData()
	 * @return the property
	 */
	public ObjectProperty<TaskGuiData> guiDataProperty() {
		return guiData;
	}
}
