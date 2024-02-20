package org.executequery.databaseobjects.impl;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.Condition;
import org.executequery.sql.sqlbuilder.Field;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DefaultDatabaseUser extends AbstractDatabaseObject {

    private String plugin;
    private boolean active;
    private boolean admin;
    private Map<String, String> tags;
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;

    public DefaultDatabaseUser(DatabaseMetaTag metaTagParent, String name) {
        this(metaTagParent, name, "");
    }

    public DefaultDatabaseUser(DatabaseMetaTag metaTagParent, String name, String plugin) {
        super(metaTagParent, name);
        this.plugin = plugin;
        active = true;
        tags = new HashMap<>();
        admin = false;
    }

    @Override
    protected String getFieldName() {
        return "USER_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("SEC$USERS", "U");
    }

    protected Field getObjectField() {
        return Field.createField(getMainTable(), getFieldName()).setName("SEC$" + getFieldName());
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        Table table = getMainTable();
        sb.appendTable(table);
        sb.setOrdering(getObjectField().getFieldTable());
        return sb;
    }

    @Override
    protected SelectBuilder builderForInfoAllObjects(SelectBuilder commonBuilder) {
        return commonBuilder;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        try {
            setFirstName(rs.getString(2).trim());
        } catch (NullPointerException e) {
            setFirstName("");
        }
        try {
            setMiddleName(rs.getString(3).trim());
        } catch (NullPointerException e) {
            setMiddleName("");
        }
        try {
            setLastName(rs.getString(4).trim());
        } catch (NullPointerException e) {
            setLastName("");
        }
        try {
            setActive(rs.getBoolean(5));
        } catch (NullPointerException e) {
            setActive(false);
        }
        try {
            setAdministrator(rs.getBoolean(6));
        } catch (NullPointerException e) {
            setAdministrator(false);
        }
        try {
            setRemarks(rs.getString(7));
        } catch (NullPointerException e) {
            setRemarks("");
        }
        try {
            setPlugin(rs.getString(8).trim());
        } catch (NullPointerException e) {
            setPlugin("");
        }
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
        return NamedObject.USER;
    }

    @Override
    public String getMetaDataKey() {
        return META_TYPES[USER];
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTag(String tag, String value) {
        tags.put(tag, value);
    }

    public void dropTag(String tag) {
        tags.remove(tag);
    }

    public String getTag(String tag) {
        return tags.get(tag);
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Boolean getAdministrator() {
        return admin;
    }

    public void setAdministrator(boolean administrator) {
        admin = administrator;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns copy of this object
     */
    @Override
    public DefaultDatabaseUser copy() {

        DefaultDatabaseUser user = new DefaultDatabaseUser((DatabaseMetaTag) getParent(), getName());
        user.setFirstName(firstName);
        user.setMiddleName(middleName);
        user.setLastName(lastName);
        user.setActive(active);
        user.setAdministrator(admin);
        user.setRemarks(remarks);
        //user.setPassword(password);
        user.setPlugin(plugin);
        user.tags = new HashMap<>();
        for (String key : tags.keySet())
            user.tags.put(key, tags.get(key));

        return user;
    }

    void loadTags() {
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        try {
            String query = "SELECT * FROM SEC$USER_ATTRIBUTES WHERE SEC$USER_NAME = '" + getName() + "' and SEC$PLUGIN = '" + getPlugin() + "'";
            ResultSet rs = querySender.getResultSet(query).getResultSet();
            while (rs.next()) {
                tags.put(rs.getString(2), rs.getString(3));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            querySender.releaseResources();
        }
    }

    public void loadData() {
        getObjectInfo();
        loadTags();
    }

    @Override
    protected String queryForInfo() {
        SelectBuilder sb = builderCommonQuery();
        sb.appendCondition(buildNameCondition(getObjectField()));
        sb.appendCondition(Condition.createCondition().setStatement(String.format("SEC$PLUGIN = '%s'", getPlugin())));
        return sb.getSQLQuery();
    }

    @Override
    public String getCreateSQLText() throws DataSourceException {
        return SQLUtils.generateCreateUser(this, true);
    }

    @Override
    public String getCreateSQLTextWithoutComment() throws DataSourceException {
        return SQLUtils.generateCreateUser(this, false);
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("USER", getName(), getPlugin(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return SQLUtils.generateCreateUser(this, Comparer.isCommentsNeed());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseUser comparingUser = (DefaultDatabaseUser) databaseObject;
        return SQLUtils.generateAlterUser(this, comparingUser, Comparer.isCommentsNeed());
    }

}
