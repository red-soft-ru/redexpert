package org.underworldlabs.swing;

import org.executequery.components.FileChooserDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FieldFileChooser extends JPanel {
    JTextField textField;
    JButton openButton;
    File file;

    public FieldFileChooser() {
        init();
    }

    void init() {
        textField = new JTextField(15);
        textField.setEditable(false);
        openButton = new JButton("...");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FileChooserDialog fileChooser = new FileChooserDialog();
                int returnVal = fileChooser.showOpenDialog(openButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    textField.setText(file.getAbsolutePath());
                }
            }
        });
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(textField)
                        .addGap(10)
                        .addComponent(openButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)

        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(openButton)
                        .addComponent(textField)
        );
    }

    public File getFile() {
        return file;
    }

    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        textField.setEnabled(flag);
        openButton.setEnabled(flag);
        if (!flag) {
            file = null;
            textField.setText("");
        }
    }
}
