/*
 * ImportExportDelimitedPanel_4.java
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

package org.executequery.gui.importexport;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.executequery.gui.WidgetFactory;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1784 $
 * @date     $Date: 2017-09-19 00:55:31 +1000 (Tue, 19 Sep 2017) $
 */
public class ImportExportDelimitedPanel_4 extends JPanel 
                                          implements ActionListener {
    
    /** The delimiter combo box */
    private JComboBox delimCombo;
    
    /** The on error combo box */
    private JComboBox errorCombo;
    
    /** The rollback combo box */
    private JComboBox rollbackCombo;

    private JCheckBox applyQuotesCheck;
    
    /** The include column names as first row check box */
    private JCheckBox columnNamesFirstRow;
    
    /** The batch process check box */
    private JCheckBox batchCheck;

    /** The whitespace trim check box */
    private JCheckBox trimCheck;
    
    /** the date parsing selection panel */
    private ParseDateSelectionPanel dateFormatPanel;
    
    /** The controlling object for this process */
    private ImportExportDataProcess parent;
    
    /** <p>Creates a new instance with the specified
     *  process as the parent.
     *
     *  @param the parent controlling the process
     */
    public ImportExportDelimitedPanel_4(ImportExportDataProcess parent) {
        super(new GridBagLayout());
        this.parent = parent;
        
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /** <p>Initialises the state of this instance and
     *  lays out components on the panel. */
    private void init() throws Exception {

        JLabel rollbackLabel = new JLabel("Rollback Segment Size:");
        
        String[] delims = {"|",",",";","#"};
        delimCombo = WidgetFactory.createComboBox(delims);
        delimCombo.setEditable(true);
        
        String[] errors = {"Log and Continue", "Stop Transfer"};
        errorCombo = WidgetFactory.createComboBox(errors);

        String[] rolls = {"50", "100", "500", "1000", "5000",
                          "10000", "50000", "End of File", "End of all Files"};

        rollbackCombo = WidgetFactory.createComboBox(rolls);
        rollbackCombo.setSelectedIndex(2);
        rollbackCombo.addActionListener(this);
        
        batchCheck = new JCheckBox("Run as a batch process");
        trimCheck = new JCheckBox("Trim whitespace");
        applyQuotesCheck = new JCheckBox("Use double quotes for char/varchar/longvarchar columns", true);
        columnNamesFirstRow = new JCheckBox("Column names as first row");
        
        Dimension comboDim = new Dimension(140, 20);
        delimCombo.setPreferredSize(comboDim);
        errorCombo.setPreferredSize(comboDim);
        rollbackCombo.setPreferredSize(comboDim);
        
        JLabel instructLabel = new JLabel("Enter any particulars of the data files " +
                                          "and select transfer options.");
        
        dateFormatPanel = new ParseDateSelectionPanel(parent);
        
        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(5,10,10,10);
        gbc.insets = ins;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 2;
        gbc.gridy++;
        add(instructLabel, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.insets.bottom = 10;
        gbc.insets.left = 20;
        add(new JLabel("Delimeter:"), gbc);
        gbc.gridx = 1;
        gbc.insets.left = 0;
        add(delimCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.left = 20;
        add(new JLabel("On Error:"), gbc);
        gbc.gridx = 1;
        gbc.insets.left = 0;
        add(errorCombo, gbc);
        gbc.gridy++;
        add(rollbackCombo, gbc);
        gbc.gridx = 0;
        gbc.insets.left = 20;
        add(rollbackLabel, gbc);
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(dateFormatPanel, gbc);
        
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets.left = 20;
        add(trimCheck, gbc);
        gbc.gridy++;
        add(batchCheck, gbc);
        gbc.gridy++;
        add(columnNamesFirstRow, gbc);
        gbc.gridy++;
        add(applyQuotesCheck, gbc);
        gbc.weighty = 1;
        gbc.gridy++;
        gbc.insets.left = 10;
        gbc.insets.top = 5;
        add(new JLabel("Select the NEXT button below to begin the process."), gbc);
        
        int type = parent.getTransferType();
        
        if (type == ImportExportDataProcess.EXPORT) {
//            dateFormatPanel.setEnabled(false);
            //dateFormatField.setOpaque(false);
            //dateFormatField.setEnabled(false);
            //dateFormatLabel.setEnabled(false);
            rollbackCombo.setOpaque(false);
            rollbackCombo.setEnabled(false);
            rollbackLabel.setEnabled(false);
            batchCheck.setEnabled(false);
        }
        
    }
    
    public void actionPerformed(ActionEvent e) {
        if (rollbackCombo.getSelectedIndex() == 8) {
            batchCheck.setEnabled(false);
        } else {
            batchCheck.setEnabled(true);
        }
    }
    
    /** <p>Retrieves the selected rollback size for
     *  the transfer.
     *
     *  @return the rollback size
     */
    public int getRollbackSize() {
        if (!rollbackCombo.isEnabled()) {
            return -1;
        }

        int index = rollbackCombo.getSelectedIndex();
        if (index == 7) {
            return ImportExportDataProcess.COMMIT_END_OF_FILE;
        } 
        else if (index == 8) {
            return ImportExportDataProcess.COMMIT_END_OF_ALL_FILES;
        }
        else {
            return Integer.parseInt((String)rollbackCombo.getSelectedItem());
        }
    }
    
    /** <p>Retrieves the action on an error occuring
     *  during the import/export process.
     *
     *  @return the action on error -<br>either:
     *          <code>ImportExportProcess.LOG_AND_CONTINUE</code> or
     *          <code>ImportExportProcess.STOP_TRANSFER</code>
     */
    public int getOnError() {
        if (errorCombo.getSelectedIndex() == 0)
            return ImportExportDataProcess.LOG_AND_CONTINUE;
        else
            return ImportExportDataProcess.STOP_TRANSFER;
    }
    
    /** <p>Retrieves the selected type of delimiter within
     *  the file to be used with this process.
     *
     *  @return the selected delimiter
     */
    public String getDelimiter() {
        return delimCombo.getSelectedItem().toString();        
    }
    
    public boolean quoteCharacterValues() {
        return applyQuotesCheck.isSelected();
    }
    
    public boolean includeColumnNames() {
        return columnNamesFirstRow.isSelected();
    }
    
    public boolean trimWhitespace() {
        return trimCheck.isSelected();
    }
    
    /** <p>Indicates whether the process (import only)
     *  should be run as a batch process.
     *
     *  @return whether to run as a batch process
     */
    public boolean runAsBatchProcess() {
        return batchCheck.isSelected();
    }
    
    /** <p>Retrieves the date format for date fields
     *  contained within the data file/database table.
     *
     *  @return the date format (ie. ddMMyyy)
     */
    public String getDateFormat() {
        return dateFormatPanel.getDateFormat();
        //return dateFormatField.getText();
    }
    
    /**
     * Returns whether to parse date values.
     *
     * @return true | false
     */
    public boolean parseDateValues() {
        return dateFormatPanel.parseDates();
    }
    
}





