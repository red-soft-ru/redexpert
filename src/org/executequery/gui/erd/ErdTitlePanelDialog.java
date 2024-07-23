/*
 * ErdTitlePanelDialog.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.erd;

import org.executequery.GUIUtilities;
import org.executequery.components.TextFieldPanel;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.underworldlabs.swing.AbstractBaseDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

/**
 * @author Takis Diakoumis
 */
public class ErdTitlePanelDialog extends AbstractBaseDialog {

    /**
     * The ERD parent panel
     */
    private final ErdViewerPanel parent;
    /**
     * The name text field
     */
    private JTextField nameTextField;
    /**
     * The date text field
     */
    private JTextField dateTextField;
    /**
     * The revision text field
     */
    private JTextField revTextField;
    /**
     * The name text field
     */
    private JTextField databaseTextField;
    /**
     * The author text field
     */
    private JTextField authorTextField;
    /**
     * The author text field
     */
    private JTextField fileTextField;
    /**
     * The description text area
     */
    private RSyntaxTextArea descTextArea;
    /**
     * Whether this is a new title panel
     */
    private final boolean isNew;

    public ErdTitlePanelDialog(ErdViewerPanel parent) {
        super(GUIUtilities.getParentFrame(), bundleString("title"), true);
        this.parent = parent;
        isNew = true;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        display();
    }

    public ErdTitlePanelDialog(ErdViewerPanel parent, String title, String date,
                               String description, String database, String author,
                               String revision, String fileName) {

        super(GUIUtilities.getParentFrame(), bundleString("title"), true);
        this.parent = parent;
        isNew = false;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        nameTextField.setText(title);
        dateTextField.setText(date);
        revTextField.setText(revision);
        databaseTextField.setText(database);
        authorTextField.setText(author);
        fileTextField.setText(fileName);
        descTextArea.setText(description);

        display();
    }

    private void display() {
        pack();
        Dimension dialogSize = new Dimension(700, 420);
        setSize(dialogSize);
        this.setLocation(GUIUtilities.getLocationForDialog(dialogSize));

        nameTextField.requestFocusInWindow();
        nameTextField.selectAll();

        setVisible(true);
    }

    private void jbInit() throws Exception {
        JButton createButton = new DefaultPanelButton(Bundles.get("common.add.button"));
        createButton.setActionCommand("Add");
        JButton cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));
        cancelButton.setActionCommand("Cancel");

        ActionListener btnListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttons_actionPerformed(e);
            }
        };

        cancelButton.addActionListener(btnListener);
        createButton.addActionListener(btnListener);

        nameTextField = WidgetFactory.createTextField("nameTextField");
        dateTextField = WidgetFactory.createTextField("dateTextField");
        revTextField = WidgetFactory.createTextField("revTextField");
        databaseTextField = WidgetFactory.createTextField("databaseTextField");
        authorTextField = WidgetFactory.createTextField("authorTextField");
        fileTextField = WidgetFactory.createTextField("fileTextField");
        descTextArea = new RSyntaxTextArea();

        descTextArea.setLineWrap(true);
        descTextArea.setWrapStyleWord(true);

        TextFieldPanel panel = new TextFieldPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(bundleString("titleLabel")), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets.left = 0;
        gbc.weightx = 1.0;
        panel.add(nameTextField, gbc);
        gbc.insets.top = 0;
        gbc.gridy = 1;
        panel.add(dateTextField, gbc);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.insets.left = 5;
        gbc.weightx = 0;
        panel.add(new JLabel(bundleString("dateLabel")), gbc);
        gbc.gridy = 2;
        panel.add(new JLabel(bundleString("DescriptionLabel")), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets.left = 0;
        gbc.weighty = 1.0;
        panel.add(new JScrollPane(descTextArea), gbc);
        gbc.weighty = 0;
        gbc.gridy = 3;
        panel.add(databaseTextField, gbc);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.insets.left = 5;
        gbc.weightx = 0;
        panel.add(new JLabel(bundleString("DatabaseLabel")), gbc);
        gbc.gridy = 4;
        panel.add(new JLabel(bundleString("RevisionLabel")), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        panel.add(revTextField, gbc);
        gbc.gridy = 5;
        gbc.gridx = 1;
        panel.add(authorTextField, gbc);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.insets.left = 5;
        gbc.weightx = 0;
        panel.add(new JLabel(bundleString("AuthorLabel")), gbc);
        gbc.gridy = 6;
        panel.add(new JLabel(bundleString("FileNameLabel")), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(fileTextField, gbc);
        gbc.gridy = 7;
        gbc.gridx = 2;
        gbc.weighty = 0;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(createButton, gbc);
        gbc.gridx = 3;
        gbc.insets.left = 0;
        gbc.weightx = 0;
        panel.add(cancelButton, gbc);

        initialiseValues();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container c = getContentPane();
        c.setLayout(new GridBagLayout());

        c.add(panel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                new Insets(7, 7, 7, 7), 0, 0));

    }

    private void initialiseValues() {
        LocalDate time = LocalDate.now();
        dateTextField.setText(time.toString());
        fileTextField.setText(parent.getErdFileName());
    }

    private void create() {

        if (isNew) {
            ErdTitlePanel erdTitlePanel = new ErdTitlePanel(parent, nameTextField.getText(),
                    dateTextField.getText(),
                    descTextArea.getText(),
                    databaseTextField.getText(),
                    authorTextField.getText(),
                    revTextField.getText(),
                    fileTextField.getText());

            parent.addTitlePanel(erdTitlePanel);
        } else {
            ErdTitlePanel erdTitlePanel = parent.getTitlePanel();
            erdTitlePanel.resetValues(nameTextField.getText(),
                    dateTextField.getText(),
                    descTextArea.getText(),
                    databaseTextField.getText(),
                    authorTextField.getText(),
                    revTextField.getText(),
                    fileTextField.getText());

            parent.repaintLayeredPane();
        }

        GUIUtilities.scheduleGC();
        dispose();
    }

    private void buttons_actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Cancel"))
            dispose();

        else if (command.equals("Add"))
            create();

    }

    private static String bundleString(String key) {
        return Bundles.get(ErdTitlePanelDialog.class, key);
    }

}
