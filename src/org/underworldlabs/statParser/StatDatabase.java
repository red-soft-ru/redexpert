package org.underworldlabs.statParser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StatDatabase {

    public int gstat_version;

    public int system_change_number;

    public LocalDateTime executed;

    public LocalDateTime completed;

    public String filename;

    public int flags;

    public int checksum;

    public int generation;

    public int page_size;

    public int oit;

    public int oat;

    public int ost;

    public int next_transaction;

    public int bumped_transaction;
    public int autosweep_gap;
    public int next_attachment_id;

    public int implementation_id;

    public String implementation;

    public int shadow_count;

    public int page_buffers;

    public int next_header_page;

    public int database_dialect;

    public LocalDateTime creation_date;

    public List<String> attributes;

    public int sweep_interval;

    public String continuation_file;

    public int last_logical_page;

    public String backup_guid;

    public String root_filename;

    public String replay_logging_file;

    public String backup_diff_file;

    public Encryption encrypted_data_pages;

    public Encryption encrypted_index_pages;

    public Encryption encrypted_blob_pages;

    public List<String> continuation_files;

    public List<StatIndex> indices;
    public List<StatTable> tables;
    public String server;
    public String ods_version;
    public int sequence_number;
    public int next_attachment_ID;

    public StatDatabase() {
        this.gstat_version = 0;
        this.system_change_number = 0;
        this.executed = null;
        this.completed = null;
        this.filename = null;
        this.flags = 0;
        this.checksum = 12345;
        this.generation = 0;
        this.page_size = 0;
        this.oit = 0;
        this.oat = 0;
        this.ost = 0;
        this.next_transaction = 0;
        this.bumped_transaction = 0;
        this.next_attachment_id = 0;
        this.implementation_id = 0;
        this.implementation = null;
        this.shadow_count = 0;
        this.page_buffers = 0;
        this.next_header_page = 0;
        this.database_dialect = 0;
        this.creation_date = null;
        this.attributes = new ArrayList<>();
        this.sweep_interval = 0;
        this.continuation_file = null;
        this.last_logical_page = 0;
        this.backup_guid = null;
        this.root_filename = null;
        this.replay_logging_file = null;
        this.backup_diff_file = null;
        this.encrypted_data_pages = null;
        this.encrypted_index_pages = null;
        this.encrypted_blob_pages = null;
        this.continuation_files = new ArrayList<>();
        this.tables = new ArrayList<>();
        this.indices = new ArrayList<>();
    }
  /*

    public class StatDatabase {

        private Integer gstat_version;
        private Integer system_change_number;
        private Boolean executed;
        private Boolean completed;
        private String filename;
        private Integer flags;
        private Integer checksum;
        private Integer generation;
        private Integer page_size;
        private Integer oit;
        private Integer oat;
        private Integer ost;
        private Integer next_transaction;
        private Integer bumped_transaction;
        private Integer next_attachment_id;
        private Integer implementation_id;
        private String implementation;
        private Integer shadow_count;
        private Integer page_buffers;
        private Integer next_header_page;
        private Integer database_dialect;
        private String creation_date;
        private List<String> attributes;
        private Integer sweep_interval;
        private String continuation_file;
        private String last_logical_page;
        private String backup_guid;
        private String root_filename;
        private String replay_logging_file;
        private String backup_diff_file;
        private Boolean encrypted_data_pages;
        private Boolean encrypted_index_pages;
        private Boolean encrypted_blob_pages;
        private List<String> continuation_files;
        private Object tables;
        private Object indices;

        public StatDatabase() {
            this.gstat_version = null;
            this.system_change_number = null;
            this.executed = null;
            this.completed = null;
            this.filename = null;
            this.flags = 0;
            this.checksum = 12345;
            this.generation = 0;
            this.page_size = 0;
            this.oit = 0;
            this.oat = 0;
            this.ost = 0;
            this.next_transaction = 0;
            this.bumped_transaction = null;
            this.next_attachment_id = 0;
            this.implementation_id = 0;
            this.implementation = null;
            this.shadow_count = 0;
            this.page_buffers = 0;
            this.next_header_page = 0;
            this.database_dialect = 0;
            this.creation_date = null;
            this.attributes = new ArrayList<>();
            this.sweep_interval = null;
            this.continuation_file = null;
            this.last_logical_page = null;
            this.backup_guid = null;
            this.root_filename = null;
            this.replay_logging_file = null;
            this.backup_diff_file = null;
            this.encrypted_data_pages = null;
            this.encrypted_index_pages = null;
            this.encrypted_blob_pages = null;
            this.continuation_files = new ArrayList<>();
            this.tables = null;
            this.indices = null;
        }*/

    public boolean has_table_stats() {
        return this.tables != null && tables.size() > 0 && tables.get(0).getPrimary_pointer_page() != 0;
    }

    public boolean has_row_stats() {
        return this.has_table_stats() && tables.get(0).getAvg_version_length() != 0;
    }

    public boolean has_index_stats() {
        return this.indices != null && this.indices.size() > 0 && indices.get(0).getDepth() != 0;
    }

    public boolean has_encryption_stats() {
        return this.encrypted_data_pages != null;
    }

    public boolean has_system() {
        for (StatTable table : tables) {
            if (table.getName().contentEquals("RDB$DATABASE"))
                return true;
        }
        return false;
    }

    public int getGstat_version() {
        return gstat_version;
    }

    public void setGstat_version(int gstat_version) {
        this.gstat_version = gstat_version;
    }

    public int getSystem_change_number() {
        return system_change_number;
    }

    public void setSystem_change_number(int system_change_number) {
        this.system_change_number = system_change_number;
    }

    public LocalDateTime getExecuted() {
        return executed;
    }

    public void setExecuted(LocalDateTime executed) {
        this.executed = executed;
    }

    public LocalDateTime getCompleted() {
        return completed;
    }

    public void setCompleted(LocalDateTime completed) {
        this.completed = completed;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public int getPage_size() {
        return page_size;
    }

    public void setPage_size(int page_size) {
        this.page_size = page_size;
    }

    public int getOit() {
        return oit;
    }

    public void setOit(int oit) {
        this.oit = oit;
    }

    public int getOat() {
        return oat;
    }

    public void setOat(int oat) {
        this.oat = oat;
    }

    public int getOst() {
        return ost;
    }

    public void setOst(int ost) {
        this.ost = ost;
    }

    public int getNext_transaction() {
        return next_transaction;
    }

    public void setNext_transaction(int next_transaction) {
        this.next_transaction = next_transaction;
    }

    public int getBumped_transaction() {
        return bumped_transaction;
    }

    public void setBumped_transaction(int bumped_transaction) {
        this.bumped_transaction = bumped_transaction;
    }

    public int getNext_attachment_id() {
        return next_attachment_id;
    }

    public void setNext_attachment_id(int next_attachment_id) {
        this.next_attachment_id = next_attachment_id;
    }

    public int getImplementation_id() {
        return implementation_id;
    }

    public void setImplementation_id(int implementation_id) {
        this.implementation_id = implementation_id;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public int getShadow_count() {
        return shadow_count;
    }

    public void setShadow_count(int shadow_count) {
        this.shadow_count = shadow_count;
    }

    public int getPage_buffers() {
        return page_buffers;
    }

    public void setPage_buffers(int page_buffers) {
        this.page_buffers = page_buffers;
    }

    public int getNext_header_page() {
        return next_header_page;
    }

    public void setNext_header_page(int next_header_page) {
        this.next_header_page = next_header_page;
    }

    public int getDatabase_dialect() {
        return database_dialect;
    }

    public void setDatabase_dialect(int database_dialect) {
        this.database_dialect = database_dialect;
    }

    public LocalDateTime getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(LocalDateTime creation_date) {
        this.creation_date = creation_date;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public int getSweep_interval() {
        return sweep_interval;
    }

    public void setSweep_interval(int sweep_interval) {
        this.sweep_interval = sweep_interval;
    }

    public String getContinuation_file() {
        return continuation_file;
    }

    public void setContinuation_file(String continuation_file) {
        this.continuation_file = continuation_file;
    }

    public int getLast_logical_page() {
        return last_logical_page;
    }

    public void setLast_logical_page(int last_logical_page) {
        this.last_logical_page = last_logical_page;
    }

    public String getBackup_guid() {
        return backup_guid;
    }

    public void setBackup_guid(String backup_guid) {
        this.backup_guid = backup_guid;
    }

    public String getRoot_filename() {
        return root_filename;
    }

    public void setRoot_filename(String root_filename) {
        this.root_filename = root_filename;
    }

    public String getReplay_logging_file() {
        return replay_logging_file;
    }

    public void setReplay_logging_file(String replay_logging_file) {
        this.replay_logging_file = replay_logging_file;
    }

    public String getBackup_diff_file() {
        return backup_diff_file;
    }

    public void setBackup_diff_file(String backup_diff_file) {
        this.backup_diff_file = backup_diff_file;
    }

    public Encryption getEncrypted_data_pages() {
        return encrypted_data_pages;
    }

    public void setEncrypted_data_pages(Encryption encrypted_data_pages) {
        this.encrypted_data_pages = encrypted_data_pages;
    }

    public Encryption getEncrypted_index_pages() {
        return encrypted_index_pages;
    }

    public void setEncrypted_index_pages(Encryption encrypted_index_pages) {
        this.encrypted_index_pages = encrypted_index_pages;
    }

    public Encryption getEncrypted_blob_pages() {
        return encrypted_blob_pages;
    }

    public void setEncrypted_blob_pages(Encryption encrypted_blob_pages) {
        this.encrypted_blob_pages = encrypted_blob_pages;
    }

    public List<String> getContinuation_files() {
        return continuation_files;
    }

    public void setContinuation_files(List<String> continuation_files) {
        this.continuation_files = continuation_files;
    }

    public List<StatIndex> getIndices() {
        return indices;
    }

    public void setIndices(List<StatIndex> indices) {
        this.indices = indices;
    }

    public List<StatTable> getTables() {
        return tables;
    }

    public void setTables(List<StatTable> tables) {
        this.tables = tables;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getAutosweep_gap() {
        return autosweep_gap;
    }

    public void setAutosweep_gap(int autosweep_gap) {
        this.autosweep_gap = autosweep_gap;
    }
}
