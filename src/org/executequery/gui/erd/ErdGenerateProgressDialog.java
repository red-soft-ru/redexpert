/*
 * ErdGenerateProgressDialog.java
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

package org.executequery.gui.erd;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.gui.GenerateErdPanel;
import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.util.SwingWorker;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class ErdGenerateProgressDialog extends AbstractBaseDialog {
    
    /** The ERD parent panel */
    private ErdViewerPanel parent;
    
    /** The generate progress bar */
    private JProgressBar progressBar;
    
    /** The selected table names */
    private Vector selectedTables;
    
    /** Utility to retrieve column data */
    private MetaDataValues metaData;
    
    /** Worker thread for process */
    private SwingWorker worker;
    
    /** The process cancel button */
    private JButton cancelButton;
    
    /** The schema name */
    private String schema;
    
    /** the connection props object */
    private DatabaseConnection databaseConnection;
    
    public ErdGenerateProgressDialog(DatabaseConnection databaseConnection,
                                     Vector selectedTables, String schema) {
        
        super(GUIUtilities.getParentFrame(), "Progress", false);
        
        this.databaseConnection = databaseConnection;
        this.schema = schema;
        this.selectedTables = selectedTables;
        
        //GUIUtilities.setFrameIconified(GenerateErdPanel.TITLE, true);
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        display();
        
    }
    
    public ErdGenerateProgressDialog(Vector selectedTables,
                                     ErdViewerPanel parent, String schema) {
        
        super(GUIUtilities.getParentFrame(), "Adding Tables", false);
        this.schema = schema;
        
        databaseConnection = parent.getDatabaseConnection();        
        this.selectedTables = selectedTables;
        this.parent = parent;
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        display();
    }
    
    private void display() {
        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        setVisible(true);
    }
    
    private void jbInit() {
        JPanel base = new JPanel(new GridBagLayout());
        
        cancelButton = new JButton(Bundles.get("common.cancel.button"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                worker.interrupt(); }
        });
        
        progressBar = new JProgressBar(0, selectedTables.size() * 2);
        progressBar.setPreferredSize(new Dimension(250, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        base.add(new JLabel("Generating ERD..."), gbc);
        gbc.gridy = 1;
        gbc.insets.top = 0;
        base.add(progressBar, gbc);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        base.add(cancelButton, gbc);
        
        base.setBorder(BorderFactory.createEtchedBorder());
        
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        c.add(base, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                                GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                                new Insets(5, 5, 5, 5), 0, 0));
        
        setResizable(false);
        
        metaData = new MetaDataValues(databaseConnection, true);
        
        worker = new SwingWorker() {
            public Object construct() {
                return doWork();
            }
            public void finished() {
                processComplete((Vector)get());
                GUIUtilities.scheduleGC();
            }
        };
        worker.start();
        
    }
    
    private Object doWork() {
        
        int v_size = selectedTables.size();
        
        Vector columnData = new Vector(v_size);
        
        try {
            
            int count = 0;
            
            for (int i = 0; i < v_size; i++) {                
                progressBar.setValue(count++);
                
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                try {
                    columnData.add(metaData.getColumnMetaData(
                                          (String)selectedTables.elementAt(i), schema));
                }
                catch (DataSourceException e) {
                    columnData.add(new ColumnData[0]);
                }
                
                progressBar.setValue(count++);
            }
            
        }
        catch (InterruptedException intExc) {
            GUIUtilities.displayWarningMessage("Process cancelled");
        }        
        finally {
            progressBar.setValue(selectedTables.size() * 2);
            cancelButton.setEnabled(false);
            metaData.closeConnection();
        }
        
        return columnData;
    }
    
    private void processComplete(Vector columnData) {
        
        if (parent == null) { // mapping a new erd
            
            if (columnData.size() != selectedTables.size()) {
                //GUIUtilities.setFrameIconified(GenerateErdPanel.TITLE, false);
                dispose();
                return;
            }
            
            GUIUtilities.showWaitCursor();
            metaData = null;
            
            ErdViewerPanel viewerPanel = 
                    new ErdViewerPanel(selectedTables, columnData, false);
            viewerPanel.setDatabaseConnection(databaseConnection);
            
            GUIUtilities.closeDialog(GenerateErdPanel.TITLE);
            //GUIUtilities.closeInternalFrame(GenerateErdPanel.TITLE);
            
            dispose();
            GUIUtilities.addCentralPane(ErdViewerPanel.TITLE,
                                        ErdViewerPanel.FRAME_ICON, 
                                        viewerPanel,
                                        null,
                                        true);
            GUIUtilities.showNormalCursor();
            selectedTables = null;
            
        } else {
            
            if (columnData.size() != selectedTables.size()) {
                dispose();
                return;
            }
            
            GUIUtilities.showWaitCursor();
            metaData = null;
            
            ErdTable table = null;
            for (int i = 0, n = selectedTables.size(); i < n; i++) {

                // create the ERD display component
                table = new ErdTable(
                        (String)selectedTables.elementAt(i),
                        (ColumnData[])columnData.elementAt(i), parent);
                table.setEditable(parent.isEditable());
                parent.addNewTable(table);                
            }
            
            parent.updateTableRelationships();
            GUIUtilities.showNormalCursor();
            selectedTables = null;
            dispose();
            
        }
        
    }
   
}





