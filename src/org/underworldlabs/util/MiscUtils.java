/*
 * MiscUtils.java
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

package org.underworldlabs.util;

import org.executequery.ApplicationContext;
import org.executequery.util.StringBundle;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author Takis Diakoumis
 * @author Dragan Vasic
 */
public final class MiscUtils {

    public static final String ACTION_DELIMETER = "+";

    private static DecimalFormat oneDigitFormat;
    private static DecimalFormat twoDigitFormat;

    private MiscUtils() {
    }

    public static double bytesToKiloBytes(long bytes) {

        return ((double) bytes / 1024);
    }

    public static double bytesToMegaBytes(long bytes) {

        return ((double) bytes / 1048576);
    }

    public static double bytesToGigaBytes(long bytes) {

        return ((double) bytes / 1073741824);
    }

    /**
     * Checks if the specified value is <code>null</code>.
     * This will also return <code>true</code> if the length
     * of the specified value is zero.
     *
     * @param value the value to check for <code>null</code>
     * @return <code>true</code> | <code>false</code>
     */
    public static boolean isNull(String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Tests if the specified value contains the specified
     * word as a WHOLE word.
     *
     * @param value the value to test for the word
     * @param word  the whole word we are looking for
     * @return <code>true</code> if found, <code>false</code> otherwise
     */
    public static boolean containsWholeWord(String value, String word) {

        int index = value.indexOf(word);

        if (index == -1) {
            return false;
        }

        int valueLength = value.length();
        int wordLength = word.length();
        int indexLength = index + wordLength;

        if (indexLength == valueLength) // same word
            return true;

        if (indexLength != valueLength) { // check for embedded word

            if (index > 0) {
                return Character.isWhitespace(value.charAt(indexLength)) &&
                        Character.isWhitespace(value.charAt(index - 1));
            } else {
                return Character.isWhitespace(value.charAt(indexLength));
            }

        } else {
            return true;
        }

    }

    public static String getExceptionName(Throwable e) {
        String exceptionName = "";
        if (e.getCause() != null) {
            Throwable _e = e.getCause();
            exceptionName = _e.getClass().getName();
        } else {
            exceptionName = e.getClass().getName();
        }

        int index = exceptionName.lastIndexOf('.');
        if (index != -1) {
            exceptionName = exceptionName.substring(index + 1);
        }
        return exceptionName;
    }

    public static String firstLetterToUpper(String value) {

        boolean nextUpper = false;
        char[] chars = value.toCharArray();
        StringBuilder sb = new StringBuilder(chars.length);

        for (int i = 0; i < chars.length; i++) {

            if (i == 0 || nextUpper) {
                sb.append(Character.toUpperCase(chars[i]));
                nextUpper = false;
                continue;
            }

            if (Character.isWhitespace(chars[i])) {
                nextUpper = true;
                sb.append(chars[i]);
                continue;
            }

            sb.append(Character.toLowerCase(chars[i]));
            nextUpper = false;

        }
        return sb.toString();
    }

    /**
     * Formats the specified the SQL exception object
     * displaying the error message, error code and
     * the SQL state code.
     *
     * @param e - the SQL exception
     */
    public static String formatSQLError(SQLException e) {
        if (e == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(e.getMessage());
        sb.append("\nError Code: " + e.getErrorCode());

        String state = e.getSQLState();
        if (state != null) {
            sb.append("\nSQL State Code: " + state);
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Returns a <code>String</code> array of the the CSV value
     * specified with the specfied delimiter.
     *
     * @param csvString the CSV value
     * @param delim     the delimiter used in the CSV value
     * @return an array of split values
     */
    public static String[] splitSeparatedValues(String csvString, String delim) {
        StringTokenizer st = new StringTokenizer(csvString, delim);
        List<String> list = new ArrayList<String>(st.countTokens());

        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }

        String[] values = list.toArray(new String[list.size()]);
        return values;
    }

    public static boolean containsValue(String[] values, String value) {

        for (int i = 0; i < values.length; i++) {

            if (values[i].compareTo(value) == 0) {
                return true;
            }

        }

        return false;

    }

    public static boolean isValidNumber(String number) {
        if (isNull(number)) {
            return false;
        }
        char[] chars = number.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isDigit(chars[i])) {
                return false;
            }
        }
        return true;
    }

    public static String getClassName(String path) {

        int index = path.indexOf(".class");
        if (index == -1) {

            return null;
        }

        char dot = '.';
        char pathSeparator = '/';
        char[] chars = path.toCharArray();
        StringBuilder sb = new StringBuilder(chars.length);

        for (int i = 0; i < chars.length; i++) {

            if (i == index) {
                break;
            }

            if (chars[i] == pathSeparator) {
                sb.append(dot);
            } else {
                sb.append(chars[i]);
            }

        }

        return sb.toString();
    }

    public static String[] findImplementingClasses(
            String interfaceName, String paths) throws IOException {
        return findImplementingClasses(interfaceName, paths, true);
    }

    public static String[] findImplementingClasses(
            String interfaceName, String paths, boolean interfaceOnly)
            throws IOException {

        URL[] urls = loadURLs(paths);
        URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());

        JarFile jarFile = null;
        String className = null;
        String[] files = splitSeparatedValues(paths, ";");
        List<String> clazzes = new ArrayList<String>();

        for (int i = 0; i < files.length; i++) {

            File file = new File(files[i]);

            if (!file.isFile()) {

                continue;
            }

            jarFile = new JarFile(files[i]);

            for (Enumeration<?> j = jarFile.entries(); j.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) j.nextElement();
                className = getClassName(entry.getName());

                if (className == null) {
                    continue;
                }

                try {
                    Class<?> clazz = loader.loadClass(className);

                    if (clazz.isInterface()) {
                        continue;
                    }

                    if (implementsClass(clazz, interfaceName)) {
                        clazzes.add(clazz.getName());
                    }

                    /*
                    String name = getImplementedClass(clazz, interfaceName);
                    if (name != null) {
                        clazzes.add(className);
                    }
                    */

                    if (!interfaceOnly) {
                        String name = null;
                        Class<?> _clazz = clazz;
                        Class<?> superClazz = null;
                        while ((superClazz = _clazz.getSuperclass()) != null) {
                            name = superClazz.getName();
                            if (interfaceName.compareTo(name) == 0) {
                                clazzes.add(clazz.getName());
                                break;
                            }
                            _clazz = superClazz;
                        }

                    }

                }
                // ignore - noticed with oracle 10g driver only
                //catch (NoClassDefFoundError e) {}
                // ignore noticed with db2 driver - no serialVersionUID
                //catch (ClassFormatError e) {}
                // ignore and continue
                catch (Throwable e) {
                }

            }

        }

