package org.executequery.databaseobjects.impl;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.util.MiscUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vasiliy on 26.01.17.
 */
public class DefaultDatabaseTrigger extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    private String triggerSourceCode;
    private boolean triggerActive;
    private String tableName;
    private String triggerDescription;
    private int triggerSequence;
    private String triggerType;
    private int intTriggerType;
    private long longTriggerType;
    private boolean isMarkedReloadActive;

    public final static long TRIGGER_TYPE_DDL = 16384;
    public final static long TRIGGER_TYPE_DB = 8192;
    public final static long RDB_TRIGGER_TYPE_MASK = 24576;

    private static String[][] DDL_TRIGGER_ACTION_NAMES =
            {
                    {null, null},
                    {"CREATE", "TABLE"},
                    {"ALTER", "TABLE"},
                    {"DROP", "TABLE"},
                    {"CREATE", "PROCEDURE"},
                    {"ALTER", "PROCEDURE"},
                    {"DROP", "PROCEDURE"},
                    {"CREATE", "FUNCTION"},
                    {"ALTER", "FUNCTION"},
                    {"DROP", "FUNCTION"},
                    {"CREATE", "TRIGGER"},
                    {"ALTER", "TRIGGER"},
                    {"DROP", "TRIGGER"},
                    {"", ""}, {"", ""}, {"", ""},    // gap for TRIGGER_TYPE_MASK - 3 bits
                    {"CREATE", "EXCEPTION"},
                    {"ALTER", "EXCEPTION"},
                    {"DROP", "EXCEPTION"},
                    {"CREATE", "VIEW"},
                    {"ALTER", "VIEW"},
                    {"DROP", "VIEW"},
                    {"CREATE", "DOMAIN"},
                    {"ALTER", "DOMAIN"},
                    {"DROP", "DOMAIN"},
                    {"CREATE", "ROLE"},
                    {"ALTER", "ROLE"},
                    {"DROP", "ROLE"},
                    {"CREATE", "INDEX"},
                    {"ALTER", "INDEX"},
                    {"DROP", "INDEX"},
                    {"CREATE", "SEQUENCE"},
                    {"ALTER", "SEQUENCE"},
                    {"DROP", "SEQUENCE"},
                    {"CREATE", "USER"},
                    {"ALTER", "USER"},
                    {"DROP", "USER"},
                    {"CREATE", "COLLATION"},
                    {"DROP", "COLLATION"},
                    {"ALTER", "CHARACTER SET"},
                    {"CREATE", "PACKAGE"},
                    {"ALTER", "PACKAGE"},
                    {"DROP", "PACKAGE"},
                    {"CREATE", "PACKAGE BODY"},
                    {"DROP", "PACKAGE BODY"},
                    {"CREATE", "MAPPING"},
                    {"ALTER", "MAPPING"},
                    {"DROP", "MAPPING"}
            };

    final String[] Trigger_prefix_types =
            {
                    "BEFORE",            // keyword
                    "AFTER"                // keyword
            };

    final short TRIGGER_TYPE_SHIFT = 13;
    final long TRIGGER_TYPE_MASK = (3 << TRIGGER_TYPE_SHIFT);

    final long DDL_TRIGGER_ANY = 0x7FFFFFFFFFFFFFFFL & ~TRIGGER_TYPE_MASK & ~1;

    final int DDL_TRIGGER_CREATE_TABLE = 1;
    final int DDL_TRIGGER_ALTER_TABLE = 2;
    final int DDL_TRIGGER_DROP_TABLE = 3;
    final int DDL_TRIGGER_CREATE_PROCEDURE = 4;
    final int DDL_TRIGGER_ALTER_PROCEDURE = 5;
    final int DDL_TRIGGER_DROP_PROCEDURE = 6;
    final int DDL_TRIGGER_CREATE_FUNCTION = 7;
    final int DDL_TRIGGER_ALTER_FUNCTION = 8;
    final int DDL_TRIGGER_DROP_FUNCTION = 9;
    final int DDL_TRIGGER_CREATE_TRIGGER = 10;
    final int DDL_TRIGGER_ALTER_TRIGGER = 11;
    final int DDL_TRIGGER_DROP_TRIGGER = 12;
    // gap for TRIGGER_TYPE_MASK - 3 bits
    final int DDL_TRIGGER_CREATE_EXCEPTION = 16;
    final int DDL_TRIGGER_ALTER_EXCEPTION = 17;
    final int DDL_TRIGGER_DROP_EXCEPTION = 18;
    final int DDL_TRIGGER_CREATE_VIEW = 19;
    final int DDL_TRIGGER_ALTER_VIEW = 20;
    final int DDL_TRIGGER_DROP_VIEW = 21;
    final int DDL_TRIGGER_CREATE_DOMAIN = 22;
    final int DDL_TRIGGER_ALTER_DOMAIN = 23;
    final int DDL_TRIGGER_DROP_DOMAIN = 24;
    final int DDL_TRIGGER_CREATE_ROLE = 25;
    final int DDL_TRIGGER_ALTER_ROLE = 26;
    final int DDL_TRIGGER_DROP_ROLE = 27;
    final int DDL_TRIGGER_CREATE_INDEX = 28;
    final int DDL_TRIGGER_ALTER_INDEX = 29;
    final int DDL_TRIGGER_DROP_INDEX = 30;
    final int DDL_TRIGGER_CREATE_SEQUENCE = 31;
    final int DDL_TRIGGER_ALTER_SEQUENCE = 32;
    final int DDL_TRIGGER_DROP_SEQUENCE = 33;
    final int DDL_TRIGGER_CREATE_USER = 34;
    final int DDL_TRIGGER_ALTER_USER = 35;
    final int DDL_TRIGGER_DROP_USER = 36;
    final int DDL_TRIGGER_CREATE_COLLATION = 37;
    final int DDL_TRIGGER_DROP_COLLATION = 38;
    final int DDL_TRIGGER_ALTER_CHARACTER_SET = 39;
    final int DDL_TRIGGER_CREATE_PACKAGE = 40;
    final int DDL_TRIGGER_ALTER_PACKAGE = 41;
    final int DDL_TRIGGER_DROP_PACKAGE = 42;
    final int DDL_TRIGGER_CREATE_PACKAGE_BODY = 43;
    final int DDL_TRIGGER_DROP_PACKAGE_BODY = 44;
    final int DDL_TRIGGER_CREATE_MAPPING = 45;
    final int DDL_TRIGGER_ALTER_MAPPING = 46;
    final int DDL_TRIGGER_DROP_MAPPING = 47;
    public static final int DATABASE_TRIGGER = 1;
    public static final int TABLE_TRIGGER = 0;
    public static final int DDL_TRIGGER = 2;

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseTrigger() {
    }

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseTrigger(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Creates a new instance with
     * the specified values.
     */
    public DefaultDatabaseTrigger(String schema, String name) {
        setName(name);
        setSchemaName(schema);
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */

    private int type = -1;
    public int getType() {
        if (type == -1) {
            if (getParent().getMetaDataKey().equalsIgnoreCase(META_TYPES[NamedObject.DATABASE_TRIGGER]))
                type = NamedObject.DATABASE_TRIGGER;
            else if (getParent().getMetaDataKey().equalsIgnoreCase(META_TYPES[NamedObject.SYSTEM_TRIGGER]))
                type = NamedObject.SYSTEM_TRIGGER;
            else if (getParent().getMetaDataKey().equalsIgnoreCase(META_TYPES[NamedObject.DDL_TRIGGER]))
                type = NamedObject.DDL_TRIGGER;
            else type = NamedObject.TRIGGER;
        }
        return type;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    public String getTriggerSourceCode() {
        if (isMarkedForReload())
            getObjectInfo();
        return triggerSourceCode;
    }

    public boolean isTriggerActive() {
        if (isMarkedReloadActive())
            getObjectInfo();
        return triggerActive;
    }

    public String getTriggerTableName() {
        if (isMarkedForReload())
            getObjectInfo();
        if (tableName == null)
            return "";
        return tableName;
    }

    public String getTriggerDescription() {
        if (isMarkedForReload())
            getObjectInfo();
        return triggerDescription;
    }

    public void setTriggerSourceCode(String triggerSourceCode) {
        this.triggerSourceCode = triggerSourceCode;
    }

    public void setTriggerActive(boolean triggerActive) {
        this.triggerActive = triggerActive;
        setMarkedReloadActive(false);
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTriggerDescription(String triggerDescription) {
        this.triggerDescription = triggerDescription;
    }

    public boolean isMarkedReloadActive() {
        return isMarkedReloadActive;
    }

    public void setMarkedReloadActive(boolean markedReloadActive) {
        isMarkedReloadActive = markedReloadActive;
    }

    public int getTriggerSequence() {
        if (isMarkedForReload())
            getObjectInfo();
        return triggerSequence;
    }

    public void setTriggerSequence(int triggerSequence) {
        this.triggerSequence = triggerSequence;
    }

    public String getStringTriggerType() {
        if (isMarkedForReload())
            getObjectInfo();
        return triggerType;
    }

    public void setTriggerType(long type) {
        triggerType = triggerTypeFromLong(type);
        intTriggerType = getIntTypeTrigger(type);
        setLongTriggerType(type);
    }

    public int getIntTriggerType() {
        if (isMarkedForReload())
            getObjectInfo();
        return intTriggerType;
    }

    public void setLongTriggerType(long type) {
        longTriggerType = type;
    }

    public long getLongTriggerType() {
        if (isMarkedForReload())
            getObjectInfo();
        return longTriggerType;
    }

    private int getIntTypeTrigger(long type) {
        if ((type & RDB_TRIGGER_TYPE_MASK) == TRIGGER_TYPE_DDL)
            return DDL_TRIGGER;
        else if ((type & RDB_TRIGGER_TYPE_MASK) == TRIGGER_TYPE_DB)
            return DATABASE_TRIGGER;
        else return TABLE_TRIGGER;
    }

    private String triggerTypeFromLong(long type) {
        if (type == 1)
            return "BEFORE INSERT"; // 	Триггер выполняется перед вставкой записи в таблицу или просмотр.
        if (type == 2)
            return "AFTER INSERT"; // 	Триггер выполняется после вставки записи в таблицу или просмотр.
        if (type == 3)
            return "BEFORE UPDATE"; // 	Триггер выполняется перед изменением записи в таблице или просмотре.
        if (type == 4)
            return "AFTER UPDATE"; // 	Триггер выполняется после изменения записи в таблице или просмотре.
        if (type == 5)
            return "BEFORE DELETE"; // 	Триггер выполняется перед удалением записи из таблицы или просмотра.
        if (type == 6)
            return "AFTER DELETE"; // 	Триггер выполняется после удаления записи из таблицы или просмотра.
        if (type == 17)
            return "BEFORE INSERT OR UPDATE"; // 	Триггер выполняется перед вставкой или изменением записи в таблице или просмотре.
        if (type == 18)
            return "AFTER INSERT OR UPDATE"; // 	Триггер выполняется после вставки или изменения записи в таблице или просмотре.
        if (type == 25)
            return "BEFORE INSERT OR DELETE"; // 	Триггер выполняется перед вставкой или удалением записи в таблице или просмотре.
        if (type == 26)
            return "AFTER INSERT OR DELETE"; // 	Триггер выполняется после вставки или удаления записи в таблице или просмотре.
        if (type == 27)
            return "BEFORE UPDATE OR DELETE"; // 	Триггер выполняется перед изменением или удалением записи в таблице или просмотре.
        if (type == 28)
            return "AFTER UPDATE OR DELETE"; // 	Триггер выполняется после изменения или удаления записи в таблице или просмотре.
        if (type == 113)
            return "BEFORE INSERT OR UPDATE OR DELETE"; // 	Триггер выполняется перед вставкой, изменением или удалением записи в таблице или просмотре.
        if (type == 114)
            return "AFTER INSERT OR UPDATE OR DELETE"; // 	Триггер выполняется после вставки, изменения или удаления записи в таблице или просмотре.
        if (type == 8192)
            return "ON CONNECT"; // 	Триггер выполняется после установления подключения к базе данных.
        if (type == 8193)
            return "ON DISCONNECT"; // 	Триггер выполняется перед отключением от базы данных.
        if (type == 8194)
            return "ON TRANSACTION START"; // 	Триггер выполняется после старта транзакции.
        if (type == 8195)
            return "ON TRANSACTION COMMIT"; // 	Триггер выполняется перед подтверждением COMMIT транзакции.
        if (type == 8196)
            return "ON TRANSACTION ROLLBACK"; // 	Триггер выполняется перед отменой ROLLBACK транзакции.

        if (type > 8196) {
            boolean first = true;
            String buffer = Trigger_prefix_types[(int) type & 1];

            if ((type & DDL_TRIGGER_ANY) == DDL_TRIGGER_ANY)
                buffer += " ANY DDL STATEMENT";
            else {
                for (int pos = 1; pos < 64; ++pos) {
                    if (((1L << pos) & TRIGGER_TYPE_MASK) != 0 || ((type & (1L << pos)) == 0))
                        continue;

                    if (first)
                        first = false;
                    else
                        buffer += " OR";

                    buffer += " ";

                    if (pos < DDL_TRIGGER_ACTION_NAMES.length) {
                        buffer += DDL_TRIGGER_ACTION_NAMES[pos][0] + " " +
                                DDL_TRIGGER_ACTION_NAMES[pos][1];
                    } else
                        buffer += "<unknown>";
                }
            }

            return buffer;
        }

        return "NULL";
    }

    @Override
    public String getCreateSQLText() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR ALTER TRIGGER ");
        sb.append(MiscUtils.getFormattedObject(getName()));
        if (!getTriggerTableName().isEmpty()) {
            sb.append(" FOR ");
            sb.append(MiscUtils.getFormattedObject(getTriggerTableName()));
        }
        sb.append("\n");
        sb.append(isTriggerActive() ? "ACTIVE" : "INACTIVE");
        sb.append(" ");
        sb.append(getStringTriggerType());
        sb.append(" POSITION ");
        sb.append(getTriggerSequence());
        sb.append("\n");
        if (triggerSourceCode != null) {
            sb.append(getTriggerSourceCode());
        }
        return sb.toString();
    }

    protected String queryForInfo() {
        return "select 0,\n" +
                "t.rdb$trigger_source,\n" +
                "t.rdb$relation_name,\n" +
                "t.rdb$trigger_sequence,\n" +
                "t.rdb$trigger_type,\n" +
                "t.rdb$trigger_inactive,\n" +
                "t.rdb$description\n" +
                "from rdb$triggers t\n" +
                "where t.rdb$trigger_name = '" + getName().trim() + "'";
    }

    @Override
    protected void getObjectInfo() {
        super.getObjectInfo();
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        try {
            ResultSet rs = querySender.getResultSet(queryForInfo()).getResultSet();
            setInfoFromResultSet(rs);
        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog("Error get info about" + getName(), e);
        } finally {
            querySender.releaseResources();
            setMarkedForReload(false);
        }


    }

    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        if (rs.next()) {
            setTableName(rs.getString(3));
            setTriggerSequence(rs.getInt(4));
            setTriggerActive(rs.getInt(6) != 1);
            setTriggerType(rs.getLong(5));
            setTriggerDescription(rs.getString(7));
            setTriggerSourceCode(rs.getString(2));
            setRemarks(rs.getString(7));
        }
    }

    public void reset() {
        super.reset();
        setMarkedReloadActive(true);
    }

}
