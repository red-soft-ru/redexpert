package org.executequery.gui.browser.generatortestdata.methodspanels;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.text.SimpleTextArea;

import javax.swing.*;
import java.awt.*;

public class GetFromListPanel extends AbstractMethodPanel {
    private SimpleTextArea textArea;
    private JTextField delimiterField;
    private JComboBox orderBox;
    private ButtonGroup buttonGroup;
    private JRadioButton fromTextAreaButton;
    private JRadioButton fromFileButton;


    public GetFromListPanel(DatabaseColumn col) {
        super(col);
        init();
    }

    private void init() {
        textArea = new SimpleTextArea();
        delimiterField = new JTextField(";");
        orderBox = new JComboBox(new String[]{
                "In order", "Random"
        });

        setLayout(new GridBagLayout());

        JLabel label = new JLabel("method");
        add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(orderBox, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        label = new JLabel("Delimiter");
        add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(delimiterField, new GridBagConstraints(1, 1, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(textArea, new GridBagConstraints(0, 2, 2, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    }

    @Override

    public Object getTestDataObject() {
        return null;
    }
}
