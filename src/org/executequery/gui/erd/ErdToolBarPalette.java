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

import org.executequery.GUIUtilities;
import org.executequery.actions.toolscommands.ComparerDBCommands;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.GenerateErdPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.components.OpenConnectionsComboboxPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.RolloverButton;
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

    private final ErdViewerPanel parent;
    private RolloverButton createTableButton;
    private RolloverButton relationButton;
    private RolloverButton deleteRelationButton;
    private RolloverButton dropTableButton;
    private RolloverButton genScriptsButton;
    private RolloverButton fontStyleButton;
    private RolloverButton lineStyleButton;
    private RolloverButton canvasBgButton;
    private RolloverButton canvasFgButton;
    private RolloverButton erdTitleButton;
    private RolloverButton erdTextBlockButton;

    private RolloverButton updateFromDatabase;
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

    private OpenConnectionsComboboxPanel connectionsComboBox;

    public ErdToolBarPalette(ErdViewerPanel parent) {
        super();
        this.parent = parent;
        jbInit();
    }

    private void jbInit() {

        dropTableButton = new RolloverButton("/org/executequery/icons/DropTable16.png",
                bundleString("dropTable"));

        connectionsComboBox = new OpenConnectionsComboboxPanel();

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

        canvasBgButton = new RolloverButton("/org/executequery/icons/ErdBackground16.png",
                bundleString("canvasBg"));

        canvasFgButton = new RolloverButton("/org/executequery/icons/ErdForeground16.png",
                bundleString("canvasFg"));

        erdTitleButton = new RolloverButton("/org/executequery/icons/ErdTitle16.png",
                bundleString("erdTitle"));

        erdTextBlockButton = new RolloverButton("/org/executequery/icons/AddComment16.png",
                bundleString("erdText"));

        updateFromDatabase = new RolloverButton("/org/executequery/icons/RecycleConnection16.png",
                bundleString("updateFromDatabase"));
        genScriptsButton.addActionListener(this);
        canvasFgButton.addActionListener(this);
        canvasBgButton.addActionListener(this);
        createTableButton.addActionListener(this);
        dropTableButton.addActionListener(this);
        lineStyleButton.addActionListener(this);
        fontStyleButton.addActionListener(this);
        relationButton.addActionListener(this);
        deleteRelationButton.addActionListener(this);
        erdTitleButton.addActionListener(this);
        erdTextBlockButton.addActionListener(this);
        updateFromDatabase.addActionListener(this);
        addButton(createTableButton);
        addButton(relationButton);
        addButton(deleteRelationButton);
        addButton(dropTableButton);
        addButton(genScriptsButton);
        addSeparator();
        addButton(erdTitleButton);
        addButton(erdTextBlockButton);
        addButton(fontStyleButton);
        addButton(lineStyleButton);
        addButton(canvasFgButton);
        addButton(canvasBgButton);

        String[] scaleValues = ErdViewerPanel.scaleValues;
        scaleCombo = WidgetFactory.createComboBox("scaleCombo", scaleValues);
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
        addButton(zoomInButton);
        addSeparator();
        addButton(updateFromDatabase);
    }

    private void setBackgroundColours(boolean forCanvas) {
        Color currentColour = null;

        if (forCanvas) {
            currentColour = parent.getCanvasBackground();
        } else {
            currentColour = parent.getTableBackground();
        }

        boolean tablesSelected = false;
        ErdMoveableComponent[] selectedTables = parent.getSelectedComponentsArray();
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
                Bundles.get("LocaleManager.ColorChooser.title"),
                currentColour);

        if (newColour == null) {
            return;
        }

        if (forCanvas) {
            parent.setCanvasBackground(newColour);
        } else {

            if (tablesSelected) {
                parent.fireChangedBgColor();
                for (ErdMoveableComponent selectedTable : selectedTables) {
                    selectedTable.setTableBackground(newColour);
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
            Vector tables = parent.getAllTablesVector();
            int v_size = tables.size();

            if (v_size == 0) {
                GUIUtilities.displayErrorMessage(bundleString("NoTablesError"));
                return;
            }

            Vector _tables = new Vector(v_size);
            for (int i = 0; i < v_size; i++) {
                _tables.add(tables.elementAt(i));
            }
            new ComparerDBCommands().erdScript(tables, null);

        } else if (btnObject == dropTableButton) {
            parent.removeSelectedTables();
        } else if (btnObject == erdTitleButton) {
            ErdTitlePanel titlePanel = parent.getTitlePanel();

            if (titlePanel != null)
                titlePanel.doubleClicked(null);
            else
                new ErdTitlePanelDialog(parent);

        } else if (btnObject == erdTextBlockButton) {

            new ErdTextBlockDialog(parent);

        } else if (btnObject == relationButton) {

            if (parent.getAllTablesVector().size() <= 1) {
                GUIUtilities.displayErrorMessage(Bundles.get("ErdPopupMenu.needMoreTablesError"));
                return;
            }

            new ErdNewRelationshipDialog(parent);

        } else if (btnObject == deleteRelationButton) {
            ErdTable[] tables = parent.getSelectedTablesArray();

            if (tables.length < 2) {
                return;
            } else if (tables.length > 2) {
                GUIUtilities.displayErrorMessage(bundleString("SelectOnlyTwoTablesError"));
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

        } else if (btnObject == updateFromDatabase) {
            updateFromDatabase();

        }

    }

    private void updateFromDatabase() {

        try {
            GUIUtilities.showWaitCursor();
            BaseDialog dialog = new BaseDialog(GenerateErdPanel.TITLE, false);
            dialog.addDisplayComponentWithEmptyBorder(new GenerateErdPanel(parent, getSelectedConnection()));
            dialog.setResizable(false);
            dialog.display();
        } finally {
            GUIUtilities.showNormalCursor();
        }

        parent.repaintLayeredPane();
    }

    private DatabaseConnection getSelectedConnection() {
        return connectionsComboBox.getSelectedConnection();
    }

    private String bundleString(String key) {
        return Bundles.get(ErdToolBarPalette.class, key);
    }

}
