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
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.GenerateErdPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.toolbar.PanelToolBar;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ErdToolBarPalette extends JPanel {

    private final ErdViewerPanel parent;
    private final String[] scaleValues;
    private int selectedScaleIndex;

    private RolloverButton createTableButton;
    private RolloverButton dropTableButton;
    private RolloverButton createRelationButton;
    private RolloverButton deleteRelationButton;
    private RolloverButton generateScriptButton;
    private RolloverButton erdTextBlockButton;
    private RolloverButton updateFromDatabase;
    private RolloverButton fontStyleButton;
    private RolloverButton lineStyleButton;
    private RolloverButton canvasBgButton;
    private RolloverButton canvasFgButton;
    private RolloverButton erdTitleButton;
    private RolloverButton zoomInButton;
    private RolloverButton zoomOutButton;

    private JComboBox<?> connectionsCombo;

    public ErdToolBarPalette(ErdViewerPanel parent) {
        super();

        this.parent = parent;
        this.selectedScaleIndex = 3;
        this.scaleValues = ErdViewerPanel.SCALE_VALUES;

        init();
        arrange();
    }

    private void init() {

        List<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        Repository repo = RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
        if (repo instanceof DatabaseConnectionRepository)
            connections = ((DatabaseConnectionRepository) repo).findAll();

        connectionsCombo = WidgetFactory.createComboBox(
                "connectionsCombo",
                connections.toArray()
        );

        createTableButton = WidgetFactory.createRolloverButton(
                "createTableButton",
                bundleString("createTable"),
                "icon_table_add",
                e -> new ErdNewTableDialog(parent)
        );

        dropTableButton = WidgetFactory.createRolloverButton(
                "dropTableButton",
                bundleString("dropTable"),
                "icon_table_drop",
                e -> parent.removeSelectedTables()
        );

        createRelationButton = WidgetFactory.createRolloverButton(
                "createRelationButton",
                bundleString("relation"),
                "icon_erd_relation_add",
                e -> createRelation()
        );

        deleteRelationButton = WidgetFactory.createRolloverButton(
                "deleteRelationButton",
                bundleString("deleteRelation"),
                "icon_erd_relation_delete",
                e -> deleteRelation()
        );

        generateScriptButton = WidgetFactory.createRolloverButton(
                "generateScriptsButton",
                bundleString("genScripts"),
                "icon_create_script",
                e -> generateScript()
        );

        erdTextBlockButton = WidgetFactory.createRolloverButton(
                "erdTextBlockButton",
                bundleString("erdText"),
                "icon_comment",
                e -> new ErdTextBlockDialog(parent)
        );

        updateFromDatabase = WidgetFactory.createRolloverButton(
                "updateFromDatabase",
                bundleString("updateFromDatabase"),
                "icon_refresh_connection",
                e -> updateFromDatabase()
        );

        erdTitleButton = WidgetFactory.createRolloverButton(
                "erdTitleButton",
                bundleString("erdTitle"),
                "icon_title",
                e -> createTitle()
        );

        fontStyleButton = WidgetFactory.createRolloverButton(
                "fontStyleButton",
                bundleString("fontStyle"),
                "icon_style_font",
                e -> parent.showFontStyleDialog()
        );

        lineStyleButton = WidgetFactory.createRolloverButton(
                "lineStyleButton",
                bundleString("lineStyle"),
                "icon_style_line",
                e -> parent.showLineStyleDialog()
        );

        canvasBgButton = WidgetFactory.createRolloverButton(
                "canvasBgButton",
                bundleString("canvasBg"),
                "icon_background",
                e -> setBackgroundColours(true)
        );

        canvasFgButton = WidgetFactory.createRolloverButton(
                "canvasFgButton",
                bundleString("canvasFg"),
                "icon_foreground",
                e -> setBackgroundColours(false)
        );

        zoomInButton = WidgetFactory.createRolloverButton(
                "zoomInButton",
                bundleString("zoomIn"),
                "icon_zoom_in",
                e -> parent.zoom(true)
        );

        zoomOutButton = WidgetFactory.createRolloverButton(
                "zoomOutButton",
                bundleString("zoomOut"),
                "icon_zoom_out",
                e -> parent.zoom(false)
        );

        connections.stream()
                .filter(DatabaseConnection::isConnected)
                .findFirst().ifPresent(dc -> connectionsCombo.setSelectedItem(dc));
    }

    private void arrange() {
        setLayout(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper().anchorWest().fillHorizontally();
        add(connectionsCombo, gbh.setWeightX(0.2).get());
        add(createTableButton, gbh.nextCol().setMinWeightX().get());
        add(dropTableButton, gbh.nextCol().get());
        add(createRelationButton, gbh.nextCol().get());
        add(deleteRelationButton, gbh.nextCol().get());
        add(generateScriptButton, gbh.nextCol().get());
        add(updateFromDatabase, gbh.nextCol().get());

        add(PanelToolBar.getSeparator(), gbh.nextCol().get());
        add(erdTextBlockButton, gbh.nextCol().get());
        add(erdTitleButton, gbh.nextCol().get());
        add(fontStyleButton, gbh.nextCol().get());
        add(lineStyleButton, gbh.nextCol().get());
        add(canvasFgButton, gbh.nextCol().get());
        add(canvasBgButton, gbh.nextCol().get());

        add(PanelToolBar.getSeparator(), gbh.nextCol().get());
        add(zoomOutButton, gbh.nextCol().get());
        add(zoomInButton, gbh.nextCol().get());

        add(new JPanel(), gbh.nextCol().setMaxWeightX().spanX().get());
    }

    private void createRelation() {

        if (parent.getAllComponentsVector().size() <= 1) {
            GUIUtilities.displayErrorMessage(Bundles.get("ErdPopupMenu.needMoreTablesError"));
            return;
        }

        new ErdNewRelationshipDialog(parent);
    }

    private void deleteRelation() {

        ErdTable[] tables = parent.getSelectedTablesArray();
        if (tables.length < 2)
            return;

        if (tables.length > 2) {
            GUIUtilities.displayErrorMessage(bundleString("SelectOnlyTwoTablesError"));
            return;
        }

        new ErdDeleteRelationshipDialog(parent, tables);
    }

    private void generateScript() {

        Vector<ErdMoveableComponent> allComponentsVector = parent.getAllComponentsVector();
        if (allComponentsVector.isEmpty()) {
            GUIUtilities.displayErrorMessage(bundleString("NoTablesError"));
            return;
        }

        Vector<ErdTable> erdTables = new Vector<>(allComponentsVector.size());
        allComponentsVector.stream()
                .filter(object -> object instanceof ErdTable)
                .forEach(erdTable -> erdTables.add((ErdTable) erdTable));

        if (erdTables.isEmpty()) {
            GUIUtilities.displayErrorMessage(bundleString("NoTablesError"));
            return;
        }

        new ComparerDBCommands().erdScript(erdTables, null);
    }

    private void createTitle() {

        ErdTitlePanel titlePanel = parent.getTitlePanel();
        if (titlePanel != null) {
            titlePanel.doubleClicked(null);
            return;
        }

        new ErdTitlePanelDialog(parent);
    }

    private void setBackgroundColours(boolean forCanvas) {

        boolean tablesSelected = false;
        ErdMoveableComponent[] selectedTables = parent.getSelectedComponentsArray();
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
            parent.fireChangedBgColor();
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

    private void updateFromDatabase() {
        try {
            GUIUtilities.showWaitCursor();

            BaseDialog dialog = new BaseDialog(GenerateErdPanel.TITLE, false);
            JPanel panel = new GenerateErdPanel(parent, dialog, getSelectedConnection());

            dialog.addDisplayComponentWithEmptyBorder(panel);
            dialog.setResizable(false);
            dialog.display();

        } finally {
            GUIUtilities.showNormalCursor();
        }

        parent.repaintLayeredPane();
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

    public DatabaseConnection getSelectedConnection() {

        Object selectedItem = connectionsCombo.getSelectedItem();
        if (selectedItem instanceof DatabaseConnection) {

            DatabaseConnection connection = (DatabaseConnection) selectedItem;
            if (!connection.isConnected())
                ConnectionMediator.getInstance().connect(connection, true);

            return connection;
        }

        return null;
    }

    public void setSelectedConnection(DatabaseConnection connection) {
        connectionsCombo.setSelectedItem(connection);
    }

    private static String bundleString(String key) {
        return Bundles.get(ErdToolBarPalette.class, key);
    }

}
