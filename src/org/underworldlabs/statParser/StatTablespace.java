package org.underworldlabs.statParser;

import java.util.ArrayList;
import java.util.List;

public class StatTablespace extends TableModelObject {
   
    public static final String[][] ITEMS_TBS = {
            {"Name:", "s", "name", null},
            {"Full path:", "s", null, bundleString("full_path")},
            {"Table count:", "i", null, bundleString("table_count")},
            {"Index count:", "i", null, bundleString("index_count")},
    };
    
    public String full_path;
    public List<String> tableNames;
    public List<String> indexNames;
    public List<StatTable> tables;
    public List<StatIndex> indices;
    public long id;
    public long table_count;
    public long index_count;
    public long tableName_count;
    public long indexName_count;

    public StatTablespace() {
        tableNames = new ArrayList<>();
        indexNames = new ArrayList<>();
        tables = new ArrayList<>();
        indices = new ArrayList<>();
    }

    @Override
    protected String[][] getItems() {
        return ITEMS_TBS;
    }

    @Override
    int getCountSkipItems() {
        return 0;
    }

    @Override
    public void calculateValues() {
        table_count = tables.size();
        index_count = indices.size();
        /*tableName_count=tableNames.size();
        indexName_count=indexNames.size();*/
    }
}
