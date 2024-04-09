/*
 * AutoCompleteSelectionsFactory.java
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

package org.executequery.gui.editor.autocomplete;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseFunction;
import org.executequery.databaseobjects.impl.DefaultDatabaseProcedure;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.log.Log;
import org.executequery.repository.KeywordRepository;
import org.executequery.repository.spi.KeywordRepositoryImpl;

import java.sql.*;
import java.util.*;

public class AutoCompleteSelectionsFactory {

    private static final String DATABASE_TABLE_DESCRIPTION = "Database Table";

    private static final String DATABASE_FUNCTION_DESCRIPTION = "Database Function";

    private static final String DATABASE_PROCEDURE_DESCRIPTION = "Database Procedure";
    private static final String DATABASE_PACKAGE_DESCRIPTION = "Database Package";

    private static final String DATABASE_TABLE_VIEW = "Database View";

    private static final String DATABASE_COLUMN_DESCRIPTION = "Database Column";

    private static final String VARIABLE_DESCRIPTION = "Variable";
    private static final String PARAMETER_DESCRIPTION = "Parameter";

    private TreeSet<String> variables;
    private TreeSet<String> parameters;

    private static final String DATABASE_SYSTEM_FUNCTION_DESCRIPTION = "System Function";

    private final AutoCompletePopupProvider provider;

    private List<AutoCompleteListItem> tables;

    public AutoCompleteSelectionsFactory(AutoCompletePopupProvider provider) {
        super();
        this.provider = provider;
    }

    public void build(DatabaseHost databaseHost, boolean autoCompleteKeywords, boolean autoCompleteSchema,
                      QueryEditor queryEditor) {

        tables = new ArrayList<AutoCompleteListItem>();

        List<AutoCompleteListItem> listSelections = new ArrayList<AutoCompleteListItem>();
        if (autoCompleteKeywords) {

            addToProvider(listSelections);
        }

        if (databaseHost != null && databaseHost.isConnected()) {

            if (autoCompleteKeywords) {

                addDatabaseDefinedKeywords(databaseHost, listSelections);
                addFirebirdDefinedKeywords(databaseHost, listSelections);
                addToProvider(listSelections);
                queryEditor.updateSQLKeywords();
            }

            if (autoCompleteSchema) {

                databaseTablesForHost(databaseHost);
//                databaseColumnsForTables(databaseHost, tables);
                databaseExecutablesForHost(databaseHost);
            }

        }

    }

    public void build(DatabaseHost databaseHost, boolean autoCompleteKeywords, boolean autoCompleteSchema,
                      SQLTextArea queryEditor) {

        tables = new ArrayList<AutoCompleteListItem>();

        List<AutoCompleteListItem> listSelections = new ArrayList<AutoCompleteListItem>();
        if (autoCompleteKeywords) {

            addToProvider(listSelections);
        }

        if (databaseHost != null && databaseHost.isConnected()) {

            if (autoCompleteKeywords) {

                addDatabaseDefinedKeywords(databaseHost, listSelections);
                databaseSystemFunctionsForHost(databaseHost, listSelections);
                addFirebirdDefinedKeywords(databaseHost, listSelections);
                addToProvider(listSelections);
                queryEditor.setSQLKeywords(true);
            }

            if (autoCompleteSchema) {

                databaseTablesForHost(databaseHost);
//                databaseColumnsForTables(databaseHost, tables);
                databaseExecutablesForHost(databaseHost);
            }
            addParametersToProvider();
            addVariablesToProvider();
        }

    }

    private void addToProvider(List<AutoCompleteListItem> listSelections) {

        provider.addListItems(listSelections);
        listSelections.clear();
    }

    public List<AutoCompleteListItem> buildKeywords(DatabaseHost databaseHost, boolean autoCompleteKeywords) {

        List<AutoCompleteListItem> listSelections = new ArrayList<AutoCompleteListItem>();
        if (autoCompleteKeywords) {
            addUserDefinedKeywords(listSelections);

            if (databaseHost != null && databaseHost.isConnected()) {
                addDatabaseDefinedKeywords(databaseHost, listSelections);
                databaseSystemFunctionsForHost(databaseHost, listSelections);

            }

            Collections.sort(listSelections, new AutoCompleteListItemComparator());
        }

        return listSelections;
    }

    private void addVariablesToProvider() {
        if(variables!=null) {
            trace("Building autocomplete variables list");
            List<String> tableNames = new ArrayList<String>();
            tableNames.addAll(variables);
            List<AutoCompleteListItem> list = new ArrayList<AutoCompleteListItem>();
            addTablesToProvider(VARIABLE_DESCRIPTION, AutoCompleteListItemType.VARIABLE, tableNames, list);
        }
    }
    private void addParametersToProvider() {
        if(parameters!=null) {
            trace("Building autocomplete variables list");
            List<String> tableNames = new ArrayList<String>();
            tableNames.addAll(parameters);
            List<AutoCompleteListItem> list = new ArrayList<AutoCompleteListItem>();
            addTablesToProvider(PARAMETER_DESCRIPTION, AutoCompleteListItemType.PARAMETER, tableNames, list);
        }
    }


    private void databaseExecutablesForHost(DatabaseHost databaseHost) {
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.FUNCTION], DATABASE_FUNCTION_DESCRIPTION, AutoCompleteListItemType.DATABASE_FUNCTION);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.UDF], DATABASE_FUNCTION_DESCRIPTION, AutoCompleteListItemType.DATABASE_FUNCTION);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.PROCEDURE], DATABASE_PROCEDURE_DESCRIPTION, AutoCompleteListItemType.DATABASE_PROCEDURE);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.PACKAGE], DATABASE_PACKAGE_DESCRIPTION, AutoCompleteListItemType.DATABASE_PACKAGE);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.SYSTEM_PACKAGE], DATABASE_PACKAGE_DESCRIPTION, AutoCompleteListItemType.DATABASE_PACKAGE);
    }

    private void databaseTablesForHost(DatabaseHost databaseHost) {

        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.TABLE], DATABASE_TABLE_DESCRIPTION, AutoCompleteListItemType.DATABASE_TABLE);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.VIEW], DATABASE_TABLE_VIEW, AutoCompleteListItemType.DATABASE_VIEW);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY], DATABASE_TABLE_DESCRIPTION, AutoCompleteListItemType.DATABASE_TABLE);

        DatabaseConnection databaseConnection = databaseHost.getDatabaseConnection();
        Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
        DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
        Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());

        if (driver.getClass().getName().contains("FBDriver")) {
            databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.SYSTEM_TABLE], DATABASE_TABLE_VIEW, AutoCompleteListItemType.DATABASE_TABLE);
        }
    }

    private void databaseSystemFunctionsForHost(DatabaseHost databaseHost, List<AutoCompleteListItem> listSelections) {

        trace("Building autocomplete object list using [ " + databaseHost.getName() + " ] for type - SYSTEM_FUNCTION");

        ResultSet rs = null;
        DatabaseMetaData databaseMetaData = databaseHost.getDatabaseMetaData();
        TreeSet<String> keywords = databaseHost.getDatabaseConnection().getKeywords();

        try {

            List<String> tableNames = new ArrayList<String>();

            extractNames(tableNames, databaseMetaData.getStringFunctions());
            extractNames(tableNames, databaseMetaData.getNumericFunctions());
            extractNames(tableNames, databaseMetaData.getTimeDateFunctions());
            addKeywordsFromList(tableNames, listSelections, DATABASE_SYSTEM_FUNCTION_DESCRIPTION, AutoCompleteListItemType.SYSTEM_FUNCTION, keywords);

        } catch (SQLException e) {

            error("Values not available for type SYSTEM_FUNCTION - driver returned: " + e.getMessage());

        } finally {

            releaseResources(rs);
            trace("Finished autocomplete object list using [ " + databaseHost.getName() + " ] for type - SYSTEM_FUNCTION");
        }

    }

    private void extractNames(List<String> tableNames, String functions) {

        if (StringUtils.isNotEmpty(functions)) {

            String[] names = functions.split(",");
            Collections.addAll(tableNames, names);

        }
    }

    private static final int INCREMENT = 5;

    private void databaseObjectsForHost(DatabaseHost databaseHost, String type,
                                        String databaseObjectDescription, AutoCompleteListItemType autocompleteType) {

        trace("Building autocomplete object list using [ " + databaseHost.getName() + " ] for type - " + type);
        List<String> tableNames = new ArrayList<String>();
        List<AutoCompleteListItem> list = new ArrayList<AutoCompleteListItem>();
        DatabaseHostNode hostNode = (DatabaseHostNode) ConnectionsTreePanel.getPanelFromBrowser().getHostNode(databaseHost.getDatabaseConnection());
        List<DatabaseObjectNode> tables = hostNode.getAllDBObjects(type);
        for (DatabaseObjectNode table : tables) {
            tableNames.add(table.getName());
        }
        addTablesToProvider(databaseObjectDescription, autocompleteType, tableNames, list);


    }

    private List<AutoCompleteListItem> tablesToAutoCompleteListItems(
            List<AutoCompleteListItem> list, List<String> tables,
            String databaseObjectDescription, AutoCompleteListItemType autoCompleteListItemType) {

        for (String table : tables) {

            list.add(new AutoCompleteListItem(table,
                    table, databaseObjectDescription, autoCompleteListItemType));
        }

        return list;
    }


    private void addDatabaseDefinedKeywords(DatabaseHost databaseHost, List<AutoCompleteListItem> list) {

        String[] keywords = databaseHost.getDatabaseKeywords();
        List<String> asList = new ArrayList<String>();

        Collections.addAll(asList, keywords);

        addKeywordsFromList(asList, list,
                "Database Defined Keyword", AutoCompleteListItemType.DATABASE_DEFINED_KEYWORD);
    }

    private void addFirebirdDefinedKeywords(DatabaseHost databaseHost, List<AutoCompleteListItem> list) {
        List<String> keywords = new ArrayList<>();
        keywords.addAll(databaseHost.getDatabaseConnection().getKeywords());
        addKeywordsFromList(keywords,
                list, "Database Defined Keyword", AutoCompleteListItemType.DATABASE_DEFINED_KEYWORD);
    }


    private void addUserDefinedKeywords(List<AutoCompleteListItem> list) {

        addKeywordsFromList(keywords().getUserDefinedSQL(),
                list, "User Defined Keyword", AutoCompleteListItemType.USER_DEFINED_KEYWORD);
    }

    private void addTablesToProvider(String databaseObjectDescription,
                                     AutoCompleteListItemType autocompleteType, List<String> tableNames,
                                     List<AutoCompleteListItem> list) {

        List<AutoCompleteListItem> autoCompleteListItems =
                tablesToAutoCompleteListItems(list, tableNames, databaseObjectDescription, autocompleteType);

        provider.addListItems(autoCompleteListItems);
        tables.addAll(autoCompleteListItems);
    }

    private void addKeywordsFromList(List<String> keywords, List<AutoCompleteListItem> list,
                                     String description, AutoCompleteListItemType autoCompleteListItemType) {

        for (String keyword : keywords) {
            list.add(new AutoCompleteListItem(keyword, keyword, description, autoCompleteListItemType));
        }

    }

    private void addKeywordsFromList(List<String> keywords, List<AutoCompleteListItem> list,
                                     String description, AutoCompleteListItemType autoCompleteListItemType, TreeSet<String> checks) {
        for (String keyword : keywords) {
            if (!checks.contains(keyword)) {
                list.add(new AutoCompleteListItem(keyword, keyword, description, autoCompleteListItemType));
            }
        }

    }

    private KeywordRepository keywords() {

        return new KeywordRepositoryImpl();
    }

    public List<AutoCompleteListItem> buildItemsForTable(DatabaseHost databaseHost, String tableString) {
        //List<ColumnInformation> columns = new ArrayList<ColumnInformation>();
        List<AutoCompleteListItem> list = new ArrayList<AutoCompleteListItem>();
        if (tableString.startsWith("\""))
            tableString = tableString.substring(1, tableString.length() - 1);
        List<DatabaseMetaTag> databaseMetaTags = databaseHost.getMetaObjects();
        NamedObject table = null;
        for (DatabaseMetaTag databaseMetaTag : databaseMetaTags) {
            if (databaseMetaTag.getSubType() == NamedObject.TABLE || databaseMetaTag.getSubType() == NamedObject.GLOBAL_TEMPORARY
                    || databaseMetaTag.getSubType() == NamedObject.VIEW || databaseMetaTag.getSubType() == NamedObject.SYSTEM_TABLE
                    || databaseMetaTag.getSubType() == NamedObject.SYSTEM_VIEW
                    || databaseMetaTag.getSubType() == NamedObject.PACKAGE
                    || databaseMetaTag.getSubType() == NamedObject.SYSTEM_PACKAGE) {
                table = databaseMetaTag.getNamedObject(tableString);
                if (table != null)
                    break;
            }
        }
        if (table != null) {
            List<NamedObject> cols = table.getObjects();
            for (NamedObject col : cols) {
                String desc = DATABASE_COLUMN_DESCRIPTION;
                AutoCompleteListItemType colType = AutoCompleteListItemType.DATABASE_TABLE_COLUMN;
                if (col instanceof DefaultDatabaseProcedure) {
                    desc = DATABASE_PROCEDURE_DESCRIPTION;
                    colType = AutoCompleteListItemType.DATABASE_PROCEDURE;
                }
                if (col instanceof DefaultDatabaseFunction) {
                    desc = DATABASE_FUNCTION_DESCRIPTION;
                    colType = AutoCompleteListItemType.DATABASE_FUNCTION;
                }
                list.add(new AutoCompleteListItem(col.getName(), tableString, col.getDescription(), desc,
                        colType));
            }
        }

        return list;

    }

    static class AutoCompleteListItemComparator implements Comparator<AutoCompleteListItem> {

        public int compare(AutoCompleteListItem o1, AutoCompleteListItem o2) {

            return o1.getValue().toUpperCase().compareTo(o2.getValue().toUpperCase());
        }

    }

    private void releaseResources(ResultSet rs) {
        try {
            if (rs != null) {
                Statement st = rs.getStatement();
                if(st!=null)
                    if(!st.isClosed())
                        st.close();
                //rs.close();
            }
        } catch (SQLException sqlExc) {
        }
    }


    public void setVariables(TreeSet<String> variables) {
        this.variables = variables;
    }

    public void setParameters(TreeSet<String> parameters) {
        this.parameters = parameters;
    }

    private void error(String message) {

        Log.error(message);
    }

    @SuppressWarnings("unused")
    private void warning(String message) {

        Log.error(message);
    }

    private void trace(String message) {

        Log.trace(message);
    }


}


