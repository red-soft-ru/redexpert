package org.underworldlabs.statParser;

import java.util.List;

public class StatTable extends StatTableIndex {
    
    public static final String[][] ITEMS_TBL = {
            {"Table name:", "s", "name", null},
            {"Tablespace:", "s", "tablespaceName", bundleString("tablespace_name")},
            {"Primary pointer page:", "i", null, bundleString("primary_pointer_page")},
            {"Index root page:", "i", null, bundleString("index_root_page")},
            {"Pointer pages:", "i", "pointer_pages", bundleString("pointer_pages")},
            {"Data pages:", "i", null, bundleString("data_pages")},
            {"data page slots:", "i", null, bundleString("data_page_slots")},
            {"Primary pages:", "i", null, bundleString("primary_pages")},
            {"secondary pages:", "i", null, bundleString("secondary_pages")},
            {"swept pages:", "i", null, bundleString("swept_pages")},
            {"Empty pages:", "i", null, bundleString("empty_pages")},
            {"full pages:", "i", null, bundleString("full_pages")},
            {"Big record pages:", "i", null, bundleString("big_record_pages")},
            {"blob pages:", "i", null, bundleString("blob_pages")},
            {"Average record length:", "f", "avg_record_length", bundleString("avg_record_length")},
            {"total records:", "i", null, bundleString("total_records")},
            {"Average version length:", "f", "avg_version_length", bundleString("avg_version_length")},
            {"total versions:", "i", null, bundleString("total_versions")},
            {"max versions:", "i", null, bundleString("max_versions")},
            {"average fill:", "p", "avg_fill", bundleString("avg_fill")},
            {"Total formats:", "i", null, bundleString("total_formats")},
            {"used formats:", "i", null, bundleString("used_formats")},
            {"Average fragment length:", "f", "avg_fragment_length", bundleString("avg_fragment_length")},
            {"total fragments:", "i", null, bundleString("total_fragments")},
            {"max fragments:", "i", null, bundleString("max_fragments")},
            {"Average unpacked length:", "f", "avg_unpacked_length", bundleString("avg_unpacked_length")},
            {"compression ratio:", "f", null, bundleString("compression_ratio")},
            {"Blobs:", "i", null, bundleString("blobs")},
            {"total length:", "i+", "blobs_total_length", bundleString("blobs_total_length")},
            {"Level 0:", "i", null, bundleString("level_0")},
            {"Level 1:", "i", null, bundleString("level_1")},
            {"Level 2:", "i", null, bundleString("level_2")},
            {"table size(without blobs):", "i", "table_size", bundleString("table_size")},
            {"size with blobs:", "i", "size_with_blobs", bundleString("size_with_blobs")},
            {"size with blob_pages:", "i", "size_with_blob_pages", bundleString("size_with_blob_pages")},
            {"full size with indices:", "i", "size_with_indices", bundleString("size_with_indices")},
    };

    public long table_id;
    public long primary_pointer_page;
    public long index_root_page;
    public float avg_record_length;
    public long total_records;
    public float avg_version_length;
    public long total_versions;
    public long max_versions;
    public long data_pages;
    public long data_page_slots;
    public long avg_fill;

    public List<StatIndex> indices;
    public long pointer_pages;
    public long total_formats;
    public long used_formats;
    public float avg_fragment_length;
    public long total_fragments;
    public long max_fragments;
    public float avg_unpacked_length;
    public float compression_ratio;
    public long primary_pages;
    public long secondary_pages;
    public long swept_pages;
    public long empty_pages;
    public long full_pages;
    public long blobs;
    public long blobs_total_length;
    public long blob_pages;
    public long big_record_pages;
    public long level_0;
    public long level_1;
    public long level_2;
    public long table_size;
    public long table_size_level0;
    public long level12;
    public long size_with_blobs;
    public long size_with_blob_pages;
    public long size_with_indices;

    public StatTable() {
    }


    @Override
    public void calculateValues() {
        table_size = data_pages * page_size;
        size_with_blobs = table_size + blobs_total_length;
        size_with_blob_pages = table_size + blob_pages * page_size;
        long ind_size = 0;
        if (indices != null)
            for (StatIndex index : indices) {
                ind_size += index.estimated_full_size;
            }
        size_with_indices = size_with_blob_pages + ind_size;
        calculateTS();
    }

    @Override
    protected String[][] getItems() {
        return ITEMS_TBL;
    }

    @Override
    int getCountSkipItems() {
        return 0;
    }
}
