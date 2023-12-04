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
    public String name;
    public String table_name;
    public double real_selectivity;
    public int indexId;
    public int depth;
    public int leaf_buckets;
    public int nodes;
    public float avg_data_length;
    public int total_dup;
    public int max_dup;

    public int root_page;
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

    public int getIndexId() {
        return indexId;
    }

    public void setIndexId(int indexId) {
        this.indexId = indexId;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getLeaf_buckets() {
        return leaf_buckets;
    }

    public void setLeaf_buckets(int leaf_buckets) {
        this.leaf_buckets = leaf_buckets;
    }

    public int getNodes() {
        return nodes;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public int getTotal_dup() {
        return total_dup;
    }

    public void setTotal_dup(int total_dup) {
        this.total_dup = total_dup;
    }

    public int getMax_dup() {
        return max_dup;
    }

    public void setMax_dup(int max_dup) {
        this.max_dup = max_dup;
    }

    public FillDistribution getDistribution() {
        return distribution;
    }

    public void setDistribution(FillDistribution distribution) {
        this.distribution = distribution;
    }

    public int getRoot_page() {
        return root_page;
    }

    public void setRoot_page(int root_page) {
        this.root_page = root_page;
    }

    public float getAvg_node_length() {
        return avg_node_length;
    }

    public void setAvg_node_length(float avg_node_length) {
        this.avg_node_length = avg_node_length;
    }

    public float getAvg_key_length() {
        return avg_key_length;
    }

    public void setAvg_key_length(float avg_key_length) {
        this.avg_key_length = avg_key_length;
    }

    public float getCompression_ratio() {
        return compression_ratio;
    }

    public void setCompression_ratio(float compression_ratio) {
        this.compression_ratio = compression_ratio;
    }

    public float getAvg_prefix_length() {
        return avg_prefix_length;
    }

    public void setAvg_prefix_length(float avg_prefix_length) {
        this.avg_prefix_length = avg_prefix_length;
    }

    public float getClustering_factor() {
        return clustering_factor;
    }

    public void setClustering_factor(float clustering_factor) {
        this.clustering_factor = clustering_factor;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }


    @Override
    public void calculateValues() {
        table_name = table.name;
        full_size = (long) (leaf_buckets * Math.pow(1 + (avg_node_length / page_size), depth - 1));
        full_size *= page_size;
        int temp = nodes - total_dup;
        real_selectivity = (1 / (double) temp);
    }

    @Override
    protected String[][] getItems() {
        return ITEMS_IDX;
    }
}