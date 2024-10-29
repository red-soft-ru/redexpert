package org.executequery.actions.databasecommands;

import biz.redsoft.IFBStatisticManager;
import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.databasemediators.ConnectionType;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.log.Log;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains methods that make it easy
 * to use the Statistic Manager
 *
 * @author Alexey Kozlov
 */
@SuppressWarnings("unused")
public class DatabaseStatisticCommand {

    public static final String FLAGS = "Flags";
    public static final String GENERATION = "Generation";
    public static final String SCN = "System Change Number";
    public static final String PAGE_SIZE = "Page size";
    public static final String SERVER = "Server";
    public static final String ODS = "ODS version";
    public static final String OIT = "Oldest transaction";
    public static final String OAT = "Oldest active";
    public static final String OST = "Oldest snapshot";
    public static final String NEXT_TRANSACTION = "Next transaction";
    public static final String AUTOSWEEP_GAP = "Autosweep gap";
    public static final String SEQUENCE_NUM = "Sequence number";
    public static final String NEXT_ATTACHMENT = "Next attachment ID";
    public static final String IMPLEMENTATION = "Implementation";
    public static final String SHADOW_COUNT = "Shadow count";
    public static final String PAGE_BUFF = "Page buffers";
    public static final String NEXT_HEADER = "Next header page";
    public static final String DIALECT = "Database dialect";
    public static final String CREATION_DATE = "Creation date";
    public static final String ATTRIBUTES = "Attributes";
    public static final String GUID = "Database GUID:";

    /// Private constructor to prevent installation
    private DatabaseStatisticCommand() {
    }

    /**
     * Returns database header statistic
     *
     * @param dc connection to use
     * @return the same result as <code>gstat -h</code> command
     */
    public static String getDatabaseHeader(DatabaseConnection dc) {

        if (ConnectionType.isEmbedded(dc))
            return null;

        try (OutputStream outputStream = getDatabaseHeaderStatistics(dc)) {
            return outputStream.toString();

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
            return null;
        }
    }

    public static String getHeaderValue(String key, String databaseHeader) {

        int index = StringUtils.indexOfIgnoreCase(databaseHeader, key);
        if (index < 0)
            return Constants.EMPTY;

        String value = databaseHeader.substring(index + key.length()).trim();

        Matcher matcher = Pattern.compile("\n").matcher(value);
        if (matcher.find())
            value = value.substring(0, matcher.start());

        return value;
    }

    private static OutputStream getDatabaseHeaderStatistics(DatabaseConnection dc) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IFBStatisticManager statisticManager = getStatisticManager(dc);

        if (statisticManager != null) {
            try {
                statisticManager.setLogger(outputStream);
                statisticManager.getHeaderPage();

            } catch (SQLException e) {
                Log.debug("Unable to get database header statistics");
                Log.debug(e.getMessage(), e);
            }
        }

        return outputStream;
    }

    private static IFBStatisticManager getStatisticManager(DatabaseConnection dc) {

        try {

            Driver driver = null;
            for (Map.Entry<String, Driver> entry : DefaultDriverLoader.getLoadedDrivers().entrySet()) {
                String driverName = entry.getKey();
                if (driverName.startsWith(String.valueOf(dc.getDriverId()))) {
                    driver = entry.getValue();
                    break;
                }
            }
            if (driver == null)
                driver = DefaultDriverLoader.getDefaultDriver();

            IFBStatisticManager statisticManager = (IFBStatisticManager) DynamicLibraryLoader.loadingObjectFromClassLoader(
                    driver.getMajorVersion(), driver, "FBStatisticManagerImpl"
            );

            statisticManager.setHost(dc.getHost());
            statisticManager.setPort(dc.getPortInt());
            statisticManager.setUser(dc.getUserName());
            if (!MiscUtils.isNull(dc.getCharset()) && !Objects.equals(dc.getCharset(), "NONE"))
                statisticManager.setCharSet(MiscUtils.getJavaCharsetFromSqlCharset(dc.getCharset()));
            statisticManager.setDatabase(dc.getSourceName());
            statisticManager.setPassword(dc.getUnencryptedPassword());

            return statisticManager;

        } catch (ClassNotFoundException | SQLException e) {
            Log.debug("Unable to init IFBStatisticManager instance");
            Log.debug(e.getMessage(), e);
        }

        return null;
    }

}
