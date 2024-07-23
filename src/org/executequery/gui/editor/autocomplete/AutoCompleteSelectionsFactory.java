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

import java.sql.*;
import java.util.*;

public class AutoCompleteSelectionsFactory {

    private static final String VARIABLE = "Variable";
    private static final String PARAMETER = "Parameter";
    private static final String DATABASE_VIEW = "Database View";
    private static final String DATABASE_TABLE = "Database Table";
    private static final String SYSTEM_FUNCTION = "System Function";
    private static final String DATABASE_COLUMN = "Database Column";
    private static final String DATABASE_PACKAGE = "Database Package";
    private static final String DATABASE_FUNCTION = "Database Function";
    private static final String DATABASE_PROCEDURE = "Database Procedure";
    private static final String DATABASE_DEFINED_KEYWORD = "Database Defined Keyword";

    private TreeSet<String> variables;
    private TreeSet<String> parameters;
    private final AutoCompletePopupProvider provider;

    public AutoCompleteSelectionsFactory(AutoCompletePopupProvider provider) {
        super();
        this.provider = provider;
    }

    public void build(DatabaseHost databaseHost, boolean autoCompleteKeywords, boolean autoCompleteObjects, QueryEditor queryEditor) {
        build(databaseHost, autoCompleteKeywords, autoCompleteObjects, (Object) queryEditor);
    }

    public void build(DatabaseHost databaseHost, boolean autoCompleteKeywords, boolean autoCompleteObjects, SQLTextArea queryEditor) {
        build(databaseHost, autoCompleteKeywords, autoCompleteObjects, (Object) queryEditor);
    }

