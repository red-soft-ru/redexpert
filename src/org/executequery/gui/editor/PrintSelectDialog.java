/*
 * PrintSelectDialog.java
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

package org.executequery.gui.editor;

import org.executequery.GUIUtilities;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.localization.Bundles;
import org.executequery.print.PrintPreviewer;
import org.executequery.print.PrintingSupport;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The print selection dialog for the Query Editor
 * allowing the user to select which portion of the editor
 * to print from - the text area or the results panel.
 *
 * @author Takis Diakoumis
 */
public class PrintSelectDialog extends JPanel {

    public static final String PRINT_TITLE = "Print";

    public static final String PRINT_PREVIEW_TITLE = "Print Preview";

    /**
     * Indicates a call to print
     */
    public static final int PRINT = 0;

    /**
     * Indicates a call for a print preview
     */
    public static final int PRINT_PREVIEW = 1;

    /**
     * The owner of this dialog
     */
    private QueryEditor queryEditor;

    /**
     * The text area radio button
     */
    private JRadioButton queryRadio;

    /**
     * The results area radio button
     */
    private JRadioButton resultsRadio;

    /**
     * The worker to perform the process
     */
    private SwingWorker worker;

    /**
     * The type of print - print or print preview
     */
    private int commandType = PRINT;

    private final ActionContainer parent;

    public PrintSelectDialog(ActionContainer parent,
                             QueryEditor queryEditor, int commandType) {

        super(new GridBagLayout());

        this.parent = parent;
        this.queryEditor = queryEditor;
        this.commandType = commandType;

        init();
    }

    /**
     * <p>Initializes the state of this instance.
     */
    private void init() {

        JButton okButton = new DefaultPanelButton(commandType == PRINT_PREVIEW ?
                "Preview" : "Print");
        JButton cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));

        queryRadio = new JRadioButton("SQL Query Text Area", true);
        resultsRadio = new JRadioButton("SQL Table Results Panel");

        queryRadio.setMnemonic('A');
        resultsRadio.setMnemonic('R');

        ButtonGroup bg = new ButtonGroup();
        bg.add(queryRadio);
        bg.add(resultsRadio);

        okButton.setMnemonic('P');
        cancelButton.setMnemonic('C');

        ActionListener btnListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printButtons_actionPerformed(e);
            }
        };

        okButton.addActionListener(btnListener);
        cancelButton.addActionListener(btnListener);

        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(10, 50, 0, 50);
        gbc.gridwidth = 2;
        gbc.insets = ins;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Select the print area:"), gbc);
        gbc.gridy = 1;
        gbc.insets.left = 52;
        gbc.insets.top = 5;
        add(queryRadio, gbc);
        gbc.insets.top = 0;
        gbc.gridy = 2;
        gbc.insets.bottom = 10;
        add(resultsRadio, gbc);
        gbc.insets.bottom = 10;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets.right = 0;
        gbc.insets.left = 67;
        gbc.gridy = 3;
        add(okButton, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = 5;
        gbc.gridx = 1;
        gbc.weighty = 1.0;
        add(cancelButton, gbc);

    }

    private String printTable() {

        if (!queryEditor.isResultSetSelected()) {

            GUIUtilities.displayErrorMessage("No SQL table results.");
            queryEditor = null;

            return "Failed";
        }

        PrintingSupport printingSupport = new PrintingSupport();
        return printingSupport.print(queryEditor.getPrintableForQueryArea(), "RedXpert - editor");
    }

    private String printText() {

        PrintingSupport printingSupport = new PrintingSupport();

        return printingSupport.print(queryEditor.getPrintableForQueryArea(), "RedXpert - editor", true);
    }

    private void printButtons_actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();

        if (command.equals("Cancel")) {

            parent.finished();
            return;
        }

        final boolean printQuery = queryRadio.isSelected();

        if (commandType == PRINT) {

            worker = new SwingWorker() {
                public Object construct() {
                    Object obj = null;

                    if (printQuery) {
                        obj = printText();
                    } else {
                        obj = printTable();
                    }

                    return obj;
                }

                public void finished() {
                    parent.finished();
                }
            };

            setVisible(false);
            worker.start();

        } else {

            if (!printQuery && !queryEditor.isResultSetSelected()) {

                GUIUtilities.displayErrorMessage("No SQL table results.");
                parent.finished();

                return;
            }

            setVisible(false);

            if (printQuery) {

                new PrintPreviewer(queryEditor.getPrintableForQueryArea(),
                        queryEditor.getPrintJobName());

            } else {

                new PrintPreviewer(queryEditor.getPrintableForResultSet(),
                        queryEditor.getPrintJobName());
            }

            queryEditor = null;
            parent.finished();
        }

    }

}







