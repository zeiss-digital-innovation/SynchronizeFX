package de.saxsys.synchronizefx.sliderdemo.server;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * The domain model that contains the synchronized values.
 * 
 * @author manuel.mauky
 * 
 */
public class Model {

    private final DoubleProperty sliderValue = new SimpleDoubleProperty();

    /**
     * @return the slider value as property.
     */
    public DoubleProperty sliderValueProperty() {
        return sliderValue;
    }

    /**
     * @param value
     *            the new value for the slider.
     */
    public void setSliderValue(final double value) {
        sliderValue.set(value);
    }

    /**
     * @return the current slider value.
     */
    public double getSliderValue() {
        return sliderValue.get();
    }
}
