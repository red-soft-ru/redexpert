package biz.redsoft;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by Vasiliy on 14.07.2017.
 */
public interface IFBUserManager {

  void setUser(String user);

  String getUser();

  void setPassword(String password);

  String getPassword();

  void setDatabase(String database);

  String getDatabase();

  String getHost();

  void setHost(String host);

  int getPort();

  void setPort(int port);

  void add(IFBUser user) throws SQLException, IOException;

  void delete(IFBUser user) throws SQLException, IOException;

  void update(IFBUser user) throws SQLException, IOException;

  Map<String, IFBUser> getUsers() throws SQLException, IOException;
}
