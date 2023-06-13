/*
 * GenerateScriptsPanel.java
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

import org.executequery.ActiveComponent;
import org.executequery.components.BottomButtonPanel;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.scriptgenerators.BaseScriptGeneratorPanel;
import org.executequery.gui.scriptgenerators.CreateTableScriptsGenerator;
import org.executequery.gui.scriptgenerators.ScriptGenerator;
import org.underworldlabs.jdbc.DataSourceException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 * @deprecated
 */
public class GenerateScriptsPanel extends BaseScriptGeneratorPanel
        implements ActiveComponent,
        ScriptGenerator,
        ActionListener {

    public static final String TITLE = "Generate SQL Scripts";
    public static final String FRAME_ICON = "CreateScripts16.svg";

    private BottomButtonPanel bottomPanel;

    /**
     * the parent container
     */
    private ActionContainer parent;

    public GenerateScriptsPanel(ActionContainer parent) {
        super();
        this.parent = parent;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        bottomPanel = new BottomButtonPanel(
                this, "Generate", "generate-scripts", parent.isDialog());
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(bottomPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST,
                GridBagConstraints.BOTH,
                new Insets(0, 5, 5, 5), 0, 0));
        add(panel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        try {
            String command = e.getActionCommand();
            if (command.equals("Generate") && hasRequiredFields()) {
                if (parent.isDialog()) {
                    JDialog dialog = ((JDialog) parent);
                    dialog.setModal(false);
                }
                CreateTableScriptsGenerator generator =
                        new CreateTableScriptsGenerator(this);
                generator.generate();
            }
        } finally {
            if (parent.isDialog()) {
                JDialog dialog = (JDialog) parent;
                if (dialog.isVisible()) {
                    dialog.setModal(true);
                }
            }
        }

    }

    /**
     * Sets the result of the script generation process.
     *
     * @param the process result
     */
    public void setResult(int result) {
        if (result == CreateTableScriptsGenerator.SUCCESS) {
            dispose();
        } else if (result == CreateTableScriptsGenerator.CANCEL_FAIL) {
            enableButtons(true);
        }
    }

    /**
     * Releases database resources before closing.
     */
    public void cleanup() {
        metaData.closeConnection();
    }

    public void dispose() {
        parent.finished();
    }

    public String getScriptFilePath() {
        return pathField.getText();
    }

    public ColumnData[] getColumnDataArray(String tableName) {
        try {
            return metaData.getColumnMetaData(tableName,
                    schemaCombo.getSelectedItem().toString());
        } catch (DataSourceException e) {
            return null;
        }
    }

    public Vector getSelectedTables() {
        return listPanel.getSelectedValues();
    }

    public boolean hasSelectedTables() {
        return listPanel.hasSelections();
    }

    public boolean includeConstraintsInCreate() {
        return consInCreateCheck.isSelected();
    }

    public boolean includeConstraints() {
        return constraintsCheck.isSelected();
    }

    public boolean includeConstraintsAsAlter() {
        return consAsAlterCheck.isSelected();
    }

    public void enableButtons(boolean enable) {
        bottomPanel.enableButtons(enable);
    }

    public String getDatabaseProductName() {
        try {
            return metaData.getDatabaseProductName();
        } catch (DataSourceException e) {
            return "Not Available";
        }
    }

    public String getSchemaName() {
        return metaData.getSchemaName().toUpperCase();
    }

}















