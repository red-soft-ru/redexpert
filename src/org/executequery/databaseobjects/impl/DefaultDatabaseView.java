/*
 * DefaultDatabaseView.java
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

package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.DatabaseView;
import org.executequery.sql.StatementGenerator;
import org.underworldlabs.jdbc.DataSourceException;

public class DefaultDatabaseView extends DefaultDatabaseObject implements DatabaseView {

    public DefaultDatabaseView(DatabaseObject object) {

        this(object.getHost());

        setCatalogName(object.getCatalogName());
        setSchemaName(object.getSchemaName());
        setName(object.getName());
        setRemarks(object.getRemarks());
    }

    public DefaultDatabaseView(DatabaseHost host) {

        super(host, "VIEW");
    }

    public String getCreateSQLText() throws DataSourceException {
        
        StatementGenerator statementGenerator = createStatementGenerator();
        return statementGenerator.viewDefinition(databaseProductName(), this);
    }

    @Override
    public boolean hasSQLDefinition() {

        return true;
    }
    
}



