/*
 * SchemaTablesScriptGenerator.java
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

import org.executequery.ApplicationException;
import org.executequery.databaseobjects.DatabaseSource;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.log.Log;
import org.underworldlabs.util.DateUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SchemaTablesScriptGenerator {

    private final int scriptType;

    private final String outputFile;

    private final List<DatabaseTable> tables;

    private final DatabaseSource source;

    private List<ScriptGenerationObserver> observers;

    private int constraintStyle;

    private StringBuilder primaryKeys;
    private StringBuilder foreignKeys;
    private StringBuilder uniqueKeys;

    private boolean cascadeConstraints;

    private boolean includeScriptBanner;

    public SchemaTablesScriptGenerator(int scriptType,
                                       String outputFile, DatabaseSource source, List<NamedObject> tables) {

        this.scriptType = scriptType;
        this.outputFile = outputFile;
        this.source = source;
        this.tables = copyAndSortTables(tables);
    }

    public void writeDropTablesScript(boolean includeScriptBanner, boolean cascadeConstraints) {

        this.includeScriptBanner = includeScriptBanner;
        this.cascadeConstraints = cascadeConstraints;

        writeScript();
    }

    public void writeCreateTablesScript(boolean includeScriptBanner, int constraintStyle) {

        this.includeScriptBanner = includeScriptBanner;
        this.constraintStyle = constraintStyle;

        writeScript();
    }

    private void writeScript() {

        primaryKeys = new StringBuilder();
        foreignKeys = new StringBuilder();
        uniqueKeys = new StringBuilder();

        PrintWriter writer = null;

        try {

            int _constraintStyle = constraintStyle();

            writer = createPrintWriter();

            if (includeScriptBanner) {

                writer.println(createHeader());
            }

            for (NamedObject namedObject : tables) {

                if (Thread.interrupted()) {

                    throw new InterruptedException();
                }

                DatabaseTable table = (DatabaseTable) namedObject;

                fireStartedNamedObjectScript(table);

                writer.println(sqlTextForTable(table));

                if (scriptType == GenerateScriptsWizard.CREATE_TABLES) {

                    writer.println();
                }

                if (_constraintStyle == DatabaseTable.STYLE_CONSTRAINTS_ALTER) {

                    primaryKeys.append(table.getAlterSQLTextForPrimaryKeys());
                    foreignKeys.append(table.getAlterSQLTextForForeignKeys());
                    uniqueKeys.append(table.getAlterSQLTextForUniqueKeys());
                }

                fireFinishedNamedObjectScript(table);
            }

            if (scriptType == GenerateScriptsWizard.CREATE_TABLES
                    && _constraintStyle == DatabaseTable.STYLE_CONSTRAINTS_ALTER) {

                writer.println(primaryKeys);
                writer.println();

                writer.println(foreignKeys);
                writer.println();

                writer.println(uniqueKeys);
            }

        } catch (IOException e) {

            handleException(e);

        } catch (InterruptedException e) {

            handleException(e);

        } finally {

            if (writer != null) {

                writer.flush();
                writer.close();
            }

        }

    }

    private String sqlTextForTable(DatabaseTable table) {

        if (scriptType == GenerateScriptsWizard.CREATE_TABLES) {

            int _constraintStyle = constraintStyle();

            if (_constraintStyle == DatabaseTable.STYLE_CONSTRAINTS_DEFAULT) {

                return table.getCreateSQLText(DatabaseTable.STYLE_CONSTRAINTS_DEFAULT);

            } else if (_constraintStyle != DatabaseTable.STYLE_NO_CONSTRAINTS) {

                return table.getCreateSQLText(DatabaseTable.STYLE_NO_CONSTRAINTS);
            }

            return table.getCreateSQLText(_constraintStyle);

        } else {

            return table.getDropSQLText(cascadeConstraints);
        }

    }

    private void handleException(Throwable e) {

        if (Log.isDebugEnabled() && !(e instanceof InterruptedException)) {

            Log.error("Error on script generation: ", e);
        }

        throw new ApplicationException(e);
    }

    private PrintWriter createPrintWriter() throws IOException {

        return new PrintWriter(new FileWriter(outputFile, false), true);
    }

    protected void fireFinishedNamedObjectScript(NamedObject namedObject) {

        if (observers != null) {

            for (ScriptGenerationObserver observer : observers) {

                observer.finishedNamedObjectScript(namedObject);
            }

        }

    }

    protected void fireStartedNamedObjectScript(NamedObject namedObject) {

        if (observers != null) {

            for (ScriptGenerationObserver observer : observers) {

                observer.startedNamedObjectScript(namedObject);
            }

        }

    }

    private int constraintStyle() {

        if (constraintStyle == GenerateScriptsWizard.CREATE_TABLE_CONSTRAINTS) {

            return DatabaseTable.STYLE_CONSTRAINTS_DEFAULT;

        } else if (constraintStyle == -1) {

            return DatabaseTable.STYLE_NO_CONSTRAINTS;
        }

        return DatabaseTable.STYLE_CONSTRAINTS_ALTER;
    }

    public void addScriptGenerationObserver(ScriptGenerationObserver observer) {

        if (observers == null) {

            observers = new ArrayList<ScriptGenerationObserver>();
        }

        observers.add(observer);
    }

    private String createHeader() {

        StringBuilder sb = new StringBuilder(500);

        String line_1 = "-- ---------------------------------------------------\n";
        sb.append(line_1).
                append("--\n-- SQL script ").
                append("generated by RedXpert.\n-- Generated ").
                append(formattedTimeNow()).
                append("\n--\n").
                append(line_1).
                append("--\n-- Program:      ").
                append(outputFile).
                append("\n-- Description:  SQL ").
                append(scriptType == GenerateScriptsWizard.CREATE_TABLES ?
                        "create " : "drop ").
                append("tables script.\n-- Schema:       ").
                append(source.getName()).
                append("\n-- Database:     ").
                append(databaseName()).
                append("\n--\n").append(line_1).
                append("\n");
        return sb.toString();
    }

    private String formattedTimeNow() {

        return new DateUtils().getLongDateTime();
    }

    private String databaseName() {

        return source.getHost().getDatabaseProductName();
    }

    private List<DatabaseTable> copyAndSortTables(List<NamedObject> tables) {

        List<DatabaseTable> destinationList =
                new ArrayList<DatabaseTable>(tables.size());

        for (Iterator<NamedObject> iter = tables.iterator(); iter.hasNext(); ) {

            destinationList.add((DatabaseTable) iter.next());
        }

//        Collections.sort(destinationList, new TableDependencySorter());

        return destinationList;
    }

/*
    private class TableDependencySorter implements Comparator<DatabaseTable> {
        
        public int compare(DatabaseTable table1, DatabaseTable table2) {

            if (table1.hasReferenceTo(table2)) {

                System.out.println(table1 + " -1 " + table2);
                
                return -1;

            } / *else if (table2.hasReferenceTo(table1)) {

                System.out.println(table1 + " 1 " + table2);
                
                return 1; 
            }
            * /

            System.out.println(table1 + " 0 " + table2);

            return 1;
        }
        
    }
    */

}







