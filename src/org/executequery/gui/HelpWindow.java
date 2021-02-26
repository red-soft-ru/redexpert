/*
 * HelpWindow.java
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

package org.executequery.gui;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.underworldlabs.swing.util.IconUtilities;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.help.*;
import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * The system Help window.
 *
 * @author Takis Diakoumis
 */
public class HelpWindow {

    private static final String HELP_SET = "eq.hs";

    /**
     * the target page to display
     */
    private String target;

    /**
     * Indicates whether a help search has been requested
     */
    private boolean isSearch;

    /**
     * IDs to be expanded
     */
    private static final String[] EXPAND_IDS = {"release-info",
            "database-explorer",
            "query-editor",
            "erd",
            "import-export"};

    /**
     * Opens a new help window
     */
    public HelpWindow() {

        this(null);
    }

    /**
     * Opens a new help window with the specified target page selected.
     *
     * @param page - the target page ID to display
     */
    public HelpWindow(String page) {

        target = page;
        isSearch = false;

        if ("search_help_on".equals(page)) {

            isSearch = true;
        }

        execute();
    }

    /**
     * Creates the help context to be shown and displays the frame.
     */
    private void execute() {

        try {

            ClassLoader classLoader = classLoader();

            URL[] urls;
            try {
                urls = MiscUtils.loadURLs("./docs/eqhelp.jar");
                classLoader = new URLClassLoader(urls, getClass().getClassLoader());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            URL url = HelpSet.findHelpSet(classLoader, HELP_SET);

            HelpSet helpSet = new HelpSet(classLoader, url);
            JHelp help = new JHelp(helpSet);

            // ----------------------
            // TODO: check this - help still showing search after a 
            // search selected then a normal selection !!??
            /*
            // check that the current navigator is the default TOC
            boolean resetNavigator = false;
            JHelpNavigator currentNavigator = help.getCurrentNavigator();
            if (!(currentNavigator instanceof JHelpTOCNavigator)) {
                resetNavigator = true;
            }
            */
            // ----------------------

            JFrame frame = createFrame();

            try {

                if (!isSearch && target != null) {

                    help.setCurrentID(target);
                }

            } catch (BadIDException badIdExc) {
            }

            expandHelpSetNodes(help);

            frame.setContentPane(help);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
            frame.toFront();

            // reset initial values - noticed some strange
            // behaviour when reopening help window.
            // TODO: check this - not making a difference -  why did i do it?
            isSearch = false;

        } catch (HelpSetException e) {

            GUIUtilities.displayExceptionErrorDialog(
                    "The system could not\nfind the help files specified.\n\n" +
                            "System Error: " + e.getMessage(), e);
        }

    }

    private void expandHelpSetNodes(JHelp help) {

        for (Enumeration<?> i = help.getHelpNavigators(); i.hasMoreElements(); ) {

            Object object = i.nextElement();

            if (object instanceof JHelpTOCNavigator) {

                JHelpTOCNavigator toc = (JHelpTOCNavigator) object;

                // make sure the toc is the current navigator
                // if we haven't launched in search mode
                if (!isSearch) {

                    help.setCurrentNavigator(toc);
                }

                for (int j = 0; j < EXPAND_IDS.length; j++) {

                    toc.expandID(EXPAND_IDS[j]);
                }

                if (!isSearch) {

                    break;
                }

            }

            if (isSearch && object instanceof JHelpSearchNavigator) {

                help.setCurrentNavigator((JHelpSearchNavigator) object);
            }
        }
    }

    private ClassLoader classLoader() {

        return getClass().getClassLoader();
    }

    private JFrame createFrame() {

        JFrame frame = new JFrame("RedXpert Help");

        ImageIcon frameIcon = IconUtilities.loadIcon(
                "/org/executequery/icons/Help16.png");

        frame.setIconImage(frameIcon.getImage());

        frame.setSize(900, 700);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();

        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }

        Frame parentFrame = GUIUtilities.getParentFrame();
        if (parentFrame != null) {

            Point parentLocation = parentFrame.getLocation();
            frame.setLocation(new Point(parentLocation.x + 40, parentLocation.y + 40));

        } else {

            frame.setLocation((screenSize.width - frameSize.width) / 2,
                    (screenSize.height - frameSize.height) / 2);
        }

        return frame;
    }

    /**
     * Allows for displaying the help viewer outside of eq itself.
     */
    public static void main(String[] args) {

        // make sure system properties are loaded
        SystemProperties.loadPropertiesResource(Constants.SYSTEM_PROPERTIES_KEY,
                "org/executequery/eq.system.properties");

        // new default window
        new HelpWindow();
    }

}






