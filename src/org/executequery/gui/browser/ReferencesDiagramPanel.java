/*
 * ReferencesDiagramPanel.java
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

package org.executequery.gui.browser;

import org.executequery.gui.erd.ErdTableInfo;
import org.executequery.gui.erd.ErdViewerPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class ReferencesDiagramPanel extends JPanel {

    private final ErdViewerPanel viewerPanel;

    public ReferencesDiagramPanel() {
        super(new BorderLayout());
        viewerPanel = new ErdViewerPanel(false, false);
        viewerPanel.setDefaultScaledView(0.85d);
        viewerPanel.setDisplayGrid(true);
        setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        add(viewerPanel, BorderLayout.CENTER);
    }

    public void cleanup() {
        viewerPanel.cleanup();
    }

    public Printable getPrintable() {
        return viewerPanel.getPrintable();
    }

    public void setTables(Vector<ErdTableInfo> tableInfos) {
        viewerPanel.resetTableValues(tableInfos);
    }

}
