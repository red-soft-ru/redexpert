/*
 * QueryTypes.java
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

package org.executequery.databasemediators;

public final class QueryTypes {

    public static final int SELECT = 10;
    public static final int SELECT_INTO = SELECT + 1;

    public static final int EXECUTE = SELECT_INTO + 1;
    public static final int CALL = EXECUTE + 1;
    public static final int COMMENT = CALL + 1;
    public static final int COMMIT = COMMENT + 1;
    public static final int ROLLBACK = COMMIT + 1;

    public static final int CONNECT = ROLLBACK + 1;
    public static final int CREATE_DATABASE = CONNECT + 1;
    public static final int SHOW_TABLES = CREATE_DATABASE + 1;
    public static final int SQL_DIALECT = SHOW_TABLES + 1;
    public static final int SET_NAMES = SQL_DIALECT + 1;
    public static final int SET_STATISTICS = SET_NAMES + 1;
    public static final int SET_BLOBFILE = SET_STATISTICS + 1;

    public static final int SET_AUTODDL_ON = SET_BLOBFILE + 1;
    public static final int SET_AUTODDL_OFF = SET_AUTODDL_ON + 1;

    public static final int EXPLAIN = SET_AUTODDL_OFF + 1;
    public static final int DESCRIBE = EXPLAIN + 1;

    public static final int GRANT = DESCRIBE + 1;
    public static final int REVOKE = GRANT + 1;

    public static final int CREATE_OBJECT = REVOKE + 1;
    public static final int CREATE_OR_ALTER = CREATE_OBJECT + 1;
    public static final int ALTER_OBJECT = CREATE_OR_ALTER + 1;
    public static final int RECREATE_OBJECT = ALTER_OBJECT + 1;
    public static final int DECLARE_OBJECT = RECREATE_OBJECT + 1;
    public static final int DROP_OBJECT = DECLARE_OBJECT + 1;

    public static final int ALL_UPDATES = 80;
    public static final int INSERT = ALL_UPDATES;
    public static final int UPDATE = INSERT + 1;
    public static final int DELETE = UPDATE + 1;

    public static final int UNKNOWN = 99;

    public static String getResultText(int result, int type, String metaName, String objectName) {

        String row = " row ";
        if (result > 1 || result == 0)
            row = " rows ";

        String resultText = "";
        switch (type) {
            case QueryTypes.INSERT:
                resultText = row + "created.";
                break;

            case QueryTypes.UPDATE:
                resultText = row + "updated.";
                break;

            case QueryTypes.DELETE:
                resultText = row + "deleted.";
                break;

            case QueryTypes.GRANT:
                resultText = "Grant succeeded.";
                break;

            case QueryTypes.COMMIT:
                resultText = "Commit complete.";
                break;

            case QueryTypes.ROLLBACK:
                resultText = "Rollback complete.";
                break;

            case QueryTypes.SELECT_INTO:
            case QueryTypes.SET_STATISTICS:
                resultText = "Statement executed successfully.";
                break;

            case QueryTypes.REVOKE:
                resultText = "Revoke succeeded.";
                break;

            case QueryTypes.DROP_OBJECT:
                resultText = metaName + " " + objectName + " dropped.";
                break;

            case QueryTypes.COMMENT:
                resultText = "Comment added";
                break;

            case QueryTypes.CREATE_OBJECT:
            case QueryTypes.CREATE_OR_ALTER:
                resultText = metaName + " " + objectName + " created";
                break;

            case QueryTypes.DECLARE_OBJECT:
                resultText = metaName + " " + objectName + " declared";
                break;

            case QueryTypes.RECREATE_OBJECT:
                resultText = metaName + " " + objectName + " recreated";
                break;

            case QueryTypes.ALTER_OBJECT:
                resultText = metaName + " " + objectName + " altered";
                break;

            case QueryTypes.UNKNOWN:
            case QueryTypes.EXECUTE:
                if (result > -1)
                    resultText = result + row + "affected.\n";
                resultText += "Statement executed successfully.";
                break;
        }

        StringBuilder sb = new StringBuilder();
        if ((result > -1 && type >= QueryTypes.ALL_UPDATES) && type != QueryTypes.UNKNOWN)
            sb.append(result);
        sb.append(resultText);

        return sb.toString();
    }
}
