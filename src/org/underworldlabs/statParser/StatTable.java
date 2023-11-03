package org.underworldlabs.statParser;

import java.util.List;

public class StatTable {
    public String name;
    public int table_id;
    public int primary_pointer_page;
    public int index_root_page;
    public float avg_record_length;
    public int total_records;
    public float avg_version_length;
    public int total_versions;
    public int max_versions;
    public int data_pages;
    public int data_page_slots;
    public Double avg_fill;
    public FillDistribution distribution;
    public List<StatIndex> indices;
    public int pointer_pages;
    public int total_formats;
    public int used_formats;
    public Double avg_fragment_length;
    public int total_fragments;
    public int max_fragments;
    public Double avg_unpacked_length;
    public Double compression_ratio;
    public int primary_pages;
    public int secondary_pages;
    public int swept_pages;
    public int empty_pages;
    public int full_pages;
    public int blobs;
    public int blobs_total_length;
    public int blob_pages;
    public int level_0;
    public int level_1;
    public int level_2;

    public StatTable() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTable_id() {
        return table_id;
    }

    public void setTable_id(int table_id) {
        this.table_id = table_id;
    }

    public int getPrimary_pointer_page() {
        return primary_pointer_page;
    }

    public void setPrimary_pointer_page(int primary_pointer_page) {
        this.primary_pointer_page = primary_pointer_page;
    }

    public int getIndex_root_page() {
        return index_root_page;
    }

    public void setIndex_root_page(int index_root_page) {
        this.index_root_page = index_root_page;
    }

    public float getAvg_record_length() {
        return avg_record_length;
    }

    public void setAvg_record_length(float avg_record_length) {
        this.avg_record_length = avg_record_length;
    }

    public int getTotal_records() {
        return total_records;
    }

    public void setTotal_records(int total_records) {
        this.total_records = total_records;
    }

    public float getAvg_version_length() {
        return avg_version_length;
    }

    public void setAvg_version_length(float avg_version_length) {
        this.avg_version_length = avg_version_length;
    }

    public int getTotal_versions() {
        return total_versions;
    }

    public void setTotal_versions(int total_versions) {
        this.total_versions = total_versions;
    }

    public int getMax_versions() {
        return max_versions;
    }

    public void setMax_versions(int max_versions) {
        this.max_versions = max_versions;
    }

    public int getData_pages() {
        return data_pages;
    }

    public void setData_pages(int data_pages) {
        this.data_pages = data_pages;
    }

    public int getData_page_slots() {
        return data_page_slots;
    }

    public void setData_page_slots(int data_page_slots) {
        this.data_page_slots = data_page_slots;
    }

    public Double getAvg_fill() {
        return avg_fill;
    }

    public void setAvg_fill(Double avg_fill) {
        this.avg_fill = avg_fill;
    }

    public FillDistribution getDistribution() {
        return distribution;
    }

    public void setDistribution(FillDistribution distribution) {
        this.distribution = distribution;
    }

    public List<StatIndex> getIndices() {
        return indices;
    }

    public void setIndices(List<StatIndex> indices) {
        this.indices = indices;
    }

    public int getPointer_pages() {
        return pointer_pages;
    }

    public void setPointer_pages(int pointer_pages) {
        this.pointer_pages = pointer_pages;
    }

    public int getTotal_formats() {
        return total_formats;
    }

    public void setTotal_formats(int total_formats) {
        this.total_formats = total_formats;
    }

    public int getUsed_formats() {
        return used_formats;
    }

    public void setUsed_formats(int used_formats) {
        this.used_formats = used_formats;
    }

    public Double getAvg_fragment_length() {
        return avg_fragment_length;
    }

    public void setAvg_fragment_length(Double avg_fragment_length) {
        this.avg_fragment_length = avg_fragment_length;
    }

    public int getTotal_fragments() {
        return total_fragments;
    }

    public void setTotal_fragments(int total_fragments) {
        this.total_fragments = total_fragments;
    }

    public int getMax_fragments() {
        return max_fragments;
    }

    public void setMax_fragments(int max_fragments) {
        this.max_fragments = max_fragments;
    }

    public Double getAvg_unpacked_length() {
        return avg_unpacked_length;
    }

    public void setAvg_unpacked_length(Double avg_unpacked_length) {
        this.avg_unpacked_length = avg_unpacked_length;
    }

    public Double getCompression_ratio() {
        return compression_ratio;
    }

    public void setCompression_ratio(Double compression_ratio) {
        this.compression_ratio = compression_ratio;
    }

    public int getPrimary_pages() {
        return primary_pages;
    }

    public void setPrimary_pages(int primary_pages) {
        this.primary_pages = primary_pages;
    }

    public int getSecondary_pages() {
        return secondary_pages;
    }

    public void setSecondary_pages(int secondary_pages) {
        this.secondary_pages = secondary_pages;
    }

    public int getSwept_pages() {
        return swept_pages;
    }

    public void setSwept_pages(int swept_pages) {
        this.swept_pages = swept_pages;
    }

    public int getEmpty_pages() {
        return empty_pages;
    }

    public void setEmpty_pages(int empty_pages) {
        this.empty_pages = empty_pages;
    }

    public int getFull_pages() {
        return full_pages;
    }

    public void setFull_pages(int full_pages) {
        this.full_pages = full_pages;
    }

    public int getBlobs() {
        return blobs;
    }

    public void setBlobs(int blobs) {
        this.blobs = blobs;
    }

    public int getBlobs_total_length() {
        return blobs_total_length;
    }

    public void setBlobs_total_length(int blobs_total_length) {
        this.blobs_total_length = blobs_total_length;
    }

    public int getBlob_pages() {
        return blob_pages;
    }

    public void setBlob_pages(int blob_pages) {
        this.blob_pages = blob_pages;
    }

    public int getLevel_0() {
        return level_0;
    }

    public void setLevel_0(int level_0) {
        this.level_0 = level_0;
    }

    public int getLevel_1() {
        return level_1;
    }

    public void setLevel_1(int level_1) {
        this.level_1 = level_1;
    }

    public int getLevel_2() {
        return level_2;
    }

    public void setLevel_2(int level_2) {
        this.level_2 = level_2;
    }
}
