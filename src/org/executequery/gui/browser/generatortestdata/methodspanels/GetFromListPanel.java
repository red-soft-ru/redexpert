package org.executequery.gui.browser.generatortestdata.methodspanels;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.text.SimpleTextArea;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Random;

public class GetFromListPanel extends AbstractMethodPanel {
    private SimpleTextArea textArea;
    private JTextField delimiterField;
    private JComboBox orderBox;
    private ButtonGroup buttonGroup;
    private JRadioButton fromTextAreaButton;
    private JRadioButton fromFileButton;
    String[] list;
    int index;


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
        add(textArea, new GridBagConstraints(0, 2, 2, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    @Override

    public Object getTestDataObject() {
        if (first) {
            fillList();
            first = false;
            index = 0;
        }
        if (index >= list.length)
            index = 0;
        if (orderBox.getSelectedIndex() == 1) {
            index = new Random().nextInt(list.length - 1);
        }
        index++;
        if (col.getFormattedDataType().contentEquals("BIGINT")) {
            return new BigInteger(list[index - 1]);
        }
        if (col.getFormattedDataType().contentEquals("INTEGER")) {
            return Integer.parseInt(list[index - 1]);
        }
        if (col.getFormattedDataType().contentEquals("SMALLINT")) {
            return Short.parseShort(list[index - 1]);
        }
        if (col.getFormattedDataType().contentEquals("TIME")) {
            return Time.valueOf(list[index - 1]);
        }
        if (col.getFormattedDataType().contentEquals("DATE")) {
            return Date.valueOf(list[index - 1]);
        }
        if (col.getFormattedDataType().contentEquals("TIMESTAMP")) {
            return Timestamp.valueOf(list[index - 1]);
        }
        if (col.getFormattedDataType().contentEquals("DOUBLE PRECISION")
                || col.getFormattedDataType().contentEquals("FLOAT")
                || col.getFormattedDataType().startsWith("DECIMAL")
                || col.getFormattedDataType().startsWith("NUMERIC")) {
            return Double.valueOf(list[index - 1]);
        }
        if (col.getFormattedDataType().contains("CHAR")) {
            return list[index - 1];
        }
        return null;
    }

    private void fillList() {
        list = textArea.getTextAreaComponent().getText().split(delimiterField.getText());
    }
}
