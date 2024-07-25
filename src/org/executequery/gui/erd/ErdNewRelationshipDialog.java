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

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Takis Diakoumis
 */
public class ErdNewRelationshipDialog extends ErdPrintableDialog {
    private static final String TITLE = bundleString("title");

    private final ErdViewerPanel parent;

    private JButton createButton;
    private JButton cancelButton;
    private JTextField nameField;

    private JComboBox<?> referencedTableCombo;
    private JComboBox<?> referencedColumnCombo;
    private JComboBox<?> referencingTableCombo;
    private JComboBox<?> referencingColumnCombo;

    public ErdNewRelationshipDialog(ErdViewerPanel parent) {
        super(TITLE);
        this.parent = parent;

        init();
        arrange();
        display();
    }

    private void init() {
        ErdTable[] tables = parent.getAllTablesArray();

        createButton = WidgetFactory.createButton("createButton", Bundles.get("common.create.button"), e -> create());
        cancelButton = WidgetFactory.createButton("cancelButton", Bundles.get("common.cancel.button"), e -> dispose());

        sqlText.setSQLTextEditable(false);
        sqlText.setPreferredSize(new Dimension(420, 150));

        nameField = WidgetFactory.createTextField("nameField");
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                setSQLText();
            }
        });

        referencingTableCombo = WidgetFactory.createComboBox("referencingTableCombo", tables);
        referencingTableCombo.addActionListener(this::tableChanged);

        referencedTableCombo = WidgetFactory.createComboBox("referencedTableCombo", tables);
        referencedTableCombo.addActionListener(this::tableChanged);

        referencingColumnCombo = WidgetFactory.createComboBox("referencingColumnCombo");
        referencingColumnCombo.addActionListener(e -> setSQLText());

        referencedColumnCombo = WidgetFactory.createComboBox("referencedColumnCombo");
        referencedColumnCombo.addActionListener(e -> setSQLText());

        referencingTableChanged();
        referencedTableChanged();
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        buttonPanel.add(new JPanel(), gbh.setMaxWeightX().get());
        buttonPanel.add(createButton, gbh.nextCol().fillNone().setMinWeightX().get());
        buttonPanel.add(cancelButton, gbh.nextCol().leftGap(5).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        mainPanel.add(new JLabel(bundleString("ConstraintName")), gbh.topGap(3).get());
        mainPanel.add(nameField, gbh.nextCol().leftGap(5).topGap(0).get());
        mainPanel.add(new JLabel(bundleString("ReferencingTable")), gbh.nextRowFirstCol().leftGap(0).topGap(8).get());
        mainPanel.add(referencingTableCombo, gbh.nextCol().leftGap(5).topGap(5).get());
        mainPanel.add(new JLabel(bundleString("ReferencingColumn")), gbh.nextRowFirstCol().leftGap(0).topGap(8).get());
        mainPanel.add(referencingColumnCombo, gbh.nextCol().leftGap(5).topGap(5).get());
        mainPanel.add(new JLabel(bundleString("ReferencedTable")), gbh.nextRowFirstCol().leftGap(0).topGap(8).get());
        mainPanel.add(referencedTableCombo, gbh.nextCol().leftGap(5).topGap(5).get());
        mainPanel.add(new JLabel(bundleString("ReferencedColumn")), gbh.nextRowFirstCol().leftGap(0).topGap(8).get());
        mainPanel.add(referencedColumnCombo, gbh.nextCol().leftGap(5).topGap(5).get());
        mainPanel.add(sqlText, gbh.nextRowFirstCol().leftGap(0).setMaxWeightY().fillBoth().spanX().get());
        mainPanel.add(buttonPanel, gbh.nextRowFirstCol().setMinWeightY().get());

        // --- base ---

        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(800, 400));
        setMinimumSize(getPreferredSize());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillBoth().spanX().spanY();
        add(mainPanel, gbh.get());
    }

    private void create() {

        Object selectedReferencingColumn = referencingColumnCombo.getSelectedItem();
        if (!(selectedReferencingColumn instanceof ColumnData)) {
            GUIUtilities.displayWarningMessage(bundleString("isEmptyOrNull", bundleString("referencingColumn")));
            return;
        }

        Object selectedReferencingTable = referencingTableCombo.getSelectedItem();
        if (!(selectedReferencingTable instanceof ErdTable)) {
            GUIUtilities.displayWarningMessage(bundleString("isEmptyOrNull", bundleString("referencingTable")));
            return;
        }

        Object selectedReferencedColumn = referencedColumnCombo.getSelectedItem();
        if (selectedReferencedColumn == null) {
            GUIUtilities.displayWarningMessage(bundleString("isEmptyOrNull", bundleString("referencedColumn")));
            return;
        }

        Object selectedReferencedTable = referencedTableCombo.getSelectedItem();
        if (selectedReferencedTable == null) {
            GUIUtilities.displayWarningMessage(bundleString("isEmptyOrNull", bundleString("referencedTable")));
            return;
        }

        ErdTable referencingTable = (ErdTable) selectedReferencingTable;
        ColumnData referencingColumn = (ColumnData) selectedReferencingColumn;

        ColumnConstraint constraint = new ColumnConstraint();
        constraint.setName(nameField.getText());
        constraint.setRefTable(selectedReferencedTable.toString());
        constraint.setColumn(referencingColumn.getColumnName());
        constraint.setRefColumn(selectedReferencedColumn.toString());
        constraint.setType(NamedObject.FOREIGN_KEY);

        referencingColumn.addConstraint(constraint);
        referencingColumn.setForeignKey(true);

        referencingTable.setAddConstraintsScript(sqlText.getSQLText());

        SwingUtilities.invokeLater(parent::updateTableRelationships);
        dispose();
    }

    private void setSQLText() {

        String query = String.format(
                "ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY(%s) REFERENCES %s (%s);\n",
                referencingTableCombo.getSelectedItem(),
                nameField.getText(),
                referencingColumnCombo.getSelectedItem(),
                referencedTableCombo.getSelectedItem(),
                referencedColumnCombo.getSelectedItem()
        );

        sqlText.setSQLText(query);
    }

    private void tableChanged(ActionEvent e) {

        Object source = e.getSource();
        if (source == referencingTableCombo)
            referencingTableChanged();
        if (source == referencedTableCombo)
            referencedTableChanged();

        setSQLText();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void referencedTableChanged() {
        Object selectedItem = referencedTableCombo.getSelectedItem();
        if (selectedItem instanceof ErdTable) {
            ErdTable table = (ErdTable) selectedItem;
            referencedColumnCombo.setModel(new DefaultComboBoxModel(table.getTableColumns()));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void referencingTableChanged() {
        Object selectedItem = referencingTableCombo.getSelectedItem();
        if (selectedItem instanceof ErdTable) {
            ErdTable table = (ErdTable) selectedItem;
            referencingColumnCombo.setModel(new DefaultComboBoxModel(table.getTableColumns()));
        }
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(ErdNewRelationshipDialog.class, key, args);
    }

}
