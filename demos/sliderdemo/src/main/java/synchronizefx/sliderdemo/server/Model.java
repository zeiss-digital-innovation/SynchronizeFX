package synchronizefx.sliderdemo.server;

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

	public DoubleProperty sliderValueProperty() {
		return sliderValue;
	}

	public void setSliderValue(final double value) {
		sliderValue.set(value);
	}

	public double getSliderValue() {
		return sliderValue.get();
	}
}
