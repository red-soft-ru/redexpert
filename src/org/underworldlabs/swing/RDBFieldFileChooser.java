package org.underworldlabs.swing;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class RDBFieldFileChooser extends JPanel {
    FieldFileChooser valueBox;
    JCheckBox nullBox;

    public RDBFieldFileChooser() {
        init();
    }

    void init() {
        valueBox = new FieldFileChooser();
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

    public File getFile() {
        if (nullBox.isSelected())
            return null;
        return valueBox.getFile();
    }
}
