package org.underworldlabs.statParser;


import java.util.ArrayList;

public class StatIndex extends StatTableIndex {

    public static final String[][] ITEMS_IDX = {
            {"Index name:", "s", "name", null},
            {"Table name:", "s", "table_name", null},
            {"Tablespace:", "s", "tablespaceName", bundleString("tablespace_name")},
            {"Real selectivity:", "f+", "real_selectivity", bundleString("real_selectivity")},
            {"Average data length:", "f", "avg_data_length", bundleString("avg_data_length")},
            {"total dup:", "i", null, bundleString("total_dup")},
            {"max dup:", "i", null, bundleString("max_dup")},
            {"Root page:", "i", null, bundleString("root_page")},
            {"Depth:", "i", null, bundleString("depth")},
            {"leaf buckets:", "i", null, bundleString("leaf_buckets")},
            {"nodes:", "i", null, bundleString("nodes")},
            {"Average node length:", "f", "avg_node_length", bundleString("avg_node_length")},
            {"Average key length:", "f", "avg_key_length", bundleString("avg_key_length")},
            {"compression ratio:", "f", null, bundleString("compression_ratio.index")},
            {"Average prefix length:", "f", "avg_prefix_length", bundleString("avg_prefix_length")},
            {"Clustering factor:", "f", null, bundleString("clustering_factor")},
            {"ratio:", "f", null, bundleString("ratio")},
            {"leaf full size:", "i", null, bundleString("leaf_full_size")},
            {"estimated full size:", "i", null, bundleString("estimated_full_size")},
    };

    public StatTable table;
    public String table_name;
    public double real_selectivity;
    public long indexId;
    public long depth;
    public long leaf_buckets;
    public long nodes;
    public float avg_data_length;
    public long total_dup;
    public long max_dup;

    public long root_page;
    public float avg_node_length;
    public float avg_key_length;
    public float compression_ratio;
    public float avg_prefix_length;
    public float clustering_factor;
    public float ratio;

    public long leaf_full_size;
    public long estimated_full_size;

    public StatIndex(StatTable table) {
        this.table = table;
        if (table != null) {
            if (table.indices == null)
                table.indices = new ArrayList<>();
            table.indices.add(this);
        }
    }

    // Getters and Setters

    public StatTable getTable() {
        return table;
    }

    public void setTable(StatTable table) {
        this.table = table;
    }


    @Override
    public void calculateValues() {
        table_name = table.name;
        leaf_full_size = leaf_buckets * page_size;
        estimated_full_size = (long) (leaf_buckets * Math.pow(1 + (avg_node_length / page_size), depth - 1));
        estimated_full_size *= page_size;
        long temp = nodes - total_dup;
        real_selectivity = (1 / (double) temp);
        calculateTS();
    }

    @Override
    protected String[][] getItems() {
        return ITEMS_IDX;
    }

    @Override
    int getCountSkipItems() {
        return 0;
    }
}