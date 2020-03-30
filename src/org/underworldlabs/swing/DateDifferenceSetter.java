package org.underworldlabs.swing;

import javax.swing.*;
import java.awt.*;

public class DateDifferenceSetter extends JPanel {
    NumberTextField yearsField;
    NumberTextField mouthsField;
    NumberTextField daysField;

    public DateDifferenceSetter() {
        init();
    }

    private void init() {
        yearsField = new NumberTextField();
        yearsField.setValue(0);

        mouthsField = new NumberTextField();
        mouthsField.setValue(0);

        daysField = new NumberTextField();
        daysField.setValue(0);

        setLayout(new GridBagLayout());
        JLabel label = new JLabel("Years");
        add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(yearsField, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

        label = new JLabel("Mouths");
        add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(mouthsField, new GridBagConstraints(1, 1, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

        label = new JLabel("Days");
        add(label, new GridBagConstraints(0, 2, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(daysField, new GridBagConstraints(1, 2, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    }

    public int getYears() {
        return yearsField.getValue();
    }

    public int getMouths() {
        return mouthsField.getValue();
    }

    public int getDays() {
        return daysField.getValue();
    }

}
