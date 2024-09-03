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
import org.executequery.localization.Bundles;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The system console base panel.
 *
 * @author Takis Diakoumis
 */
public class ConsolePanel extends DefaultTabView
        implements ActiveComponent,
        NamedView {

    public static final String FRAME_ICON = "icon_console_system";
    private static final List<String> instances = new ArrayList<>();

    private String title;
    private Console console;

    public ConsolePanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {
        instances.add(buildUniqueTitle());
        console = new Console(true, title);

        setPreferredSize(new Dimension(600, 400));
        add(console, BorderLayout.CENTER);
    }

    private String buildUniqueTitle() {
        final String bundledTitle = Bundles.get(ConsolePanel.class, "title");

        int maxIndex = instances.stream()
                .map(s -> s.replace(bundledTitle, "").trim())
                .map(Integer::parseInt)
                .max(Integer::compare).orElse(0);

        title = bundledTitle + " " + (maxIndex + 1);
        return title;
    }

    public String getTitle() {
        return title;
    }

    // --- ActiveComponent impl ---

    @Override
    public void cleanup() {
        console.cleanup();
    }

    // --- NamedView impl ----

    @Override
    public String getDisplayName() {
        return title;
    }

    // --- DockedTabView impl ---

    @Override
    public boolean tabViewClosing() {
        instances.remove(title);
        cleanup();

        return true;
    }

}
