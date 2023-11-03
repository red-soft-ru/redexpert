package org.underworldlabs.statParser;

public class StatIndex {
    public StatTable table;
    public String name;
    public int indexId;
    public int depth;
    public int leafBuckets;
    public int nodes;
    public int avgDataLength;
    public int totalDup;
    public int maxDup;
    public FillDistribution distribution;

    public int root_page;
    public int avg_node_length;
    public int avg_key_length;
    public int compression_ratio;
    public int avg_prefix_length;
    public int clustering_factor;
    public int ratio;

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

    public int getLeafBuckets() {
        return leafBuckets;
    }

    public void setLeafBuckets(int leafBuckets) {
        this.leafBuckets = leafBuckets;
    }

    public int getNodes() {
        return nodes;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public int getAvgDataLength() {
        return avgDataLength;
    }

    public void setAvgDataLength(int avgDataLength) {
        this.avgDataLength = avgDataLength;
    }

    public int getTotalDup() {
        return totalDup;
    }

    public void setTotalDup(int totalDup) {
        this.totalDup = totalDup;
    }

    public int getMaxDup() {
        return maxDup;
    }

    public void setMaxDup(int maxDup) {
        this.maxDup = maxDup;
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

    public int getAvg_node_length() {
        return avg_node_length;
    }

    public void setAvg_node_length(int avg_node_length) {
        this.avg_node_length = avg_node_length;
    }

    public int getAvg_key_length() {
        return avg_key_length;
    }

    public void setAvg_key_length(int avg_key_length) {
        this.avg_key_length = avg_key_length;
    }

    public int getCompression_ratio() {
        return compression_ratio;
    }

    public void setCompression_ratio(int compression_ratio) {
        this.compression_ratio = compression_ratio;
    }

    public int getAvg_prefix_length() {
        return avg_prefix_length;
    }

    public void setAvg_prefix_length(int avg_prefix_length) {
        this.avg_prefix_length = avg_prefix_length;
    }

    public int getClustering_factor() {
        return clustering_factor;
    }

    public void setClustering_factor(int clustering_factor) {
        this.clustering_factor = clustering_factor;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }
}