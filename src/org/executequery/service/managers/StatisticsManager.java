package org.executequery.service.managers;

import biz.redsoft.IFBStatisticManager;
import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains methods that make it easy
 * to use the Statistic Manager
 *
 * @author Alexey Kozlov
 */
public class StatisticsManager extends AbstractServiceManager {
    private static final String CLASS_NAME = "FBStatisticManagerImpl";

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

    public StatisticsManager(DatabaseConnection dc) {
        super(dc, CLASS_NAME);
    }

    /**
     * Returns database header statistics for the specified <code>DatabaseConnection</code>.
     *
     * @return the text of the database header statistics<br>(same result as the <code>gstat -h</code> command)
     */
    public static String getDatabaseHeader(DatabaseConnection dc) {
        return new StatisticsManager(dc).getDatabaseHeader();
    }

    /**
     * Parses the database header statistics text at the specific key.
     *
     * @param key  the key to find value for
     * @param text the database header statistics text
     * @return value next to the specified key
     */
    public static String getHeaderValue(String key, String text) {

        int index = StringUtils.indexOfIgnoreCase(text, key);
        if (index < 0)
            return Constants.EMPTY;

        String value = text.substring(index + key.length()).trim();

        Matcher matcher = Pattern.compile("\n").matcher(value);
        if (matcher.find())
            value = value.substring(0, matcher.start());

        return value;
    }

    // --- helper methods ---

    private String getDatabaseHeader() {

        try (OutputStream outputStream = getDatabaseHeaderStatistics()) {
            return outputStream.toString();

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
            return null;
        }
    }

    private OutputStream getDatabaseHeaderStatistics() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        IFBStatisticManager statisticManager = getServiceManager();
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

    // --- AbstractServiceManager impl ---

    @Override
    public IFBStatisticManager getServiceManager() {
        return (IFBStatisticManager) serviceManager;
    }

}
