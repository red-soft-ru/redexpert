package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vasiliy on 26.01.17.
 */
public class DefaultDatabaseTrigger extends DefaultDatabaseExecutable {

    private int type = -1;

    private String triggerSourceCode;
    private boolean triggerActive;
    private String tableName;
    private int triggerSequence;
    private String triggerType;
    private int intTriggerType;
    private long longTriggerType;
    private boolean isMarkedReloadActive;

    public final static long TRIGGER_TYPE_DDL = 16384;
    public final static long TRIGGER_TYPE_DB = 8192;
    public final static long RDB_TRIGGER_TYPE_MASK = 24576;

    private static final String[][] DDL_TRIGGER_ACTION_NAMES =
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
    public static final int DATABASE_TRIGGER = NamedObject.DATABASE_TRIGGER;
    public static final int TABLE_TRIGGER = NamedObject.TRIGGER;
    public static final int DDL_TRIGGER = NamedObject.DDL_TRIGGER;


    /**
     * Creates a new instance.
     */
    public DefaultDatabaseTrigger(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }


    /**
     * Returns the database object type.
     *
     * @return the object type
     */
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
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    @Override
    public boolean allowsChildren() {
        return false;
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
        return tableName;
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
        if (getIntTypeTrigger(type) == TABLE_TRIGGER) {

            long parse_type = type + 1;
            String buffer = Trigger_prefix_types[(int) parse_type & 1];
            parse_type = parse_type >> 1;
            boolean first = true;
            for (int i = 0; i < 3; i++) {
                int action_type = (int) parse_type & 3;
                if (action_type != 0) {
                    if (first)
                        first = false;
                    else
                        buffer += " OR";

                    buffer += " ";
                    switch (action_type) {
                        case 1:
                            buffer += "INSERT";
                            break;
                        case 2:
                            buffer += "UPDATE";
                            break;
                        case 3:
                            buffer += "DELETE";
                            break;
                    }
                }
                parse_type = parse_type >> 2;
            }
            return buffer;
        }

        if (getIntTypeTrigger(type) == DATABASE_TRIGGER) {
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
        }
        if (getIntTypeTrigger(type) == DDL_TRIGGER) {
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
        return SQLUtils.generateCreateTriggerStatement(getName(), getTriggerTableName(), isTriggerActive(), getStringTriggerType(),
                getTriggerSequence(), getTriggerSourceCode(), getEngine(), getEntryPoint(), getSqlSecurity(), getRemarks(), false);
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("TRIGGER", getName());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        String comment = Comparer.isCommentsNeed() ? getRemarks() : null;
        return SQLUtils.generateCreateTriggerStatement(getName(), getTriggerTableName(), isTriggerActive(), getStringTriggerType(),
                getTriggerSequence(), getTriggerSourceCode(), getEngine(), getEntryPoint(), getSqlSecurity(), comment, true);
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return (!this.getCompareCreateSQL().equals(databaseObject.getCompareCreateSQL())) ?
                databaseObject.getCompareCreateSQL() : "/* there are no changes */";
    }

    protected static final String TRIGGER_SOURCE = "TRIGGER_SOURCE";
    protected static final String TRIGGER_SEQUENCE = "TRIGGER_SEQUENCE";
    protected static final String TRIGGER_TYPE = "TRIGGER_TYPE";
    protected static final String TRIGGER_INACTIVE = "TRIGGER_INACTIVE";


    @Override
    protected String getFieldName() {
        return "TRIGGER_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$TRIGGERS", "T");
    }


    Condition checkTriggerType(long longTriggerType) {
        return Condition.createCondition()
                .setStatement(Function.createFunction("BIN_AND")
                        .appendArgument(Field.createField(getMainTable(), TRIGGER_TYPE).getFieldTable())
                        .appendArgument(DefaultDatabaseTrigger.RDB_TRIGGER_TYPE_MASK + "")
                        .getStatement()
                        + " = " + longTriggerType);
    }

    @Override
    protected SelectBuilder builderForInfoAllObjects(SelectBuilder commonBuilder) {
        SelectBuilder sb = super.builderForInfoAllObjects(commonBuilder);
        if (getType() == NamedObject.DDL_TRIGGER)
            sb.appendCondition(checkTriggerType(TRIGGER_TYPE_DDL));
        else if (getType() == NamedObject.DATABASE_TRIGGER)
            sb.appendCondition(checkTriggerType(TRIGGER_TYPE_DB));
        else if (getType() == NamedObject.TRIGGER)
            sb.appendCondition(Condition.createCondition(Field.createField(getMainTable(), TRIGGER_TYPE), "<=", "114"));
        return sb;
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = SelectBuilder.createSelectBuilder();
        Table triggers = getMainTable();
        sb.appendTable(triggers);
        sb.appendFields(triggers, getFieldName(), TRIGGER_SOURCE, RELATION_NAME, TRIGGER_SEQUENCE, TRIGGER_TYPE, TRIGGER_INACTIVE, DESCRIPTION);
        sb.appendFields(triggers, !externalCheck(), ENGINE_NAME, ENTRYPOINT);
        sb.appendField(buildSqlSecurityField(triggers));
        sb.setOrdering(getObjectField().getFieldTable());
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        setTableName(getFromResultSet(rs, RELATION_NAME));
        setTriggerSequence(rs.getInt(TRIGGER_SEQUENCE));
        setTriggerActive(rs.getInt(TRIGGER_INACTIVE) != 1);
        setTriggerType(rs.getLong(TRIGGER_TYPE));
        setTriggerSourceCode(getFromResultSet(rs, TRIGGER_SOURCE));
        setRemarks(getFromResultSet(rs, DESCRIPTION));
        setEngine(getFromResultSet(rs, ENGINE_NAME));
        setEntryPoint(getFromResultSet(rs, ENTRYPOINT));
        setSqlSecurity(getFromResultSet(rs, SQL_SECURITY));
        return null;
    }

    @Override
    public void prepareLoadingInfo() {

    }

    @Override
    public void finishLoadingInfo() {

    }

    @Override
    public boolean isAnyRowsResultSet() {
        return false;
    }


    public void reset() {
        super.reset();
        setMarkedReloadActive(true);
    }

}
