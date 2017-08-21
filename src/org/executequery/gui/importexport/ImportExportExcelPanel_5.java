/*
 * ImportExportExcelPanel_5.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.executequery.gui.WidgetFactory;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1767 $
 * @date     $Date: 2017-08-16 22:26:50 +1000 (Wed, 16 Aug 2017) $
 */
public class ImportExportExcelPanel_5 extends JPanel {
    
    /** The data format text field */
    private JTextField dateFormatField;
    /** The on error combo box */
    private JComboBox errorCombo;
    /** The rollback combo box */
    private JComboBox rollbackCombo;
    /** The batch process check box */
    private JCheckBox batchCheck;
    
    /** The controlling object for this process */
    private ImportExportDataProcess parent;
    
    /** <p>Creates a new instance with the specified
     *  process as the parent.
     *
     *  @param the parent controlling the process
     */
    public ImportExportExcelPanel_5(ImportExportDataProcess parent) {
        super(new GridBagLayout());
        this.parent = parent;
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /** <p>Initialises the state of this instance and
     *  lays out components on the panel. */
    private void jbInit() throws Exception {
        JLabel rollbackLabel = new JLabel("Rollback Segment Size:");
        JLabel dateFormatLabel = new JLabel("Date Format:");
        
        dateFormatField = WidgetFactory.createTextField();
        
        String[] errors = {"Log and Continue", "Stop Transfer"};
        errorCombo = WidgetFactory.createComboBox(errors);
        
        String[] rolls = {"50", "100", "500", "1000", "5000",
        "10000", "50000", "End of File"};
        rollbackCombo = WidgetFactory.createComboBox(rolls);
        rollbackCombo.setSelectedIndex(2);
        
        batchCheck = new JCheckBox("Run as a batch process");
        
        Dimension comboDim = new Dimension(140, 20);
        errorCombo.setPreferredSize(comboDim);
        rollbackCombo.setPreferredSize(comboDim);
        
        JLabel instructLabel = new JLabel("Enter any particulars of the spreadsheet " +
                                          "files and select transfer options.");
        
        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(5,10,20,10);
        gbc.insets = ins;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 2;
        this.add(instructLabel, gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.insets.bottom = 10;
        gbc.insets.left = 20;
        this.add(new JLabel("On Error:"), gbc);
        gbc.gridx = 1;
        gbc.insets.left = 0;
        this.add(errorCombo, gbc);
        gbc.gridy = 2;
        this.add(rollbackCombo, gbc);
        gbc.gridx = 0;
        gbc.insets.left = 20;
        this.add(rollbackLabel, gbc);
        gbc.gridy = 3;
        this.add(dateFormatLabel, gbc);
        gbc.gridx = 1;
        gbc.insets.left = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(dateFormatField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets.left = 20;
        this.add(batchCheck, gbc);
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.gridy = 5;
        gbc.insets.left = 10;
        gbc.insets.top = 15;
        this.add(new JLabel("Select the NEXT button below to begin the process."), gbc);
        
        int type = parent.getTransferType();
        
        if (type == ImportExportDataProcess.EXPORT) {
            dateFormatField.setOpaque(false);
            dateFormatField.setEnabled(false);
            dateFormatLabel.setEnabled(false);
            rollbackCombo.setOpaque(false);
            rollbackCombo.setEnabled(false);
            rollbackLabel.setEnabled(false);
            batchCheck.setEnabled(false);
        }
        
    }
    
    /** <p>Retrieves the selected rollback size for
     *  the transfer.
     *
     *  @return the rollback size
     */
    public int getRollbackSize() {
        if (!rollbackCombo.isEnabled() || rollbackCombo.getSelectedIndex() == 7)
            return -1;
        else
            return Integer.parseInt((String)rollbackCombo.getSelectedItem());
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
        return dateFormatField.getText();
    }
    
}














