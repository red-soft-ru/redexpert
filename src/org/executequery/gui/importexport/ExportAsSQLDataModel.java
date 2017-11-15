/*
 * ExportAsSQLDataModel.java
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

import org.executequery.databaseobjects.DatabaseHost;

/** 
 *
 * @author   Takis Diakoumis
 */
public class ExportAsSQLDataModel extends DefaultImportExportDataModel {

    private boolean includeCreateTableStatements;
    private boolean includePrimaryKeyConstraints;
    private boolean includeForeignKeyConstraints;
    private boolean includeUniqueKeyConstraints;
    
    public ExportAsSQLDataModel(DatabaseHost databaseHost) {
        super();
        setDatabaseHost(databaseHost);
    }

    public void setIncludeCreateTableStatements(boolean includeCreateTableStatements) {
        this.includeCreateTableStatements = includeCreateTableStatements;
    }

    public boolean includeCreateTableStatements() {
        return includeCreateTableStatements;
    }
    
    public boolean includePrimaryKeyConstraints() {
        return includePrimaryKeyConstraints;
    }
    
    public boolean includeForeignKeyConstraints() {
        return includeForeignKeyConstraints;
    }

    public void setIncludePrimaryKeyConstraints(boolean includePrimaryKeyConstraints) {
        this.includePrimaryKeyConstraints = includePrimaryKeyConstraints;
    }

    public void setIncludeForeignKeyConstraints(boolean includeForeignKeyConstraints) {
        this.includeForeignKeyConstraints = includeForeignKeyConstraints;
    }

    public void setIncludeUniqueKeyConstraints(boolean includeUniqueKeyConstraints) {
        this.includeUniqueKeyConstraints = includeUniqueKeyConstraints;
    }

    public boolean includeUniqueKeyConstraints() {
        return includeUniqueKeyConstraints;
    }

}







