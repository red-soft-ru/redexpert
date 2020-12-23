/*
 * DerivedQuery.java
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

package org.executequery.sql;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.QueryTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DerivedQuery {

    private static DerivedTableStrategy defaultDerivedTableStrategy;

    private static Map<Integer, DerivedTableStrategy> derivedTableStrategies;

    private String derivedQuery;

    private final String originalQuery;

    private String queryWithoutComments;

    private List<QueryTable> queryTables;

    static {

        defaultDerivedTableStrategy = new DefaultDerivedTableStrategy();
        derivedTableStrategies = new HashMap<Integer, DerivedTableStrategy>();
        derivedTableStrategies.put(QueryTypes.SELECT, new SelectDerivedTableStrategy());
        derivedTableStrategies.put(QueryTypes.INSERT, new InsertDerivedTableStrategy());
        derivedTableStrategies.put(QueryTypes.UPDATE, new UpdateDerivedTableStrategy());
        derivedTableStrategies.put(QueryTypes.DELETE, new DeleteDerivedTableStrategy());
        derivedTableStrategies.put(QueryTypes.DROP_TABLE, new DropTableDerivedTableStrategy());
        derivedTableStrategies.put(QueryTypes.ALTER_TABLE, new AlterTableDerivedTableStrategy());
    }

    public DerivedQuery(String originalQuery) {
        super();
        this.originalQuery = originalQuery;
        this.derivedQuery = originalQuery;
    }

    public DerivedQuery(String originalQuery, String queryWithoutComments) {
        this(originalQuery);
        this.queryWithoutComments = queryWithoutComments;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public String getLoggingQuery() {

        if (derivedQuery.length() > 50) {

            return derivedQuery.substring(0, 50) + " ... ";
        }

        return derivedQuery;
    }

    public String getDerivedQuery() {
        return derivedQuery;
    }

    public void setDerivedQuery(String derivedQuery) {

        String query = derivedQuery.replaceAll("\t", " ");

        if (query.endsWith(";")) {

            query = query.substring(0, query.length() - 1);
        }

        this.derivedQuery = query;
    }

    public boolean isExecutable() {

        return StringUtils.isNotBlank(getDerivedQuery());
    }

    private void deriveTables() {

        if (queryTables != null) {

            return;
        }

        int queryType = getQueryType();
        DerivedTableStrategy derivedTableStrategy = derivedTableStrategies.get(queryType);
        if (derivedTableStrategy == null) {

            derivedTableStrategy = defaultDerivedTableStrategy;
        }

        queryTables = derivedTableStrategy.deriveTables(derivedQuery);
    }

    public List<QueryTable> tableForWord(String word) {

        deriveTables();

        int dotPoint = word.indexOf('.');
        if (dotPoint == -1) {

            return queryTables;
        }

        String pattern = word.substring(0, dotPoint);
        return asList(getTableForNameOrAlias(pattern));
    }

    private List<QueryTable> asList(QueryTable queryTable) {

        List<QueryTable> list = new ArrayList<QueryTable>(1);
        if (queryTable != null) {

            list.add(queryTable);
        }

        return list;
    }

    public QueryTable getTableForNameOrAlias(String nameOrAlias) {

        deriveTables();
        if (!queryTables.isEmpty()) {

            for (QueryTable queryTable : queryTables) {

                if (queryTable.isNameOrAlias(nameOrAlias)) {

                    return queryTable;
                }

            }
        }

        return null;
    }

    int type = -1;
    public int getQueryType() {

        if (type != -1)
            return type;
        if (queryWithoutComments == null)
            queryWithoutComments = derivedQuery;
        String query = queryWithoutComments.replaceAll("\n", " ").toUpperCase().trim();

        if (query.indexOf("SELECT ") == 0 && query.contains(" INTO ")) {

            type = QueryTypes.SELECT_INTO;

        } else if (query.indexOf("SELECT ") == 0) {

            type = QueryTypes.SELECT;

        } else if (query.indexOf("INSERT ") == 0) {

            type = QueryTypes.INSERT;

        } else if (query.indexOf("UPDATE ") == 0) {

            type = QueryTypes.UPDATE;

        } else if (query.indexOf("DELETE ") == 0) {

            type = QueryTypes.DELETE;

        } else if (query.indexOf("CREATE TABLE ") == 0) {

            type = QueryTypes.CREATE_TABLE;

        } else if (query.indexOf("CREATE TRIGGER") == 0 || query.indexOf("CREATE OR ALTER TRIGGER") == 0) {

            type = QueryTypes.CREATE_TRIGGER;

        } else if (query.indexOf("CREATE ") == 0 && (query.indexOf("PROCEDURE ") != -1 &&
                query.indexOf("PACKAGE ") == -1)) {

            type = QueryTypes.CREATE_PROCEDURE;

        } else if (query.indexOf("CREATE ") == 0 && query.indexOf("FUNCTION ") != -1) {

            type = QueryTypes.CREATE_FUNCTION;

        } else if (query.indexOf("DROP TABLE ") == 0) {

            type = QueryTypes.DROP_TABLE;

        } else if (query.indexOf("ALTER TABLE ") == 0) {

            type = QueryTypes.ALTER_TABLE;

        } else if (query.indexOf("CREATE SEQUENCE ") == 0) {

            type = QueryTypes.CREATE_SEQUENCE;

        } else if (query.indexOf("CREATE SYNONYM ") == 0) {

            type = QueryTypes.CREATE_SYNONYM;

        } else if (query.indexOf("GRANT ") == 0) {

            type = QueryTypes.GRANT;

        } else if (query.indexOf("EXECUTE ") == 0) {

            type = QueryTypes.EXECUTE;

        } else if (query.indexOf("CALL ") == 0) {

            type = QueryTypes.CALL;

        } else if (query.indexOf("COMMIT") == 0) {

            type = QueryTypes.COMMIT;

        } else if (query.indexOf("ROLLBACK") == 0 && query.indexOf("ROLLBACK TO") == -1) {

            type = QueryTypes.ROLLBACK;

        } else if (query.indexOf("EXPLAIN ") == 0) {

            type = QueryTypes.EXPLAIN;

        } else if (query.indexOf("DESC ") == 0 || query.indexOf("DESCRIBE ") == 0) {

            type = QueryTypes.DESCRIBE;

        } else if (query.indexOf("SHOW TABLES") == 0) {

            type = QueryTypes.SHOW_TABLES;

        } else if (query.indexOf("CREATE ROLE ") == 0) {

            type = QueryTypes.CREATE_ROLE;

        } else if (query.indexOf("DROP ") == 0) {

            type = QueryTypes.DROP_OBJECT;

        } else if (query.indexOf("REVOKE ") == 0) {

            type = QueryTypes.REVOKE;

        } else if (query.indexOf("COMMENT") == 0) {

            type = QueryTypes.COMMENT;

        } else if (query.indexOf("CREATE DATABASE") == 0) {

            type = QueryTypes.CREATE_DATABASE;

        } else if (query.indexOf("CREATE OR ALTER") == 0) {

            type = QueryTypes.CREATE_OR_ALTER;

        } else if (query.indexOf("CREATE") == 0) {

            type = QueryTypes.CREATE_OBJECT;

        } else if (query.indexOf("RECREATE") == 0) {

            type = QueryTypes.RECREATE_OBJECT;

        } else if (query.indexOf("ALTER") == 0) {

            type = QueryTypes.ALTER_OBJECT;

        } else if (query.indexOf("SET SQL DIALECT") == 0) {

            type = QueryTypes.SQL_DIALECT;

        } else if (query.indexOf("SET AUTODDL ON") == 0) {

            type = QueryTypes.SET_AUTODDL_ON;

        } else if (query.indexOf("SET AUTODDL OFF") == 0) {

            type = QueryTypes.SET_AUTODDL_OFF;

        } else {

            type = QueryTypes.UNKNOWN;
        }

        return type;
    }

    public String getQueryWithoutComments() {
        return queryWithoutComments;
    }

    public void setQueryWithoutComments(String queryWithoutComments) {
        this.queryWithoutComments = queryWithoutComments;
    }
}


