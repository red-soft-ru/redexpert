/*
 * ImportExportPanelThree.java
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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.gui.DefaultTable;


/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ImportExportPanelThree extends AbstractImportExportPanel {

    private JTable table;
    
    private FilePerTableTableModel filePerTableTableModel;

    private SingleFileForTablesTableModel singleFileForTablesTableModel;
    
    public ImportExportPanelThree(ImportExportWizard importExportWizard) {
        
        super(new GridBagLayout(), importExportWizard);
        init();
    }

    private void init() {

        table = new DefaultTable();
        table.setRowHeight(23);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scroller = new JScrollPane(table);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel(bundledString("ImportExportPanelThree.label")), gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 1;
        gbc.insets.top = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(scroller, gbc);

    }

    private BrowseButtonRenderer browseButtonCellRenderer;

    private BrowseButtonEditor browseButtonCellEditor;

    private void prepareTable() {
        TableColumnModel tcm = table.getColumnModel();
        TableColumn col = tcm.getColumn(0);
        col.setPreferredWidth(140);
        
        col = tcm.getColumn(1);
        col.setPreferredWidth(255);

        if (browseButtonCellRenderer == null) {
         
            browseButtonCellRenderer = new BrowseButtonRenderer();
        }

        if (browseButtonCellEditor == null) {
            
            browseButtonCellEditor = new BrowseButtonEditor(new JCheckBox());            
        }
        
        col = tcm.getColumn(2);
        col.setCellRenderer(browseButtonCellRenderer);
        col.setCellEditor(browseButtonCellEditor);
        col.setPreferredWidth(80);
    }

    public boolean hasSelections() {

        if (isSingleFileMultiTableExport()) {

            return (singleFileForTablesTableModel.getValueAt(0, 1) != null);

        } else {
            
            return filePerTableTableModel.hasValidSelections();
        }
        
    }

    public String getSingleFileExportName() {
        
        if (singleFileForTablesTableModel != null) {

            return singleFileForTablesTableModel.getExportDataFilePath();
        }

        return null;
    }
    
    public void panelSelected() {

        if (isSingleFileMultiTableExport()) {
            
            initSingleFileForTablesTableModel();

        } else {

            initFilePerTableTableModel();
            filePerTableTableModel.setImportExportFileList(
                    importExportDataModel().getImportExportFiles());
        }
        
        prepareTable();
    }

    private boolean isSingleFileMultiTableExport() {
        
        return importExportDataModel().isSingleFileMultiTableExport();
    }

    private void initFilePerTableTableModel() {
        
        if (filePerTableTableModel == null) {
            
            filePerTableTableModel = new FilePerTableTableModel();
        }

        table.setModel(filePerTableTableModel);        
    }
    
    private void initSingleFileForTablesTableModel() {
     
        if (singleFileForTablesTableModel == null) {
            
            singleFileForTablesTableModel = new SingleFileForTablesTableModel();
        }

        table.setModel(singleFileForTablesTableModel);
    }
    
    private String getFileSuffix() {
        
        return importExportWizard().getFileSuffix();
    }
    
    public class BrowseButtonEditor extends DefaultCellEditor {
        
        private String lastPath;
        private JButton button;
        private boolean isPushed;
        
        private String browseButtonText;
        
        public BrowseButtonEditor(JCheckBox checkBox) {

            super(checkBox);

            button = new JButton();
            
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
            
            browseButtonText = bundledString("ImportExportPanelThree.browseButton");
        }
        
        public Component getTableCellEditorComponent(JTable table, 
                                                     Object value,
                                                     boolean isSelected, 
                                                     int row, 
                                                     int column) {

            button.setText(browseButtonText);
            isPushed = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            
            if (isPushed) {
                FileChooserDialog fileChooser = null;
                
                if (lastPath == null) {
                    fileChooser = new FileChooserDialog();
                } else {
                    fileChooser = new FileChooserDialog(lastPath);
                }

                String dialogTitle = null;
                
                if (isExport()) {

                    dialogTitle = bundledString("ImportExportPanelThree.exportFileDialogTitle");
                    
                } else {
                    
                    dialogTitle = bundledString("ImportExportPanelThree.importFileDialogTitle");
                }
                
                fileChooser.setDialogTitle(dialogTitle);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

                int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");                
                if (result == JFileChooser.CANCEL_OPTION) {
                    return "";
                }

                File file = fileChooser.getSelectedFile();
                if (!isExport()) {

                    if (file == null || (!file.exists() && !file.isFile())) {

                        GUIUtilities.displayErrorMessage(
                                bundledString("ImportExportPanelThree.invalidFileName"));
                    }
                    
                } else {

                    String fileName = file.getName();
                    String suffix = findExtension(fileName);
                    
                    if (StringUtils.isBlank(suffix)) {
                        
                        file = new File(file.getParent(), fileName + "." + getFileSuffix());
                    }
                        
                }

                lastPath = file.getParent();
                
                if (isSingleFileMultiTableExport()) {
                    
                    singleFileForTablesTableModel.setValueAt(
                            file, table.getEditingRow(), 1);
                    
                } else {

                    filePerTableTableModel.setDataFile(table.getEditingRow(), file);
                }

            }
            
            isPushed = false;
            return "";
        }
        
        private String findExtension(String fileName) {
            int index = fileName.lastIndexOf('.');
            if (index != -1) {
                return fileName.substring(index);
            }
            return null;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
        
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
        
    } // BrowseButtonEditor

    private static final String[] TABLE_HEADER = {"Table Name", "File Path", ""};

    /** 
     * Defines the table model for the table to file selection for the transfer. 
     */
    class FilePerTableTableModel extends AbstractTableModel {
    
        private List<ImportExportFile> data;
    
        private boolean hasValidSelections() {
            
            if (data != null && !data.isEmpty()) {
                
                for (ImportExportFile importExportFile : data) {

                    if (importExportFile.getFile() == null) {
                        
                        return false;
                    }

                }

                return true;
            }

            return false;
        }
        
        public int getRowCount() {
            
            if (data == null) {
                
                return 0;
            }
            
            return data.size();
        }
        
        public int getColumnCount() {
            return TABLE_HEADER.length;
        }
        
        public void setImportExportFileList(List<ImportExportFile> data) {
            this.data = data;
            if (table.isEditing()) {
                fireTableRowsUpdated(0, data.size());
            }
        }
        
        public void setDataFile(int row, File file) {
            ImportExportFile importExportFile = (ImportExportFile)data.get(row);
            importExportFile.setFile(file);
            fireTableRowsUpdated(row, row);
        }
        
        public Object getValueAt(int row, int col) {
            ImportExportFile importExportFile = (ImportExportFile)data.get(row);
            
            switch(col) {
                case 0:
                    return importExportFile.getDatabaseTable().getName();
                case 1:
                    
                    if (importExportFile.getFile() != null) {
                        return importExportFile.getFile().getAbsolutePath();
                    }

                    return "";

                default:
                    return null;
            }
        }
        
        public void setValueAt(Object value, int row, int col) {

            if (col == 1) {
            
                ImportExportFile importExportFile = (ImportExportFile)data.get(row);
                importExportFile.setFile(new File(value.toString()));

                fireTableRowsUpdated(row, row);                
            }
        }

        public String getColumnName(int col) {
            return TABLE_HEADER[col];
        }
        
        public boolean isCellEditable(int row, int col) {
            return (col > 0);
        }
        
    } // FilePerTableTableModel

    /** 
     * Defines the table model for the table to file selection for the transfer. 
     */
    class SingleFileForTablesTableModel extends AbstractTableModel {
    
        private File singleExportfile;

        public int getRowCount() {
            return 1;
        }
        
        public String getExportDataFilePath() {

            if (singleExportfile != null) {
                
                return singleExportfile.getAbsolutePath();
            }
            
            return null;
        }

        public int getColumnCount() {
            return TABLE_HEADER.length;
        }
        
        public Object getValueAt(int row, int col) {
            
            switch(col) {
                case 0:
                    return "ALL TABLES";
                case 1:

                    if (singleExportfile != null) {
                        return singleExportfile.getAbsolutePath();
                    }

                    return null;

                default:
                    return null;
            }
        }
        
        public void setValueAt(Object value, int row, int col) {
        
            if (col == 1) {

                if (value != null && !(value instanceof File)) {
                 
                    singleExportfile = new File(value.toString());
                    
                } else {
                    
                    singleExportfile = (File) value;                    
                }
                
                fireTableRowsUpdated(row, row);                
            }

        }
        
        public String getColumnName(int col) {
            return TABLE_HEADER[col];
        }
        
        public boolean isCellEditable(int row, int col) {
            return (col > 0);
        }
        
    } // SingleFileForTablesTableModel

    class BrowseButtonRenderer extends JButton implements TableCellRenderer {
        
        public BrowseButtonRenderer() {
            setText(bundledString("ImportExportPanelThree.browseButton"));
            setMargin(new Insets(1,1,1,1));
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }

    } // BrowseButtonRenderer

}







