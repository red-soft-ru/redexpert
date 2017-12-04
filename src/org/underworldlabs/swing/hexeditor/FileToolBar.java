package org.underworldlabs.swing.hexeditor;

import javax.swing.*;

public class FileToolBar extends JToolBar {

    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;

    public FileToolBar() {
        super("File Tool Bar");

        newButton = new JButton("New");
        openButton = new JButton("Open");
        saveButton = new JButton("Save");

        add(newButton);
        add(openButton);
        add(saveButton);

        setFloatable(true);
        setRollover(true);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), getBorder()));
    }
}

