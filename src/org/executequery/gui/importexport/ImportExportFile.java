/*
 * ImportExportFile.java
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

import java.io.File;
import java.util.List;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseTable;

/**
 *
 * @author   Takis Diakoumis
 */
public class ImportExportFile {

    private File file;
    
    private DatabaseTable databaseTable;

    private List<DatabaseColumn> databaseTableColumns;
    
    public ImportExportFile() {}
    
    public ImportExportFile(DatabaseTable databaseTable) {
        this.databaseTable = databaseTable;
    }

    public ImportExportFile(DatabaseTable databaseTable, List<DatabaseColumn> databaseTableColumns) {
        this.databaseTable = databaseTable;
        this.setDatabaseTableColumns(databaseTableColumns);
    }

    public DatabaseTable getDatabaseTable() {
        return databaseTable;
    }

    public void setDatabaseTable(DatabaseTable databaseTable) {
        this.databaseTable = databaseTable;
    }

    public boolean fileExists() {
        
        if (file != null) {
            
            return file.exists();
        }
        
        return false;
    }
    
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setFile(String file) {
        setFile(new File(file));
    }

    public void setDatabaseTableColumns(List<DatabaseColumn> databaseTableColumns) {
        this.databaseTableColumns = databaseTableColumns;
    }

    public List<DatabaseColumn> getDatabaseTableColumns() {
        return databaseTableColumns;
    }
    
    public boolean hasColumnSelections() {
        return databaseTableColumns != null && !databaseTableColumns.isEmpty();
    }
    
}


