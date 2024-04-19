package org.underworldlabs.swing.celleditor.picker;

import org.executequery.GUIUtilities;

import javax.swing.event.DocumentListener;
import java.awt.*;

public class DatePicker extends com.github.lgooddatepicker.components.DatePicker {

    public DatePicker() {
        super();

        getComponentToggleCalendarButton().setMargin(new Insets(0, 0, 0, 0));
        getComponentToggleCalendarButton().setText("");
        getComponentToggleCalendarButton().setIcon(GUIUtilities.loadIcon("clndr_ico.svg", 16));
        getComponentDateTextField().setMargin(new Insets(0, 0, 0, 0));
        getSettings().setGapBeforeButtonPixels(0);

        repaint();
    }

    public void addDocumentListener(DocumentListener l) {
        getComponentDateTextField().getDocument().addDocumentListener(l);
    }

}
