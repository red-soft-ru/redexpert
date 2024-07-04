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
import org.executequery.components.BottomButtonPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.erd.ErdGenerateProgressDialog;
import org.executequery.gui.erd.ErdSelectionPanel;
import org.executequery.gui.erd.ErdViewerPanel;
import org.executequery.localization.Bundles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Takis Diakoumis
 */
public class GenerateErdPanel extends JPanel
        implements ActionListener {

    public static final String TITLE = Bundles.get(GenerateErdPanel.class, "title");

    private ErdSelectionPanel selectionPanel;
    private final ErdViewerPanel parentErdPanel;
    private final DatabaseConnection connection;

    public GenerateErdPanel() {
        this(null, null);
    }

    public GenerateErdPanel(ErdViewerPanel parentErdPanel, DatabaseConnection connection) {
        super(new BorderLayout());
        this.parentErdPanel = parentErdPanel;
        this.connection = connection;
        init();
    }

    private void init() {

        selectionPanel = new ErdSelectionPanel(connection, parentErdPanel);
        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.add(selectionPanel, BorderLayout.NORTH);
        basePanel.add(new BottomButtonPanel(this, bundleString("Generate"), "erd", true),
                BorderLayout.SOUTH);
        add(basePanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (selectionPanel.hasSelections()) {

            if (parentErdPanel == null) {
                new ErdGenerateProgressDialog(
                        selectionPanel.getDatabaseConnection(),
                        selectionPanel.getSelectedValues()
                );

            } else {
                new ErdGenerateProgressDialog(
                        selectionPanel.getSelectedValues(),
                        parentErdPanel,
                        selectionPanel.getDatabaseConnection()
                );

                SwingUtilities.getWindowAncestor(this).dispose();
            }

        } else
            GUIUtilities.displayErrorMessage(bundleString("SelectMoreTablesError"));
    }

    private String bundleString(String key) {
        return Bundles.get(GenerateErdPanel.class, key);
    }

}
