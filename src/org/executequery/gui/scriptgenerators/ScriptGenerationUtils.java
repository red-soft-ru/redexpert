/*
 * ScriptGenerationUtils.java
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

package org.executequery.gui.scriptgenerators;

import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.underworldlabs.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.executequery.databaseobjects.NamedObject.FOREIGN_KEY;
import static org.executequery.databaseobjects.NamedObject.PRIMARY_KEY;

/**
 * Simple utility methods to asist in generating SQL scripts
 * for tables/schemas.
 *
 * @author Takis Diakoumis
 * @deprecated
 */
public class ScriptGenerationUtils implements CreateTableSQLSyntax {

    /**
     * Generates a create table script for the specified
     * column data array.
     */
    public static String createTableScript(ColumnData[] cda) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < cda.length; i++) {
            ColumnData cd = cda[i];

            if (i == 0) {
                sb.append(CREATE_TABLE);

                String schema = cd.getSchema();
                if (!MiscUtils.isNull(schema)) {
                    sb.append(schema);
                    sb.append(DOT);
                }
                sb.append(cd.getTableName());
                sb.append(SPACE);
                sb.append(B_OPEN);
            }

            sb.append(NEW_LINE_2).append(cd.getColumnName()).append(SPACE);

            if (cd.getColumnType() != null) {
                sb.append(cd.getColumnType());

                if (!cd.getColumnType().equalsIgnoreCase(DATE)) {
                    sb.append(B_OPEN).append(cd.getColumnSize());

                    if (cd.getColumnScale() != 0) {
                        sb.append(COMMA).append(cd.getColumnScale());
                    }

                    sb.append(B_CLOSE);
                }

            }
            sb.append(cd.isRequired() ? NOT_NULL : EMPTY);

            if (i != cda.length - 1) {
                sb.append(COMMA);
            }

        }
        sb.append(B_CLOSE);
        sb.append(SEMI_COLON);
        return sb.toString();
    }

    public static int ALTER_CONSTRAINTS = 0;

    public static int DEFAULT_CONSTRAINTS = 1;

    public static String createTableScript(String tableName,
                                           ColumnData[] cda)
            throws InterruptedException {
        return createTableScript(tableName, cda, false);
    }

    public static String createTableScript(String tableName,
                                           ColumnData[] cda,
                                           boolean includeConstraints)
            throws InterruptedException {

        int sepLength = -1;
        StringBuffer sb = new StringBuffer(500);
        StringBuffer sb_spaces_1 = new StringBuffer(50);
        StringBuffer sb_spaces_2 = new StringBuffer(30);
        String initialSpaces = "               ";

        List<ColumnConstraint> columnConstraints =
                new ArrayList<ColumnConstraint>();

        // opening create table line
        sb.append(CREATE_TABLE).
                append(tableName).
                append(SPACE).
                append(B_OPEN);

        if (cda.length > 0) {
            sb_spaces_1.append(initialSpaces);
            int tn_length = tableName.length();

            // spaces between beginning of line and column name
            for (int k = 0; k < tn_length; k++) {
                sb_spaces_1.append(SPACE);
            }

            String spaces_1 = sb_spaces_1.toString();

            // loop through each column
            for (int j = 0; j < cda.length; j++) {

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                sepLength = getSpaceLength(cda) + 5;

                if (j > 0) {
                    sb.append(spaces_1);
                }

                ColumnData column = cda[j];
                int l_size = sepLength - column.getColumnName().length();

                for (int m = 0; m < l_size; m++) {
                    sb_spaces_2.append(SPACE);
                }

                // column name and data type
                sb.append(column.getColumnName()).
                        append(sb_spaces_2).
                        append(column.getFormattedDataType());

                // column nullable
                sb.append(column.isRequired() ? NOT_NULL : EMPTY);

                if (column.isKey() && includeConstraints) {
                    Vector<ColumnConstraint> ccv = column.getColumnConstraintsVector();
                    for (int a = 0, b = ccv.size(); a < b; a++) {
                        columnConstraints.add(ccv.get(a));
                    }

                }

                if (j != cda.length - 1) {
                    sb.append(COMMA).append(NEW_LINE);
                }
                sb_spaces_2.setLength(0);
            }

            int v_size = columnConstraints.size();
            if (v_size > 0) {
                sb.append(COMMA).append(NEW_LINE);

                ColumnConstraint cc = null;
                for (int j = 0; j < v_size; j++) {
                    cc = columnConstraints.get(j);
                    sb.append(spaces_1).
                            append(CONSTRAINT).
                            append(cc.getName()).
                            append(SPACE).
                            append(cc.getTypeName()).
                            append(KEY).
                            append(B_OPEN).
                            append(cc.getColumn()).
                            append(B_CLOSE);

                    if (cc.getType() == FOREIGN_KEY) {
                        sb.append(REFERENCES);
                        if (cc.hasSchema()) {
                            sb.append(cc.getRefSchema()).
                                    append(DOT);
                        }
                        sb.append(cc.getRefTable()).
                                append(B_OPEN).
                                append(cc.getRefColumn()).
                                append(B_CLOSE);
                    }

                    if (j < v_size - 1) {
                        sb.append(COMMA).
                                append(NEW_LINE);
                    }

                }

            }

            columnConstraints.clear();
            sb.append(B_CLOSE).
                    append(SEMI_COLON).
                    append(NEW_LINE).
                    append(NEW_LINE);

        } else { // no columns
            sb.append(B_CLOSE).
                    append(SEMI_COLON).
                    append(NEW_LINE).
                    append(NEW_LINE);
        }
        return sb.toString();
    }

    public static String alterTableConstraintsScript(
            Vector<ColumnConstraint> columnConstraints) {

        int type = -1;
        StringBuffer primaryKeys = new StringBuffer();
        StringBuffer foreignKeys = new StringBuffer();

        for (int i = 0, n = columnConstraints.size(); i < n; i++) {
            ColumnConstraint cc = columnConstraints.get(i);
            type = cc.getType();

            if (type == FOREIGN_KEY) {

                foreignKeys.append(ALTER_TABLE).
                        append(cc.getTable()).
                        append(ADD).
                        append(CONSTRAINT).
                        append(cc.getName()).
                        append(SPACE).
                        append(cc.getTypeName()).
                        append(KEY).
                        append(B_OPEN).
                        append(cc.getColumn()).
                        append(B_CLOSE).
                        append(REFERENCES);

                // if (cc.hasSchema())
                // fKeys.append(cc.getSchema()).append(DOT);

                foreignKeys.append(cc.getRefTable()).
                        append(B_OPEN).
                        append(cc.getRefColumn()).
                        append(B_CLOSE).
                        append(SEMI_COLON).
                        append(NEW_LINE);
            } else if (type == PRIMARY_KEY) {
                primaryKeys.append(ALTER_TABLE).
                        append(cc.getTable()).
                        append(ADD).
                        append(CONSTRAINT).
                        append(cc.getName()).
                        append(SPACE).
                        append(cc.getTypeName()).
                        append(KEY).
                        append(B_OPEN).
                        append(cc.getColumn()).
                        append(B_CLOSE).
                        append(SEMI_COLON).
                        append(NEW_LINE);
            }

        }

        if (foreignKeys.length() > 0) {
            primaryKeys.append(NEW_LINE).
                    append(foreignKeys);
        }

        return primaryKeys.toString();
    }

    private static int getSpaceLength(ColumnData[] cda) {
        int spaces = 0;
        // spaces between end of column name and data type name
        for (int i = 0; i < cda.length; i++) {
            spaces = Math.max(spaces, cda[i].getColumnName().length());
        }
        return spaces;
    }

    private ScriptGenerationUtils() {
    }

}

