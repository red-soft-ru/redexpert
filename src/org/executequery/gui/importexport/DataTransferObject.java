/*
 * DataTransferObject.java
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

/**
 * Defines a single table row with all relevant data 
 * for the transfer - table name and path to data file.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class DataTransferObject {
    
    /** The table name */
    private String tableName;
    
    /** The path to the data file */
    private String fileName;
    
    public DataTransferObject(String tableName) {
        this.tableName = tableName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    /** <p>Setter method for the file name.
     *  @param the file name */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public boolean hasDataFile(int type) {
        
        if (type == ImportExportDataProcess.IMPORT && fileName != null) {
            
            File file = new File(fileName);
            return file.isFile() && file.exists();

        } else {

            return fileName != null && fileName.length() > 0;
        }

    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String toString() {
        return tableName;
    }
    
}









