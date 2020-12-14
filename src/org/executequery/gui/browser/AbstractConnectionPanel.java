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

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.ActionPanel;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractConnectionPanel extends ActionPanel {

    public AbstractConnectionPanel(LayoutManager layout) {

        super(layout);
    }

    protected void addComponents(JPanel panel, ComponentToolTipPair... components) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.bottom = 0;

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


