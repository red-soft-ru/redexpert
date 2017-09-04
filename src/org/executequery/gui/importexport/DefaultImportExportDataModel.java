/*
 * DefaultImportExportDataModel.java
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

import java.util.ArrayList;
import java.util.List;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseSource;
import org.executequery.databaseobjects.DatabaseTable;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class DefaultImportExportDataModel implements ImportExportDataModel {

    private String singleFileExport;
    
    private DatabaseHost databaseHost;
    
    private DatabaseSource databaseSource;
    
    private List<DatabaseTable> databaseTables;
    
    private List<DatabaseColumn> databaseTableColumns;
    
    private ImportExportType importExportType;

    private ImportExportFileType importExportFileType;

    private List<ImportExportFile> importExportFiles;
    
    private OnErrorOption onErrorOption;
    
    private boolean hostSelectionChanged;
    
    private boolean importExportTypeChanged;
    
    public boolean isMultipleTableImportExport() {
        return ImportExportType.isMultipleTableImportExport(importExportType);
    }

    public DatabaseHost getDatabaseHost() {
        return databaseHost;
    }

    public void setDatabaseHost(DatabaseHost databaseHost) {
        
        hostSelectionChanged = (this.databaseHost != databaseHost);

        this.databaseHost = databaseHost;
    }

    public DatabaseSource getDatabaseSource() {
        return databaseSource;
    }

    public void setDatabaseSource(DatabaseSource databaseSource) {     
        this.databaseSource = databaseSource;
    }

    public List<DatabaseTable> getDatabaseTables() {
        return databaseTables;
    }

    public void setDatabaseTables(List<DatabaseTable> databaseTables) {
        this.databaseTables = databaseTables;
    }

    public ImportExportFile getImportExportFileForTable(DatabaseTable databaseTable) {
        
        if (isSingleFileMultiTableExport()) {
            
            ImportExportFile importExportFile = new ImportExportFile(databaseTable);
            importExportFile.setFile(getSingleFileExport());
            importExportFile.setDatabaseTableColumns(databaseTableColumns);
            
            return importExportFile;
        }

        for (ImportExportFile importExportFile : importExportFiles) {
            
            if (importExportFile.getDatabaseTable() == databaseTable) {
                
                return importExportFile;
            }
            
        }

        return null;
    }
    
    public List<ImportExportFile> getImportExportFiles() {
        
        if (importExportFiles == null) {
            
            importExportFiles = new ArrayList<ImportExportFile>();
        }

        if (isSingleFileMultiTableExport()) {

            importExportFiles.clear();
            
            ImportExportFile importExportFile = new ImportExportFile(
                    databaseTables.get(0), databaseTableColumns);
            importExportFile.setFile(singleFileExport);
            importExportFiles.add(importExportFile);

            return importExportFiles;
        }
        
        boolean addTable;
        for (DatabaseTable databaseTable : databaseTables) {

            addTable = true;
            
            for (ImportExportFile importExportFile : importExportFiles) {

                if (importExportFile.getDatabaseTable() == databaseTable) {
                    addTable = false;
                    break;
                }
                
            }

            if (addTable) {

                ImportExportFile importExportFile = new ImportExportFile(databaseTable);                
                if (!isMultipleTableImportExport()) {
                
                    importExportFile.setDatabaseTableColumns(databaseTableColumns);
                }
                
                importExportFiles.add(importExportFile);
            }

        }

        boolean removeTable;
        List<ImportExportFile> toBeRemoved = new ArrayList<ImportExportFile>();

        for (ImportExportFile importExportFile : importExportFiles) {
            
            removeTable = true;            
            DatabaseTable databaseTableFromImportExportFile = importExportFile.getDatabaseTable();
            
            for (DatabaseTable databaseTable : databaseTables) {
                
                if (databaseTable == databaseTableFromImportExportFile) {
                    removeTable = false;
                    break;
                    
                }
                
            }

            if (removeTable) {
                toBeRemoved.add(importExportFile);
            }

        }
        
        if (!toBeRemoved.isEmpty()) {
            importExportFiles.removeAll(toBeRemoved);
        }
        
        return importExportFiles;
    }

    public List<DatabaseColumn> getDatabaseTableColumns() {
        return databaseTableColumns;
    }

    public void setDatabaseTableColumns(List<DatabaseColumn> databaseTableColumns) {
        this.databaseTableColumns = databaseTableColumns;
    }

    public ImportExportType getImportExportType() {
        return importExportType;
    }

    public void setImportExportType(ImportExportType importExportType) {

        importExportTypeChanged = (this.importExportType != importExportType);
        
        this.importExportType = importExportType;
    }

    public boolean isHostSelectionChanged() {
        return hostSelectionChanged;
    }

    public ImportExportFileType getImportExportFileType() {
        return importExportFileType;
    }

    public void setImportExportFileType(ImportExportFileType importExportFileType) {
        this.importExportFileType = importExportFileType;
    }

    public void setSingleFileExport(String singleFileExport) {
        this.singleFileExport = singleFileExport;
    }

    public String getSingleFileExport() {
        return singleFileExport;
    }

    public void setOnErrorOption(OnErrorOption onErrorOption) {
        this.onErrorOption = onErrorOption;
    }

    public OnErrorOption getOnErrorOption() {
        return onErrorOption;
    }

    public boolean isImportExportTypeChanged() {
        return importExportTypeChanged;
    }

    public boolean isSingleFileMultiTableExport() {
        return (isMultipleTableImportExport() 
                && getImportExportFileType() == ImportExportFileType.SINGLE_FILE);
    }
    
    public boolean isExport() {
        return ImportExportType.isDataExport(getImportExportType());
    }
    
}







