package org.underworldlabs.swing;

import com.github.lgooddatepicker.components.DatePicker;

import java.awt.*;

public class EQDatePicker extends DatePicker {

    public EQDatePicker() {
        super();
        getComponentToggleCalendarButton().setMargin(new Insets(0, 0, 0, 0));
        getComponentDateTextField().setMargin(new Insets(0, 0, 0, 0));
        repaint();
    }
}