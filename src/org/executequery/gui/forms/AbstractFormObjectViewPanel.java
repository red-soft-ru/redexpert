/*
 * AbstractFormObjectViewPanel.java
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

package org.executequery.gui.forms;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.GradientLabel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.print.Printable;

/**
 * @author Takis Diakoumis
 */
public abstract class AbstractFormObjectViewPanel extends JPanel
        implements FormObjectView {
    private boolean reload;

    private String nameObject;

    private DatabaseObjectNode node;

    private DatabaseConnection databaseConnection;

    protected static Border emptyBorder;

    protected GradientLabel gradientLabel;

    private static GridBagConstraints panelConstraints;

    static {
        emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        panelConstraints = new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0);
    }

    public AbstractFormObjectViewPanel() {

        super(new BorderLayout());

        gradientLabel = new GradientLabel();
//        if (!UIUtils.isNativeMacLookAndFeel()) {
//        	gradientLabel.setForeground(new ColorUIResource(0x333333));
//        }
        //add(gradientLabel, BorderLayout.NORTH);
    }

    protected void setContentPanel(JComponent panel) {

        add(panel, BorderLayout.CENTER);
    }

    public String getHeaderText() {
        return gradientLabel.getText();
    }

    public void setHeader(String text, ImageIcon icon) {
        gradientLabel.setText(text);
    }

    public void setHeaderText(String text) {
        gradientLabel.setText(text);
    }

    public void setHeaderIcon(ImageIcon icon) {
    }

    /**
     * Performs some cleanup and releases resources before being closed.
     */
    public abstract void cleanup();

    /**
     * Flags to refresh the data and clears the cache - if any.
     */
    public void refresh() {
        setReload(true);
    }

    /**
     * Returns the print object - if any.
     */
    public abstract Printable getPrintable();

    /**
     * Returns the name of this panel.
     */
    public abstract String getLayoutName();

    /**
     * Returns the standard layout constraints for the panel.
     */
    public static GridBagConstraints getPanelConstraints() {
        return panelConstraints;
    }

    public boolean isReload() {
        return reload;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
    }

    protected String bundleString(String key) {

        return Bundles.get(getClass(), key);
    }

    public void setObjectName(String nameObject)
    {
        this.nameObject = nameObject;
    }

    @Override
    public DatabaseObjectNode getDatabaseObjectNode() {
        return node;
    }

    @Override
    public void setDatabaseObjectNode(DatabaseObjectNode node) {
        this.node = node;
    }

    @Override
    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    @Override
    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public String getObjectName() {
        return nameObject;
    }
}

