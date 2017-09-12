package biz.redsoft;

import java.util.Map;

/**
 * Created by Vasiliy on 14.07.2017.
 */
public interface IFBUser {

    void setUserName(String userName);

    String getUserName();

    void setPassword(String password);

    String getPassword();

    void setFirstName(String firstName);

    String getFirstName();

    void setMiddleName(String middleName);

    String getMiddleName();

    void setLastName(String lastName);

    String getLastName();

    void setGroupId(int groupId);

    int getGroupId();

    void setUserId(int userId);

    int getUserId();

    void setDescription(String description);

    String getDescription();

    void setActive(boolean active);

    Boolean getActive();

    void setTag(String tag, String value);

    void setTags(Map<String, String> tags);

    void dropTag(String tag);

    String getTag(String tag);

    Map<String, String> getTags();

}
