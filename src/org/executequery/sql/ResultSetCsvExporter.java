/*
 * ResultSetCsvExporter.java
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.executequery.Constants;

public class ResultSetCsvExporter implements ResultSetExporter {

    public int export(ResultSet resultSet, File output) {
    
        int count = 0;
        PrintWriter writer = null;
        try {

            writer = new PrintWriter(new FileWriter(output, false), true);

            StringBuilder sb = new StringBuilder();
            ResultSetMetaData metaData = resultSet.getMetaData();

            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {

                sb.append(metaData.getColumnLabel(i)).append(",");
            }
            write(writer, sb);

            while (resultSet.next()) {

                sb.setLength(0);
                for (int i = 1; i <= columnCount; i++) {

                    String value = resultSet.getString(i);
                    if (value == null || resultSet.wasNull()) {

                        value = Constants.EMPTY;
                    }

                    if (columnIsChars(metaData, i)) {

                        sb.append("\"").append(value).append("\"");
                    
                    } else {

                        sb.append(value).append(",");
                    }

                }

                write(writer, sb);
                count++;
            }

            writer.flush();

        } catch (SQLException e) {

            throw new RuntimeException(e);

        } catch (IOException e) {

            throw new RuntimeException(e);

        } finally {

            if (writer != null) {

                writer.close();
            }
        }

        return count;
    }

    private void write(PrintWriter writer, StringBuilder sb) {

        sb.deleteCharAt(sb.length() - 1);
        writer.println(sb);
    }

    private boolean columnIsChars(ResultSetMetaData metaData, int i) throws SQLException {

        int type = metaData.getColumnType(i);
        return (type == Types.CHAR || type == Types.VARCHAR || type == Types.LONGNVARCHAR);
    }

}


