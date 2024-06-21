package org.underworldlabs.swing.celleditor.picker;

import com.github.lgooddatepicker.zinternaltools.InternalUtilities;
import org.executequery.GUIUtilities;

import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;

public class DatePicker extends com.github.lgooddatepicker.components.DatePicker {

    public DatePicker() {
        super();

        getComponentToggleCalendarButton().setMargin(new Insets(0, 0, 0, 0));
        getComponentToggleCalendarButton().setText("");
        getComponentToggleCalendarButton().setIcon(GUIUtilities.loadIcon("icon_calendar"));
        getComponentDateTextField().setMargin(new Insets(0, 0, 0, 0));
        getSettings().setGapBeforeButtonPixels(0);

        repaint();
    }

    public LocalDate getRealDate() {
        return InternalUtilities.getParsedDateOrNull(
                getText(),
                getSettings().getFormatForDatesCommonEra(),
                getSettings().getFormatForDatesBeforeCommonEra(),
                getSettings().getFormatsForParsing()
        );
    }

    public void addDocumentListener(DocumentListener l) {
        getComponentDateTextField().getDocument().addDocumentListener(l);
    }

}
