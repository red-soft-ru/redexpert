package org.underworldlabs.statParser;


public class StatIndex extends StatTableIndex {
    public static final String[][] ITEMS_IDX = {
            {"Index name:", "s", "name"},
            {"Table name:", "s", "table_name"},
            {"Real selectivity:", "f+", "real_selectivity"},
            {"Average data length:", "f", "avg_data_length"},
            {"total dup:", "i", null},
            {"max dup:", "i", null},
            {"Root page:", "i", null},
            {"Depth:", "i", null},
            {"leaf buckets:", "i", null},
            {"nodes:", "i", null},
            {"Average node length:", "f", "avg_node_length"},
            {"total dup:", "i", null},
            {"max dup:", "i", null},
            {"Average key length:", "f", "avg_key_length"},
            {"compression ratio:", "f", null},
            {"Average prefix length:", "f", "avg_prefix_length"},
            {"average data length:", "f", "avg_data_length"},
            {"Clustering factor:", "f", null},
            {"ratio:", "f", null},
            {"full size:", "i+", null}
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
    public long page_size;
    public long full_size;

    public StatIndex(StatTable table) {
        this.table = table;
    }

    // Getters and Setters

    public StatTable getTable() {
        return table;
    }

    public void setTable(StatTable table) {
        this.table = table;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public void calculateValues() {
        table_name = table.name;
        full_size = (long) (leaf_buckets * Math.pow(1 + (avg_node_length / page_size), depth - 1));
        full_size *= page_size;
        long temp = nodes - total_dup;
        real_selectivity = (1 / (double) temp);
    }

    @Override
    protected String[][] getItems() {
        return ITEMS_IDX;
    }

    @Override
    int getCountSkipItems() {
        return 2;
    }
}