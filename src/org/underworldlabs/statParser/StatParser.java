package org.underworldlabs.statParser;

import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class StatParser {

    public static final int GSTAT_25 = 2;
    public static final int GSTAT_30 = 3;
    public static final List<String> ATTRIBUTES = Arrays.asList("force write", "no reserve", "shared cache disabled",
            "active shadow", "multi-user maintenance", "single-user maintenance", "full shutdown", "read only",
            "backup lock", "backup merge", "wrong backup state");
    public static final int ATTR_FORCE_WRITE = 0;
    public static final int ATTR_NO_RESERVE = 1;
    public static final int ATTR_NO_SHARED_CACHE = 2;
    public static final int ATTR_ACTIVE_SHADOW = 3;
    public static final int ATTR_SHUTDOWN_MULTI = 4;
    public static final int ATTR_SHUTDOWN_SINGLE = 5;
    public static final int ATTR_SHUTDOWN_FULL = 6;
    public static final int ATTR_READ_ONLY = 7;
    public static final int ATTR_BACKUP_LOCK = 8;
    public static final int ATTR_BACKUP_MERGE = 9;
    public static final int ATTR_BACKUP_WRONG = 10;

    public static List<String> items_fill = Arrays.asList("0 - 19%", "20 - 39%", "40 - 59%", "60 - 79%", "80 - 99%");


    //private static final String _LOCALE_ = System.getProperty("java.version").startsWith("1.8") ? "LC_ALL" : "LC_CTYPE";

    public static StatDatabase parse(List<String> lines) {
        StatDatabase db = new StatDatabase();
        int line_no = 0;
        StatTable table = null;
        StatIndex index = null;
        boolean new_block = true;
        boolean in_table = false;
        int step = 0;
        Locale locale = Locale.getDefault();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy", Locale.forLanguageTag("en"));
        for (String line : lines) {
            try {
                line = line.trim();
                line_no += 1;

                if (line.startsWith("Gstat completion time")) {
                    db.setCompleted(LocalDateTime.parse(line.substring(22).replace("  ", " 0"), formatter));
                } else if (step == 0) {
                    if (line.startsWith("Gstat execution time")) {
                        db.setExecuted(LocalDateTime.parse(line.substring(21).replace("  ", " 0"), formatter));
                    } else if (line.startsWith("Database header page information:")) {
                        step = 1;
                    } else if (line.startsWith("Variable header data:")) {
                        step = 2;
                    } else if (line.startsWith("Database file sequence:")) {
                        step = 3;
                    } else if (line.contains("encrypted") && line.contains("non-crypted")) {
                        parse_encryption(line, db, line_no);
                    } else if (line.startsWith("Analyzing database pages ...")) {
                        step = 4;
                    } else if (MiscUtils.isNull(line)) {
                        continue;
                    } else if (line.startsWith("Database \"")) {
                        String[] parts = line.split(" ");
                        db.setFilename(parts[1]);
                        step = 0;
                    } else {
                        throw new ParseError("Unrecognized data (line " + line_no + ")");
                    }
                } else if (step == 1) {
                    if (MiscUtils.isNull(line)) {
                        step = 0;
                    } else {
                        parse_hdr(line, db, line_no);
                    }
                } else if (step == 2) {
                    if (MiscUtils.isNull(line)) {
                        step = 0;
                    } else {
                        parse_var(line, db, line_no);
                    }
                } else if (step == 3) {
                    if (MiscUtils.isNull(line)) {
                        step = 0;
                    } else {
                        parse_fseq(line, db, line_no);
                    }
                } else if (step == 4) {
                    if (MiscUtils.isNull(line)) {
                        new_block = true;
                    } else {
                        if (new_block) {
                            new_block = false;
                            if (!line.startsWith("Index ")) {
                                table = new StatTable();
                                db.getTables().add(table);
                                in_table = true;
                                parse_table(line, table, db, line_no);
                            } else {
                                index = new StatIndex(table);
                                db.getIndices().add(index);
                                in_table = false;
                                parse_index(line, index, db, line_no);
                            }
                        } else {
                            if (in_table) {
                                parse_table(line, table, db, line_no);
                            } else {
                                parse_index(line, index, db, line_no);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.info("error parse " + line_no + " line:" + e.getMessage());
                //e.printStackTrace();
            }
        }

        if (db.has_table_stats()) {
            for (StatTable t : db.getTables()) {
                t.setDistribution(new FillDistribution());
            }
        }

        if (db.has_index_stats()) {
            for (StatIndex i : db.getIndices()) {
                i.setDistribution(new FillDistribution());
            }
        }

        //db.tables.freeze();TODO check freeze
        //db.indices.freeze();


        return db;
    }

    public static void parse_hdr(String line, StatDatabase db, int line_no) throws ParseError, NoSuchFieldException, IllegalAccessException {
        String[][] items_hdr = {
                {"Flags", "i", null},
                {"Checksum", "i", null},
                {"Generation", "i", null},
                {"System Change Number", "i", "system_change_number"},
                {"Page size", "i", null},
                {"Server", "s", null},
                {"ODS version", "s", "ods_version"},
                {"Oldest transaction", "i", "oit"},
                {"Oldest active", "i", "oat"},
                {"Oldest snapshot", "i", "ost"},
                {"Next transaction", "i", null},
                {"Bumped transaction", "i", null},
                {"Autosweep gap", "i", null},
                {"Sequence number", "i", null},
                {"Next attachment ID", "i", null},
                {"Implementation ID", "i", null},
                {"Implementation", "s", null},
                {"Shadow count", "i", null},
                {"Page buffers", "i", null},
                {"Next header page", "i", null},
                {"Database dialect", "i", null},
                {"Creation date", "d", null},
                {"Attributes", "l", null}
        };

        for (String[] item : items_hdr) {
            String key = item[0];
            String valtype = item[1];
            String name = item[2];
            if (name == null) {
                name = key.substring(0, 1).toLowerCase() + key.substring(1);
                name = name.replace(" ", "_");
            }

            if (line.startsWith(key)) {
                if (db.getGstat_version() == 0) {
                    if (key.equals("Checksum")) {
                        db.setGstat_version(GSTAT_25);
                    } else if (key.equals("System Change Number")) {
                        db.setGstat_version(GSTAT_30);
                    }
                }

                String value = line.substring(key.length()).trim();

                if (valtype.equals("i")) {
                    db.getClass().getField(name).setInt(db, Integer.parseInt(value));
                } else if (valtype.equals("s")) {
                    db.getClass().getField(name).set(db, value);
                } else if (valtype.equals("d")) {
                    LocalDateTime dateTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("MMM d, yyyy H:mm:ss", Locale.forLanguageTag("en")));
                    db.getClass().getField(name).set(db, dateTime);
                } else if (valtype.equals("l")) {
                    if (value.equals("")) {
                        db.getClass().getField(name).set(db, new ArrayList<>());
                    } else {
                        String[] values = value.split(",");
                        List<Integer> list = new ArrayList<>();
                        for (String v : values) {
                            list.add(ATTRIBUTES.indexOf(v.trim()));
                        }
                        db.getClass().getField(name).set(db, list);
                    }
                } else {
                    throw new ParseError("Unknown value type " + valtype);
                }

                return;
            }
        }

        throw new ParseError("Unknown information (line " + line_no + ")");
    }

    public static void parse_var(String line, StatDatabase db, int line_no) throws ParseError, NoSuchFieldException, IllegalAccessException {
        String[][] items_var = {
                {"Sweep interval:", "i", null},
                {"Continuation file:", "s", null},
                {"Last logical page:", "i", null},
                {"Database backup GUID:", "s", "backup_guid"},
                {"Root file name:", "s", "root_filename"},
                {"Replay logging file:", "s", null},
                {"Backup difference file:", "s", "backup_diff_file"}
        };

        for (String[] item : items_var) {
            String key = item[0];
            String valtype = item[1];
            String name = item[2];
            if (name == null) {
                name = key.substring(0, 1).toLowerCase() + key.substring(1);
                name = name.replace(" ", "_");
            }

            if (line.startsWith(key)) {
                String value = line.substring(key.length()).trim();

                if (valtype.equals("i")) {
                    db.getClass().getField(name).setInt(db, Integer.parseInt(value));
                } else if (valtype.equals("s")) {
                    db.getClass().getField(name).set(db, value);
                } else {
                    throw new ParseError("Unknown value type " + valtype);
                }

                return;
            }
        }

        throw new ParseError("Unknown information (line " + line_no + ")");
    }

    public static void parse_fseq(String line, StatDatabase db, int line_no) throws ParseError {
        if (!line.startsWith("File ")) {
            throw new ParseError("Bad file specification (line " + line_no + ")");
        }

        if (line.contains("is the only file")) {
            return;
        }

        if (line.contains(" is the ")) {
            db.getContinuation_files().add(line.substring(5, line.indexOf(" is the ")));
        } else {
            throw new ParseError("Bad file specification (line " + line_no + ")");
        }
    }

    public static void parse_table(String line, StatTable table, StatDatabase db, int line_no) throws ParseError, NoSuchFieldException, IllegalAccessException {
        if (table.getName() == null) {
            String[] parts = line.split(" \\(");
            table.setName(parts[0]);
            table.setTable_id(Integer.parseInt(parts[1].replace("(", "").replace(")", "")));
        } else {
            if (line.contains(",")) {
                String[] items = line.split(",");
                for (String item : items) {
                    item = item.trim();
                    boolean found = false;
                    String[][] items_tbl = {
                            {"Primary pointer page:", "i", null},
                            {"Index root page:", "i", null},
                            {"Pointer pages:", "i", "pointer_pages"},
                            {"Average record length:", "f", "avg_record_length"},
                            {"total records:", "i", null},
                            {"Average version length:", "f", "avg_version_length"},
                            {"total versions:", "i", null},
                            {"max versions:", "i", null},
                            {"Data pages:", "i", null},
                            {"data page slots:", "i", null},
                            {"average fill:", "p", "avg_fill"},
                            {"Primary pages:", "i", null},
                            {"secondary pages:", "i", null},
                            {"swept pages:", "i", null},
                            {"Empty pages:", "i", null},
                            {"full pages:", "i", null},
                            {"Primary pointer page:", "i", null},
                            {"Index root page:", "i", null},
                            {"Total formats:", "i", null},
                            {"used formats:", "i", null},
                            {"Average record length:", "f", "avg_record_length"},
                            {"total records:", "i", null},
                            {"Average version length:", "f", "avg_version_length"},
                            {"total versions:", "i", null},
                            {"max versions:", "i", null},
                            {"Average fragment length:", "f", "avg_fragment_length"},
                            {"total fragments:", "i", null},
                            {"max fragments:", "i", null},
                            {"Average unpacked length:", "f", "avg_unpacked_length"},
                            {"compression ratio:", "f", null},
                            {"Pointer pages:", "i", "pointer_pages"},
                            {"data page slots:", "i", null},
                            {"Data pages:", "i", null},
                            {"average fill:", "p", "avg_fill"},
                            {"Primary pages:", "i", null},
                            {"secondary pages:", "i", null},
                            {"swept pages:", "i", null},
                            {"Empty pages:", "i", null},
                            {"full pages:", "i", null},
                            {"Blobs:", "i", null},
                            {"total length:", "i", "blobs_total_length"},
                            {"blob pages:", "i", null},
                            {"Level 0:", "i", null},
                            {"Level 1:", "i", null},
                            {"Level 2:", "i", null}
                    };

                    if (!line.startsWith("Fill distribution:")) {
                        for (String[] item_tbl : items_tbl) {
                            String key = item_tbl[0];
                            String valtype = item_tbl[1];
                            String name = item_tbl[2];

                            if (item.startsWith(key)) {
                                String value = item.substring(key.length()).trim();

                                if (valtype.equals("i")) {
                                    table.getClass().getField(name).setInt(table, Integer.parseInt(value));
                                } else if (valtype.equals("f")) {
                                    table.getClass().getField(name).setFloat(table, Float.parseFloat(value));
                                } else if (valtype.equals("p")) {
                                    table.getClass().getField(name).setInt(table, Integer.parseInt(value.replace("%", "")));
                                } else {
                                    throw new ParseError("Unknown value type " + valtype);
                                }

                                found = true;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        throw new ParseError("Unknown information (line " + line_no + ")");
                    }
                }
            } else {
                if (line.contains("=")) {
                    String[] parts = line.split("=");
                    String fill_range = parts[0].trim();
                    String fill_value = parts[1];
                    int i = items_fill.indexOf(fill_range.trim());

                    if (table.getDistribution() == null) {
                        table.setDistribution(new FillDistribution(0, 0, 0, 0, 0));
                    }

                    switch (i) {
                        case 0:
                            table.getDistribution().range_0_19 = Integer.parseInt(fill_value.trim());
                            break;
                        case 1:
                            table.getDistribution().range_20_39 = Integer.parseInt(fill_value.trim());
                            break;
                        case 2:
                            table.getDistribution().range_40_59 = Integer.parseInt(fill_value.trim());
                            break;
                        case 3:
                            table.getDistribution().range_60_79 = Integer.parseInt(fill_value.trim());
                            break;
                        case 4:
                            table.getDistribution().range_80_99 = Integer.parseInt(fill_value.trim());
                            break;
                    }
                } else if (!line.startsWith("Fill distribution:")) {
                    throw new ParseError("Unknown information (line " + line_no + ")");
                }
            }
        }
    }

    public static void parse_index(String line, StatIndex index, StatDatabase db, int line_no) throws ParseError, NoSuchFieldException, IllegalAccessException {
        if (index.getName() == null) {
            String[] parts = line.substring(6).split(" \\(");
            index.setName(parts[0]);
            index.setIndexId(Integer.parseInt(parts[1].replace("(", "").replace(")", "")));
        } else {
            if (line.contains(",")) {
                String[] items = line.split(",");
                for (String item : items) {
                    item = item.trim();
                    boolean found = false;
                    String[][] items_idx = {
                            {"Depth:", "i", null},
                            {"leaf buckets:", "i", null},
                            {"nodes:", "i", null},
                            {"Average data length:", "f", "avg_data_length"},
                            {"total dup:", "i", null},
                            {"max dup:", "i", null},
                            {"Root page:", "i", null},
                            {"depth:", "i", null},
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
                            {"ratio:", "f", null}
                    };

                    if (!line.startsWith("Fill distribution:")) {
                        for (String[] item_idx2 : items_idx) {
                            String key = item_idx2[0];
                            String valtype = item_idx2[1];
                            String name = item_idx2[2];
                            if (name == null) {
                                name = key.substring(0, 1).toLowerCase() + key.substring(1);
                                name = name.replace(" ", "_");
                            }

                            if (item.startsWith(key)) {
                                String value = item.substring(key.length()).trim();

                                if (valtype.equals("i")) {
                                    index.getClass().getField(name).setInt(index, Integer.parseInt(value));
                                } else if (valtype.equals("f")) {
                                    index.getClass().getField(name).setFloat(index, Float.parseFloat(value));
                                } else {
                                    throw new ParseError("Unknown value type " + valtype);
                                }

                                found = true;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        throw new ParseError("Unknown information (line " + line_no + ")");
                    }
                }
            } else {
                if (line.contains("=")) {
                    String[] parts = line.split("=");
                    String fill_range = parts[0].trim();
                    String fill_value = parts[1];
                    int i = items_fill.indexOf(fill_range.trim());

                    if (index.getDistribution() == null) {
                        index.setDistribution(new FillDistribution(0, 0, 0, 0, 0));
                    }

                    switch (i) {
                        case 0:
                            index.getDistribution().range_0_19 = Integer.parseInt(fill_value.trim());
                            break;
                        case 1:
                            index.getDistribution().range_20_39 = Integer.parseInt(fill_value.trim());
                            break;
                        case 2:
                            index.getDistribution().range_40_59 = Integer.parseInt(fill_value.trim());
                            break;
                        case 3:
                            index.getDistribution().range_60_79 = Integer.parseInt(fill_value.trim());
                            break;
                        case 4:
                            index.getDistribution().range_80_99 = Integer.parseInt(fill_value.trim());
                            break;
                    }
                } else if (!line.startsWith("Fill distribution:")) {
                    throw new ParseError("Unknown information (line " + line_no + ")");
                }
            }
        }
    }

    public static void parse_encryption(String line, StatDatabase db, int line_no) throws ParseError {
        try {
            String[] parts = line.split(",");
            String total = parts[0].trim();
            String encrypted = parts[1].trim();
            String unencrypted = parts[2].trim();

            int totalValue = Integer.parseInt(total.substring(total.lastIndexOf(" ") + 1));
            int encryptedValue = Integer.parseInt(encrypted.substring(encrypted.lastIndexOf(" ") + 1));
            int unencryptedValue = Integer.parseInt(unencrypted.substring(unencrypted.lastIndexOf(" ") + 1));

            Encryption data;

            if (line.contains("Data pages:")) {
                data = new Encryption(totalValue, encryptedValue, unencryptedValue);
                db.setEncrypted_data_pages(data);
            } else if (line.contains("Index pages:")) {
                data = new Encryption(totalValue, encryptedValue, unencryptedValue);
                db.setEncrypted_index_pages(data);
            } else if (line.contains("Blob pages:")) {
                data = new Encryption(totalValue, encryptedValue, unencryptedValue);
                db.setEncrypted_blob_pages(data);
            } else {
                throw new ParseError("Unknown encryption information (line " + line_no + ")");
            }
        } catch (Exception e) {
            throw new ParseError("Malformed encryption information (line " + line_no + ")");
        }
    }

    static class ParseError extends Exception {
        public ParseError(String message) {
            super(message);
        }
    }
}
