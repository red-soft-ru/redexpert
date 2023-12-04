package org.underworldlabs.statParser;

import java.util.List;

public class StatTable extends StatTableIndex {
    public static final String[][] ITEMS_TBL = {
            {"Table name:", "s", "name"},
            {"Primary pointer page:", "i", null},
            {"Index root page:", "i", null},
            {"Pointer pages:", "i", "pointer_pages"},
            {"Data pages:", "i", null},
            {"data page slots:", "i", null},
            {"Primary pages:", "i", null},
            {"secondary pages:", "i", null},
            {"swept pages:", "i", null},
            {"Empty pages:", "i", null},
            {"full pages:", "i", null},
            {"Big record pages:", "i", null},
            {"blob pages:", "i", null},
            {"Average record length:", "f", "avg_record_length"},
            {"total records:", "i", null},
            {"Average version length:", "f", "avg_version_length"},
            {"total versions:", "i", null},
            {"max versions:", "i", null},
            {"average fill:", "p", "avg_fill"},
            {"Total formats:", "i", null},
            {"used formats:", "i", null},
            {"Average fragment length:", "f", "avg_fragment_length"},
            {"total fragments:", "i", null},
            {"max fragments:", "i", null},
            {"Average unpacked length:", "f", "avg_unpacked_length"},
            {"compression ratio:", "f", null},
            {"Blobs:", "i", null},
            {"total length:", "i+", "blobs_total_length"},
            {"Level 0:", "i", null},
            {"Level 1:", "i", null},
            {"Level 2:", "i", null}
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

    public StatTable() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public void calculateValues() {

    }

    @Override
    protected String[][] getItems() {
        return ITEMS_TBL;
    }
}
