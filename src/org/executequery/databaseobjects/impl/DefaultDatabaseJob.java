package org.executequery.databaseobjects.impl;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DefaultDatabaseJob extends AbstractDatabaseObject{

    public static final int PSQL_TYPE=0;
    public static final int BASH_TYPE=1;
    private String id;
    private String source;
    private boolean active;
    private int jobType;
    private String cronSchedule;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String database;

    public DefaultDatabaseJob(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
        querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    private static final String JOB_ID = "JOB_ID";
    private static final String SOURCE = "JOB_SOURCE";

    private static final String ACTIVE = "JOB_INACTIVE";
    private static final String JOB_TYPE = "JOB_TYPE";
    private static final String SCHEDULE = "JOB_SCHEDULE";
    private static final String START_DATE = "START_DATE";
    private static final String END_DATE = "END_DATE";
    private static final String DATABASE = "DATABASE";


    @Override
    protected String getFieldName() {
        return "JOB_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$JOBS", "J");
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        Table jobs = getMainTable();
        sb.appendTable(jobs);

        sb.appendFields(jobs, JOB_ID, SOURCE, ACTIVE, JOB_TYPE, SCHEDULE, START_DATE, END_DATE, DATABASE, DESCRIPTION);
        sb.setOrdering(getObjectField().getFieldTable());
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        setId(getFromResultSet(rs, JOB_ID));
        setSource(getFromResultSet(rs, SOURCE));
        setCronSchedule(getFromResultSet(rs, SCHEDULE));
        setDatabase(getFromResultSet(rs, DATABASE));
        setRemarks(getFromResultSet(rs, DESCRIPTION));
        setJobType(rs.getInt(JOB_TYPE));
        setActive(rs.getInt(ACTIVE) == 0);
        setStartDate(rs.getObject(START_DATE, LocalDateTime.class));
        setEndDate(rs.getObject(END_DATE, LocalDateTime.class));
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

    @Override
    public int getType() {
        return NamedObject.JOB;
    }

    @Override
    public String getMetaDataKey() {
        return NamedObject.META_TYPES[NamedObject.JOB];
    }

    @Override
    public String getCreateSQLText() throws DataSourceException {
        return SQLUtils.generateCreateJob(getName(), getCronSchedule(), isActive(),
                getStartDate(), getEndDate(), getJobType(), getSource(), getRemarks(), true, getHost().getDatabaseConnection());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("JOB", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return getCreateSQLText();
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseJob comparingJob = (DefaultDatabaseJob) databaseObject;
        return SQLUtils.generateAlterJob(this, comparingJob);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getSource() {
        if (source == null || isMarkedForReload()) {
            getObjectInfo();
        }
        return source;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getJobType() {
        return jobType;
    }

    public void setJobType(int jobType) {
        this.jobType = jobType;
    }

    public String getCronSchedule() {
        if (cronSchedule == null || isMarkedForReload()) {
            getObjectInfo();
        }
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getDatabase() {
        if (database == null || isMarkedForReload()) {
            getObjectInfo();
        }
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
