package org.executequery.gui.editor;

import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ValueOrNullParameterField extends JPanel {
    private final JTextField textField;
    private JCheckBox nullBox;

    public ValueOrNullParameterField() {
        this(new JTextField());
    }

    public ValueOrNullParameterField(JTextField textField) {
        this.textField = textField;
        textField.setColumns(15);
        init();
    }

    private void init() {
        nullBox = new JCheckBox("NULL");
        nullBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                textField.setEnabled(!nullBox.isSelected());
            }
        });

        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
        gbh.defaults();
        add(textField, gbh.get());
        add(nullBox, gbh.setLabelDefault().nextCol().get());
    }

    public void setValue(String value) {
        if (value == null)
            nullBox.setSelected(true);
        else textField.setText(value);

    }

    public String getValue() {
        if (nullBox.isSelected())
            return null;
        else return textField.getText();
    }
}
