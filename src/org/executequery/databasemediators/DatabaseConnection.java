/*
 * DatabaseConnection.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.databasemediators;

import org.executequery.gui.browser.ConnectionsFolder;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

/**
 * <p>This class maintains the necessary information for each
 * saved database connection.<br>
 * Each saved connection appears by name within the
 * saved connections drop-down box displayed on respective
 * windows.
 *
 * @author Takis Diakoumis
 */
public interface DatabaseConnection extends Serializable {

    boolean isPasswordStored();

    void setPasswordStored(boolean storePwd);

    void setJdbcProperties(Properties jdbcProperties);

    Properties getJdbcProperties();

    boolean hasAdvancedProperties();

    DatabaseDriver getJDBCDriver();

    boolean hasURL();

    int getPortInt();

    void setJDBCDriver(DatabaseDriver driver);

    String getDriverName();

    void setDriverName(String dName);

    String getPort();

    boolean hasPort();

    void setPort(String port);

    String getCharset();

    void setCharset(String charset);

    String getRole();

    void setRole(String role);

    String getCertificate();

    void setCertificate(String certificate);

    boolean isContainerPasswordStored();

    void setContainerPasswordStored(boolean storePwd);

    boolean isVerifyServerCertCheck();

    void setVerifyServerCertCheck(boolean verifyServer);

    boolean useNewAPI();

    void setUseNewAPI(boolean useNewAPI);

    String getContainerPassword();

    void setContainerPassword(String password);

    String getAuthMethod();

    void setAuthMethod(String method);

    String getConnectionMethod();

    void setConnectionMethod(String method);

    String getURL();

    void setURL(String url);

    String getDatabaseType();

    void setDatabaseType(String databaseType);

    String getPassword();

    String getUnencryptedPassword();

    void setEncryptedPassword(String password);

    void setPassword(String password);

    String getSourceName();

    void setSourceName(String sourceName);

    boolean hasHost();

    boolean hasSourceName();

    String getHost();

    void setHost(String host);

    String getUserName();

    void setUserName(String userName);

    String getName();

    void setName(String name);

    boolean isPasswordEncrypted();

    void setPasswordEncrypted(boolean passwordEncrypted);

    boolean isConnected();

    void setConnected(boolean connected);

    int getTransactionIsolation();

    void setTransactionIsolation(int transactionIsolation);

    boolean isAutoCommit();

    void setAutoCommit(boolean autoCommit);

    long getDriverId();

    void setDriverId(long driverId);

    String getId();

    void setId(String id);

    DatabaseConnection copy();

    String getFolderId();

    ConnectionsFolder getFolder();

    void setFolderId(String folderId);

    DatabaseConnection withName(String name);

    DatabaseConnection withSource(String source);

    void setSshPasswordStored(boolean sshPasswordStored);

    void setSshTunnel(boolean sshTunnel);

    void setSshPort(int sshPort);

    void setSshUserName(String sshUserName);

    void setSshPassword(String sshPassword);

    boolean isSshPasswordStored();

    int getSshPort();

    String getSshPassword();

    String getSshUserName();

    boolean isSshTunnel();

    String getUnencryptedSshPassword();

    void setEncryptedSshPassword(String sshPassword);

    DatabaseConnection withNewId();

    List<String> getListObjectsDB();

    int getServerVersion();

    void setServerVersion(int serverVersion);

    boolean isNamesToUpperCase();

    void setNamesToUpperCase(boolean flag);
}


