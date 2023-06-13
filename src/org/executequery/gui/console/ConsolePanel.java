/*
 * ConsolePanel.java
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

package org.executequery.gui.console;

import org.executequery.ActiveComponent;
import org.executequery.base.DefaultTabView;
import org.executequery.gui.NamedView;

import java.awt.*;

/**
 * The system console base panel.
 *
 * @author Takis Diakoumis
 */
public class ConsolePanel extends DefaultTabView
        implements ActiveComponent,
        NamedView {

    public static final String TITLE = "System Console ";
    public static final String FRAME_ICON = "SystemConsole16.svg";

    private Console console;

    /**
     * Constructs a new instance.
     */
    public ConsolePanel() {
        super(new BorderLayout());

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Initializes the state of this instance.
     */
    private void jbInit() throws Exception {
        console = new Console(true);
        setPreferredSize(new Dimension(600, 400));
        add(console, BorderLayout.CENTER);
    }

    public void cleanup() {
        console.cleanup();
    }

    public String toString() {
        return TITLE;
    }

    /**
     * the instance counter
     */
    private static int count = 1;

    /**
     * Returns the display name for this view.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return TITLE + (count++);
    }

    // --------------------------------------------
    // DockedTabView implementation
    // --------------------------------------------

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {
        cleanup();
        return true;
    }

}















