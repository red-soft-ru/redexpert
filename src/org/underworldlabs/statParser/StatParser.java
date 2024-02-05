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

    public static final int STEP_FREE_PARSE = 0;
    public static final int STEP_HEADER_PARSE = STEP_FREE_PARSE + 1;
    public static final int STEP_VARIABLES_HEADER_PARSE = STEP_HEADER_PARSE + 1;
    public static final int STEP_DATABASE_FILE_PARSE = STEP_VARIABLES_HEADER_PARSE + 1;
    public static final int STEP_TABLESPACES_PARSE = STEP_DATABASE_FILE_PARSE + 1;
    public static final int STEP_DATABASE_PAGES_PARSE = STEP_TABLESPACES_PARSE + 1;

    public static List<String> items_fill = Arrays.asList("0 - 19%", "20 - 39%", "40 - 59%", "60 - 79%", "80 - 99%");


    //private static final String _LOCALE_ = System.getProperty("java.version").startsWith("1.8") ? "LC_ALL" : "LC_CTYPE";

    public static ParserParameters parse(ParserParameters parserParameters, String line) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy", Locale.forLanguageTag("en"));
        try {
            line = line.trim();
            parserParameters.line_no += 1;
            StatDatabase db = parserParameters.db;
            if (line.startsWith("Gstat completion time")) {
                db.setCompleted(LocalDateTime.parse(line.substring(22).replace("  ", " 0"), formatter));
            } else if (parserParameters.step == STEP_FREE_PARSE) {
                if (line.startsWith("Gstat execution time")) {
                    db.setExecuted(LocalDateTime.parse(line.substring(21).replace("  ", " 0"), formatter));
                } else if (line.startsWith("Database header page information:")) {
                    parserParameters.step = STEP_HEADER_PARSE;
                } else if (line.startsWith("Variable header data:")) {
                    parserParameters.step = STEP_VARIABLES_HEADER_PARSE;
                } else if (line.startsWith("Database file sequence:")) {
                    parserParameters.step = STEP_DATABASE_FILE_PARSE;
                } else if (line.startsWith("Tablespaces:")) {
                    parserParameters.step = STEP_TABLESPACES_PARSE;
                } else if (line.contains("encrypted") && line.contains("non-crypted")) {
                    parse_encryption(line, db, parserParameters.line_no);
                } else if (line.startsWith("Analyzing database pages ...")) {
                    parserParameters.step = STEP_DATABASE_PAGES_PARSE;
                } else if (MiscUtils.isNull(line)) {
                    return parserParameters;
                } else if (line.startsWith("Database \"")) {
                    String[] parts = line.split(" ");
                    db.setFilename(parts[1]);
                    parserParameters.step = STEP_FREE_PARSE;
                } else {
                    throw new ParseError("Unrecognized data (line " + parserParameters.line_no + ")");
                }
            } else if (parserParameters.step == STEP_HEADER_PARSE) {
                if (MiscUtils.isNull(line)) {
                    parserParameters.step = STEP_FREE_PARSE;
                } else {
                    parse_hdr(line, db, parserParameters.line_no);
                }
            } else if (parserParameters.step == STEP_VARIABLES_HEADER_PARSE) {
                if (MiscUtils.isNull(line) || line.contains("*END*")) {
                    parserParameters.step = STEP_FREE_PARSE;
                } else {
                    parse_var(line, db, parserParameters.line_no);
                }
            } else if (parserParameters.step == STEP_DATABASE_FILE_PARSE) {
                if (MiscUtils.isNull(line)) {
                    parserParameters.step = STEP_FREE_PARSE;
                } else {
                    if (!line.startsWith("File") && line.contains("(") && line.contains(")")) {
                        parserParameters.step = STEP_DATABASE_PAGES_PARSE;
                        parserParameters.table = new StatTable();
                        parserParameters.table.db = parserParameters.db;
                        db.getTables().add(parserParameters.table);
                        parserParameters.in_table = true;
                        parse_table(line, parserParameters.table, db, parserParameters.line_no);
                    }
                    parse_fseq(line, db, parserParameters.line_no);
                }
            } else if (parserParameters.step == STEP_TABLESPACES_PARSE) {
                if (MiscUtils.isNull(line)) {
                    parserParameters.new_block = true;
                    parserParameters.in_tablespace_indices = false;
                    parserParameters.in_tablespace_tables = false;
                    parserParameters.in_tablespace = false;
                } else {
                    if (parserParameters.new_block) {
                        parserParameters.new_block = false;
                        if (line.startsWith("Analyzing database pages ...")) {
                            parserParameters.step = STEP_DATABASE_PAGES_PARSE;
                            parserParameters.new_block = true;
                        } else {
                            parserParameters.tablespace = new StatTablespace();
                            parserParameters.tablespace.db = parserParameters.db;
                            db.getTablespaces().add(parserParameters.tablespace);
                            parserParameters.in_tablespace = true;
                            StatTablespace tablespace = parserParameters.tablespace;
                            if (tablespace.name == null) {
                                String[] parts = line.split(" \\(");
                                tablespace.name = parts[0];
                                tablespace.id = (Long.parseLong(parts[1].replace("(", "").replace(")", "")));
                            }
                        }
                    } else {
                        if (line.startsWith("Tables:")) {
                            parserParameters.in_tablespace_tables = true;
                            parserParameters.in_tablespace_indices = false;
                        } else if (line.startsWith("Indices:")) {
                            parserParameters.in_tablespace_tables = false;
                            parserParameters.in_tablespace_indices = true;
                        } else {
                            if (parserParameters.in_tablespace_tables) {
                                parserParameters.tablespace.tableNames.add(line);
                            } else if (parserParameters.in_tablespace_indices) {
                                parserParameters.tablespace.indexNames.add(line);
                            } else if (line.startsWith("Full path: ")) {
                                parserParameters.tablespace.full_path = line.replace("Full path:", "");
                            } else Log.info("error parse " + parserParameters.line_no + " line: " + "ts error");
                        }
                    }
                }
            } else if (parserParameters.step == STEP_DATABASE_PAGES_PARSE) {
                if (MiscUtils.isNull(line)) {
                    parserParameters.new_block = true;
                    if (parserParameters.index != null) {
                        parserParameters.index.page_size = db.page_size;
                    }
                    if (parserParameters.table != null) {
                        parserParameters.table.page_size = db.page_size;
                    }
                } else {
                    if (parserParameters.new_block) {
                        parserParameters.new_block = false;
                        if (!line.startsWith("Index ")) {
                            parserParameters.table = new StatTable();
                            parserParameters.table.db = parserParameters.db;
                            db.getTables().add(parserParameters.table);
                            parserParameters.in_table = true;
                            parse_table(line, parserParameters.table, db, parserParameters.line_no);
                        } else {
                            parserParameters.index = new StatIndex(parserParameters.table);
                            parserParameters.index.db = parserParameters.db;
                            db.getIndices().add(parserParameters.index);
                            parserParameters.in_table = false;
                            parse_index(line, parserParameters.index, db, parserParameters.line_no);
                        }
                        } else {
                        if (parserParameters.in_table) {
                            parse_table(line, parserParameters.table, db, parserParameters.line_no);
                        } else {
                            parse_index(line, parserParameters.index, db, parserParameters.line_no);
                        }
                    }
                    }
                }
            } catch (Exception e) {
            Log.info("error parse " + parserParameters.line_no + " line:" + e.getMessage());
                //e.printStackTrace();
            }
        return parserParameters;
    }

    public static void parse_hdr(String line, StatDatabase db, int line_no) throws ParseError, NoSuchFieldException, IllegalAccessException {


        for (String[] item : StatDatabase.ITEMS_HDR) {
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

    public static void parse_table(String line, StatTable table, StatDatabase db, int line_no) throws ParseError, NoSuchFieldException, IllegalAccessException {
        if (table.getName() == null) {
            String[] parts = line.split(" \\(");
            table.setName(parts[0]);
            table.table_id = (Long.parseLong(parts[1].replace("(", "").replace(")", "")));
        } else {
            if (line.contains(",") || !line.contains("=")) {
                String[] items = new String[]{line};
                if (line.contains(",")) {
                    items = line.split(",");
                }
                for (String item : items) {
                    item = item.trim();
                    boolean found = false;


                    if (!line.startsWith("Fill distribution:")) {
                        for (String[] item_tbl : StatTable.ITEMS_TBL) {
                            String key = item_tbl[0];
                            String valtype = item_tbl[1];
                            String name = item_tbl[2];
                            if (name == null) {
                                name = key.substring(0, 1).toLowerCase() + key.substring(1, key.length() - 1);
                                name = name.replace(" ", "_");
                            }

                            if (item.startsWith(key)) {
                                String value = item.substring(key.length()).trim();

                                if (valtype.equals("i")) {
                                    table.getClass().getField(name).setLong(table, Long.parseLong(value));
                                } else if (valtype.equals("i+")) {
                                    table.getClass().getField(name).setLong(table, Long.parseLong(value));
                                } else if (valtype.equals("f")) {
                                    table.getClass().getField(name).setFloat(table, Float.parseFloat(value));
                                } else if (valtype.equals("p")) {
                                    table.getClass().getField(name).setInt(table, Integer.parseInt(value.replace("%", "")));
                                } else if (valtype.equals("s")) {
                                    table.getClass().getField(name).set(table, value);
                                } else {
                                    throw new ParseError("Unknown value type " + valtype);
                                }

                                found = true;
                                break;
                            }
                        }
                    } else {
                        found = true;
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

    public static void parse_var(String line, StatDatabase db, int line_no) throws ParseError, NoSuchFieldException, IllegalAccessException {
        String[] parts = line.split(":");
        StatDatabase.Variable var = new StatDatabase.Variable();
        if (parts.length > 1) {
            var.name = parts[0].trim();
        } else throw new ParseError("Unknown information (line " + line_no + ")");
        var.value = "";
        for (int i = 1; i < parts.length; i++)
            var.value += parts[i];
        var.value = var.value.trim();
        db.variables.add(var);
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

    public static void parse_index(String line, StatIndex index, StatDatabase db, int line_no) throws ParseError, NoSuchFieldException, IllegalAccessException {
        if (index.getName() == null) {
            String[] parts = line.substring(6).split(" \\(");
            index.setName(parts[0]);
            index.indexId = (Long.parseLong(parts[1].replace("(", "").replace(")", "")));
        } else {
            if (line.contains(",") || !line.contains("=")) {
                String[] items = line.split(",");
                for (String item : items) {
                    item = item.trim();
                    boolean found = false;
                    if (!line.startsWith("Fill distribution:")) {
                        for (String[] item_idx2 : StatIndex.ITEMS_IDX) {
                            String key = item_idx2[0];
                            String valtype = item_idx2[1];
                            String name = item_idx2[2];
                            if (name == null) {
                                name = key.substring(0, 1).toLowerCase() + key.substring(1, key.length() - 1);
                                name = name.replace(" ", "_");
                            }

                            if (item.toLowerCase().startsWith(key.toLowerCase())) {
                                String value = item.substring(key.length()).trim();

                                if (valtype.equals("i")) {
                                    index.getClass().getField(name).setLong(index, Long.parseLong(value));
                                } else if (valtype.equals("f")) {
                                    index.getClass().getField(name).setFloat(index, Float.parseFloat(value));
                                } else if (valtype.equals("s")) {
                                    index.getClass().getField(name).set(index, value);
                                } else {
                                    throw new ParseError("Unknown value type " + valtype);
                                }

                                found = true;
                                break;
                            }
                        }
                    } else {
                        found = true;
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

    public static class ParserParameters {
        public StatDatabase db;
        public int line_no = 0;
        public StatTable table;
        public StatIndex index;
        public StatTablespace tablespace;
        public boolean new_block = true;
        public boolean in_table = false;
        public boolean in_tablespace = false;
        public boolean in_tablespace_tables = false;
        public boolean in_tablespace_indices = false;
        public int step = STEP_FREE_PARSE;
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
