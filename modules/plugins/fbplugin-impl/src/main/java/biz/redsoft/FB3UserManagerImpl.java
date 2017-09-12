package biz.redsoft;

import org.firebirdsql.management.FBUser;
import org.firebirdsql.management.FBUserManager;
import org.firebirdsql.management.User;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class FB3UserManagerImpl implements IFBUserManager {

    Connection con;
    String user;
    FBUserManager fbUserManager;

    public FB3UserManagerImpl(Connection connection) {
        con = connection;
        fbUserManager = new FBUserManager();
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setPassword(String password) {
        fbUserManager.setPassword(password);
    }

    @Override
    public String getPassword() {
        return fbUserManager.getPassword();
    }

    @Override
    public void setDatabase(String database) {
        fbUserManager.setDatabase(database);
    }

    @Override
    public String getDatabase() {
        return fbUserManager.getDatabase();
    }

    @Override
    public String getHost() {
        return fbUserManager.getHost();
    }

    @Override
    public void setHost(String host) {
        fbUserManager.setHost(host);
    }

    @Override
    public int getPort() {
        return fbUserManager.getPort();
    }

    @Override
    public void setPort(int port) {
        fbUserManager.setPort(port);
    }

    @Override
    public void add(IFBUser user) throws SQLException, IOException {
        String query = "CREATE USER " + user.getUserName() + "\n" +
                "PASSWORD '" + user.getPassword() + "'";
        execute_query(query);
        update(user);
    }

    @Override
    public void delete(IFBUser user) throws SQLException, IOException {
        String query = "DROP USER " + user.getUserName() + "\n";
        execute_query(query);

    }

    @Override
    public void update(IFBUser user) throws SQLException, IOException {
        String query = "CREATE OR ALTER USER " + user.getUserName() + "\n" +
                " FIRSTNAME '" + user.getFirstName() + "'\n" +
                " MIDDLENAME '" + user.getMiddleName() + "'\n" +
                " LASTNAME '" + user.getLastName() + "'";
        execute_query(query);
        if (user.getPassword() != "") {
            query = "CREATE OR ALTER USER " + user.getUserName() + "\n" +
                    " PASSWORD '" + user.getPassword() + "'\n";
            execute_query(query);
        }
        if (user.getActive()) {
            query = "CREATE OR ALTER USER " + user.getUserName() + "\n" +
                    " ACTIVE";
            execute_query(query);
        } else {
            query = "CREATE OR ALTER USER " + user.getUserName() + "\n" +
                    " INACTIVE";
            execute_query(query);
        }
        Map<String, String> tags = getTags(user.getUserName());
        for (String tag : tags.keySet()) {
            query = "CREATE OR ALTER USER " + user.getUserName() + "\n" +
                    "TAGS (DROP " + tag + ")";
            execute_query(query);
        }
        tags = user.getTags();
        for (String tag : tags.keySet()) {
            query = "CREATE OR ALTER USER " + user.getUserName() + "\n" +
                    "TAGS (" + tag + " = '" + tags.get(tag) + "')";
            execute_query(query);
        }
        query = "COMMENT ON USER " + user.getUserName() + " is '" + user.getDescription() + "'";
        execute_query(query);
    }

    @Override
    public Map<String, IFBUser> getUsers() throws SQLException, IOException {
        Map<String, IFBUser> mUsers = new TreeMap<>();

        Statement state = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        String query = "SELECT * FROM SEC$USERS";
        ResultSet result = state.executeQuery(query);
        while (result.next()) {
            // do code
            String key = result.getString(1).trim();
            FBUserImpl value = new FBUserImpl();
            value.setUserName(key);
            value.setPassword("");
            try {
                value.setFirstName(result.getString(2).trim());
            } catch (NullPointerException e) {
                value.setFirstName("");
            }
            try {
                value.setMiddleName(result.getString(3).trim());
            } catch (NullPointerException e) {
                value.setMiddleName("");
            }
            try {
                value.setLastName(result.getString(4).trim());
            } catch (NullPointerException e) {
                value.setLastName("");
            }
            try {
                value.setActive(result.getBoolean(5));
            } catch (NullPointerException e) {
                value.setActive(true);
            }
            try {
                value.setDescription(result.getString(7));
            } catch (NullPointerException e) {
                value.setDescription("");
            }
            value.setTags(getTags(key));
            mUsers.put(key, value);
        }
        state.close();

        return mUsers;
    }

    private Map<String, String> getTags(String name) {
        Map<String, String> tags = new HashMap<>();
        try {

            Statement state1 = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
            String query = "SELECT * FROM SEC$USER_ATTRIBUTES WHERE SEC$USER_NAME = '" + name + "' ";
            ResultSet result1 = state1.executeQuery(query);
            while (result1.next()) {
                tags.put(result1.getString(2), result1.getString(3));
            }
            state1.close();
            return tags;

        } catch (Exception e) {
            return tags;
        }
    }

    private void execute_query(String query) throws SQLException, IOException {

        Statement state = con.createStatement();
        state.executeUpdate(query);
        state.close();
    }
}
