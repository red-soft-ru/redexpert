package biz.redsoft;

import org.firebirdsql.management.FBUser;

/**
 * Created by Vasiliy on 14.07.2017.
 */
public class FBUserImpl implements IFBUser {

  private FBUser fbUser;

  public FBUserImpl() {
    fbUser = new FBUser();
  }

  @Override
  public void setUserName(String userName) {
    fbUser.setUserName(userName);
  }

  @Override
  public String getUserName() {
    return fbUser.getUserName();
  }

  @Override
  public void setPassword(String password) {
    fbUser.setPassword(password);
  }

  @Override
  public String getPassword() {
    return fbUser.getPassword();
  }

  @Override
  public void setFirstName(String firstName) {
    fbUser.setFirstName(firstName);
  }

  @Override
  public String getFirstName() {
    return fbUser.getFirstName();
  }

  @Override
  public void setMiddleName(String middleName) {
    fbUser.setMiddleName(middleName);
  }

  @Override
  public String getMiddleName() {
    return fbUser.getMiddleName();
  }

  @Override
  public void setLastName(String lastName) {
    fbUser.setLastName(lastName);
  }

  @Override
  public String getLastName() {
    return fbUser.getLastName();
  }

  @Override
  public void setGroupId(int groupId) {
    fbUser.setGroupId(groupId);
  }

  @Override
  public int getGroupId() {
    return fbUser.getGroupId();
  }

  @Override
  public void setUserId(int userId) {
    fbUser.setUserId(userId);
  }

  @Override
  public int getUserId() {
    return fbUser.getUserId();
  }
}
