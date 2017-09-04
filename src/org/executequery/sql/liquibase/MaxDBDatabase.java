/*
 * MaxDBDatabase.java
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

package org.executequery.sql.liquibase;

import java.util.HashSet;
import java.util.Set;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.View;

/* liquibase moved this out of core after 3.0.8 */

public class MaxDBDatabase extends AbstractJdbcDatabase {

    public static final String PRODUCT_NAME = "SAP DB";
    protected Set<String> systemTablesAndViews = new HashSet<String>();

    public MaxDBDatabase() {
        super.setCurrentDateTimeFunction("TIMESTAMP");
        super.sequenceNextValueFunction="%s.NEXTVAL";
        systemTablesAndViews.add("---");

        systemTablesAndViews.add("ACTIVECONFIGURATION");
        systemTablesAndViews.add("BACKUPTHREADS");
        systemTablesAndViews.add("CACHESTATISTICS");
        systemTablesAndViews.add("CATALOGCACHESTATISTICS");
        systemTablesAndViews.add("COMMANDCACHESTATISTICS");
        systemTablesAndViews.add("COMMANDCACHESTATISTICSRESET");
        systemTablesAndViews.add("COMMANDSTATISTICS");
        systemTablesAndViews.add("COMMANDSTATISTICSRESET");
        systemTablesAndViews.add("CONSISTENTVIEWS");
        systemTablesAndViews.add("DATACACHE");
        systemTablesAndViews.add("DATASTATISTICS");
        systemTablesAndViews.add("DATASTATISTICSRESET");
        systemTablesAndViews.add("DATAVOLUMES");
        systemTablesAndViews.add("DATASTATISTICSRESET");
        systemTablesAndViews.add("FILEDIRECTORIES");
        systemTablesAndViews.add("FILES");
        systemTablesAndViews.add("HOTSTANDBYCOMPONENT");
        systemTablesAndViews.add("HOTSTANDBYGROUP");
        systemTablesAndViews.add("INSTANCE");
        systemTablesAndViews.add("IOBUFFERCACHES");
        systemTablesAndViews.add("IOTHREADSTATISTICS");
        systemTablesAndViews.add("IOTHREADSTATISTICSRESET");
        systemTablesAndViews.add("INDEXSIZE");
        systemTablesAndViews.add("INDEXSTORAGEDETAILS");
        systemTablesAndViews.add("LOGINFORMATION");
        systemTablesAndViews.add("LOGQUEUESTATISTICS");
        systemTablesAndViews.add("LOGQUEUESTATISTICSRESET");
        systemTablesAndViews.add("LOGSTATISTICS");
        systemTablesAndViews.add("LOGSTATISTICSRESET");
        systemTablesAndViews.add("LOGVOLUMES");
        systemTablesAndViews.add("MACHINECONFIGURATION");
        systemTablesAndViews.add("MACHINEUTILIZATION");
        systemTablesAndViews.add("MEMORYALLOCATORSTATISTICS");
        systemTablesAndViews.add("OPTIMIZERINFORMATION");
        systemTablesAndViews.add("READERWRITERLOCKINFORMATION");
        systemTablesAndViews.add("READERWRITERLOCKSTATISTICS");
        systemTablesAndViews.add("READERWRITERLOCKSTATISTICSRESET");
        systemTablesAndViews.add("READERWRITERLOCKWAITINGTASKS");
        systemTablesAndViews.add("REGIONINFORMATION");
        systemTablesAndViews.add("REGIONSTATISTICS");
        systemTablesAndViews.add("REGIONSTATISTICSRESET");
        systemTablesAndViews.add("RESTARTINFORMATION");
        systemTablesAndViews.add("SCHEMASIZE");
        systemTablesAndViews.add("SERVERTASKS");
        systemTablesAndViews.add("SESSIONS");
        systemTablesAndViews.add("SNAPSHOTS");
        systemTablesAndViews.add("SPINLOCKPOOLSTATISTICS");
        systemTablesAndViews.add("SPINLOCKPOOLSTATISTICSRESET");
        systemTablesAndViews.add("SPINLOCKSTATISTICS");
        systemTablesAndViews.add("SPINLOCKSTATISTICSRESET");
        systemTablesAndViews.add("TABLESIZE");
        systemTablesAndViews.add("TABLESTORAGEDETAILS");
        systemTablesAndViews.add("TASKGROUPSTATISTICS");
        systemTablesAndViews.add("TASKGROUPSTATISTICSRESET");
        systemTablesAndViews.add("TASKLOADBALANCINGINFORMATION");
        systemTablesAndViews.add("TASKLOADBALANCINGTASKGROUPSTATES");
        systemTablesAndViews.add("TASKLOADBALANCINGTASKMOVES");
        systemTablesAndViews.add("TRANSACTIONHISTORY");
        systemTablesAndViews.add("TRANSACTIONS");
        systemTablesAndViews.add("UNLOADEDSTATEMENTS");
        systemTablesAndViews.add("VERSION");
        super.sequenceNextValueFunction = "%s.NEXTVAL";
        super.sequenceCurrentValueFunction = "%s.CURRVAL";
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    public String getShortName() {
        return "maxdb";
    }

    public Integer getDefaultPort() {
        return 7210;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return PRODUCT_NAME;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public String getsDefaultDriver(String url) {
        if (url.startsWith("jdbc:sapdb")) {
            return "com.sap.dbtech.jdbc.DriverSapDB";
        }
        return null;
    }


    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if (super.isSystemObject(example)) {
            return true;
        }
        if (example instanceof View && example.getSchema() != null) {

            if ("DOMAIN".equalsIgnoreCase(example.getSchema().getName())) {
                return true;
            } else if ("SYSINFO".equalsIgnoreCase(example.getSchema().getName())) {
                return true;
            } else if ("SYSLOADER".equalsIgnoreCase(example.getSchema().getName())) {
                return true;
            } else if ("SYSDBA".equalsIgnoreCase(example.getSchema().getName())) {
                return true;
            }

        }
        return false;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sapdb")) {
            return "com.sap.dbtech.jdbc.DriverSapDB";
        }
        return null;
    }

}


