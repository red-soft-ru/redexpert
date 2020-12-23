/*
 * ErdToolBarPalette.java
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

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.toolbar.PanelToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ErdToolBarPalette extends PanelToolBar
        implements ActionListener {

    private ErdViewerPanel parent;
    private RolloverButton createTableButton;
    private RolloverButton addTableButton;
    private RolloverButton relationButton;
    private RolloverButton deleteRelationButton;
    private RolloverButton dropTableButton;
    private RolloverButton genScriptsButton;
    private RolloverButton fontStyleButton;
    private RolloverButton lineStyleButton;
    private RolloverButton commitButton;
    private RolloverButton canvasBgButton;
    private RolloverButton canvasFgButton;
    private RolloverButton erdTitleButton;

    /**
     * The zoom in button
     */
    private RolloverButton zoomInButton;
    /**
     * The zoom out button
     */
    private RolloverButton zoomOutButton;
    /**
     * The scale combo box
     */
    private JComboBox scaleCombo;

    public ErdToolBarPalette(ErdViewerPanel parent) {
        super();
        this.parent = parent;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {

        dropTableButton = new RolloverButton("/org/executequery/icons/DropTable16.png",
                bundleString("dropTable"));

//        commitButton = new RolloverButton("/org/executequery/icons/Commit16.png",
//                                         "Commit any schema changes");

        relationButton = new RolloverButton("/org/executequery/icons/TableRelationship16.png",
                bundleString("relation"));

        deleteRelationButton = new RolloverButton(
                "/org/executequery/icons/TableRelationshipDelete16.png",
                bundleString("deleteRelation"));

        genScriptsButton = new RolloverButton("/org/executequery/icons/CreateScripts16.png",
                bundleString("genScripts"));

        fontStyleButton = new RolloverButton("/org/executequery/icons/FontStyle16.png",
                bundleString("fontStyle"));

        lineStyleButton = new RolloverButton("/org/executequery/icons/LineStyle16.png",
                bundleString("lineStyle"));

        createTableButton = new RolloverButton("/org/executequery/icons/NewTable16.png",
                bundleString("createTable"));

        addTableButton = new RolloverButton("/org/executequery/icons/AddTable16.png",
                "Add tables from an existing schema");

        canvasBgButton = new RolloverButton("/org/executequery/icons/ErdBackground16.png",
                bundleString("canvasBg"));

        canvasFgButton = new RolloverButton("/org/executequery/icons/ErdForeground16.png",
                bundleString("canvasFg"));

        erdTitleButton = new RolloverButton("/org/executequery/icons/ErdTitle16.png",
                bundleString("erdTitle"));

        genScriptsButton.addActionListener(this);
        canvasFgButton.addActionListener(this);
        canvasBgButton.addActionListener(this);
        addTableButton.addActionListener(this);
        createTableButton.addActionListener(this);
        dropTableButton.addActionListener(this);
        lineStyleButton.addActionListener(this);
        fontStyleButton.addActionListener(this);
        //commitButton.addActionListener(this);
        relationButton.addActionListener(this);
        deleteRelationButton.addActionListener(this);
        erdTitleButton.addActionListener(this);

        addButton(createTableButton);
        //addButton(addTableButton);
        addButton(relationButton);
        addButton(deleteRelationButton);
        addButton(dropTableButton);
        addButton(genScriptsButton);
        //addButton(commitButton);
        addSeparator();
        addButton(erdTitleButton);
        addButton(fontStyleButton);
        addButton(lineStyleButton);
        addButton(canvasFgButton);
        addButton(canvasBgButton);

        String[] scaleValues = ErdViewerPanel.scaleValues;
        scaleCombo = WidgetFactory.createComboBox(scaleValues);
        scaleCombo.setFont(new Font("dialog", Font.PLAIN, 10));
        scaleCombo.setPreferredSize(new Dimension(58, 20));
        scaleCombo.setLightWeightPopupEnabled(false);
        scaleCombo.setSelectedIndex(3);

        zoomInButton = new RolloverButton("/org/executequery/icons/ZoomIn16.png",
                bundleString("zoomIn"));
        zoomOutButton = new RolloverButton("/org/executequery/icons/ZoomOut16.png",
                bundleString("zoomOut"));

        zoomInButton.addActionListener(this);
        zoomOutButton.addActionListener(this);
        scaleCombo.addActionListener(this);

        addSeparator();
        addButton(zoomOutButton);
        //addComboBox(scaleCombo);
        addButton(zoomInButton);

    }

    private void setBackgroundColours(boolean forCanvas) {
        Color currentColour = null;

        if (forCanvas) {
            currentColour = parent.getCanvasBackground();
        } else {
            currentColour = parent.getTableBackground();
        }

        boolean tablesSelected = false;
        ErdTable[] selectedTables = parent.getSelectedTablesArray();
        if (selectedTables != null) {
            tablesSelected = true;
            if (selectedTables.length == 1) {
                currentColour = selectedTables[0].getTableBackground();
            } else {
                // could be different colours in selected tables 
                // so null out the current colour
                currentColour = null;
            }
        }

        Color newColour = JColorChooser.showDialog(parent,
                "Select Background Colour",
                currentColour);

        if (newColour == null) {
            return;
        }

        if (forCanvas) {
            parent.setCanvasBackground(newColour);
        } else {

            if (tablesSelected) {
                for (int i = 0; i < selectedTables.length; i++) {
                    selectedTables[i].setTableBackground(newColour);
                }
                parent.repaintLayeredPane();
            } else {
                parent.setTableBackground(newColour);
            }
        }
    }

    public void incrementScaleCombo(int num) {

        int index = scaleCombo.getSelectedIndex() + num;

        if (index <= scaleCombo.getComponentCount() - 1) {

            setScaleComboIndex(index);
            parent.setPopupMenuScaleValue(index);
        }

    }

    public void setScaleComboIndex(int index) {

        if (index <= scaleCombo.getComponentCount() - 1) {

            scaleCombo.setSelectedIndex(index);
        }

    }

    public void setScaleComboValue(String value) {
        scaleCombo.setSelectedItem(value);
    }

    public void actionPerformed(ActionEvent e) {
        Object btnObject = e.getSource();

        if (btnObject == lineStyleButton) {
            parent.showLineStyleDialog();
        } else if (btnObject == fontStyleButton) {
            parent.showFontStyleDialog();
        } else if (btnObject == createTableButton) {
            new ErdNewTableDialog(parent);
        } else if (btnObject == genScriptsButton) {
            Vector tables = parent.getAllComponentsVector();
            int v_size = tables.size();

            if (v_size == 0) {
                GUIUtilities.displayErrorMessage("No tables in ERD");
                return;
            }

            Vector _tables = new Vector(v_size);
            for (int i = 0; i < v_size; i++) {
                _tables.add(tables.elementAt(i));
            }
            new ErdScriptGenerator(_tables, parent);
        } else if (btnObject == addTableButton) {
            new ErdSelectionDialog(parent);
        } else if (btnObject == commitButton) {
            String sql = parent.getAllSQLText();

            if (sql != null && sql.length() > 0)
                new ErdExecuteSQL(parent, sql);
            else
                GUIUtilities.displayErrorMessage("No schema changes have been recorded");

        } else if (btnObject == dropTableButton) {
            parent.removeSelectedTables();
        } else if (btnObject == erdTitleButton) {
            ErdTitlePanel titlePanel = parent.getTitlePanel();

            if (titlePanel != null)
                titlePanel.doubleClicked(null);
            else
                new ErdTitlePanelDialog(parent);

        } else if (btnObject == relationButton) {

            if (parent.getAllComponentsVector().size() <= 1) {
                GUIUtilities.displayErrorMessage(
                        "You need at least 2 tables to create a relationship");
                return;
            }

            new ErdNewRelationshipDialog(parent);

        } else if (btnObject == deleteRelationButton) {
            ErdTable[] tables = parent.getSelectedTablesArray();

            if (tables.length < 2) {
                return;
            } else if (tables.length > 2) {
                GUIUtilities.displayErrorMessage(
                        "Please select only 2 related tables");
                return;
            }
            new ErdDeleteRelationshipDialog(parent, tables);
        } else if (btnObject == canvasFgButton) {
            setBackgroundColours(false);
        } else if (btnObject == canvasBgButton) {
            setBackgroundColours(true);
        } else if (btnObject == zoomInButton) {
            parent.zoom(true);
        } else if (btnObject == zoomOutButton) {
            parent.zoom(false);
        } else if (btnObject == scaleCombo) {
            int index = scaleCombo.getSelectedIndex();

            switch (index) {
                case 0:
                    parent.setScaledView(0.25);
                    break;
                case 1:
                    parent.setScaledView(0.5);
                    break;
                case 2:
                    parent.setScaledView(0.75);
                    break;
                case 3:
                    parent.setScaledView(1.0);
                    break;
                case 4:
                    parent.setScaledView(1.25);
                    break;
                case 5:
                    parent.setScaledView(1.5);
                    break;
                case 6:
                    parent.setScaledView(1.75);
                    break;
                case 7:
                    parent.setScaledView(2.0);
                    break;
            }

            parent.setPopupMenuScaleValue(index);

        }

    }

    /**
     * Creates a button with the action specified by the action name
     * and with the specified tool tip text.
     */
    private RolloverButton createButton(String actionId, String toolTipText) {
        RolloverButton button =
                new RolloverButton(ActionBuilder.get(actionId), toolTipText);
        button.setText(Constants.EMPTY);
        return button;
    }

    private String bundleString(String key) {
        return Bundles.get(ErdToolBarPalette.class, key);
    }

}