        loader.close();
        return clazzes.toArray(new String[clazzes.size()]);
    }

    public static boolean implementsClass(Class<?> clazz, String implementation) {
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {

            for (int k = 0; k < interfaces.length; k++) {
                String name = interfaces[k].getName();
                if (name.compareTo(implementation) == 0) {
                    return true;
                } else if (implementsClass(interfaces[k], implementation)) {
                    return true;
                }
            }

        }

        Class<?> superClazz = clazz.getSuperclass();
        return superClazz != null && !superClazz.isInterface()
                && implementsClass(superClazz, implementation);

    }


    public static String getImplementedClass(Class<?> clazz, String implementation) {
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces == null || interfaces.length == 0) {
            return null;
        }

        for (int k = 0; k < interfaces.length; k++) {
            String name = interfaces[k].getName();
            if (name.compareTo(implementation) == 0) {
                return clazz.getName();
            }
            name = getImplementedClass(interfaces[k], implementation);
            if (name != null) {
                return clazz.getName();
            }
        }

        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            return getImplementedClass(superClazz, implementation);
        }
        return null;
    }

    public static URL[] loadURLs(String paths) throws MalformedURLException {
        String token = ";";
        Vector<String> pathsVector = new Vector<String>();
        String exe_path = ApplicationContext.getInstance().getExternalProcessName();
        if (exe_path != null && !exe_path.isEmpty()) {
            exe_path = exe_path.substring(0, exe_path.lastIndexOf("bin"));

        }

        if (paths.indexOf(token) != -1) {
            StringTokenizer st = new StringTokenizer(paths, token);
            while (st.hasMoreTokens()) {
                pathsVector.add(st.nextToken());
            }
        } else {
            pathsVector.add(paths);
        }

        URL[] urls;
        if (exe_path != null && !exe_path.isEmpty()) {
            urls = new URL[pathsVector.size() * 2];
        } else {
            urls = new URL[pathsVector.size()];
        }
        for (int i = 0; i < pathsVector.size(); i++) {
            File f = new File(pathsVector.elementAt(i));
            urls[i] = f.toURI().toURL();
        }
        if (exe_path != null && !exe_path.isEmpty())
            for (int i = pathsVector.size(); i < urls.length; i++) {
                try {
                    File f = new File(exe_path + pathsVector.elementAt(i - pathsVector.size()));
                    urls[i] = f.toURI().toURL();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        return urls;
    }

    public static String formatNumber(long number, String pattern) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern(pattern);
        return df.format(number);
    }

    public static String keyStrokeToString(KeyStroke keyStroke) {
        String value = null;
        if (keyStroke != null) {
            int mod = keyStroke.getModifiers();
            value = KeyEvent.getKeyModifiersText(mod);

            if (!MiscUtils.isNull(value)) {
                value += ACTION_DELIMETER;
            }

            String keyText = KeyEvent.getKeyText(keyStroke.getKeyCode());

            if (!MiscUtils.isNull(keyText)) {
                value += keyText;
            }

        }
        return value;
    }

    /**
     * Returns the system properties from <code>System.getProperties()</code>
     * as a 2 dimensional array of key/name.
     */
    public static String[][] getSystemProperties() {
        Properties sysProps = System.getProperties();
        String[] keys = new String[sysProps.size()];

        int count = 0;
        for (Enumeration<?> i = sysProps.propertyNames(); i.hasMoreElements(); ) {
            keys[count++] = (String) i.nextElement();
        }

        Arrays.sort(keys);
        String[][] properties = new String[keys.length][2];
        for (int i = 0; i < keys.length; i++) {
            properties[i][0] = keys[i];
            properties[i][1] = sysProps.getProperty(keys[i]);
        }
        return properties;
    }

    /**
     * Prints the system properties as [key: name].
     */
    public static void printSystemProperties() {
        String[][] properties = getSystemProperties();
        for (int i = 0; i < properties.length; i++) {
            System.out.println(properties[i][0] + ":\t" + properties[i][1]);
        }
    }

    public static void printActionMap(JComponent component) {
        printActionMap(component.getActionMap(), component.getClass().getName());
    }

    public static void printInputMap(JComponent component) {
        printInputMap(component.getInputMap(JComponent.WHEN_FOCUSED),
                "Input map used when focused");
        printInputMap(component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
                "Input map used when ancestor of focused component");
        printInputMap(component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                "Input map used when in focused window");
    }

    public static void printActionMap(ActionMap actionMap, String who) {
        System.out.println("Action map for " + who + ":");
        Object[] keys = actionMap.allKeys();
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                Object key = keys[i];
                Action targetAction = actionMap.get(key);
                System.out.println("\tName: <" + key + ">, action: "
                        + targetAction.getClass().getName());
            }
        }
    }

    public static void printInputMap(InputMap inputMap, String heading) {
        System.out.println("\n" + heading + ":");
        KeyStroke[] keys = inputMap.allKeys();
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                KeyStroke key = keys[i];
                Object actionName = inputMap.get(key);
                System.out.println("\tKey: <" + key + ">, action name: "
                        + actionName);
            }
        }
    }

    public static String formatDuration(long value) {

        if (twoDigitFormat == null || oneDigitFormat == null) {
            oneDigitFormat = new DecimalFormat("0");
            twoDigitFormat = new DecimalFormat("00");
            //threeDigitFormat = new DecimalFormat("000");
        }

        // {"milliseconds","seconds","minutes","hours"}
        long[] divisors = {1000, 60, 60, 24};
        double[] result = new double[divisors.length];

        for (int i = 0; i < divisors.length; i++) {
            result[i] = value % divisors[i];
            value /= divisors[i];
        }
       /*
       String[] labels = {"milliseconds","seconds","minutes","hours"};
       for(int i = divisors.length-1;i >= 0;i--) {
          System.out.print(" " + result[i] + " " + labels[i]);
       }
       System.out.println();
       */

        //build "hh:mm:ss.SSS"
        StringBuilder buffer = new StringBuilder(" ");
        buffer.append(oneDigitFormat.format(result[3]));
        buffer.append(':');
        buffer.append(twoDigitFormat.format(result[2]));
        buffer.append(':');
        buffer.append(twoDigitFormat.format(result[1]));
        buffer.append('.');
        buffer.append(twoDigitFormat.format(result[0]));

        return buffer.toString();
    }

    public static boolean getBooleanValue(String value) {
        return Boolean.valueOf(value).booleanValue();
    }

    public static String charsToString(char[] chars) {
        StringBuilder sb = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            sb.append(chars[i]);
        }
        return sb.toString();
    }

    /**
     * Returns whether the current version of the JVM is at least
     * that specified for major and minor version numbers. For example,
     * with a minium required of 1.4, the major version is 1 and minor is 4.
     *
     * @param major - the major version
     * @param minor - the minor version
     * @return whether the system version is at least major.minor
     */
    public static boolean isMinJavaVersion(int major, int minor) {
        //String version = System.getProperty("java.vm.version");
        String version = System.getProperty("java.version");
        String installedVersion = version;

        int index = version.indexOf("_");
        if (index > 0) {
            installedVersion = version.substring(0, index);
        }

        String[] installed = splitSeparatedValues(installedVersion, ".");

        // expect to get something like x.x.x - need at least x.x
        if (installed.length < 2) {
            return false;
        }

        // major at position 0
        int _version = Integer.parseInt(installed[0]);
        if (_version < major) {
            return false;
        }

        _version = Integer.parseInt(installed[1]);
        return _version >= minor;

    }

    /**
     * Returns the running Java VM version in full format using
     * <code>System.getProperty("java.version")</code>.
     *
     * @return the Java VM version
     */
    public static final String getVMVersionFull() {
        return System.getProperty("java.version");
    }

    /**
     * Returns the running Java VM version in short format (major versio only)
     * using <code>System.getProperty("java.version")</code>.
     *
     * @return the Java VM version
     */
    public static final double getVMVersion() {
        String property = System.getProperty("java.version");
        return Double.parseDouble(property.length() >= 3 ? property.substring(0,3) : property);
    }

    public static byte[] inputStreamToBytes(InputStream is) {
        byte[] retVal = new byte[0];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (is != null) {
            byte[] elementi = new byte[10000];
            int size = 0;
            try {
                while ((size = is.read(elementi)) != -1) {
                    //retVal = addBytes(retVal,elementi,(retVal.length),size);
                    System.out.print(".");
                    baos.write(elementi, 0, size);
                }
                retVal = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                retVal = new byte[0];
            }
        }
        return retVal;
    }

    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static String formattedSQLValue(String value, int type) {
        boolean str = false;
        switch (type) {

            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.CLOB:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
            case Types.BINARY:
                value = "'" + value;
                str = true;
                break;
            default:
                break;
        }
        if (str) {
            value += "'";
        }
        return value;
    }

    public static void printThreadStack(StackTraceElement[] stackTrace) {

        for (StackTraceElement stackTraceElement : stackTrace) {

            System.err.println(stackTraceElement);
        }

    }

    public static String getJavaCharsetFromSqlCharset(String sqlcharset) {
        ResourceBundle bundle = ResourceBundle.getBundle("org/executequery/sqlToJavaCharset");
        org.executequery.util.StringBundle sb = new StringBundle(bundle);
        return sb.getString(sqlcharset);
    }

    public static String replaceUnsupportedSimbolsToDot(String b) {
        b = b.replace("\0", ".");
        b = b.replace("\b", ".");
        b = b.replace("\t", ".");
        b = b.replace("\n", ".");
        b = b.replace("\f", ".");
        b = b.replace("\r", ".");
        return b;
    }

    public static String wordInQuotes(String world) {
        return "\"" + world + "\"";
    }

}

