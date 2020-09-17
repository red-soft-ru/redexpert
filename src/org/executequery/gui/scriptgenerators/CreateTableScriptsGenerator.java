/*
 * CreateTableScriptsGenerator.java
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

import org.executequery.GUIUtilities;
import org.executequery.gui.GeneratedScriptViewer;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.DateUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static org.executequery.databaseobjects.NamedObject.FOREIGN_KEY;
import static org.executequery.databaseobjects.NamedObject.PRIMARY_KEY;

/**
 * @author Takis Diakoumis
 * @deprecated
 */
public class CreateTableScriptsGenerator {

    public static final int SUCCESS = 0;

    public static final int CANCEL_FAIL = 1;

    /**
     * The generator object
     */
    private ScriptGenerator generator;

    /**
     * The progress dialog
     */
    private ProgressDialog progDialog;

    /**
     * The worker thread
     */
    private SwingWorker worker;

    /**
     * The file to save to
     */
    private File file;

    public CreateTableScriptsGenerator(ScriptGenerator generator) {
        this.generator = generator;
    }

    public void generate() {
        worker = new SwingWorker() {
            public Object construct() {
                return doWork();
            }

            public void finished() {
                progDialog.setStatus(-1);
                Map<String, String> result = (Map<String, String>) get();

                try {
                    Thread.sleep(400);
                } catch (InterruptedException intExc) {
                }

                progDialog.dispose();

                if (result.containsKey("Done")) {
                    int yesno = GUIUtilities.displayYesNoDialog(
                            "The script was generated successfully to " +
                                    result.get("Done") +
                                    ".\nDo you wish to view the generated script?", "Finished");

                    if (yesno == JOptionPane.YES_OPTION) {
                        final String script = result.get("script");
                        //String fileName = (String)result.get("Done");
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                GUIUtilities.addCentralPane(
                                        GeneratedScriptViewer.TITLE,
                                        GeneratedScriptViewer.FRAME_ICON,
                                        new GeneratedScriptViewer(script, file),
                                        GeneratedScriptViewer.TITLE,
                                        true);
                            }
                        });
                    }
                    generator.setResult(SUCCESS);
                    generator.dispose();
                    return;
                } else if (result.containsKey("cancelled")) {
                    GUIUtilities.displayInformationMessage("Process Cancelled.");
                    generator.setResult(CANCEL_FAIL);
                } else if (result.containsKey("Failed")) {
                    String message = "An error occured generating the script.\n";

                    Object object = result.get("Failed");
                    if (object instanceof Exception) {
                        Exception e = (Exception) object;
                        message += "\n" + e.getMessage();
                        GUIUtilities.displayExceptionErrorDialog(message, e);
                    } else {
                        GUIUtilities.displayErrorMessage(message);
                    }
                    generator.setResult(CANCEL_FAIL);
                }
                generator.dispose();
            }
        };
        worker.start();
    }

    private Object doWork() {

        Vector selections = generator.getSelectedTables();

        Map<String, String> result = new HashMap<String, String>();

        file = new File(generator.getScriptFilePath());
        progDialog = new ProgressDialog(selections.size());

        PrintWriter writer = null;

        String CREATE_TABLE = "CREATE TABLE ";
        String ALTER_TABLE = "ALTER TABLE ";
        String NOT_NULL = " NOT NULL";
        String CONSTRAINT = "CONSTRAINT ";
        String KEY = " KEY ";
        String REFERENCES = " REFERENCES ";
        String ADD = " ADD ";
        String EMPTY = "";

        char B_OPEN = '(';
        char B_CLOSE = ')';
        char SPACE = ' ';
        char NEW_LINE = '\n';
        char COMMA = ',';
        char SEMI_COLON = ';';
        char DOT = '.';

        boolean includeConstraints = generator.includeConstraints();
        boolean useCreateForConstraints = generator.includeConstraintsInCreate();
        boolean useAlterForConstraints = generator.includeConstraintsAsAlter();

        StringBuffer pKeys = null;
        StringBuffer fKeys = null;

        if (useAlterForConstraints) {
            pKeys = new StringBuffer();
            fKeys = new StringBuffer();
        }

        try {
            generator.enableButtons(false);

            Log.info("Generating SQL script...");
            writer = new PrintWriter(new FileWriter(file, false), true);
            StringBuffer sb_script = new StringBuffer(1000);

            StringBuffer sb_header = new StringBuffer(500);
            String line_1 = "-- ---------------------------------------------------\n";
            sb_header.append(line_1).append("--\n-- SQL script ").
                    append("generated by Red Expert.\n-- Generated ");

            DateUtils dt = new DateUtils();
            sb_header.append(dt.getLongDateTime()).append("\n--\n").
                    append(line_1).append("--\n-- Program:      ").
                    append(file.getName()).append("\n-- Description:  SQL create ").
                    append("tables script.\n-- Schema:       ").
                    append(generator.getSchemaName()).append("\n-- Database:     ").
                    append(generator.getDatabaseProductName()).append("\n--\n").append(line_1).
                    append(NEW_LINE).append(NEW_LINE);

            writer.println(sb_header.toString());
            sb_script.append(sb_header);
            sb_header = null;
            line_1 = null;
            dt = null;

            int sepLength = -1;
            StringBuffer sb = new StringBuffer(500);
            StringBuffer sb_spaces_1 = new StringBuffer(50);
            StringBuffer sb_spaces_2 = new StringBuffer(30);
            String initialSpaces = "               ";

            int s_size = selections.size();
            Vector columnConstraints = new Vector();

            for (int i = 0; i < s_size; i++) {

                if (Thread.interrupted()) {
                    file.delete();
                    progDialog.setStatus(-1);
                    throw new InterruptedException();
                }

                String tableName = selections.elementAt(i).toString();
                ColumnData[] cda = generator.getColumnDataArray(tableName);

                sb.append(CREATE_TABLE).
                        append(tableName).
                        append(SPACE).
                        append(B_OPEN);

                if (cda.length > 0) {
                    sb_spaces_1.append(initialSpaces);
                    int tn_length = tableName.length();
                    // spaces between beginning of line and column name
                    for (int k = 0; k < tn_length; k++) {
                        sb_spaces_1.append(SPACE);
                    }

                    String spaces_1 = sb_spaces_1.toString();

                    for (int j = 0; j < cda.length; j++) {
                        ColumnData column = cda[j];
                        sepLength = getSpaceLength(cda) + 5;

                        if (j > 0) {
                            sb.append(spaces_1);
                        }

                        int l_size = sepLength - column.getColumnName().length();
                        for (int m = 0; m < l_size; m++) {
                            sb_spaces_2.append(SPACE);
                        }

                        sb.append(column.getFormattedColumnName()).
                                append(sb_spaces_2).
                                append(column.getFormattedDataType());

                        sb.append(column.isRequired() ? NOT_NULL : EMPTY);

                        if (column.isKey() && includeConstraints) {

                            Vector ccv = column.getColumnConstraintsVector();

                            for (int a = 0, b = ccv.size(); a < b; a++) {
                                columnConstraints.add(ccv.get(a));
                            }

                        }

                        if (j != cda.length - 1) {
                            sb.append(COMMA).append(NEW_LINE);
                        }
                        sb_spaces_2.setLength(0);
                    }

                    int v_size = columnConstraints.size();

                    if (v_size > 0) {

                        if (useCreateForConstraints) {
                            sb.append(COMMA).append(NEW_LINE);

                            ColumnConstraint cc = null;
                            for (int j = 0; j < v_size; j++) {
                                cc = (ColumnConstraint) columnConstraints.get(j);
                                sb.append(spaces_1).append(CONSTRAINT).
                                        append(cc.getName()).append(SPACE).
                                        append(cc.getTypeName()).append(KEY).
                                        append(B_OPEN).append(cc.getColumn()).
                                        append(B_CLOSE);

                                if (cc.getType() == FOREIGN_KEY) {
                                    sb.append(REFERENCES);

                                    if (cc.hasSchema())
                                        sb.append(cc.getRefSchema()).append(DOT);

                                    sb.append(cc.getRefTable()).append(B_OPEN).
                                            append(cc.getRefColumn()).append(B_CLOSE);
                                }

                                cc = null;

                                if (j < v_size - 1)
                                    sb.append(COMMA).append(NEW_LINE);

                            }

                        } else if (useAlterForConstraints) {
                            int type = -1;
                            ColumnConstraint cc = null;

                            for (int j = 0; j < v_size; j++) {
                                cc = (ColumnConstraint) columnConstraints.get(j);
                                type = cc.getType();

                                if (type == FOREIGN_KEY) {

                                    fKeys.append(ALTER_TABLE).append(cc.getTable()).
                                            append(ADD).append(CONSTRAINT).
                                            append(cc.getName()).append(SPACE).
                                            append(cc.getTypeName()).append(KEY).
                                            append(B_OPEN).append(cc.getColumn()).
                                            append(B_CLOSE).append(REFERENCES);

                                    //                                  if (cc.hasSchema())
                                    //                                    fKeys.append(cc.getSchema()).append(DOT);

                                    fKeys.append(cc.getRefTable()).append(B_OPEN).
                                            append(cc.getRefColumn()).append(B_CLOSE).
                                            append(SEMI_COLON).append(NEW_LINE);
                                } else if (type == PRIMARY_KEY) {

                                    pKeys.append(ALTER_TABLE).append(cc.getTable()).
                                            append(ADD).append(CONSTRAINT).
                                            append(cc.getName()).append(SPACE).
                                            append(cc.getTypeName()).append(KEY).
                                            append(B_OPEN).append(cc.getColumn()).
                                            append(B_CLOSE).append(SEMI_COLON).append(NEW_LINE);
                                }

                                cc = null;

                            }

                        }

                    }

                    sb.append(B_CLOSE).append(SEMI_COLON).
                            append(NEW_LINE).append(NEW_LINE);

                    spaces_1 = null;

                } else {
                    sb.append(B_CLOSE).append(SEMI_COLON).
                            append(NEW_LINE).append(NEW_LINE);
                }

                writer.println(sb.toString());
                sb_script.append(sb).append(NEW_LINE);

                sb.setLength(0);
                sb_spaces_1.setLength(0);
                columnConstraints.clear();

                cda = null;
                tableName = null;

                progDialog.setStatus(i + 1);
            }

            if (fKeys != null && pKeys != null) {
                pKeys.append(NEW_LINE).
                        append(NEW_LINE).
                        append(fKeys);
                writer.println(pKeys.toString());
                sb_script.append(pKeys);
            }

            sb = null;
            pKeys = null;
            fKeys = null;
            sb_spaces_1 = null;
            sb_spaces_2 = null;
            initialSpaces = null;
            columnConstraints = null;

            result.put("Done", file.getName());
            result.put("script", sb_script.toString());
            sb_script = null;
            return result;

        } catch (IOException e) {
            result.put("Failed", e.getMessage());
            return result;
        } catch (InterruptedException e) {
            result.put("cancelled", EMPTY);
            return result;
        } finally {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        }

    }

    public void cancelTransfer() {
        if (worker != null) {
            worker.interrupt();
        }
    }

    private int getSpaceLength(ColumnData[] cda) {
        int spaces = 0;
        // spaces between end of column name and data type name
        for (int i = 0; i < cda.length; i++) {
            spaces = Math.max(spaces, cda[i].getColumnName().length());
        }
        return spaces;
    }

    class ProgressDialog extends AbstractBaseDialog
            implements ActionListener {

        private JProgressBar transferProg;
        private JButton cancButton;
        private int max;

        public ProgressDialog(int tables) {

            super(GUIUtilities.getParentFrame(), "Progress", false);

            max = tables;
            transferProg = new JProgressBar(1, max);
            transferProg.setPreferredSize(new Dimension(230, 20));

            cancButton = new JButton(Bundles.get("common.cancel.button"));
            cancButton.addActionListener(this);

            JPanel base = new JPanel(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            Insets ins = new Insets(10, 10, 10, 10);
            gbc.insets = ins;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            base.add(new JLabel("Generating SQL script..."), gbc);
            gbc.gridy = 1;
            gbc.insets.top = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            base.add(transferProg, gbc);
            gbc.insets.bottom = 10;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridy = 2;
            base.add(cancButton, gbc);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(base, BorderLayout.CENTER);

            setResizable(false);

            pack();
            setLocation(GUIUtilities.getLocationForDialog(getSize()));
            setVisible(true);

        }

        public void actionPerformed(ActionEvent e) {
            cancelTransfer();
        }

        public void setStatus(int value) {
            if (value == -1)
                setProgressStatus(max + 1);
            else
                setProgressStatus(value);
        }

        private void setProgressStatus(final int status) {
            Runnable setProgressBarValue = new Runnable() {
                public void run() {
                    transferProg.setValue(status);
                }
            };

            setProgressBarValue.run();

        }

    } // ProgressDialog


}


















