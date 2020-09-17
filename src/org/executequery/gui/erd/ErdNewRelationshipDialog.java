/*
 * ErdNewRelationshipDialog.java
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

import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Takis Diakoumis
 */
public class ErdNewRelationshipDialog extends ErdPrintableDialog {

    /**
     * The controller for the ERD viewer
     */
    private ErdViewerPanel parent;
    /**
     * The constraint name text field
     */
    private JTextField nameField;
    /**
     * The referencing table combo
     */
    private JComboBox referencingTableCombo;
    /**
     * The referencing column combo
     */
    private JComboBox referencingColumnCombo;
    /**
     * The referenced table combo
     */
    private JComboBox referencedTableCombo;
    /**
     * The referenced column combo
     */
    private JComboBox referencedColumnCombo;
    /**
     * The SQL text string buffer
     */
    private StringBuffer sqlBuffer;

    /**
     * The literal 'ALTER TABLE '
     */
    private static final String ALTER_TABLE = "ALTER TABLE ";
    /**
     * The literal ' ADD CONSTRAINT '
     */
    private static final String ADD_CONSTRAINT = "\n  ADD CONSTRAINT ";
    /**
     * The literal ' FOREIGN KEY('
     */
    private static final String FOREIGN_KEY = " FOREIGN KEY(";
    /**
     * The literal ') REFERENCES '
     */
    private static final String REFERENCES = ")\n  REFERENCES ";
    /**
     * The literal '('
     */
    private static final char OPEN_B = '(';
    /**
     * The literal ');'
     */
    private static final String CLOSE_END = ");\n";

    private static final int DIALOG_WIDTH = 600;
    private static final int DIALOG_HEIGHT = 400;

    public ErdNewRelationshipDialog(ErdViewerPanel parent) {
        super("New Table Relationship");

        this.parent = parent;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        display();

    }

    private void jbInit() throws Exception {
        JButton createButton = new DefaultPanelButton(Bundles.get("common.create.button"));
        JButton cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));

        ActionListener btnListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttons_actionPerformed(e);
            }
        };

        cancelButton.addActionListener(btnListener);
        createButton.addActionListener(btnListener);

        sqlText.setPreferredSize(new Dimension(420, 120));

        nameField = WidgetFactory.createTextField();
        nameField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                setSQLText();
            }
        });

        ErdTable[] tables = parent.getAllComponentsArray();
        referencingTableCombo = WidgetFactory.createComboBox(tables);
        referencedTableCombo = WidgetFactory.createComboBox(tables);

        referencingColumnCombo = WidgetFactory.createComboBox();
        referencedColumnCombo = WidgetFactory.createComboBox();

        referencingTableCombo.addActionListener(btnListener);
        referencedTableCombo.addActionListener(btnListener);
        referencingColumnCombo.addActionListener(btnListener);
        referencedColumnCombo.addActionListener(btnListener);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gbc = new GridBagConstraints();

        WidgetFactory.addLabelFieldPair(panel, "Constraint Name:", nameField, gbc);
        WidgetFactory.addLabelFieldPair(panel, "Referencing Table:", referencingTableCombo, gbc);
        WidgetFactory.addLabelFieldPair(panel, "Referencing Column:", referencingColumnCombo, gbc);
        WidgetFactory.addLabelFieldPair(panel, "Referenced Table:", referencedTableCombo, gbc);
        WidgetFactory.addLabelFieldPair(panel, "Referenced Column:", referencedColumnCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.left = 5;
        gbc.insets.bottom = 5;
        panel.add(sqlText, gbc);
        gbc.gridy = 6;
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

        Container c = getContentPane();
        c.setLayout(new GridBagLayout());

        c.add(panel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                new Insets(7, 7, 7, 7), 0, 0));

        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                int height = getHeight();

                if (width < DIALOG_WIDTH)
                    width = DIALOG_WIDTH;

                if (height < DIALOG_HEIGHT)
                    height = DIALOG_HEIGHT;

                setSize(width, height);
            }
        });

        sqlBuffer = new StringBuffer();

        ErdTable table = (ErdTable) referencingTableCombo.getSelectedItem();
        referencingColumnCombo.setModel(new DefaultComboBoxModel(
                table.getTableColumns()));
        table = (ErdTable) referencedTableCombo.getSelectedItem();
        referencedColumnCombo.setModel(new DefaultComboBoxModel(
                table.getTableColumns()));

        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    }

    private void setSQLText() {
        sqlBuffer.delete(0, sqlBuffer.length());

        sqlBuffer.append(ALTER_TABLE).
                append(referencingTableCombo.getSelectedItem()).
                append(ADD_CONSTRAINT).
                append(nameField.getText()).
                append(FOREIGN_KEY).
                append(referencingColumnCombo.getSelectedItem()).
                append(REFERENCES).
                append(referencedTableCombo.getSelectedItem()).
                append(OPEN_B).
                append(referencedColumnCombo.getSelectedItem()).
                append(CLOSE_END);

        sqlText.setSQLText(sqlBuffer.toString());

    }

    private void create() {

        ColumnData column = (ColumnData) referencingColumnCombo.getSelectedItem();

        ColumnConstraint constraint = new ColumnConstraint();
        constraint.setName(nameField.getText());
        constraint.setRefTable(referencedTableCombo.getSelectedItem().toString());
        constraint.setColumn(column.getColumnName());
        constraint.setRefColumn(referencedColumnCombo.getSelectedItem().toString());
        constraint.setType(NamedObject.FOREIGN_KEY);

        column.addConstraint(constraint);
        column.setForeignKey(true);

        ErdTable referencingTable = (ErdTable) referencingTableCombo.getSelectedItem();
        referencingTable.setAddConstraintsScript(sqlText.getSQLText());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                parent.updateTableRelationships();
            }
        });

        dispose();

    }

    private void buttons_actionPerformed(ActionEvent e) {
        Object button = e.getSource();

        if (button instanceof JButton) {
            String command = e.getActionCommand();

            if (command.equals("Cancel"))
                dispose();

            else if (command.equals("Create"))
                create();

        } else {

            if (button == referencingTableCombo) {
                ErdTable table = (ErdTable) referencingTableCombo.getSelectedItem();
                referencingColumnCombo.setModel(new DefaultComboBoxModel(
                        table.getTableColumns()));
            } else if (button == referencedTableCombo) {
                ErdTable table = (ErdTable) referencedTableCombo.getSelectedItem();
                referencedColumnCombo.setModel(new DefaultComboBoxModel(
                        table.getTableColumns()));
            }

            setSQLText();

        }

    }

}


















