/*
 * ErdExecuteSQL.java
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
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.gui.editor.TransactionParametersPanel;
import org.executequery.localization.Bundles;
import org.executequery.sql.QueryDelegate;
import org.executequery.sql.QueryDispatcher;
import org.underworldlabs.swing.FlatSplitPane;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Takis Diakoumis
 */
public class ErdExecuteSQL extends ErdPrintableDialog
        implements QueryDelegate {

    /**
     * The results text area
     */
    private JTextArea resultsArea;

    /**
     * Utility to perform the execution
     */
    private QueryDispatcher queryAnalyser;

    /**
     * The 'Cancel' button
     */
    private JButton cancelButton;

    /**
     * The 'Close' button
     */
    private JButton closeButton;

    public ErdExecuteSQL(ErdViewerPanel parent) {
        super("Perform Schema Changes");

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sqlText.setSQLText(parent.getAllSQLText());

        display();

    }

    public ErdExecuteSQL(ErdViewerPanel parent, String sql) {

        super("Perform Schema Changes");

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sqlText.setSQLText(sql);

        display();

    }

    private void jbInit() throws Exception {
        sqlText.setAppending(true);

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setBackground(null);
        resultsArea.setMargin(new Insets(2, 2, 2, 2));
        JScrollPane resultsScroller = new JScrollPane(resultsArea);

        Border panelBorder = BorderFactory.createMatteBorder(
                1, 1, 1, 1, GUIUtilities.getDefaultBorderColour());

        resultsScroller.setBorder(panelBorder);
        sqlText.setBorder(panelBorder);

        Dimension textDim = new Dimension(530, 150);
        sqlText.setPreferredSize(textDim);
        resultsArea.setPreferredSize(textDim);

        JSplitPane splitPane = new FlatSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(sqlText);
        splitPane.setBottomComponent(resultsScroller);
        splitPane.setDividerLocation(0.5);
        splitPane.setDividerSize(5);

        closeButton = new JButton(Bundles.get("common.close.button"));
        cancelButton = new JButton(Bundles.getCommon("execute"));

        Dimension btnDim = new Dimension(80, 30);
        cancelButton.setPreferredSize(btnDim);
        closeButton.setPreferredSize(btnDim);

        ActionListener btnListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttons_actionPerformed(e);
            }
        };

        cancelButton.addActionListener(btnListener);
        closeButton.addActionListener(btnListener);

        queryAnalyser = new QueryDispatcher(this);

        Container c = getContentPane();
        c.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        c.add(splitPane, gbc);
        gbc.insets.top = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridy = 1;
        c.add(cancelButton, gbc);
        gbc.insets.left = 0;
        gbc.gridx = 1;
        gbc.weightx = 0;
        c.add(closeButton, gbc);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void execute() {
        resultsArea.append(Bundles.getCommon("executing"));
        queryAnalyser.executeSQLQuery(sqlText.getSQLText(), false, false);
    }

    public void setStopButtonEnabled(boolean enable) {
        if (enable) {
            cancelButton.setText(Bundles.getCommon("stop"));
        }
        cancelButton.setEnabled(enable);
    }

    public void setOutputMessage(DatabaseConnection dc, int type, String text) {
        resultsArea.append("\n\n" + text);
    }

    /**
     * Propagates the call to setOutputMessage(type, text) -
     * the selectTab is effectively ignored.
     *
     * @param type and other the error message to display
     */
    public void setOutputMessage(DatabaseConnection dc, int type, String text, boolean selectTab) {
        setOutputMessage(dc, type, text);
    }

    public void setResult(DatabaseConnection dc, int result, int type, String metaName) {
        String text = null;

        switch (type) {
            case QueryTypes.DROP_OBJECT:
                text = "\n\nTable dropped.";
                break;
            case QueryTypes.CREATE_OBJECT:
                text = "\n\nTable created.";
                break;
            case QueryTypes.ALTER_OBJECT:
                text = "\n\nTable altered.";
                break;
            case 24:
                text = "\n\nStatement executed successfully with result code 1.";
                break;
        }

        resultsArea.append(text);
    }

    private void buttons_actionPerformed(ActionEvent e) {
        Object button = e.getSource();

        if (button == closeButton) {
            queryAnalyser.closeConnection();
            queryAnalyser = null;
            dispose();
        } else if (button == cancelButton) {

            String btnText = cancelButton.getText();

            if (btnText.equals(Bundles.getCommon("execute")))
                execute();

            else if (btnText.equals(Bundles.getCommon("stop"))) {
                queryAnalyser.interruptStatement();
                resultsArea.append("\nProcess cancelled");
            }

        }

    }


    // -------------------------------------------
    // ---- Unimplemented QueryDelegate methods ----
    // -------------------------------------------

    public void finished(String message) {
    }

    public void commitModeChanged(boolean autoCommit) {
    }

    public void setStatusMessage(String text) {
    }

    public void statementExecuted(String text) {
    }

    public void executing() {
    }

    public void rollback(boolean anyConnections) {
    }

    public void commit(boolean anyConnections) {
    }

    public void interrupt() {
    }

    public void log(String message) {
    }

    public void executeQuery(String query, boolean anyConnections) {
    }

    public void executeQuery(String query, boolean executeAsBlock, boolean anyConnections) {
    }

    @Override
    public void setTPP(TransactionParametersPanel tpp) {

    }

    @Override
    public TransactionParametersPanel getTPP() {
        return null;
    }

    public void setResultSet(ResultSet rs, String query) throws SQLException {
    }

    public boolean isLogEnabled() {
        return false;
    }

    // -------------------------------------------

}









