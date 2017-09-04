/*
 * SystemOutputPanel.java
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

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.executequery.GUIUtilities;
import org.executequery.components.BasicPopupMenuListener;
import org.executequery.components.TextAreaLogAppender;
import org.executequery.log.Log;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class SystemOutputPanel extends AbstractDockedTabPanel implements ReadOnlyTextPane {

    /** This panel's title */
    public static final String TITLE = "Output Console";

    /** the output text area */
    private JTextArea textArea;

    public SystemOutputPanel() {

        super(new BorderLayout());

        try {

            init();

        } catch(Exception e) {

            e.printStackTrace();
        }

    }

    private void init() throws Exception {

        textArea = new JTextArea();
        textArea.setFont(new Font("dialog", 0, 11));
        textArea.setEditable(false);

        SystemOutputPanelPopUpMenu systemOutputPanelPopUpMenu = new SystemOutputPanelPopUpMenu(this);
        textArea.addMouseListener(new BasicPopupMenuListener(systemOutputPanelPopUpMenu));

        Appender appender = new TextAreaLogAppender(textArea);
        appender.setLayout(new PatternLayout(Log.PATTERN));
        Log.addAppender(appender);

        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        add(scroller, BorderLayout.CENTER);
    }

    public Icon getIcon() {

        return GUIUtilities.loadIcon("SystemOutput.png");
    }

    public String toString() {

        return "Output Console";
    }

    // ----------------------------------------
    // DockedTabView Implementation
    // ----------------------------------------

    public static final String MENU_ITEM_KEY = "viewConsole";

    public static final String PROPERTY_KEY = "system.display.console";

    /**
     * Returns the display title for this view.
     *
     * @return the title displayed for this view
     */
    public String getTitle() {

        return TITLE;
    }

    /**
     * Returns the name defining the property name for this docked tab view.
     *
     * @return the key
     */
    public String getPropertyKey() {

        return PROPERTY_KEY;
    }

    /**
     * Returns the name defining the menu cache property
     * for this docked tab view.
     *
     * @return the preferences key
     */
    public String getMenuItemKey() {

        return MENU_ITEM_KEY;
    }

    public void clear() {

        textArea.setText("");
    }

    public void copy() {

        textArea.copy();
    }

    public void selectAll() {

        textArea.selectAll();

    }

    public String getText() {

        return textArea.getText();
    }

    public JTextComponent getTextComponent() {
        
        return textArea;
    }
    
}




