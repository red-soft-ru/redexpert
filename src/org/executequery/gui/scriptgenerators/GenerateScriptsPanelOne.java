/*
 * GenerateScriptsPanelOne.java
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

package org.executequery.gui.scriptgenerators;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.WidgetFactory;

/**
 * Step one panel in the generate scripts wizard.
 *
 * @author   Takis Diakoumis
 */
public class GenerateScriptsPanelOne extends JPanel implements GenerateScriptsPanel {

    /** create table script radio button */
    private JRadioButton createTableButton;

    /** drop table script radio button */
    private JRadioButton dropTableButton;

    /** The connection combo selection */
    private JComboBox connectionsCombo;

    /** Creates a new instance of GenerateScriptsPanelOne */
    public GenerateScriptsPanelOne(GenerateScriptsWizard parent) {

        super(new GridBagLayout());

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void init() throws Exception {

        createTableButton = new JRadioButton("CREATE TABLE/OBJECT script", true);
        dropTableButton = new JRadioButton("DROP TABLE/OBJECT script");

        ButtonGroup bg = new ButtonGroup();
        bg.add(createTableButton);
        bg.add(dropTableButton);

        connectionsCombo = WidgetFactory.createComboBox();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.insets = new Insets(7,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Connection:"), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.insets.top = 5;
        add(connectionsCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets.top = 10;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Select the type of scipt to be generated:"), gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.left = 20;
        add(createTableButton, gbc);
        gbc.gridy++;
        gbc.weighty = 1.0;
        add(dropTableButton, gbc);

        setPreferredSize(GenerateScriptsWizard.CHILD_DIMENSION);
    }

    public void panelSelected() {}

    protected JComboBox getConnectionsCombo() {

        return connectionsCombo;
    }

    /**
     * Returns the selected database connection properties object.
     *
     * @return the connection properties object
     */
    public DatabaseConnection getDatabaseConnection() {

        return (DatabaseConnection)connectionsCombo.getSelectedItem();
    }

    /**
     * Returns the type of script to be generated.
     *
     * @return the script type
     */
    protected int getScriptType() {

        if (createTableButton.isSelected()) {

            return GenerateScriptsWizard.CREATE_TABLES;

        } else {

            return GenerateScriptsWizard.DROP_TABLES;
        }

    }

}







