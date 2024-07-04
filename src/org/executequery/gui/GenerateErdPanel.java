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
import org.executequery.gui.erd.ErdGenerateProgressDialog;
import org.executequery.gui.erd.ErdViewerPanel;
import org.executequery.gui.erd.ErdSelectionPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class GenerateErdPanel extends JPanel {

    public static final String TITLE = Bundles.get(GenerateErdPanel.class, "title");

    private ErdSelectionPanel selectionPanel;
    private JButton generateButton;
    private JButton cancelButton;

    private final DatabaseConnection connection;
    private final ErdViewerPanel erdPanel;
    private final ActionContainer parent;

    public GenerateErdPanel(ActionContainer parent) {
        this(null, parent, null);
    }

    public GenerateErdPanel(ErdViewerPanel erdPanel, ActionContainer parent, DatabaseConnection connection) {
        super(new BorderLayout());
        this.connection = connection;
        this.erdPanel = erdPanel;
        this.parent = parent;

        init();
        arrange();
    }

    private void init() {
        selectionPanel = new ErdSelectionPanel(connection, erdPanel);

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
        mainPanel.add(selectionPanel, gbh.setMaxWeightY().spanX().get());
        mainPanel.add(buttonPanel, gbh.nextRowFirstCol().setMinWeightY().fillNone().get());

        // --- base ---

        add(mainPanel, BorderLayout.CENTER);
    }

    private void generate() {

        if (!selectionPanel.hasSelections()) {
            GUIUtilities.displayErrorMessage(bundleString("SelectMoreTablesError"));
            return;
        }

        if (erdPanel == null) {
            new ErdGenerateProgressDialog(
                    selectionPanel.getSelectedValues(),
                    selectionPanel.getDatabaseConnection()
            );

        } else {
            new ErdGenerateProgressDialog(
                    selectionPanel.getSelectedValues(),
                    erdPanel,
                    selectionPanel.getDatabaseConnection()
            );
        }

        dispose();
    }

    public void dispose() {
        parent.finished();
    }

    private String bundleString(String key) {
        return Bundles.get(GenerateErdPanel.class, key);
    }

}
