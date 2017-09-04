/*
 * ImportExportType.java
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

enum ImportExportType {

    EXPORT_SQL_ALL_TABLES,
    EXPORT_SQL_ONE_TABLE;
    
    public static boolean isMultipleTableImportExport(ImportExportType importExportType) {
     
        return importExportType == EXPORT_SQL_ALL_TABLES;
    }

    public static boolean isDataExport(ImportExportType importExportType) {
        
        ImportExportType[] exportTypes = {
                EXPORT_SQL_ALL_TABLES,
                EXPORT_SQL_ONE_TABLE
        };
        
        for (ImportExportType type : exportTypes) {
        
            if (importExportType == type) {
                
                return true;
            }

        }

        return false;
    }

}







