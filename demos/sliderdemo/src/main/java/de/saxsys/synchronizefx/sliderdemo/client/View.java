package de.saxsys.synchronizefx.sliderdemo.client;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;

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

        final Text header = TextBuilder.create().text("SynchronizeFX Example")
                .fill(Color.DIMGRAY).style("-fx-font-size:24").build();

        slider = SliderBuilder.create().min(0).max(100).showTickLabels(true)
                .showTickMarks(true).majorTickUnit(20).minorTickCount(5)
                .snapToTicks(true).build();

        final Label valueLabel = LabelBuilder.create().textFill(Color.DIMGRAY)
                .style("-fx-font-size:15").build();

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
