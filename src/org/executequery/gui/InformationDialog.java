/*
 * InformationDialog.java
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

import org.executequery.localization.Bundles;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author Takis Diakoumis
 */
public class InformationDialog extends ActionDialog {

    public static final int RESOURCE_PATH_VALUE = 0;
    public static final int TEXT_CONTENT_VALUE = 1;

    /**
     * Creates a new instance of InformationDialog
     */
    public InformationDialog(String name, String value, int valueType, String charSet) {

        super(name, true);

        try {

            String text = null;

            if (valueType == RESOURCE_PATH_VALUE) {
                if (charSet == null)
                text = FileUtils.loadResource(value);
                else
                    text = FileUtils.loadResource(value, charSet);

            } else {

                text = value;
            }

            JTextArea textArea = new JTextArea(text);
            textArea.setFont(new Font("monospaced", Font.PLAIN, 11));
            textArea.setEditable(false);

            JPanel panel = new JPanel(new GridBagLayout());
            JButton closeButton = new DefaultPanelButton(this, Bundles.get("common.close.button"), "dispose");

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets.top = 5;
            gbc.insets.bottom = 5;
            gbc.insets.left = 5;
            gbc.insets.right = 5;
            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            panel.add(new JScrollPane(textArea), gbc);
            gbc.gridy++;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.insets.top = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(closeButton, gbc);

            panel.setPreferredSize(new Dimension(650, 500));

            addDisplayComponent(panel);
            display();

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

}






