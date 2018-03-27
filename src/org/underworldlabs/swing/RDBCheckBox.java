package org.underworldlabs.swing;

import org.executequery.localization.Bundles;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RDBCheckBox extends JPanel {
    JCheckBox valueBox;
    JCheckBox nullBox;

    public RDBCheckBox() {
        init();
    }

    void init() {
        valueBox = new JCheckBox(Bundles.getCommon("value"));
        nullBox = new JCheckBox("NULL");
        nullBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                valueBox.setEnabled(!nullBox.isSelected());
            }
        });
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(valueBox)
                        .addGap(10)
                        .addComponent(nullBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)

        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(nullBox)
                        .addComponent(valueBox)
        );
    }

    public String getStringValue() {
        if (nullBox.isSelected())
            return "";
        return String.valueOf(valueBox.isSelected());
    }

    public void setStingValue(String value) {
        if (MiscUtils.isNull(value)) {
            nullBox.setSelected(true);
        } else valueBox.setSelected(value.toLowerCase().contentEquals("true"));
    }
}
