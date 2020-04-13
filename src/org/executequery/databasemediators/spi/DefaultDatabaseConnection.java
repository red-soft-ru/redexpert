/*
 * DefaultDatabaseConnection.java
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

package org.executequery.databasemediators.spi;

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.crypto.PasswordEncoderDecoder;
import org.executequery.crypto.spi.DefaultPasswordEncoderDecoderFactory;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.ConnectionsFolder;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.repository.ConnectionFoldersRepository;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.util.MiscUtils;

import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * <p>This class maintains the necessary information for each
 * saved database connection.<br>
 * Each saved connection appears by name within the
 * saved connections drop-down box displayed on respective
 * windows.
 *
 * @author Takis Diakoumis
 */
public class DefaultDatabaseConnection implements DatabaseConnection {

    private static final String ENCRYPTION_KEY = "yb7UD9jH";

    /**
     * the unique id for this connection
     */
    private String id;

    /**
     * The unique name for this connection
     */
    private String name;

    /**
     * The user name for this connection
     */
    private String userName;

    /**
     * The password for this connection
     */
    private String password;

    /**
     * The host for this connection
     */
    private String host;

    /**
     * The data source name for this connection
     */
    private String sourceName;

    /**
     * The database vendor's name for this connection
     */
    private String databaseType;

    /**
     * The port number for this connection
     */
    private String port;

    /**
     * The driver specific URL for this connection
     */
    private String url;

    /**
     * The unique name of the JDBC/ODBC driver used with this connection
     */
    private String driverName;

    /**
     * The unique ID of the JDBC/ODBC driver used with this connection
     */
    private long driverId;

    /**
     * The JDBC/ODBC Driver used with this connection
     */
    private DatabaseDriver driver;

    /**
     * The advanced Properties for this connection
     */
    private Properties jdbcProperties;

    /**
     * Whether this connection's password is stored
     */
    private boolean passwordStored;

    /**
     * Whether the password is encrypted
     */
    private boolean passwordEncrypted;

    /**
     * the tx isolation level
     */
    private int transactionIsolation;

    private String charset;

    private String role;

    /**
     * the path to certificate for multifactor authentication
     */
    private String certificate;

    /**
     * Whether container password is stored
     */
    private boolean containerPasswordStored;

    /**
     * The password for certificate container
     */
    private String containerPassword;

    /**
     * Whether server certificate is verified
     */
    private boolean verifyServerCertificate;

    private String authMethod;

    private String connectionMethod;

    private String folderId;

    private transient ConnectionsFolder folder;

    private boolean sshTunnel;

    private String sshUserName;

    private String sshPassword;

    private int sshPort;

    private boolean sshPasswordStored;

    /**
     * the commit mode
     */
    private transient boolean autoCommit = true;

    /**
     * Whether this connection is active
     */
    private transient boolean connected = false;

    private transient PasswordEncoderDecoder encoderDecoder;

    private int serverVersion;

    private boolean namesToUpperCase = true;

    @Override
    public boolean isNamesToUpperCase() {
        return namesToUpperCase;
    }

    @Override
    public void setNamesToUpperCase(boolean namesToUpperCase) {
        this.namesToUpperCase = namesToUpperCase;
    }

    /**
     * Creates a new empty <code>DatabaseConnection</code> object.
     */
    public DefaultDatabaseConnection() {

        this(null);
    }

    /**
     * Creates a new empty <code>DatabaseConnection</code> object
     * with the specified name.
     *
     * @param A unique name for this connection.
     */
    public DefaultDatabaseConnection(String name) {

        this.name = name;
        transactionIsolation = -1;
    }

    public boolean isPasswordStored() {
        return passwordStored;
    }

    public void setPasswordStored(boolean storePwd) {
        this.passwordStored = storePwd;
    }

    public void setJdbcProperties(Properties jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }

    public Properties getJdbcProperties() {
        return jdbcProperties;
    }

