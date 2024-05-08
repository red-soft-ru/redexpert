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

import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.executequery.Constants;
import org.executequery.components.BasicPopupMenuListener;
import org.executequery.components.TextAreaLogAppender;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class SystemOutputPanel extends AbstractDockedTabPanel
        implements ReadOnlyTextPane {

    public static final String TITLE = Bundles.get(SystemOutputPanel.class, "title");
    public static final String PROPERTY_KEY = "system.display.console";
    public static final String MENU_ITEM_KEY = "viewConsole";

    private JTextArea textArea;

    public SystemOutputPanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {

        textArea = new JTextArea();
        textArea.setFont(new Font("dialog", Font.PLAIN, 11));
        textArea.setEditable(false);

        SystemOutputPanelPopUpMenu systemOutputPanelPopUpMenu = new SystemOutputPanelPopUpMenu(this);
        textArea.addMouseListener(new BasicPopupMenuListener(systemOutputPanelPopUpMenu));

        Appender appender = new TextAreaLogAppender(textArea);
        appender.setLayout(new PatternLayout(Log.PATTERN));
        Log.addAppender(appender);

        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        add(scroller, BorderLayout.CENTER);
        setMinimumSize(new Dimension(0, 150));
        reloadFont();
    }

    public void setTextFont(Font font) {
        textArea.setFont(font);
    }

    public void reloadFont() {
        String fontName = SystemProperties.getProperty("user", "console.font.name");
        String fontSize = SystemProperties.getProperty("user", "console.font.size");

        if (!MiscUtils.isNull(fontName) && !MiscUtils.isNull(fontSize)) {
            setTextFont(new Font(fontName, Font.PLAIN, Integer.parseInt(fontSize)));

        } else {
            Font consoleFont = UIManager.getDefaults().getFont("TextArea.font");
            SystemProperties.setProperty("user", "console.font.name", consoleFont.getFontName());
            reloadFont();
        }
    }

    // --- DockedTabView impl ---

    /**
     * Returns the display title for this view.
     *
     * @return the title displayed for this view
     */
    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Returns the name defining the property name for this docked tab view.
     *
     * @return the key
     */
    @Override
    public String getPropertyKey() {
        return PROPERTY_KEY;
    }

    /**
     * Returns the name defining the menu cache property
     * for this docked tab view.
     *
     * @return the preferences key
     */
    @Override
    public String getMenuItemKey() {
        return MENU_ITEM_KEY;
    }

    @Override
    public void clear() {
        textArea.setText(Constants.EMPTY);
    }

    @Override
    public void copy() {
        textArea.copy();
    }

    @Override
    public void selectAll() {
        textArea.selectAll();

    }

    @Override
    public String getText() {
        return textArea.getText();
    }

    @Override
    public JTextComponent getTextComponent() {
        return textArea;
    }

    // ---

    @Override
    public String toString() {
        return "Output Console";
    }

}
