package de.saxsys.synchronizefx.core.testutils;

import java.util.List;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

/**
 * Simple data model that contains sprints which contain stories which contain tasks.
 * 
 * @author michael.thiele
 * 
 */
public class ComplexDomainModel {

    private final ListProperty<Sprint> sprints = new SimpleListProperty<Sprint>(
            FXCollections.<Sprint> observableArrayList());

    /**
     * @return all contained sprints
     */
    public List<Sprint> getSprints() {
        return sprints.get();
    }

    /**
     * A simple sprint representation.
     * 
     * @author michael.thiele
     * 
     */
    public static class Sprint {
        private final ListProperty<Story> stories = new SimpleListProperty<Story>(
                FXCollections.<Story> observableArrayList());

        /**
         * @return all contained stories
         */
        public List<Story> getStories() {
            return stories.get();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((stories == null) ? 0 : stories.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Sprint other = (Sprint) obj;
            if (stories == null) {
                if (other.stories != null)
                    return false;
            } else if (!stories.get().equals(other.stories.get()))
                return false;
            return true;
        }

    }

    /**
     * A simple representation of a story.
     * 
     * @author michael.thiele
     * 
     */
    public static class Story {
        private final ListProperty<Task> tasks = new SimpleListProperty<Task>(
                FXCollections.<Task> observableArrayList());

        /**
         * @return all contained tasks.
         */
        public List<Task> getTasks() {
            return tasks.get();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Story other = (Story) obj;
            if (tasks == null) {
                if (other.tasks != null)
                    return false;
            } else if (!tasks.get().equals(other.tasks.get()))
                return false;
            return true;
        }
    }

    /**
     * A simple representation of a task with a name.
     * 
     * @author michael.thiele
     * 
     */
    public static class Task {

        private final StringProperty name = new SimpleStringProperty();

        /**
         * Sets the name for this task.
         * 
         * @param name the name to set
         */
        public void setName(final String name) {
            this.name.set(name);
        }

        /**
         * @return the name of this task
         */
        public String getName() {
            return name.get();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Task other = (Task) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.get().equals(other.name.get()))
                return false;
            return true;
        }
    }

}
