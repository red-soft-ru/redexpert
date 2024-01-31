package org.executequery.gui.browser.managment.tracemanager;

/**
 * Created by vasiliy on 21.12.16.
 */
public class LogConstants {
    public static final String ID_COLUMN = "NUM";
    public static final String TSTAMP_COLUMN = "TSTAMP";
    public static final String ID_PROCESS_COLUMN = "ID_PROCESS";
    public static final String ID_THREAD_COLUMN = "ID_THREAD";
    public static final String FAILED_COLUMN = "FAILED";
    public static final String EVENT_TYPE_COLUMN = "EVENT_TYPE";
    public static final String ID_SESSION_COLUMN = "ID_SESSION";
    public static final String NAME_SESSION_COLUMN = "NAME_SESSION";
    public static final String ID_SERVICE_COLUMN = "ID_SERVICE";
    public static final String USERNAME_COLUMN = "USERNAME";
    public static final String PROTOCOL_CONNECTION_COLUMN = "PROTOCOL_CONNECTION";
    public static final String CLIENT_ADDRESS_COLUMN = "CLIENT_ADDRESS";
    public static final String TYPE_QUERY_SERVICE_COLUMN = "TYPE_QUERY_SERVICE";
    public static final String OPTIONS_START_SERVICE_COLUMN = "OPTIONS_START_SERVICE";
    public static final String ROLE_COLUMN = "ROLE";
    public static final String DATABASE_COLUMN = "DATABASE";
    public static final String CHARSET_COLUMN = "CHARSET";
    public static final String ID_CONNECTION_COLUMN = "ID_CONNECTION";
    public static final String CLIENT_PROCESS_COLUMN = "CLIENT_PROCESS";
    public static final String ID_CLIENT_PROCESS_COLUMN = "ID_CLIENT_PROCESS";
    public static final String ID_TRANSACTION_COLUMN = "ID_TRANSACTION";
    public static final String LEVEL_ISOLATION_COLUMN = "LEVEL_ISOLATION";
    public static final String MODE_OF_BLOCK_COLUMN = "MODE_OF_BLOCK";
    public static final String MODE_OF_ACCESS_COLUMN = "MODE_OF_ACCESS";
    public static final String TIME_EXECUTION_COLUMN = "TIME_EXECUTION";
    public static final String COUNT_READS_COLUMN = "COUNT_READS";
    public static final String COUNT_WRITES_COLUMN = "COUNT_WRITES";
    public static final String COUNT_FETCHES_COLUMN = "COUNT_FETCHES";
    public static final String COUNT_MARKS_COLUMN = "COUNT_MARKS";
    public static final String ID_STATEMENT_COLUMN = "ID_STATEMENT";
    public static final String RECORDS_FETCHED_COLUMN = "RECORDS_FETCHED";
    public static final String STATEMENT_TEXT_COLUMN = "STATEMENT_TEXT";
    public static final String PARAMETERS_TEXT_COLUMN = "PARAMETERS_TEXT";
    public static final String PLAN_TEXT_COLUMN = "PLAN_TEXT";
    public static final String TABLE_COUNTERS_COLUMN = "TABLE_COUNTERS";
    public static final String DECLARE_CONTEXT_VARIABLES_TEXT_COLUMN = "DECLARE_CONTEXT_VARIABLES";
    public static final String EXECUTOR_COLUMN = "EXECUTOR";
    public static final String GRANTOR_COLUMN = "GRANTOR";
    public static final String PRIVILEGE_COLUMN = "PRIVILEGE";
    public static final String PRIVILEGE_OBJECT_COLUMN = "PRIVILEGE_OBJECT";
    public static final String PRIVILEGE_USERNAME_COLUMN = "PRIVILEGE_USERNAME";
    public static final String PRIVILEGE_ATTACHMENT_COLUMN = "PRIVILEGE_ATTACHMENT";
    public static final String PRIVILEGE_TRANSACTION_COLUMN = "PRIVILEGE_TRANSACTION";
    public static final String PROCEDURE_NAME_COLUMN = "PROCEDURE_NAME";
    public static final String RETURN_VALUE_COLUMN = "RETURN_VALUE";
    public static final String TRIGGER_INFO_COLUMN = "TRIGGER_INFO";
    public static final String SENT_DATA_COLUMN = "SENT_DATA";
    public static final String RECEIVED_DATA_COLUMN = "RECEIVED_DATA";
    public static final String ERROR_MESSAGE_COLUMN = "ERROR_MESSAGE";
    public static final String OLDEST_INTERESTING_COLUMN = "OLDEST_INTERESTING";
    public static final String OLDEST_ACTIVE_COLUMN = "OLDEST_ACTIVE";
    public static final String OLDEST_SNAPSHOT_COLUMN = "OLDEST_SNAPSHOT";
    public static final String NEXT_TRANSACTION_COLUMN = "NEXT_TRANSACTION";
    public static final String SORT_MEMORY_USAGE_TOTAL_COLUMN = "SORT_MEMORY_USAGE_TOTAL";
    public static final String SORT_MEMORY_USAGE_CACHED_COLUMN = "SORT_MEMORY_USAGE_CACHED";
    public static final String SORT_MEMORY_USAGE_ON_DISK_COLUMN = "SORT_MEMORY_USAGE_ON_DISK";
    public static final String[] COLUMNS =
            {
                    ID_COLUMN,
                    TSTAMP_COLUMN,
                    ID_PROCESS_COLUMN,
                    ID_THREAD_COLUMN,
                    EVENT_TYPE_COLUMN,
                    FAILED_COLUMN,
                    ID_SESSION_COLUMN,
                    NAME_SESSION_COLUMN,
                    ID_SERVICE_COLUMN,
                    USERNAME_COLUMN,
                    PROTOCOL_CONNECTION_COLUMN,
                    CLIENT_ADDRESS_COLUMN,
                    TYPE_QUERY_SERVICE_COLUMN,
                    OPTIONS_START_SERVICE_COLUMN,
                    ROLE_COLUMN,
                    DATABASE_COLUMN,
                    CHARSET_COLUMN,
                    ID_CONNECTION_COLUMN,
                    CLIENT_PROCESS_COLUMN,
                    ID_CLIENT_PROCESS_COLUMN,
                    ID_TRANSACTION_COLUMN,
                    LEVEL_ISOLATION_COLUMN,
                    MODE_OF_BLOCK_COLUMN,
                    MODE_OF_ACCESS_COLUMN,
                    TIME_EXECUTION_COLUMN,
                    COUNT_READS_COLUMN,
                    COUNT_WRITES_COLUMN,
                    COUNT_FETCHES_COLUMN,
                    COUNT_MARKS_COLUMN,
                    ID_STATEMENT_COLUMN,
                    RECORDS_FETCHED_COLUMN,
                    STATEMENT_TEXT_COLUMN,
                    PARAMETERS_TEXT_COLUMN,
                    PLAN_TEXT_COLUMN,
                    TABLE_COUNTERS_COLUMN,
                    DECLARE_CONTEXT_VARIABLES_TEXT_COLUMN,
                    EXECUTOR_COLUMN,
                    GRANTOR_COLUMN,
                    PRIVILEGE_COLUMN,
                    PRIVILEGE_OBJECT_COLUMN,
                    PRIVILEGE_USERNAME_COLUMN,
                    PRIVILEGE_ATTACHMENT_COLUMN,
                    PRIVILEGE_TRANSACTION_COLUMN,
                    PROCEDURE_NAME_COLUMN,
                    RETURN_VALUE_COLUMN,
                    TRIGGER_INFO_COLUMN,
                    SENT_DATA_COLUMN,
                    RECEIVED_DATA_COLUMN,
                    ERROR_MESSAGE_COLUMN,
                    OLDEST_INTERESTING_COLUMN,
                    OLDEST_ACTIVE_COLUMN,
                    OLDEST_SNAPSHOT_COLUMN,
                    NEXT_TRANSACTION_COLUMN,
                    SORT_MEMORY_USAGE_TOTAL_COLUMN,
                    SORT_MEMORY_USAGE_CACHED_COLUMN,
                    SORT_MEMORY_USAGE_ON_DISK_COLUMN
            };
    public static final String TABLE = "Table";
    public static final String NATURAL = "Natural";
    public static final String INDEX = "Index";
    public static final String UPDATE = "Update";
    public static final String INSERT = "Insert";
    public static final String DELETE = "Delete";
    public static final String BACKOUT = "Backout";
    public static final String PURGE = "Purge";
    public static final String EXPUNGE = "Expunge";
    public static final String LOCK = "Lock";
    public static final String WAIT = "Wait";
    public static final String CONFLICT = "Conflict";
    public static final String BVERSION = "BVersion";
    public static final String FRAGMENT = "Fragment";
    public static final String REFETCH = "Refetch";
    public static final String[] TABLE_COUNTERS = {
            TABLE,
            NATURAL,
            INDEX,
            UPDATE,
            INSERT,
            DELETE,
            BACKOUT,
            PURGE,
            EXPUNGE,
            LOCK,
            WAIT,
            CONFLICT,
            BVERSION,
            FRAGMENT,
            REFETCH
    };


}
