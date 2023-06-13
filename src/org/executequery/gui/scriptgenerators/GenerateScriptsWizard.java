/*
 * GenerateScriptsWizard.java
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

package org.executequery.gui.scriptgenerators;

import org.executequery.ActiveComponent;
import org.executequery.GUIUtilities;
import org.executequery.components.ItemSelectionListener;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databaseobjects.DatabaseSource;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.wizard.DefaultWizardProcessModel;
import org.underworldlabs.swing.wizard.WizardProcessPanel;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.List;

/**
 * Base panel for the generate scripts process.
 *
 * @author Takis Diakoumis
 */
public class GenerateScriptsWizard extends WizardProcessPanel
        implements ActiveComponent,
        ItemSelectionListener {

    public static final String TITLE = "Generate SQL Scripts";
    public static final String FRAME_ICON = "CreateScripts16.svg";

    /**
     * script type identifier for DROP TABLE
     */
    public static final int DROP_TABLES = 0;

    /**
     * script type identifier for CREATE TABLE
     */
    public static final int CREATE_TABLES = 1;

    /**
     * indicator to include constraints as ALTER TABLE statements
     */
    public static final int ALTER_TABLE_CONSTRAINTS = 0;

    /**
     * indicator to include constraints with CREATE TABLE statements
     */
    public static final int CREATE_TABLE_CONSTRAINTS = 1;

    /**
     * the first panel
     */
    private GenerateScriptsPanelOne firstPanel;

    /**
     * the second panel
     */
    private GenerateScriptsPanelTwo secondPanel;

    /**
     * the third panel
     */
    private GenerateScriptsPanelThree thirdPanel;

    /**
     * the fourth and final panel
     */
    private GenerateScriptsPanelFour fourthPanel;

    /**
     * the wizard model
     */
    private GenerateScriptsWizardModel model;

    /**
     * the parent container
     */
    private ActionContainer parent;

    private TableSelectionCombosGroup combosGroup;

    /**
     * the panel dimension
     */
    protected static final Dimension CHILD_DIMENSION = new Dimension(525, 320);

    /**
     * Creates a new instance of GenerateScriptsWizard
     */
    public GenerateScriptsWizard(ActionContainer parent) {

        this.parent = parent;

        model = new GenerateScriptsWizardModel();

        setModel(model);

        setHelpAction(ActionBuilder.get("help-command"), "generate-scripts");

        firstPanel = new GenerateScriptsPanelOne(this);
        secondPanel = new GenerateScriptsPanelTwo(this);

        combosGroup = new TableSelectionCombosGroup(
                firstPanel.getConnectionsCombo(), secondPanel.getSchemasCombo());

        // TODO I'm not sure about this, but another item listener not working when the scheme item selected
        secondPanel.getSchemasCombo().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {

                    DatabaseSource source = getSelectedSource();

                    if (source != null) {

                        List<NamedObject> tables = combosGroup.tablesForSchema(source);
                        secondPanel.schemaSelectionChanged(tables);
                    }

                } catch (DataSourceException ex) {

                    Log.error("Error on table selection for index", ex);

                } finally {

                    parent.unblock();
                }
            }
        });
        combosGroup.addItemSelectionListener(this);

        model.addPanel(firstPanel);

        prepare();
    }

    public void itemStateChanging(ItemEvent e) {

        parent.block();
    }

    public void itemStateChanged(ItemEvent event) {

        try {

            DatabaseSource source = getSelectedSource();

            if (source != null) {

                List<NamedObject> tables = combosGroup.tablesForSchema(source);
                secondPanel.schemaSelectionChanged(tables);
            }

        } catch (DataSourceException e) {

            Log.error("Error on table selection for index", e);

        } finally {

            parent.unblock();
        }

    }

    public DatabaseSource getSelectedSource() {

        return combosGroup.getSelectedSource();
    }

    public int getSelectedItemCount() {

        List<NamedObject> tables = getSelectedTables();

        if (tables != null) {

            return tables.size();
        }

        return 0;
    }

    public List<NamedObject> getSelectedTables() {

        return secondPanel.getSelectedTables();
    }

    /**
     * Whether to include the CASCADE keyword within DROP statments.
     *
     * @return true | false
     */
    protected boolean cascadeWithDrop() {
        return thirdPanel.cascadeWithDrop();
    }

    /**
     * Returns the constraints definition format -
     * as ALTER TABLE statements or within the CREATE TABLE statements.
     */
    protected int getConstraintsStyle() {
        return thirdPanel.getConstraintsStyle();
    }

    /**
     * Returns the output file path.
     *
     * @return the output file path
     */
    protected String getOutputFilePath() {
        return thirdPanel.getOutputFilePath();
    }

    protected boolean isWritingToFile() {
        return thirdPanel.isWritingToFile();
    }

    /**
     * Returns the type of script to be generated.
     *
     * @return the script type
     */
    protected int getScriptType() {
        return firstPanel.getScriptType();
    }

    /**
     * Starts the script generation process.
     */
    private void start() {

        setNextButtonEnabled(false);
        setBackButtonEnabled(false);
        setCancelButtonEnabled(false);

        fourthPanel.start();
    }

    /**
     * Defines the action for the NEXT button.
     */
    private boolean doNext() {
        JPanel nextPanel = null;
        int index = model.getSelectedIndex();
        switch (index) {

            case 0:
                if (secondPanel == null) {
                    secondPanel = new GenerateScriptsPanelTwo(this);
                }
                nextPanel = secondPanel;
                break;

            case 1:

                if (!secondPanel.hasSelections()) {
                    GUIUtilities.displayErrorMessage(
                            "You must select at least one table.");
                    return false;
                }

                if (thirdPanel == null) {
                    thirdPanel = new GenerateScriptsPanelThree(this);
                }
                nextPanel = thirdPanel;
                break;

            case 2:

                if (!thirdPanel.hasOutputStrategy()) {
                    GUIUtilities.displayErrorMessage(
                            "You must select either an output file to write to\n" +
                                    "or select to view within a Query Editor.");
                    return false;
                }

                if (fourthPanel == null) {
                    fourthPanel = new GenerateScriptsPanelFour(this);
                }
                nextPanel = fourthPanel;
                model.addPanel(nextPanel);
                start();
                return true;

        }

        ((GenerateScriptsPanel) nextPanel).panelSelected();
        model.addPanel(nextPanel);
        return true;
    }

    /**
     * Defines the action for the BACK button.
     */
    private boolean doPrevious() {
        // make sure the cancel button says cancel
        setCancelButtonText(Bundles.get("common.cancel.button"));
        return true;
    }

    /**
     * whether the process was successful
     */
    private boolean processSuccessful;

    protected void finished(boolean success) {
        setButtonsEnabled(true);
        setNextButtonEnabled(false);
        setBackButtonEnabled(true);
        setCancelButtonEnabled(true);

        processSuccessful = success;
        if (success) {
            setCancelButtonText("Finish");
        }

    }

    /**
     * Defines the action for the CANCEL button.
     */
    public void cancel() {
        parent.finished();
    }

    /**
     * Releases database resources before closing.
     */
    public void cleanup() {

        combosGroup.close();

        // check if we're viewing the script
        if (processSuccessful) {
            if (thirdPanel.openInQueryEditor()) {
                GUIUtils.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            GUIUtilities.showWaitCursor();
                            GUIUtilities.addCentralPane(
                                    QueryEditor.TITLE,
                                    QueryEditor.FRAME_ICON,
                                    new QueryEditor(getScriptText()),
                                    null,
                                    true);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }

                    private String getScriptText() {
                        try {
                            return FileUtils.loadFile(fourthPanel.getGeneratedFilePath());
                        } catch (IOException e) {
                            return "";
                        }
                    }
                });
            }
        }

    }


    private class GenerateScriptsWizardModel extends DefaultWizardProcessModel {

        public GenerateScriptsWizardModel() {
            String[] titles = {"Connection and Script Type",
                    "Schema and Table Selection",
                    "Output File Selection",
                    "Generating..."};
            setTitles(titles);

            String[] steps = {"Select the database connection and script type",
                    "Select the schema and tables",
                    "Select the output SQL file and further options",
                    "Generate the script"};
            setSteps(steps);
        }

        public boolean previous() {
            if (doPrevious()) {
                return super.previous();
            }
            return false;
        }

        public boolean next() {
            if (doNext()) {
                return super.next();
            }
            return false;
        }

    }

}







