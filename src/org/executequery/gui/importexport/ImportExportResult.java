/*
 * ImportExportResult.java
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

enum ImportExportResult {

    SUCCESS,
    CANCELLED,
    FAILED;

    public static boolean isSuccess(ImportExportResult importExportResult) {
        return importExportResult == SUCCESS;
    }
    
    public static boolean isCancelled(ImportExportResult importExportResult) {
        return importExportResult == CANCELLED;
    }
    
    public static boolean isFailed(ImportExportResult importExportResult) {
        return importExportResult == FAILED;
    }
    
}






