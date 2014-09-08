package de.saxsys.synchronizefx.sliderdemo.client;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * The javafx view component.
 * 
 * @author manuel.mauky
 * 
 */
public class View extends VBox {

    private final Slider slider;

    /**
     * Create an instance of the View.
     */
    public View() {
        setSpacing(20);
        setPadding(new Insets(20));

        final Text header = new Text("SynchronizeFX Example");
        header.setFill(Color.DIMGRAY);
        header.setStyle("-fx-font-size:24");

        slider = new Slider();
        slider.setMin(0);
        slider.setMax(100);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(20);
        slider.setMinorTickCount(5);
        slider.setSnapToTicks(true);


        final Label valueLabel = new Label();
        valueLabel.setTextFill(Color.DIMGRAY);
        valueLabel.setStyle("-fx-font-size:15");

        valueLabel.textProperty()
                .bind(Bindings.format("Current Value: %1$.1f",
                        slider.valueProperty()));

        getChildren().addAll(header, slider, valueLabel);
    }

    /**
     * @return the slider value as property.
     */
    public DoubleProperty sliderValue() {
        return slider.valueProperty();
    }

}
