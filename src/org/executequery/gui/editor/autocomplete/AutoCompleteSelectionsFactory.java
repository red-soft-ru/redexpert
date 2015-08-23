/*
 * AutoCompleteSelectionsFactory.java
 *
 * Copyright (C) 2002-2013 Takis Diakoumis
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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseSource;
import org.executequery.databaseobjects.impl.ColumnInformation;
import org.executequery.databaseobjects.impl.ColumnInformationFactory;
import org.executequery.log.Log;
import org.executequery.repository.KeywordRepository;
import org.executequery.repository.RepositoryCache;

public class AutoCompleteSelectionsFactory {

    private static final String DATABASE_TABLE_DESCRIPTION = "Database Table";
    
    private static final String DATABASE_FUNCTION_DESCRIPTION = "Database Function";
    
    private static final String DATABASE_PROCEDURE_DESCRIPTION = "Database Procedure";
    
    private static final String DATABASE_TABLE_VIEW = "Database View";
    
    private static final String DATABASE_COLUMN_DESCRIPTION = "Database Column";
    
    private static final String DATABASE_SYSTEM_FUNCTION_DESCRIPTION = "System Function";
    
    private QueryEditorAutoCompletePopupProvider provider;
    
    private List<AutoCompleteListItem> tables;
    
    public AutoCompleteSelectionsFactory(QueryEditorAutoCompletePopupProvider provider) {
        super();
        this.provider = provider;
    }

    public void build(DatabaseHost databaseHost, boolean autoCompleteKeywords, boolean autoCompleteSchema) {

        tables = new ArrayList<AutoCompleteListItem>();
        
        List<AutoCompleteListItem> listSelections = new ArrayList<AutoCompleteListItem>();
        if (autoCompleteKeywords) {
        
            addSQL92Keywords(listSelections);
            addUserDefinedKeywords(listSelections);
            
            addToProvider(listSelections);
        }

        if (databaseHost != null && databaseHost.isConnected()) {

            if (autoCompleteKeywords) {
            
                addDatabaseDefinedKeywords(databaseHost, listSelections);
                databaseSystemFunctionsForHost(databaseHost, listSelections);
                addToProvider(listSelections);
            }

            if (autoCompleteSchema) {

                databaseTablesForHost(databaseHost);
                databaseColumnsForTables(databaseHost, tables);
                databaseFunctionsAndProceduresForHost(databaseHost);
            }

        }

    }

    private void addToProvider(List<AutoCompleteListItem> listSelections) {

        provider.addListItems(listSelections);
        listSelections.clear();
    }
    
    public List<AutoCompleteListItem> buildKeywords(DatabaseHost databaseHost, boolean autoCompleteKeywords) {

        List<AutoCompleteListItem> listSelections = new ArrayList<AutoCompleteListItem>();
        if (autoCompleteKeywords) {
        
            addSQL92Keywords(listSelections);
            addUserDefinedKeywords(listSelections);
    
            if (databaseHost != null && databaseHost.isConnected()) {
    
                databaseSystemFunctionsForHost(databaseHost, listSelections);
                addDatabaseDefinedKeywords(databaseHost, listSelections);
            }
    
            Collections.sort(listSelections, new AutoCompleteListItemComparator());
        }

        return listSelections;
    }

    private void databaseFunctionsAndProceduresForHost(DatabaseHost databaseHost) {

        databaseExecutableForHost(databaseHost, "FUNCTION", DATABASE_FUNCTION_DESCRIPTION, AutoCompleteListItemType.DATABASE_FUNCTION);
        databaseExecutableForHost(databaseHost, "PROCEDURE", DATABASE_PROCEDURE_DESCRIPTION, AutoCompleteListItemType.DATABASE_PROCEDURE);
    }
    
    private void databaseTablesForHost(DatabaseHost databaseHost) {

        databaseObjectsForHost(databaseHost, "TABLE", DATABASE_TABLE_DESCRIPTION, AutoCompleteListItemType.DATABASE_TABLE);
        databaseObjectsForHost(databaseHost, "VIEW", DATABASE_TABLE_VIEW, AutoCompleteListItemType.DATABASE_VIEW);
    }

    private void databaseSystemFunctionsForHost(DatabaseHost databaseHost, List<AutoCompleteListItem> listSelections) {
        
        trace("Building autocomplete object list using [ " + databaseHost.getName() + " ] for type - SYSTEM_FUNCTION");
        
        ResultSet rs = null;
        DatabaseMetaData databaseMetaData = databaseHost.getDatabaseMetaData();
        
        try {

            List<String> tableNames = new ArrayList<String>();

            extractNames(tableNames, databaseMetaData.getStringFunctions());
            extractNames(tableNames, databaseMetaData.getNumericFunctions());
            extractNames(tableNames, databaseMetaData.getTimeDateFunctions());

            addKeywordsFromList(tableNames, listSelections, DATABASE_SYSTEM_FUNCTION_DESCRIPTION, AutoCompleteListItemType.SYSTEM_FUNCTION);
            
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
            for (String name : names) {
                
                tableNames.add(name);
            }

        }
    }
    
    private static final int INCREMENT = 5;
    
    private void databaseObjectsForHost(DatabaseHost databaseHost, String type, 
            String databaseObjectDescription, AutoCompleteListItemType autocompleteType) {
        
        trace("Building autocomplete object list using [ " + databaseHost.getName() + " ] for type - " + type);
		
		ResultSet rs = null;
		DatabaseMetaData databaseMetaData = databaseHost.getDatabaseMetaData();
        try {
            String catalog = databaseHost.getCatalogNameForQueries(defaultCatalogForHost(databaseHost));
            String schema = databaseHost.getSchemaNameForQueries(defaultSchemaForHost(databaseHost));

            String typeName = null;
            List<String> tableNames = new ArrayList<String>();
            List<AutoCompleteListItem> list = new ArrayList<AutoCompleteListItem>();
            String[] types = new String[]{type};

            int count = 0;
            
            rs = databaseMetaData.getTables(catalog, schema, null, types);
            while (rs.next()) {

                try {
                    if (Thread.interrupted() || databaseMetaData.getConnection().isClosed()) {

                        return;
                    }
                } catch (SQLException e) {}
                
                typeName = rs.getString(4);

                // only include if the returned reported type matches
                if (type != null && type.equalsIgnoreCase(typeName)) {

                    tableNames.add(rs.getString(3));
                    count++;
                }
                
                if (count >= INCREMENT) {
                    
                    addTablesToProvider(databaseObjectDescription, autocompleteType, tableNames, list);
                    count = 0;
                    list.clear();
                    tableNames.clear();
                }
                
            }
            
            addTablesToProvider(databaseObjectDescription, autocompleteType, tableNames, list);

        } catch (SQLException e) {

            error("Tables not available for type " + type + " - driver returned: " + e.getMessage());

        } finally {

            releaseResources(rs);
            trace("Finished autocomplete object list using [ " + databaseHost.getName() + " ] for type - " + type);
        }
    		
    }

    @SuppressWarnings("resource")
    private void databaseExecutableForHost(DatabaseHost databaseHost, String type, 
            String databaseObjectDescription, AutoCompleteListItemType autocompleteType) {
        
        trace("Building autocomplete object list using [ " + databaseHost.getName() + " ] for type - " + type);
        
        ResultSet rs = null;
        try {

            DatabaseMetaData databaseMetaData = databaseHost.getDatabaseMetaData();
            String catalog = databaseHost.getCatalogNameForQueries(defaultCatalogForHost(databaseHost));
            String schema = databaseHost.getSchemaNameForQueries(defaultSchemaForHost(databaseHost));

            List<String> names = new ArrayList<String>();
            List<AutoCompleteListItem> list = new ArrayList<AutoCompleteListItem>();
            
            if (autocompleteType == AutoCompleteListItemType.DATABASE_FUNCTION) {
                
                try {
                
                    rs = databaseMetaData.getFunctions(catalog, schema, null);

                } catch (Throwable e) {

                    trace("Functions not available using [ getFunctions() ] - reverting to [ getProcedures() ] - " + e.getMessage());
                    rs = getProcedures(databaseMetaData, catalog, schema);
                }
                
            } else {
                
                rs = getProcedures(databaseMetaData, catalog, schema);
            }
            
            if (rs != null) {
            
                int count = 0;
                while (rs.next()) {
    
                    try {
                        if (Thread.interrupted() || databaseMetaData.getConnection().isClosed()) {
    
                            return;
                        }
                    } catch (SQLException e) {}
                    
                    names.add(rs.getString(3));
                    count++;
                    
                    if (count >= INCREMENT) {
                        
                        addTablesToProvider(databaseObjectDescription, autocompleteType, names, list);
                        count = 0;
                        list.clear();
                        names.clear();
                    }
                    
                }
                
                addTablesToProvider(databaseObjectDescription, autocompleteType, names, list);

            }

        } catch (Exception e) {

            error("Tables not available for type " + type + " - driver returned: " + e.getMessage());

        } finally {

            releaseResources(rs);
            trace("Finished autocomplete object list using [ " + databaseHost.getName() + " ] for type - " + type);
        }
            
    }

    private ResultSet getProcedures(DatabaseMetaData databaseMetaData,
            String catalog, String schema) throws SQLException {
        ResultSet rs;
        rs = databaseMetaData.getProcedures(catalog, schema, null);
        return rs;
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

    private ColumnInformationFactory columnInformationFactory = new ColumnInformationFactory();
    
    private void databaseColumnsForTables(DatabaseHost databaseHost, List<AutoCompleteListItem> tables) {

        trace("Retrieving column names for tables for host [ " + databaseHost.getName() + " ]");

        ResultSet rs = null;
        List<ColumnInformation> columns = new ArrayList<ColumnInformation>();
        List<AutoCompleteListItem> list = new ArrayList<AutoCompleteListItem>();

        String catalog = databaseHost.getCatalogNameForQueries(defaultCatalogForHost(databaseHost));
        String schema = databaseHost.getSchemaNameForQueries(defaultSchemaForHost(databaseHost));
        DatabaseMetaData dmd = databaseHost.getDatabaseMetaData();

        for (int i = 0, n = tables.size(); i < n; i++) {

            try {
                if (Thread.interrupted() || dmd.getConnection().isClosed()) {
                    
                    return;
                }
            } catch (SQLException e) {}
            
            AutoCompleteListItem table = tables.get(i);            
            if (table == null) {
                
                continue;
            }
            
            trace("Retrieving column names for table [ " + table.getValue() + " ]");
        
            try {
            
                rs = dmd.getColumns(catalog, schema, table.getValue(), null);
                while (rs.next()) {

                    String name = rs.getString(4);
                    columns.add(columnInformationFactory.build(
                            table.getValue(), 
                            name, 
                            rs.getString(6),
                            rs.getInt(5),
                            rs.getInt(7),
                            rs.getInt(9),
                            rs.getInt(11) == DatabaseMetaData.columnNoNulls));
                }
            
                for (ColumnInformation column : columns) {
                    
                    list.add(new AutoCompleteListItem(
                            column.getName(), 
                            table.getValue(),
                            column.getDescription(),
                            DATABASE_COLUMN_DESCRIPTION, 
                            AutoCompleteListItemType.DATABASE_TABLE_COLUMN)); 
                }
                
                provider.addListItems(list);
                releaseResources(rs);
                columns.clear();
                list.clear();
            
            } catch (Throwable e) {

                // don't want to break the editor here so just log and bail...
                
                error("Error retrieving column data for table " + table.getDisplayValue() + " - driver returned: " + e.getMessage());
                
            } finally {

                releaseResources(rs);
            }

        }
        
        trace("Finished retrieving column names for tables for host [ " + databaseHost.getName() + " ]");
    }

    private String defaultSchemaForHost(DatabaseHost databaseHost) {
        
        if (databaseHost.isConnected()) {

            DatabaseSource schema = databaseHost.getDefaultSchema();
            if (schema != null) {
            
                return schema.getName();
            }
        }        
        return null;
    }

    private String defaultCatalogForHost(DatabaseHost databaseHost) {

        if (databaseHost.isConnected()) {
        
            DatabaseSource catalog = databaseHost.getDefaultCatalog();
            if (catalog != null) {
            
                return catalog.getName();
            }
        }        
        return null;
    }

    private void addDatabaseDefinedKeywords(DatabaseHost databaseHost, List<AutoCompleteListItem> list) {

        String[] keywords = databaseHost.getDatabaseKeywords();
        List<String> asList = new ArrayList<String>();

        for (String keyword : keywords) {
            
            asList.add(keyword);
        }

        addKeywordsFromList(asList, list, 
                "Database Defined Keyword", AutoCompleteListItemType.DATABASE_DEFINED_KEYWORD);
    }

    private void addSQL92Keywords(List<AutoCompleteListItem> list) {

        addKeywordsFromList(keywords().getSQL92(),
                list, "SQL92 Keyword", AutoCompleteListItemType.SQL92_KEYWORD);
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
    
    private KeywordRepository keywords() {

        return (KeywordRepository)RepositoryCache.load(KeywordRepository.REPOSITORY_ID);
    }

    
    static class AutoCompleteListItemComparator implements Comparator<AutoCompleteListItem> {

        public int compare(AutoCompleteListItem o1, AutoCompleteListItem o2) {

            return o1.getValue().toUpperCase().compareTo(o2.getValue().toUpperCase());
        }
        
    }

    private void releaseResources(ResultSet rs) {
        try {
            if (rs != null) {

                rs.close();
            }
        } catch (SQLException sqlExc) {}
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
