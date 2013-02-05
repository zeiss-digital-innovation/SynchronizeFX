package de.saxsys.synchronizefx.example.server.domain;

import java.util.List;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

/**
 * Represents the board that contains the notes.
 * 
 * @author raik.bieniek
 *
 */
public class Board {
    private ListProperty<Note> notes = new SimpleListProperty<>(FXCollections.<Note> observableArrayList());

    /**
     * 
     * @return the notes that are currently placed on the board.
     */
    public List<Note> getNotes() {
        return notes.get();
    }

    /**
     * 
     * @see Board#getNotes()
     * @return the property
     */
    public ListProperty<Note> notesProperty() {
        return notes;
    }
}
