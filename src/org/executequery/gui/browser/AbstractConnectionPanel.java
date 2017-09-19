/*
 * AbstractConnectionPanel.java
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

import java.awt.GridBagConstraints;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.ActionPanel;
import org.underworldlabs.swing.DefaultFieldLabel;

public abstract class AbstractConnectionPanel extends ActionPanel {

    public AbstractConnectionPanel(LayoutManager layout) {

        super(layout);
    }

    protected void addComponents(JPanel panel, ComponentToolTipPair... components) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.bottom = 10;

        int count = 0;
        for (ComponentToolTipPair pair : components) {

            pair.component.setToolTipText(pair.toolTip);

            gbc.gridx++;
            gbc.gridwidth = 1;
            gbc.insets.top = 0;
            gbc.weightx = 0;

            if (count > 0) {

                gbc.insets.left = 15;
            }

            count++;
            if (count == components.length) {

                gbc.weightx = 1.0;
                gbc.insets.right = 5;
            }

            panel.add(pair.component, gbc);
        }

    }

    protected void addLabelFieldPair(JPanel panel, String label,
            JComponent field, String toolTip, GridBagConstraints gbc) {

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 10;

        if (panel.getComponentCount() > 0) {

            gbc.insets.top = 0;
        }

        gbc.insets.left = 10;
        gbc.weightx = 0;
        panel.add(new DefaultFieldLabel(label), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridx = 1;
        gbc.insets.left = 5;
        gbc.weightx = 1.0;
        panel.add(field, gbc);

        if (toolTip != null) {

            field.setToolTipText(toolTip);
        }

    }

    class ComponentToolTipPair {
        
        final JComponent component;
        final String toolTip;

        public ComponentToolTipPair(JComponent component, String toolTip) {
            this.component = component;
            this.toolTip = toolTip;
        }
        
    }

    protected String bundleString(String key) {
        
        return Bundles.get(getClass(), key);
    }

}


