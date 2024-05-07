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
import org.executequery.databaseobjects.NamedObject;

public final class DerivedQuery {

    private final String originalQuery;
    private final String endDelimiter;
    private final boolean isSetTerm;

    private String derivedQuery;
    private String queryWithoutComments;

    private int type;
    private int typeObject;
    private String metaName;
    private String objectName;

    public DerivedQuery(String originalQuery) {
        this(originalQuery, null, ";", false);
    }

    public DerivedQuery(String originalQuery, String queryWithoutComments, String endDelimiter, boolean isSetTerm) {
        this.originalQuery = originalQuery;
        this.derivedQuery = originalQuery;
        this.queryWithoutComments = queryWithoutComments;
        this.endDelimiter = endDelimiter;
        this.isSetTerm = isSetTerm;
        this.type = -1;
    }

    public void setDerivedQuery(String derivedQuery) {

        String query = derivedQuery.replaceAll("\t", " ");
        if (query.endsWith(";"))
            query = query.substring(0, query.length() - 1);

        this.derivedQuery = query;
    }

    private int indexSpace(String str) {
        int res = str.indexOf(" ");
        int ind = str.indexOf("\t");

        if (res == -1 || (ind != -1 && ind < res))
            res = ind;

        if (res == -1)
            res = str.length();

        return res;
    }

    private void setTypeObject(String query, String firstOperator) {

        query = query.substring(firstOperator.length()).trim();
        int indexSpace = indexSpace(query);

        metaName = query.substring(0, indexSpace).trim();
        for (int i = 0; i < NamedObject.META_TYPES.length; i++) {
            if (NamedObject.META_TYPES[i].startsWith(metaName)) {
                typeObject = i;
                metaName = NamedObject.META_TYPES[i];
                if (i == NamedObject.GLOBAL_TEMPORARY)
                    metaName = "GLOBAL TEMPORARY TABLE";
                break;
            }
        }

        query = query.substring(metaName.length()).trim();
        indexSpace = indexSpace(query);

        objectName = query.substring(0, indexSpace);
        if (objectName.startsWith("\"")) {
            query = query.substring(1);
            indexSpace = query.indexOf("\"");
            objectName = query.substring(0, indexSpace);
        }

        indexSpace = queryWithoutComments.toUpperCase().indexOf(objectName);
        objectName = indexSpace > -1 ? queryWithoutComments.substring(indexSpace, objectName.length() + indexSpace) : objectName;
    }

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

        } else if (query.indexOf("GRANT ") == 0) {
            type = QueryTypes.GRANT;

        } else if (query.indexOf("EXECUTE ") == 0) {
            type = QueryTypes.EXECUTE;

        } else if (query.indexOf("CALL ") == 0) {
            type = QueryTypes.CALL;

        } else if (query.indexOf("COMMIT") == 0) {
            type = QueryTypes.COMMIT;

        } else if (query.indexOf("ROLLBACK") == 0 && !query.contains("ROLLBACK TO")) {
            type = QueryTypes.ROLLBACK;

        } else if (query.indexOf("EXPLAIN ") == 0) {
            type = QueryTypes.EXPLAIN;

        } else if (query.indexOf("DESC ") == 0 || query.indexOf("DESCRIBE ") == 0) {
            type = QueryTypes.DESCRIBE;

        } else if (query.indexOf("SHOW TABLES") == 0) {
            type = QueryTypes.SHOW_TABLES;

        } else if (query.indexOf("DROP ") == 0) {
            type = QueryTypes.DROP_OBJECT;
            setTypeObject(query, "DROP");

        } else if (query.indexOf("REVOKE ") == 0) {
            type = QueryTypes.REVOKE;

        } else if (query.indexOf("COMMENT") == 0) {
            type = QueryTypes.COMMENT;

        } else if (query.indexOf("CREATE DATABASE") == 0) {
            type = QueryTypes.CREATE_DATABASE;

        } else if (query.indexOf("CONNECT") == 0) {
            type = QueryTypes.CONNECT;

        } else if (query.indexOf("CREATE OR ALTER") == 0) {
            type = QueryTypes.CREATE_OR_ALTER;
            setTypeObject(query, "CREATE OR ALTER");

        } else if (query.indexOf("CREATE") == 0) {
            type = QueryTypes.CREATE_OBJECT;
            setTypeObject(query, "CREATE");

        } else if (query.indexOf("RECREATE") == 0) {
            type = QueryTypes.RECREATE_OBJECT;
            setTypeObject(query, "RECREATE");

        } else if (query.indexOf("ALTER") == 0) {
            type = QueryTypes.ALTER_OBJECT;
            setTypeObject(query, "ALTER");

        } else if (query.indexOf("SET SQL DIALECT") == 0) {
            type = QueryTypes.SQL_DIALECT;

        } else if (query.indexOf("SET NAMES") == 0) {
            type = QueryTypes.SET_NAMES;

        } else if (query.indexOf("SET BLOBFILE") == 0) {
            type = QueryTypes.SET_NAMES;

        } else if (query.indexOf("SET AUTODDL ON") == 0) {
            type = QueryTypes.SET_AUTODDL_ON;

        } else if (query.indexOf("SET AUTODDL OFF") == 0) {
            type = QueryTypes.SET_AUTODDL_OFF;

        } else if (query.indexOf("SET STATISTICS") == 0) {
            type = QueryTypes.SET_STATISTICS;

        } else if (query.indexOf("DECLARE") == 0) {
            type = QueryTypes.DECLARE_OBJECT;
            setTypeObject(query, "DECLARE");

        } else
            type = QueryTypes.UNKNOWN;

        return type;
    }

    public void setQueryWithoutComments(String queryWithoutComments) {
        this.queryWithoutComments = queryWithoutComments;
    }

    public String getQueryWithoutComments() {
        return queryWithoutComments;
    }

    public int getTypeObject() {
        return typeObject;
    }

    public String getMetaName() {
        return metaName;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public String getDerivedQuery() {
        return derivedQuery;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getEndDelimiter() {
        return endDelimiter;
    }

    public boolean isSetTerm() {
        return isSetTerm;
    }

    public boolean isExecutable() {
        return StringUtils.isNotBlank(getDerivedQuery());
    }

}
