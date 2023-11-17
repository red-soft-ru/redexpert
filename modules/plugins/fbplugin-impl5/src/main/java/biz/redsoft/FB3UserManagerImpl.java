package biz.redsoft;


import org.firebirdsql.management.FBUserManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class FB3UserManagerImpl extends AbstractServiceManager implements IFBUserManager {

    Connection con;
    String user;
    FBUserManager fbUserManager;

    public FB3UserManagerImpl(Connection connection) {
        super();
        con = connection;
    }

    @Override
    protected void initServiceManager() {
        fbUserManager = new FBUserManager();
        fbServiceManager = fbUserManager;
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
    public void add(IFBUser user) throws SQLException, IOException {
        String query;
        if (!user.getPlugin().equals("")) {
            query = "CREATE USER " + user.getUserName() + "\n" +
                    "PASSWORD '" + user.getPassword() + "'\n" +
                    "USING PLUGIN " + user.getPlugin();
            execute_query(query);
        } else {
            query = "CREATE USER " + user.getUserName() + "\n" +
                    "PASSWORD '" + user.getPassword() + "'";
            execute_query(query);
        }
        update(user, true);
    }

    @Override
    public void delete(IFBUser user) throws SQLException, IOException {
        String query;
        if (user.getPlugin().equals(""))
            query = "DROP USER \"" + user.getUserName() + "\"\n";
        else
            query = "DROP USER \"" + user.getUserName() + "\"\n" +
                    "USING PLUGIN " + user.getPlugin();
        execute_query(query);

    }

    @Override
    public void update(IFBUser user) throws SQLException, IOException {
        update(user, false);

    }

    void update(IFBUser user, boolean create) throws SQLException, IOException {
        IFBUser user1;
        if (create) {
            user1 = new FBUserImpl();
            user1.setUserName(user.getUserName().toUpperCase());
        } else
            user1 = getUsers().get(user.getUserName() + ":" + user.getPlugin());
        user.setUserName(user1.getUserName());
        if (!user.equals(user1)) {
            String query = "ALTER USER \"" + user.getUserName() + "\"";
            if (user.getFirstName() != user1.getFirstName())
                query += "\nFIRSTNAME '" + user.getFirstName() + "'";
            if (user.getMiddleName() != user1.getMiddleName())
                query += "\nMIDDLENAME '" + user.getMiddleName() + "'";
            if (user.getLastName() != user1.getLastName())
                query += "\nLASTNAME '" + user.getLastName() + "'";
            if (user.getPassword() != "" && user.getPassword() != null) {
                query += "\nPASSWORD '" + user.getPassword() + "'\n";
            }
            if (user.getActive()) {
                query += "\nACTIVE";
            } else {
                query +=
                        "\nINACTIVE";
            }
            if (user.getAdministrator() != user1.getAdministrator())
                if (user.getAdministrator()) {
                    query += "\nGRANT ADMIN ROLE";
                } else {
                    query += "\nREVOKE ADMIN ROLE";
                }
            if (!user.getPlugin().equals(""))
                query += "\nUSING PLUGIN " + user.getPlugin();
            Map<String, String> tags = user.getTags();
            if (create && tags.size() > 0) {
                query += "\nTAGS (";
                for (String tag : tags.keySet()) {
                    query += tag + " = '" + tags.get(tag) + "' , ";
                }
                query = query.substring(0, query.lastIndexOf(","));
                query += " )";
            } else {
                Map<String, String> tags1 = getTags(user.getUserName(), user.getPlugin());
                if (!tags.equals(tags1)) {
                    query += "\nTAGS (";
                    for (String tag : tags1.keySet()) {
                        if (!tags.containsKey(tag)) {
                            query += "DROP " + tag + " , ";
                        }
                    }
                    for (String tag : tags.keySet()) {
                        query += tag + " = '" + tags.get(tag) + "' , ";
                    }
                    query = query.substring(0, query.lastIndexOf(","));
                    query += " )";
                }
            }
            execute_query(query);
            query = "COMMENT ON USER \"" + user.getUserName() + "\" is '" + user.getDescription() + "'";
            execute_query(query);
        }
    }

    @Override
    public Map<String, IFBUser> getUsers() throws SQLException {
        Map<String, IFBUser> mUsers = new TreeMap<>();

        Statement state = null;
        try {
            state = con.createStatement();
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
                    value.setActive(false);
                }
                try {
                    value.setAdministrator(result.getBoolean(6));
                } catch (NullPointerException e) {
                    value.setAdministrator(false);
                }
                try {
                    value.setDescription(result.getString(7));
                } catch (NullPointerException e) {
                    value.setDescription("");
                }
                try {
                    value.setPlugin(result.getString(8).trim());
                } catch (NullPointerException e) {
                    value.setPlugin("");
                }
                //value.setTags(getTags(key,value.getPlugin()));
                mUsers.put(key + ":" + value.getPlugin(), value);
            }
        } finally {
            if (state != null && !state.isClosed())
                state.close();
        }
        for (IFBUser u : mUsers.values()) {
            u.setTags(getTags(u.getUserName(), u.getPlugin()));
        }
        return mUsers;
    }

    private Map<String, String> getTags(String name, String Plugin) {
        Map<String, String> tags = new HashMap<>();
        Statement state1 = null;
        try {
            state1 = con.createStatement();
            String query = "SELECT * FROM SEC$USER_ATTRIBUTES WHERE SEC$USER_NAME = '" + name + "' and SEC$PLUGIN = '" + Plugin + "'";
            ResultSet result1 = state1.executeQuery(query);
            while (result1.next()) {
                tags.put(result1.getString(2), result1.getString(3));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (state1 != null && !state1.isClosed())
                    state1.close();
            } catch (SQLException e) {
                // nothing to do
            }
        }
        return tags;
    }

    private void execute_query(String query) throws SQLException {
        Statement state = null;
        try {
            state = con.createStatement();
            state.executeUpdate(query);
            if (!con.getAutoCommit())
                con.commit();
        } finally {
            if (state != null && !state.isClosed())
                state.close();
        }

    }
}