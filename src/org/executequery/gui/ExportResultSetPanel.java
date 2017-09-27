/*
 * ExportResultSetPanel.java
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import org.apache.commons.lang.StringUtils;
import org.executequery.ActiveComponent;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabViewActionPanel;
import org.executequery.components.FileChooserDialog;
import org.executequery.components.MinimumWidthActionButton;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.event.KeywordEvent;
import org.executequery.event.KeywordListener;
import org.executequery.gui.importexport.ImportExportDataException;
import org.executequery.gui.importexport.ResultSetDelimitedFileWriter;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.executequery.log.Log;
import org.executequery.sql.DerivedQuery;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.AbstractStatusBarPanel;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.ProgressBar;
import org.underworldlabs.swing.ProgressBarFactory;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

/** 
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ExportResultSetPanel extends DefaultTabViewActionPanel
                                  implements NamedView,
                                             FocusComponentPanel,
                                             ActiveComponent,
                                             KeywordListener,
                                             ConnectionListener,
                                             TextEditorContainer {
    
    public static final String TITLE = "Export Result Set ";
    public static final String FRAME_ICON = "ExportDelimited16.png";
    
    private JComboBox connectionsCombo; 

    private JComboBox delimiterCombo;

    private JCheckBox applyQuotesCheck;
    
    private JTextField fileNameField;

    private JCheckBox includeColumNamesCheck;
    
    private SimpleSqlTextPanel sqlText;

    private TableSelectionCombosGroup combosGroup;

    private LoggingOutputPanel outputPanel;
    
    private SqlTextPaneStatusBar statusBar;
    private MinimumWidthActionButton stopButton;
    
    private static final KeyStroke EXECUTE_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
    
    public ExportResultSetPanel() {

        super(new BorderLayout());
        init();
    }
    
    private void init() {
        
        fileNameField = WidgetFactory.createTextField();
        connectionsCombo = WidgetFactory.createComboBox();

        String[] delims = {"|", ",", ";", "#"};
        delimiterCombo = WidgetFactory.createComboBox(delims);
        delimiterCombo.setEditable(true);
        
        combosGroup = new TableSelectionCombosGroup(connectionsCombo);

        includeColumNamesCheck = new JCheckBox("Include column names as first row");
        applyQuotesCheck = new JCheckBox("Use double quotes for char/varchar/longvarchar columns", true);
        
        sqlText = new SimpleSqlTextPanel();
//        sqlText.getTextPane().setBackground(Color.WHITE);
        sqlText.setBorder(null);
        sqlText.setScrollPaneBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, UIUtils.getDefaultBorderColour()));

        statusBar = new SqlTextPaneStatusBar();
        JPanel sqlPanel = new JPanel(new BorderLayout());
        sqlPanel.add(sqlText, BorderLayout.CENTER);
        sqlPanel.add(statusBar, BorderLayout.SOUTH);
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));

        outputPanel = new LoggingOutputPanel();
        FlatSplitPane splitPane = new FlatSplitPane(
                JSplitPane.VERTICAL_SPLIT, sqlPanel, outputPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.8);
        splitPane.setDividerSize(5);

        JButton button = WidgetFactory.createInlineFieldButton("Browse");
        button.setActionCommand("browse");
        button.addActionListener(this);
        button.setMnemonic('r');

        JPanel mainPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridheight = 1;
        gbc.insets.top = 5;
        gbc.insets.bottom = 5;
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Connection:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(connectionsCombo, gbc);
        gbc.insets.left = 5;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 0;
        mainPanel.add(new JLabel("Data Delimiter:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(delimiterCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 2;
        mainPanel.add(new JLabel("Output File:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(fileNameField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(button, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets.top = 2;
        gbc.insets.left = 5;
        gbc.insets.bottom = 0;
        mainPanel.add(includeColumNamesCheck, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        mainPanel.add(applyQuotesCheck, gbc);
        gbc.insets.top = 5;
        gbc.gridy++;
        gbc.insets.bottom = 10;
        mainPanel.add(new JLabel(instructionNote()), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        gbc.insets.left = 5;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(splitPane, gbc);

        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        int minimumButtonWidth = 85;
        executeButton = new MinimumWidthActionButton(minimumButtonWidth, this, "Execute", "executeAndExport");
        stopButton = new MinimumWidthActionButton(minimumButtonWidth, this, "Stop", "stop");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 5));
        buttonPanel.add(executeButton);
        buttonPanel.add(stopButton);

        stopButton.setEnabled(false);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);        
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // register as a keyword and connection listener
        EventMediator.registerListener(this);
        
        JTextPane textPane = sqlText.getTextPane();
        ActionMap actionMap = textPane.getActionMap();

        String actionKey = "executeQueryAction";
        actionMap.put(actionKey, executeQueryAction);

        InputMap inputMap = textPane.getInputMap();
        inputMap.put(EXECUTE_KEYSTROKE, actionKey);

        JPopupMenu popupMenu = sqlText.getPopup();
        popupMenu.addSeparator();
        popupMenu.add(executeQueryAction);        
    }

    public void browse() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle("Select Export File Path");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();
        if (file.exists()) {
            
            result = GUIUtilities.displayConfirmCancelDialog("The selected file exists.\nOverwrite existing file?");

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION) {
                
                browse();
                return;
            }

        }
        
        fileNameField.setText(file.getAbsolutePath());
    }

    private boolean fieldsValid() {
        
        if (StringUtils.isBlank(delimiterCombo.getSelectedItem().toString())) {
            
            GUIUtilities.displayErrorMessage("Please select or enter an appropriate delimiter");
            return false;
        }

        if (StringUtils.isBlank(fileNameField.getText())) {

            GUIUtilities.displayErrorMessage("Please select an output file");
            return false;            
        }
        
        if (StringUtils.isBlank(sqlText.getEditorText())) {

            GUIUtilities.displayErrorMessage("Please enter a valid SQL query");
            return false;            
        }
        
        return true;
    }
    
    public boolean tabViewClosing() {

        cleanup();        
        return true;
    }

    public void cleanup() {

        combosGroup.close();

        if (statusBar != null) {
         
            statusBar.cleanup();
        }
        
        EventMediator.deregisterListener(this);
    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof DefaultKeywordEvent) || (event instanceof ConnectionEvent);
    }

    /** Notification of a new keyword added to the list. */
    public void keywordsAdded(KeywordEvent e) {

        sqlText.setSQLKeywords(true);
    }

    /** Notification of a keyword removed from the list. */
    public void keywordsRemoved(KeywordEvent e) {

        sqlText.setSQLKeywords(true);
    }

    public Component getDefaultFocusComponent() {

        return fileNameField;
    }

    public void stop() {

        if (executing) {

            if (swingWorker != null) {
                
                swingWorker.interrupt();
            }

        }
            
    }
    
    private void enableButtons(final boolean enableExecute, final boolean enableStop) {

        GUIUtils.invokeLater(new Runnable() {
           public void run() {
               executeButton.setEnabled(enableExecute);
               stopButton.setEnabled(enableStop);                
            } 
            
        });        
    }

    private SwingWorker swingWorker;
    private boolean executing;
    
    public void executeAndExport() {

        if (!executing) {

            if (fieldsValid()) {
    
                enableButtons(false, true);
                
                swingWorker = new SwingWorker() {
                    public Object construct() {

                        executing = true;
                        return execute();
                    }
                    public void finished() {
    
                        try {
    
                            Integer recordCount = (Integer) get();
                            if (recordCount != -1) {
        
                                outputPanel.append("Records transferred: " + recordCount);
                                
                                File file = outputFile();
                                StringBuilder sb = new StringBuilder();
                                sb.append("Output file: ");
                                sb.append(file.getName());
                                sb.append(" (");
                                sb.append(new DecimalFormat("###,###.##").format(MiscUtils.bytesToMegaBytes(file.length())));
                                sb.append("Mb)");
                                
                                outputPanel.append(sb.toString());
                            }
    
                        } finally {

                            executing = false;
                            enableButtons(true, false);
                        }
                    }
                };
                swingWorker.start();
            }
            
        }
    }
    
    private File outputFile() {
        
        return new File(fileNameField.getText());
    }
    
    private int execute() {
     
        ResultSet resultSet = null;
        DatabaseHost host = combosGroup.getSelectedHost();
        StatementExecutor statementExecutor = new DefaultStatementExecutor(host.getDatabaseConnection(), true);
        statementExecutor.setCommitMode(false);

        int result = -1;
        long startTime = System.currentTimeMillis();
        SqlStatementResult statementResult = null;

        try {

            String query = sqlText.getEditorText();
            
            statusBar.setStatusText("Executing...");
            statusBar.startProgressBar();

            outputPanel.appendAction("Executing:");
            outputPanel.appendActionFixedWidth(query);

            DerivedQuery derivedQuery = new DerivedQuery(query);
            statementResult = statementExecutor.execute(derivedQuery.getQueryType(), query, fetchSizeForDatabaseProduct(host));

            if (statementResult.isException()) {
                
                throw statementResult.getSqlException();
                
            } else if (statementResult.isResultSet()) {

                resultSet = statementResult.getResultSet();
                result = writeToFile(resultSet);

            } else {

                outputPanel.appendWarning("The executed query did not return a valid result set");
            }
            
        } catch (SQLException e) {

            if (statementResult != null) {
            
                outputPanel.appendError(statementResult.getErrorMessage());

            } else {

                outputPanel.appendError("Execution error:\n" + e.getMessage());
            }
            
        } catch (ImportExportDataException e) {

            outputPanel.appendError("Execution error:\n" + e.getMessage());

        } catch (InterruptedException e) {

            outputPanel.appendWarning("Operation cancelled by user action");

        } finally {

            long endTime = System.currentTimeMillis();

            statusBar.setStatusText("Done");
            statusBar.stopProgressBar();

            outputPanel.append("Duration: " + MiscUtils.formatDuration(endTime - startTime));

            try {
                
                if (resultSet != null) {

                    resultSet.close();
                }
                
                statementExecutor.destroyConnection();

            } catch (SQLException e) {

                e.printStackTrace();
            }

            host.close();
            GUIUtilities.scheduleGC();
        }
        
        return result;
    }
    
    private int fetchSizeForDatabaseProduct(DatabaseHost host) {

        // we only care about mysql right now which needs Integer.MIN_VALUE
        // to provide row-by-row return on the result set cursor
        // otherwise default to 10000 row fetch size...

        if (host.getDatabaseProductName().toUpperCase().contains("MYSQL")) {
            
            return Integer.MIN_VALUE;
        }
        
        return 10000;
    }

    
    private int writeToFile(ResultSet resultSet) throws InterruptedException {

        ResultSetDelimitedFileWriter writer = new ResultSetDelimitedFileWriter(); 
        return writer.write(fileNameField.getText(), 
                delimiterCombo.getSelectedItem().toString(), resultSet, 
                includeColumNamesCheck.isSelected(), applyQuotesCheck.isSelected());
    }

    // ------------------------------------------------
    // ----- TextEditorContainer implementations ------
    // ------------------------------------------------
    
    /**
     * Returns the SQL text pane as the TextEditor component 
     * that this container holds.
     */
    public TextEditor getTextEditor() {

        return sqlText;
    }
    
    private static int count = 1;
    public String getDisplayName() {

        return TITLE + (count++);
    }

    public String toString() {

        return getDisplayName();
    }

    // ---------------------------------------------
    // ConnectionListener implementation
    // ---------------------------------------------
    
    /**
     * Indicates a connection has been established.
     * 
     * @param the encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {

        combosGroup.connectionOpened(connectionEvent.getDatabaseConnection());
    }

    /**
     * Indicates a connection has been closed.
     * 
     * @param the encapsulating event
     */
    public void disconnected(ConnectionEvent connectionEvent) {

        combosGroup.connectionClosed(connectionEvent.getDatabaseConnection());
    }

    private static final int STATUS_BAR_HEIGHT = 21;

    private ProgressBar progressBar;
    private JButton executeButton;
    
    class SqlTextPaneStatusBar extends AbstractStatusBarPanel {

        protected SqlTextPaneStatusBar() {
            
            super(STATUS_BAR_HEIGHT);

            addLabel(0, 200, true);
            progressBar = ProgressBarFactory.create(false, true);
            addComponent(((JComponent) progressBar), 1, 120, false);
        }
        
        public void setStatusText(String text) {
            
            setLabelText(0, text);
        }
        
        public void cleanup() {
         
            progressBar.cleanup();
            progressBar = null;
        }
        
        public void startProgressBar() {

            progressBar.start();
        }

        public void stopProgressBar() {
            
            progressBar.stop();
        }

    } // SqlTextPaneStatusBar
    
    private final ExecuteQueryAction executeQueryAction = new ExecuteQueryAction();

    class ExecuteQueryAction extends AbstractAction {
        
        public ExecuteQueryAction() {

            super("Execute");
            putValue(Action.ACCELERATOR_KEY, EXECUTE_KEYSTROKE);
        }
        
        public void actionPerformed(ActionEvent e) {

            executeAndExport();
        }

    } // ExecuteQueryAction

    private String instructionNote() {

        try {

            return FileUtils.loadResource(
                    "org/executequery/gui/resource/exportResultSetInstruction.html");

        } catch (IOException e) {

            if (Log.isDebugEnabled()) {
                
                Log.debug("Error loading export result set instruction note", e);
            }

        }

        return "Enter the SQL SELECT query below";
    }

}






