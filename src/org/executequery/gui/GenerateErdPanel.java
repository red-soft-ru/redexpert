/*
 * GenerateErdPanel.java
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

package org.executequery.gui;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.gui.erd.ErdGenerateProgressDialog;
import org.executequery.gui.erd.ErdViewerPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class GenerateErdPanel extends JPanel {

    public static final String TITLE = Bundles.get(GenerateErdPanel.class, "title");

    private ListSelectionPanel listPanel;
    private JButton generateButton;
    private JButton cancelButton;

    private final DatabaseConnection connection;
    private final ActionContainer parent;
    private final ErdViewerPanel erdPanel;

    public GenerateErdPanel(DatabaseConnection connection, ActionContainer parent, ErdViewerPanel erdPanel) {
        super(new BorderLayout());
        this.erdPanel = erdPanel;
        this.connection = connection;
        this.parent = parent;

        init();
        arrange();
    }

    private void init() {

        MetaDataValues metaData = new MetaDataValues(true);
        metaData.setDatabaseConnection(connection);

        listPanel = new ListSelectionPanel(
                Bundles.get("ErdSelectionPanel.availableTables"),
                Bundles.get("ErdSelectionPanel.selectedTables")
        );
        listPanel.createAvailableList(metaData.getTables(null, null, "TABLE"));

        generateButton = WidgetFactory.createButton(
                "generateButton",
                bundleString("Generate"),
                e -> generate()
        );
        cancelButton = WidgetFactory.createButton(
                "cancelButton",
                Bundles.get("common.cancel.button"),
                e -> dispose()
        );

        metaData.closeConnection();
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().rightGap(5).anchorEast();
        buttonPanel.add(generateButton, gbh.get());
        buttonPanel.add(cancelButton, gbh.nextCol().rightGap(0).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillBoth();
        mainPanel.add(listPanel, gbh.setMaxWeightY().spanX().get());
        mainPanel.add(buttonPanel, gbh.nextRowFirstCol().setMinWeightY().fillNone().get());

        // --- base ---

        add(mainPanel, BorderLayout.CENTER);
    }

    private void generate() {

        if (!listPanel.hasSelections()) {
            GUIUtilities.displayErrorMessage(bundleString("SelectMoreTablesError"));
            return;
        }

        new ErdGenerateProgressDialog(connection, listPanel.getSelectedValues(), null, erdPanel);
    }

    public void dispose() {
        listPanel.clear();
        parent.finished();
    }

    private String bundleString(String key) {
        return Bundles.get(GenerateErdPanel.class, key);
    }

}