    public boolean hasAdvancedProperties() {
        return jdbcProperties != null && jdbcProperties.size() > 0;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFolderId() {
        return folderId;
    }

    public ConnectionsFolder getFolder() {

        if (folder == null) {

            folder = folderById(folderId);
        }

        return folder;
    }

    public DatabaseDriver getJDBCDriver() {

        if (driver == null) {

            driver = driverById(getDriverId());
        }

        return driver;
    }

    public boolean hasURL() {
        return StringUtils.isNotBlank(url);
    }

    public int getPortInt() {
        if (port.isEmpty())
            return 3050;
        return Integer.parseInt(port);
    }

    public void setJDBCDriver(DatabaseDriver driver) {
        this.driver = driver;
    }

    public String getDriverName() {
        return driverName == null ? Constants.EMPTY : driverName;
    }

    public void setDriverName(String dName) {
        this.driverName = dName;
    }

    public String getPort() {
        return port == null ? Constants.EMPTY : port;
    }

    public boolean hasPort() {
        return port != null && port.length() > 0;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getCharset() {
        return charset == null ? Constants.EMPTY : charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getRole() {
        return role == null ? Constants.EMPTY : role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCertificate() {
        return certificate == null ? Constants.EMPTY : certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public boolean isContainerPasswordStored() {
        return containerPasswordStored;
    }

    public String getContainerPassword() {
        return this.containerPassword;
    }

    public void setContainerPassword(String password) {
        this.containerPassword = password;
    }

    public void setContainerPasswordStored(boolean storePwd) {
        this.containerPasswordStored = storePwd;
    }

    public boolean isVerifyServerCertCheck() {
        return verifyServerCertificate;
    }

    public void setVerifyServerCertCheck(boolean verifyServer) {
        this.verifyServerCertificate = verifyServer;
    }

    public String getConnectionMethod() {
        return connectionMethod == null ? "Standard" : connectionMethod;
    }

    public void setConnectionMethod(String method) {
        this.connectionMethod = method;
    }

    public String getAuthMethod() {
        return authMethod == null ? "Basic" : authMethod;
    }

    public void setAuthMethod(String method) {
        this.authMethod = method;
    }

    public String getURL() {
        return url == null ? Constants.EMPTY : url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getPassword() {
        return password;
    }

    public String getUnencryptedPassword() {
        String _password = password;
        if (passwordEncrypted && !MiscUtils.isNull(password)) {
            _password = decrypt(password);
        }
        return _password;
    }

    private String decrypt(String password) {
        return passwordEncoderDecoder().decode(ENCRYPTION_KEY, password);
    }

    private String encrypt(String password) {
        return passwordEncoderDecoder().encode(ENCRYPTION_KEY, password);
    }

    private PasswordEncoderDecoder passwordEncoderDecoder() {

        if (encoderDecoder == null) {

            encoderDecoder = new DefaultPasswordEncoderDecoderFactory().create();
        }

        return encoderDecoder;
    }

    public void setEncryptedPassword(String password) {
        this.password = password;
    }

    public void setPassword(String password) {

        if (passwordEncrypted && !MiscUtils.isNull(password)) {

            this.password = encrypt(password);

        } else {

            this.password = password;
        }
    }

    public String getSourceName() {
        return sourceName == null ? Constants.EMPTY : sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public boolean hasHost() {
        return host != null && host.length() > 0;
    }

    public boolean hasSourceName() {
        return sourceName != null && sourceName.length() > 0;
    }

    public String getHost() {
        return host == null ? Constants.EMPTY : host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public boolean isPasswordEncrypted() {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(boolean passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public int getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(int transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public long getDriverId() {
        return driverId;
    }

    public void setDriverId(long driverId) {
        this.driverId = driverId;
    }

    public String getId() {
        if (id == null) {
            setId(generateId());
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DatabaseConnection withNewId() {
        setId(generateId());
        return this;
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    public DatabaseConnection copy() {

        DatabaseConnection copy = new DefaultDatabaseConnection(getName());

        copy.setId(generateId());
        copy.setPasswordStored(isPasswordStored());
        copy.setPasswordEncrypted(isPasswordEncrypted());
        copy.setDriverId(getDriverId());
        copy.setDatabaseType(getDatabaseType());
        copy.setHost(getHost());
        copy.setPort(getPort());
        copy.setSourceName(getSourceName());
        copy.setTransactionIsolation(getTransactionIsolation());
        copy.setURL(getURL());
        copy.setUserName(getUserName());

        if (getJdbcProperties() != null) {

            copy.setJdbcProperties((Properties) getJdbcProperties().clone());
        }

        if (copy.isPasswordEncrypted()) {

            copy.setEncryptedPassword(getPassword());
        } else {

            copy.setPassword(getPassword());
        }

        copy.setSshTunnel(isSshTunnel());
        copy.setSshUserName(getSshUserName());
        copy.setSshPort(getSshPort());
        copy.setEncryptedSshPassword(getSshPassword());
        copy.setSshPasswordStored(isSshPasswordStored());
        copy.setCertificate(getCertificate());
        copy.setAuthMethod(getAuthMethod());
        copy.setContainerPassword(getContainerPassword());
        copy.setContainerPasswordStored(isContainerPasswordStored());
        copy.setVerifyServerCertCheck(isVerifyServerCertCheck());
        copy.setServerVersion(getServerVersion());

        return copy;
    }

    @Override
    public boolean isSshTunnel() {

        return sshTunnel;
    }

    @Override
    public String getSshUserName() {

        return sshUserName;
    }

    public String getUnencryptedSshPassword() {
        String _password = sshPassword;
        if (!MiscUtils.isNull(sshPassword)) {
            _password = decrypt(sshPassword);
        }
        return _password;
    }

    @Override
    public String getSshPassword() {

        return sshPassword;
    }

    @Override
    public int getSshPort() {

        return sshPort;
    }

    @Override
    public boolean isSshPasswordStored() {

        return sshPasswordStored;
    }

    @Override
    public void setSshPassword(String sshPassword) {

        if (!MiscUtils.isNull(sshPassword)) {

            this.sshPassword = encrypt(sshPassword);
        }
    }

    public void setEncryptedSshPassword(String sshPassword) {

        this.sshPassword = sshPassword;
    }

    @Override
    public void setSshUserName(String sshUserName) {

        this.sshUserName = sshUserName;
    }

    @Override
    public void setSshPort(int sshPort) {

        this.sshPort = sshPort;
    }

    @Override
    public void setSshTunnel(boolean sshTunnel) {

        this.sshTunnel = sshTunnel;
    }

    @Override
    public void setSshPasswordStored(boolean sshPasswordStored) {

        this.sshPasswordStored = sshPasswordStored;
    }

    private DatabaseDriver driverById(long driverId) {

        return ((DatabaseDriverRepository) RepositoryCache.load(DatabaseDriverRepository.REPOSITORY_ID)).findById(driverId);
    }

    private ConnectionsFolder folderById(String folderId) {

        return ((ConnectionFoldersRepository) RepositoryCache.load(ConnectionFoldersRepository.REPOSITORY_ID)).findById(folderId);
    }

    @Override
    public DatabaseConnection withName(String name) {

        setName(name);
        return this;
    }

    @Override
    public DatabaseConnection withSource(String source) {

        setSourceName(source);
        return this;
    }

    public List<String> getListObjectsDB() {
        List<String> list = new ArrayList<>();
        DatabaseObjectNode host = ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getHostNode(this);
        addingChild(list, host);
        return list;

    }

    @Override
    public int getServerVersion() {
        return serverVersion;
    }

    @Override
    public void setServerVersion(int serverVersion) {
        this.serverVersion = serverVersion;
    }

    private void addingChild(List<String> list, DatabaseObjectNode root) {
        root.populateChildren();
        Enumeration<TreeNode> nodes = root.children();
        while (nodes.hasMoreElements()) {
            DatabaseObjectNode node = (DatabaseObjectNode) nodes.nextElement();
            if (!node.isHostNode() && node.getType() != NamedObject.META_TAG)
                list.add(node.getName().replace("$", "\\$"));
            if (node.isHostNode() || node.getType() == NamedObject.META_TAG)
                addingChild(list, node);
        }
    }


    private static final long serialVersionUID = 950081216942320441L;

}



