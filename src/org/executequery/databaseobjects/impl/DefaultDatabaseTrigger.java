package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;

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

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseTrigger() {}

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
    public int getType() {
        return TRIGGER;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[TRIGGER];
    }

    public String getTriggerSourceCode() {

        return triggerSourceCode;
    }

    public boolean isTriggerActive() {

            return triggerActive;
    }

    public String getTriggerTableName() {

            return tableName;
    }

    public String getTriggerDescription() {

            return triggerDescription;
    }

    public void setTriggerSourceCode(String triggerSourceCode) {
        this.triggerSourceCode = triggerSourceCode;
    }

    public void setTriggerActive(boolean triggerActive) {
        this.triggerActive = triggerActive;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTriggerDescription(String triggerDescription) {
        this.triggerDescription = triggerDescription;
    }

    public int getTriggerSequence() {
        return triggerSequence;
    }

    public void setTriggerSequence(int triggerSequence) {
        this.triggerSequence = triggerSequence;
    }

    public String getStringTriggerType() {
        return triggerType;
    }

    public void setTriggerType(int type) {
        triggerType = triggerTypeFromInt(type);
    }

    private String triggerTypeFromInt(int type) {
        switch (type) {
            case 1:
                return "BEFORE INSERT"; // 	Триггер выполняется перед вставкой записи в таблицу или просмотр.
            case 2:
                return "AFTER INSERT"; // 	Триггер выполняется после вставки записи в таблицу или просмотр.
            case 3:
                return "BEFORE UPDATE"; // 	Триггер выполняется перед изменением записи в таблице или просмотре.
            case 4:
                return "AFTER UPDATE"; // 	Триггер выполняется после изменения записи в таблице или просмотре.
            case 5:
                return "BEFORE DELETE"; // 	Триггер выполняется перед удалением записи из таблицы или просмотра.
            case 6:
                return "AFTER DELETE"; // 	Триггер выполняется после удаления записи из таблицы или просмотра.
            case 17:
                return "BEFORE INSERT OR UPDATE"; // 	Триггер выполняется перед вставкой или изменением записи в таблице или просмотре.
            case 18:
                return "AFTER INSERT OR UPDATE"; // 	Триггер выполняется после вставки или изменения записи в таблице или просмотре.
            case 25:
                return "BEFORE INSERT OR DELETE"; // 	Триггер выполняется перед вставкой или удалением записи в таблице или просмотре.
            case 26:
                return "AFTER INSERT OR DELETE"; // 	Триггер выполняется после вставки или удаления записи в таблице или просмотре.
            case 27:
                return "BEFORE UPDATE OR DELETE"; // 	Триггер выполняется перед изменением или удалением записи в таблице или просмотре.
            case 28:
                return "AFTER UPDATE OR DELETE"; // 	Триггер выполняется после изменения или удаления записи в таблице или просмотре.
            case 113:
                return "BEFORE INSERT OR UPDATE OR DELETE"; // 	Триггер выполняется перед вставкой, изменением или удалением записи в таблице или просмотре.
            case 114:
                return "AFTER INSERT OR UPDATE OR DELETE"; // 	Триггер выполняется после вставки, изменения или удаления записи в таблице или просмотре.
            case 8192:
                return "ON CONNECT"; // 	Триггер выполняется после установления подключения к базе данных.
            case 8193:
                return "ON DISCONNECT"; // 	Триггер выполняется перед отключением от базы данных.
            case 8194:
                return "ON TRANSACTION START"; // 	Триггер выполняется после старта транзакции.
            case 8195:
                return "ON TRANSACTION COMMIT"; // 	Триггер выполняется перед подтверждением COMMIT транзакции.
            case 8196:
                return "ON TRANSACTION ROLLBACK"; // 	Триггер выполняется перед отменой ROLLBACK транзакции.
            default:
                return "NULL";
        }
    }

    @Override
    public String getCreateSQLText() {
        StringBuilder sb = new StringBuilder();

        sb.append("SET TERM ^ ;");
        sb.append("\n\n");
        sb.append("CREATE OR ALTER TRIGGER ");
        sb.append(getName());
        sb.append(" FOR ");
        sb.append(getTriggerTableName());
        sb.append("\n");
        sb.append(isTriggerActive() ? "ACTIVE" : "INACTIVE");
        sb.append(" ");
        sb.append(getStringTriggerType());
        sb.append(" POSITION ");
        sb.append(getTriggerSequence());
        sb.append("\n");
        if (triggerSourceCode != null) {
            sb.append(getTriggerSourceCode());
            sb.append("^");
        }
        sb.append("\n\n");
        sb.append("SET TERM ; ^");

        return sb.toString();
    }

}
