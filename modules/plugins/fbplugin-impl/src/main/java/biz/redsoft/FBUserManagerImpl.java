package biz.redsoft;

import org.firebirdsql.management.FBUser;
import org.firebirdsql.management.FBUserManager;
import org.firebirdsql.management.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Vasiliy on 14.07.2017.
 */
public class FBUserManagerImpl implements IFBUserManager {

    private FBUserManager fbUserManager;

    public FBUserManagerImpl() {
        if (fbUserManager == null)
            fbUserManager = new FBUserManager();
    }

    @Override
    public void setUser(String user) {
        fbUserManager.setUser(user);
    }

    @Override
    public String getUser() {
        return fbUserManager.getUser();
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
        FBUser fbUser = new FBUser();
        fbUser.setFirstName(user.getFirstName());
        fbUser.setGroupId(user.getGroupId());
        fbUser.setLastName(user.getLastName());
        fbUser.setMiddleName(user.getMiddleName());
        fbUser.setPassword(user.getPassword());
        fbUser.setUserId(user.getUserId());
        fbUser.setUserName(user.getUserName());
        fbUserManager.add(fbUser);
    }

    @Override
    public void delete(IFBUser user) throws SQLException, IOException {
        FBUser fbUser = new FBUser();
        fbUser.setFirstName(user.getFirstName());
        fbUser.setGroupId(user.getGroupId());
        fbUser.setLastName(user.getLastName());
        fbUser.setMiddleName(user.getMiddleName());
        fbUser.setPassword(user.getPassword());
        fbUser.setUserId(user.getUserId());
        fbUser.setUserName(user.getUserName());
        fbUserManager.delete(fbUser);
    }

    @Override
    public void update(IFBUser user) throws SQLException, IOException {
        FBUser fbUser = new FBUser();
        fbUser.setFirstName(user.getFirstName());
        fbUser.setGroupId(user.getGroupId());
        fbUser.setLastName(user.getLastName());
        fbUser.setMiddleName(user.getMiddleName());
        fbUser.setPassword(user.getPassword());
        fbUser.setUserId(user.getUserId());
        fbUser.setUserName(user.getUserName());
        fbUserManager.update(fbUser);
    }

    @Override
    public Map<String, IFBUser> getUsers() throws SQLException, IOException {
        Map<String, User> users = fbUserManager.getUsers();
        Map<String, IFBUser> mUsers = new TreeMap<>();
        for (Map.Entry<String, User> entry : users.entrySet()) {
            String key = entry.getKey();
            FBUserImpl value = new FBUserImpl();
            value.setUserName(entry.getValue().getUserName());
            value.setPassword(entry.getValue().getPassword());
            value.setFirstName(entry.getValue().getFirstName());
            value.setMiddleName(entry.getValue().getMiddleName());
            value.setLastName(entry.getValue().getLastName());
            value.setUserId(entry.getValue().getUserId());
            value.setGroupId(entry.getValue().getGroupId());
            mUsers.put(key, value);
        }
        return mUsers;
    }
}
