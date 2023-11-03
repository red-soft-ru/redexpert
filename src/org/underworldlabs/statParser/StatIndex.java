package org.underworldlabs.statParser;

public class StatIndex {
    public StatTable table;
    public String name;
    public int indexId;
    public int depth;
    public int leaf_buckets;
    public int nodes;
    public float avg_data_length;
    public int total_dup;
    public int max_dup;
    public FillDistribution distribution;

    public int root_page;
    public float avg_node_length;
    public float avg_key_length;
    public float compression_ratio;
    public float avg_prefix_length;
    public float clustering_factor;
    public float ratio;

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
}