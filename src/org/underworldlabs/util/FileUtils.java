/*
 * FileUtils.java
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

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

/**
 * File access utilities.
 *
 * @author Takis Diakoumis
 */
public class FileUtils {

    private FileUtils() {
    }

    public static boolean deleteDirectory(File path) {

        if (path.exists()) {

            File[] files = path.listFiles();
            for (File file : files) {

                if (file.isDirectory()) {

                    deleteDirectory(file);

                } else {

                    file.delete();
                }

            }

        }

        return path.delete();
    }

    public static String randomTempFilePath() {
        return System.getProperty("java.io.tmpdir") +
                System.getProperty("file.separator") + UUID.randomUUID().toString();
    }

    public static boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static void writeFile(String path, String text) throws IOException {
        writeFile(new File(path), text, false);
    }

    public static void writeFile(String path, String text, boolean append) throws IOException {
        writeFile(new File(path), text, append);
    }

    public static void writeFile(File file, String text) throws IOException {
        writeFile(file, text, false);
    }

    public static void writeFile(File file, String text, boolean append) throws IOException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(file, append), true);
            writer.println(text);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static String loadFile(File file) throws IOException {
        return loadFile(file, true);
    }

    public static String loadFile(File file, String encoding) throws IOException {
        return loadFile(file, true, encoding);
    }

    public static String loadFile(String path) throws IOException {
        return loadFile(new File(path), true);
    }

    public static String loadFile(String path, boolean escapeLines) throws IOException {
        return loadFile(new File(path), escapeLines);
    }

    public static String loadFile(File file, boolean escapeLines) throws IOException {

        FileReader fileReader = null;
        BufferedReader reader = null;

        try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            String value = null;
            StringBuilder sb = new StringBuilder();

            while ((value = reader.readLine()) != null) {
                sb.append(value);

                if (escapeLines) {
                    sb.append('\n');
                }

            }

            String charset = new EncodingDetector().detectCharset(file);
            if (StringUtils.isNotBlank(charset)) {

                return new String(sb.toString().getBytes(), charset);
            }

            return sb.toString();

        } finally {
            if (reader != null) {
                reader.close();
            }
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }

    public static String loadFile(File file, boolean escapeLines, String encoding) throws IOException {

        FileReader fileReader = null;
        BufferedReader reader = null;

        try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            String value = null;
            StringBuilder sb = new StringBuilder();

            while ((value = reader.readLine()) != null) {
                sb.append(value);

                if (escapeLines) {
                    sb.append('\n');
                }

            }
            if (StringUtils.isNotBlank(encoding)) {

                return new String(sb.toString().getBytes(), encoding);
            }

            return sb.toString();

        } finally {
            if (reader != null) {
                reader.close();
            }
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }


    public static String loadResource(String path) throws IOException {
        InputStream input = null;
        BufferedReader reader = null;

        try {

            ClassLoader cl = FileUtils.class.getClassLoader();

            if (cl != null) {
                input = cl.getResourceAsStream(path);
            } else {
                input = ClassLoader.getSystemResourceAsStream(path);
            }

            reader = new BufferedReader(new InputStreamReader(input));

            String line = null;
            StringBuilder buf = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append("\n");
            }

            return buf.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (input != null) {
                input.close();
            }
        }
    }

    public static String loadResource(String path, String charSet) throws IOException {
        InputStream input = null;
        BufferedReader reader = null;

        try {

            ClassLoader cl = FileUtils.class.getClassLoader();

            if (cl != null) {
                input = cl.getResourceAsStream(path);
            } else {
                input = ClassLoader.getSystemResourceAsStream(path);
            }

            reader = new BufferedReader(new InputStreamReader(input, charSet));

            String line = null;
            StringBuilder buf = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append("\n");
            }

            return buf.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (input != null) {
                input.close();
            }
        }
    }

    public static Properties loadProperties(String path, Properties defaults) throws IOException {
        return loadProperties(new File(path), defaults);
    }

    public static Properties loadProperties(File file, Properties defaults) throws IOException {
        InputStream input = null;

        try {
            Properties properties = null;

            if (defaults != null) {
                properties = new Properties(defaults);
            } else {
                properties = new Properties();
            }

            input = new FileInputStream(file);
            properties.load(input);
            return properties;
        } finally {
            if (input != null) {
                input.close();
            }
        }

    }

    public static Properties loadProperties(URL[] urls) throws IOException, URISyntaxException {
        for (int i = 0; i < urls.length; i++) {
            File f = new File(urls[i].toURI().getPath());
            if (f.exists())
                return loadProperties(f, null);
        }
        return null;
    }

    public static Properties loadProperties(String path) throws IOException {
        return loadProperties(new File(path), null);
    }

    public static Properties loadProperties(File file) throws IOException {
        return loadProperties(file, null);
    }

    public static void storeProperties(String path,
                                       Properties properties, String header) throws IOException {
        storeProperties(new File(path), properties, header);
    }

    public static void storeProperties(File file,
                                       Properties properties, String header) throws IOException {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            properties.store(output, header);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    public static Properties loadPropertiesResource(String path) throws IOException {
        InputStream input = null;

        try {
            ClassLoader cl = FileUtils.class.getClassLoader();

            if (cl != null) {
                input = cl.getResourceAsStream(path);
            } else {
                input = ClassLoader.getSystemResourceAsStream(path);
            }

            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    public static Object readObject(String path) throws IOException {

        return readObject(new File(path));
    }

    public static Object readObject(File file) throws IOException {

        FileInputStream fileIn = null;
        BufferedInputStream buffIn = null;
        ObjectInputStream obIn = null;

        try {
            fileIn = new FileInputStream(file);
            buffIn = new BufferedInputStream(fileIn);
            obIn = new ObjectInputStream(buffIn);
            return obIn.readObject();
        } catch (ClassNotFoundException cExc) {
            cExc.printStackTrace();
            return null;
        } finally {
            try {
                if (obIn != null) {
                    obIn.close();
                }
                if (buffIn != null) {
                    buffIn.close();
                }
                if (fileIn != null) {
                    fileIn.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static void writeObject(Object object, String path) throws IOException {

        FileOutputStream fileOut = null;
        BufferedOutputStream bufferedOut = null;
        ObjectOutputStream objectOut = null;

        try {

            fileOut = new FileOutputStream(path);
            bufferedOut = new BufferedOutputStream(fileOut);
            objectOut = new ObjectOutputStream(bufferedOut);
            objectOut.writeObject(object);
            objectOut.flush();

        } finally {

            try {
                if (objectOut != null) {
                    objectOut.close();
                }
                if (bufferedOut != null) {
                    bufferedOut.close();
                }
                if (fileOut != null) {
                    fileOut.close();
                }
            } catch (IOException e) {
            }

        }

    }

    /**
     * default buffer read size
     */
    private static final int DEFAULT_BUFFER_SIZE = 32768;

    /**
     * Copies the file at the specified from path to the
     * specified to path.
     *
     * @param from - the from path
     * @param to   - the to path
     * @throws IOException
     */
    public static void copyResource(String from, String to) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            ClassLoader cl = FileUtils.class.getClassLoader();

            if (cl != null) {
                in = cl.getResourceAsStream(from);
            } else {
                in = ClassLoader.getSystemResourceAsStream(from);
            }

            out = new FileOutputStream(to);

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            while (true) {
                synchronized (buffer) {
                    int amountRead = in.read(buffer);
                    if (amountRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, amountRead);
                }
            }

            out.flush();

        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static void copyFile(String from, String to) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            while (true) {
                synchronized (buffer) {
                    int amountRead = in.read(buffer);
                    if (amountRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, amountRead);
                }
            }

            out.flush();

        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static byte[] readBytes(File source)
            throws IOException {
        if (null == source)
            throw new IllegalArgumentException("Source can't be null.");

        try (FileInputStream fileInputStream = new FileInputStream(source)) {
            return readBytes(fileInputStream);
        }
    }

    public static byte[] readBytes(InputStream inputStream)
            throws IOException {
        if (null == inputStream)
            throw new IllegalArgumentException("InputStream can't be null.");

        return readStream(inputStream).toByteArray();
    }

    public static ByteArrayOutputStream readStream(InputStream inputStream)
            throws IOException {
        if (null == inputStream)
            throw new IllegalArgumentException("InputStream can't be null.");

        byte[] buffer = new byte[1024];
        try (ByteArrayOutputStream output_stream = new ByteArrayOutputStream(buffer.length);
             InputStream input = inputStream)
        {
            int return_value = input.read(buffer);

            while (-1 != return_value)
            {
                output_stream.write(buffer, 0, return_value);
                return_value = input.read(buffer);
            }

            return output_stream;
        }
    }

}











