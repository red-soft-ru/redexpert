package org.executequery.gui.jdbclogger;

import org.executequery.gui.AbstractDockedTabPanel;
import org.underworldlabs.util.MiscUtils;
import org.yaml.snakeyaml.constructor.Construct;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by vasiliy on 10.12.16.
 */

public class JdbcLoggerPanel extends AbstractDockedTabPanel {

    public static final String TITLE = "Jdbc Logger";

    private JPanel loggerPanel;

    public JdbcLoggerPanel() {
        super(new BorderLayout());

        Object odb = null;
        {
            ClassLoader classLoader = this.getClass().getClassLoader();
            URL[] urls = new URL[0];
            Class clazzdb = null;

            try {

                urls = MiscUtils.loadURLs("./lib/jdbc-perf-logger-driver-0.8.1-SNAPSHOT.jar");
                ClassLoader cl = new URLClassLoader(urls, classLoader);
                clazzdb = cl.loadClass("ch.sla.jdbcperflogger.model.ConnectionInfo");
                Constructor constructor = clazzdb.getConstructor(UUID.class, int.class, String.class, Date.class, long.class, Properties.class);
                odb = constructor.newInstance(new Object[] {null, 0, "foo", null, 0, null});

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ClassLoader classLoader = odb.getClass().getClassLoader()/*this.getClass().getClassLoader()*/;
        URL[] urls = new URL[0];
        Class clazzdb = null;
        Object odb2 = null;
        try {

            urls = MiscUtils.loadURLs("./lib/jdbc-logger.jar");
            ClassLoader cl = new URLClassLoader(urls, classLoader);
            clazzdb = cl.loadClass("biz.redsoft.gui.LoggerPanel");
            odb2 = clazzdb.newInstance();

        } catch (Exception e) {
            e.printStackTrace();
        }

        loggerPanel = (JPanel) odb2;
        this.add(loggerPanel);
    }



    public static final String MENU_ITEM_KEY = "jdbcLogger";

    public static final String PROPERTY_KEY = "system.display.jdbclogger";

    @Override
    public String getPropertyKey() {
        return PROPERTY_KEY;
    }

    @Override
    public String getMenuItemKey() {
        return MENU_ITEM_KEY;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