    private void build(DatabaseHost databaseHost, boolean autoCompleteKeywords, boolean autoCompleteObjects, Object editor) {

        List<AutoCompleteListItem> listSelections = new ArrayList<>();
        if (autoCompleteKeywords)
            addToProvider(listSelections);

        if (databaseHost != null && databaseHost.isConnected()) {

            if (autoCompleteKeywords) {
                addDatabaseDefinedKeywords(databaseHost, listSelections);
                databaseSystemFunctionsForHost(databaseHost, listSelections);
                addFirebirdDefinedKeywords(databaseHost, listSelections);
                addToProvider(listSelections);

                if (editor instanceof SQLTextArea)
                    ((SQLTextArea) editor).setSQLKeywords(true);
                else if (editor instanceof QueryEditor)
                    ((QueryEditor) editor).updateSQLKeywords();
            }

            if (autoCompleteObjects) {
                databaseTablesForHost(databaseHost);
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

        if (!autoCompleteKeywords)
            return new ArrayList<>();

        List<AutoCompleteListItem> listSelections = new ArrayList<>();
        if (databaseHost != null && databaseHost.isConnected()) {
            addDatabaseDefinedKeywords(databaseHost, listSelections);
            databaseSystemFunctionsForHost(databaseHost, listSelections);
        }

        listSelections.sort(new AutoCompleteListItemComparator());
        return listSelections;
    }

    private void addVariablesToProvider() {
        if (variables != null) {
            trace("Building autocomplete variables list");

            addTablesToProvider(
                    VARIABLE,
                    AutoCompleteListItemType.VARIABLE,
                    new ArrayList<>(variables),
                    new ArrayList<>()
            );
        }
    }

    private void addParametersToProvider() {
        if (parameters != null) {
            trace("Building autocomplete variables list");

            addTablesToProvider(
                    PARAMETER,
                    AutoCompleteListItemType.PARAMETER,
                    new ArrayList<>(parameters),
                    new ArrayList<>()
            );
        }
    }

    private void databaseExecutablesForHost(DatabaseHost databaseHost) {
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.FUNCTION], DATABASE_FUNCTION, AutoCompleteListItemType.DATABASE_FUNCTION);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.UDF], DATABASE_FUNCTION, AutoCompleteListItemType.DATABASE_FUNCTION);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.PROCEDURE], DATABASE_PROCEDURE, AutoCompleteListItemType.DATABASE_PROCEDURE);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.PACKAGE], DATABASE_PACKAGE, AutoCompleteListItemType.DATABASE_PACKAGE);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.SYSTEM_PACKAGE], DATABASE_PACKAGE, AutoCompleteListItemType.DATABASE_PACKAGE);
    }

    private void databaseTablesForHost(DatabaseHost databaseHost) {

        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.TABLE], DATABASE_TABLE, AutoCompleteListItemType.DATABASE_TABLE);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.VIEW], DATABASE_VIEW, AutoCompleteListItemType.DATABASE_VIEW);
        databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY], DATABASE_TABLE, AutoCompleteListItemType.DATABASE_TABLE);

        DatabaseConnection databaseConnection = databaseHost.getDatabaseConnection();
        Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
        DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
        Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());

        if (driver.getClass().getName().contains("FBDriver"))
            databaseObjectsForHost(databaseHost, NamedObject.META_TYPES[NamedObject.SYSTEM_TABLE], DATABASE_VIEW, AutoCompleteListItemType.DATABASE_TABLE);
    }

    private void databaseSystemFunctionsForHost(DatabaseHost databaseHost, List<AutoCompleteListItem> listSelections) {
        trace("Building autocomplete object list using [ " + databaseHost.getName() + " ] for type - SYSTEM_FUNCTION");

        DatabaseMetaData databaseMetaData = databaseHost.getDatabaseMetaData();
        TreeSet<String> keywords = databaseHost.getDatabaseConnection().getKeywords();

        try {
            List<String> tableNames = new ArrayList<>();
            extractNames(tableNames, databaseMetaData.getStringFunctions());
            extractNames(tableNames, databaseMetaData.getNumericFunctions());
            extractNames(tableNames, databaseMetaData.getTimeDateFunctions());

            addKeywordsFromList(
                    tableNames,
                    listSelections,
                    SYSTEM_FUNCTION,
                    AutoCompleteListItemType.SYSTEM_FUNCTION,
                    keywords
            );

        } catch (SQLException e) {
            error("Values not available for type SYSTEM_FUNCTION - driver returned: " + e.getMessage());

        } finally {
            trace("Finished autocomplete object list using [ " + databaseHost.getName() + " ] for type - SYSTEM_FUNCTION");
        }
    }

    private void extractNames(List<String> tableNames, String functions) {
        if (StringUtils.isNotEmpty(functions))
            Collections.addAll(tableNames, functions.split(","));
    }

    private void databaseObjectsForHost(DatabaseHost databaseHost, String type, String description, AutoCompleteListItemType itemType) {
        trace("Building autocomplete object list using [ " + databaseHost.getName() + " ] for type - " + type);

        List<String> tableNames = new ArrayList<>();
        List<AutoCompleteListItem> list = new ArrayList<>();
        DatabaseHostNode hostNode = (DatabaseHostNode) ConnectionsTreePanel.getPanelFromBrowser().getHostNode(databaseHost.getDatabaseConnection());

        for (DatabaseObjectNode table : hostNode.getAllDBObjects(type))
            tableNames.add(table.getName());

        addTablesToProvider(description, itemType, tableNames, list);
    }

    private List<AutoCompleteListItem> tablesToAutoCompleteListItems(List<AutoCompleteListItem> list, List<String> tables,
                                                                     String description, AutoCompleteListItemType itemType) {

        for (String table : tables)
            list.add(new AutoCompleteListItem(table, table, description, itemType));

        return list;
    }


    private void addDatabaseDefinedKeywords(DatabaseHost databaseHost, List<AutoCompleteListItem> list) {
        List<String> arrayList = new ArrayList<>();
        Collections.addAll(arrayList, databaseHost.getDatabaseKeywords());

        addDatabaseDefinedKeywordsFromList(arrayList, list);
    }

    private void addFirebirdDefinedKeywords(DatabaseHost databaseHost, List<AutoCompleteListItem> list) {
        addDatabaseDefinedKeywordsFromList(new ArrayList<>(databaseHost.getDatabaseConnection().getKeywords()), list);
    }

    private void addTablesToProvider(String description, AutoCompleteListItemType itemType, List<String> tableNames, List<AutoCompleteListItem> itemList) {

        List<AutoCompleteListItem> autoCompleteListItems = tablesToAutoCompleteListItems(
                itemList,
                tableNames,
                description,
                itemType
        );

        provider.addListItems(autoCompleteListItems);
    }

    private void addDatabaseDefinedKeywordsFromList(List<String> keywords, List<AutoCompleteListItem> list) {
        addKeywordsFromList(keywords, list, DATABASE_DEFINED_KEYWORD, AutoCompleteListItemType.DATABASE_DEFINED_KEYWORD, new TreeSet<>());
    }

    private void addKeywordsFromList(List<String> keywords, List<AutoCompleteListItem> list, String description,
                                     AutoCompleteListItemType itemType, TreeSet<String> checks) {

        keywords.stream()
                .filter(keyword -> !checks.contains(keyword))
                .map(keyword -> new AutoCompleteListItem(
                        keyword,
                        keyword,
                        description,
                        itemType
                ))
                .forEach(list::add);
    }

    public List<AutoCompleteListItem> buildItemsForTable(DatabaseHost databaseHost, String tableString) {

        if (databaseHost == null)
            return new ArrayList<>();

        if (tableString.startsWith("\""))
            tableString = tableString.substring(1, tableString.length() - 1);

        NamedObject table = null;
        for (DatabaseMetaTag databaseMetaTag : databaseHost.getMetaObjects()) {
            if (databaseMetaTag.getSubType() == NamedObject.TABLE
                    || databaseMetaTag.getSubType() == NamedObject.GLOBAL_TEMPORARY
                    || databaseMetaTag.getSubType() == NamedObject.VIEW
                    || databaseMetaTag.getSubType() == NamedObject.SYSTEM_TABLE
                    || databaseMetaTag.getSubType() == NamedObject.SYSTEM_VIEW
                    || databaseMetaTag.getSubType() == NamedObject.PACKAGE
                    || databaseMetaTag.getSubType() == NamedObject.SYSTEM_PACKAGE
            ) {
                table = databaseMetaTag.getNamedObject(tableString);
                if (table != null)
                    break;
            }
        }

        if (table == null)
            return new ArrayList<>();

        List<AutoCompleteListItem> list = new ArrayList<>();
        for (NamedObject object : table.getObjects()) {

            String description;
            AutoCompleteListItemType colType;

            if (object instanceof DefaultDatabaseProcedure) {
                description = DATABASE_PROCEDURE;
                colType = AutoCompleteListItemType.DATABASE_PROCEDURE;

            } else if (object instanceof DefaultDatabaseFunction) {
                description = DATABASE_FUNCTION;
                colType = AutoCompleteListItemType.DATABASE_FUNCTION;

            } else {
                description = DATABASE_COLUMN;
                colType = AutoCompleteListItemType.DATABASE_TABLE_COLUMN;
            }

            list.add(new AutoCompleteListItem(
                    object.getName(),
                    tableString,
                    object.getDescription(),
                    description,
                    colType
            ));
        }

        return list;
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

    private void trace(String message) {
        Log.trace(message);
    }

    private static class AutoCompleteListItemComparator implements Comparator<AutoCompleteListItem> {

        @Override
        public int compare(AutoCompleteListItem o1, AutoCompleteListItem o2) {
            return o1.getValue().toUpperCase().compareTo(o2.getValue().toUpperCase());
        }

    } // AutoCompleteListItemComparator class

}
