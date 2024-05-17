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
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.toolbar.PanelToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * @author Takis Diakoumis
 */
public class ErdToolBarPalette extends PanelToolBar
        implements ActionListener {

    private final ErdViewerPanel parent;
    private final String[] scaleValues;
    private int selectedScaleIndex;

    private RolloverButton createTableButton;
    private RolloverButton addTableButton;
    private RolloverButton dropTableButton;
    private RolloverButton createRelationButton;
    private RolloverButton deleteRelationButton;
    private RolloverButton generateScriptButton;
    private RolloverButton fontStyleButton;
    private RolloverButton lineStyleButton;
    private RolloverButton canvasBgButton;
    private RolloverButton canvasFgButton;
    private RolloverButton erdTitleButton;
    private RolloverButton zoomInButton;
    private RolloverButton zoomOutButton;

    public ErdToolBarPalette(ErdViewerPanel parent) {
        super();

        this.parent = parent;
        this.selectedScaleIndex = 3;
        this.scaleValues = ErdViewerPanel.scaleValues;

        init();
        arrange();
    }

    private void init() {

        createTableButton = WidgetFactory.createRolloverButton(
                "createTableButton",
                bundleString("createTable"),
                "NewTable16.png",
                this
        );

        addTableButton = WidgetFactory.createRolloverButton(
                "addTableButton",
                bundleString("addTable"),
                "AddTable16.png",
                this
        );

        dropTableButton = WidgetFactory.createRolloverButton(
                "dropTableButton",
                bundleString("dropTable"),
                "DropTable16.png",
                this
        );

        createRelationButton = WidgetFactory.createRolloverButton(
                "createRelationButton",
                bundleString("relation"),
                "TableRelationship16.png",
                this
        );

        deleteRelationButton = WidgetFactory.createRolloverButton(
                "deleteRelationButton",
                bundleString("deleteRelation"),
                "TableRelationshipDelete16.png",
                this
        );

        generateScriptButton = WidgetFactory.createRolloverButton(
                "generateScriptsButton",
                bundleString("genScripts"),
                "CreateScripts16.png",
                this
        );

        erdTitleButton = WidgetFactory.createRolloverButton(
                "erdTitleButton",
                bundleString("erdTitle"),
                "ErdTitle16.png",
                this
        );

        fontStyleButton = WidgetFactory.createRolloverButton(
                "fontStyleButton",
                bundleString("fontStyle"),
                "FontStyle16.png",
                this
        );

        lineStyleButton = WidgetFactory.createRolloverButton(
                "lineStyleButton",
                bundleString("lineStyle"),
                "LineStyle16.png",
                this
        );

        canvasBgButton = WidgetFactory.createRolloverButton(
                "canvasBgButton",
                bundleString("canvasBg"),
                "ErdBackground16.png",
                this
        );

        canvasFgButton = WidgetFactory.createRolloverButton(
                "canvasFgButton",
                bundleString("canvasFg"),
                "ErdForeground16.png",
                this
        );

        zoomInButton = WidgetFactory.createRolloverButton(
                "zoomInButton",
                bundleString("zoomIn"),
                "ZoomIn16.png",
                this
        );

        zoomOutButton = WidgetFactory.createRolloverButton(
                "zoomOutButton",
                bundleString("zoomOut"),
                "ZoomOut16.png",
                this
        );
    }

    private void arrange() {

        addButton(createTableButton);
        addButton(addTableButton);
        addButton(dropTableButton);
        addButton(createRelationButton);
        addButton(deleteRelationButton);
        addButton(generateScriptButton);

        addSeparator();
        addButton(erdTitleButton);
        addButton(fontStyleButton);
        addButton(lineStyleButton);
        addButton(canvasFgButton);
        addButton(canvasBgButton);

        addSeparator();
        addButton(zoomOutButton);
        addButton(zoomInButton);
    }

    private void setBackgroundColours(boolean forCanvas) {

        boolean tablesSelected = false;
        ErdTable[] selectedTables = parent.getSelectedTablesArray();
        Color currentColour = forCanvas ? parent.getCanvasBackground() : parent.getTableBackground();

        if (selectedTables != null) {
            tablesSelected = true;
            currentColour = selectedTables.length == 1 ? selectedTables[0].getTableBackground() : null;
        }

        Color newColour = JColorChooser.showDialog(parent, Bundles.get("LocaleManager.ColorChooser.title"), currentColour);
        if (newColour == null)
            return;

        if (forCanvas) {
            parent.setCanvasBackground(newColour);

        } else if (tablesSelected) {
            Arrays.stream(selectedTables).forEach(table -> table.setTableBackground(newColour));
            parent.repaintLayeredPane();

        } else
            parent.setTableBackground(newColour);
    }

    public void incrementScale(int num) {
        if (selectedScaleIndex + num > -1 && selectedScaleIndex + num < scaleValues.length) {
            selectedScaleIndex += num;
            updateScale();
        }
    }

    public void setScale(String scale) {
        for (int i = 0; i < scaleValues.length; i++) {
            if (Objects.equals(scale, scaleValues[i])) {
                selectedScaleIndex = i;
                updateScale();
                break;
            }
        }
    }

    private void updateScale() {
        switch (selectedScaleIndex) {
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

        parent.setPopupMenuScaleValue(selectedScaleIndex);
    }

    // --- ActionListener impl ---

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (Objects.equals(source, createTableButton)) {
            new ErdNewTableDialog(parent);

        } else if (Objects.equals(source, addTableButton)) {
            new ErdSelectionDialog(parent);

        } else if (Objects.equals(source, dropTableButton)) {
            parent.removeSelectedTables();

        } else if (Objects.equals(source, createRelationButton)) {

            if (parent.getAllComponentsVector().size() <= 1) {
                GUIUtilities.displayErrorMessage(Bundles.get("ErdPopupMenu.needMoreTablesError"));
                return;
            }
            new ErdNewRelationshipDialog(parent);

        } else if (Objects.equals(source, deleteRelationButton)) {

            ErdTable[] tables = parent.getSelectedTablesArray();
            if (tables.length < 2)
                return;

            if (tables.length > 2) {
                GUIUtilities.displayErrorMessage(bundleString("SelectOnlyTwoTablesError"));
                return;
            }
            new ErdDeleteRelationshipDialog(parent, tables);

        } else if (Objects.equals(source, generateScriptButton)) {

            Vector tables = parent.getAllComponentsVector();
            if (tables.isEmpty()) {
                GUIUtilities.displayErrorMessage(bundleString("NoTablesError"));
                return;
            }

            Vector clonedTables = new Vector<>(tables.size());
            for (int i = 0; i < tables.size(); i++)
                clonedTables.add(tables.elementAt(i));

            new ErdScriptGenerator(clonedTables, parent);

        } else if (source == erdTitleButton) {

            ErdTitlePanel titlePanel = parent.getTitlePanel();
            if (titlePanel != null)
                titlePanel.doubleClicked(null);
            else
                new ErdTitlePanelDialog(parent);

        } else if (source == fontStyleButton) {
            parent.showFontStyleDialog();

        } else if (source == lineStyleButton) {
            parent.showLineStyleDialog();

        } else if (source == canvasFgButton) {
            setBackgroundColours(false);

        } else if (source == canvasBgButton) {
            setBackgroundColours(true);

        } else if (source == zoomInButton) {
            parent.zoom(true);

        } else if (source == zoomOutButton) {
            parent.zoom(false);
        }
    }

    // ---

    private String bundleString(String key) {
        return Bundles.get(ErdToolBarPalette.class, key);
    }

}
