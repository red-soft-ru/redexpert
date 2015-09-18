/*
 * GenerateScriptsPanelTwo.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.ListSelectionPanel;

/**
 * Step two panel in the generate scripts wizard.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class GenerateScriptsPanelTwo extends JPanel implements GenerateScriptsPanel {
    
    /** The list table/column list selection panel */
    private ListSelectionPanel list;
    
    /** The schema list */
    private JComboBox schemaCombo;

    /** Creates a new instance of GenerateScriptsPanelTwo */
    public GenerateScriptsPanelTwo(GenerateScriptsWizard parent) {
        
        super(new GridBagLayout());
        
        try {
            
            init();
            
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void init() throws Exception {

        schemaCombo = WidgetFactory.createComboBox();

        list = new ListSelectionPanel("Available Tables:", "Selected Tables:");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(7,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Schema:"), gbc);
        gbc.gridx = 1;
        gbc.insets.top = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(schemaCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(list, gbc);

        setPreferredSize(GenerateScriptsWizard.CHILD_DIMENSION);
    }

    protected JComboBox getSchemasCombo() {

        return schemaCombo;
    }

    public void panelSelected() {}
    
    protected void schemaSelectionChanged(List<NamedObject> tables) {
        
        if (tables != null && !tables.isEmpty()) {

            Vector<NamedObject> tablesVector = new Vector<NamedObject>(tables.size());
            tablesVector.addAll(tables);
            
            list.createAvailableList(tablesVector);

        } else {

            list.clear();
        }

    }

    /**
     * Whether the selected list has any values in it.
     */
    protected boolean hasSelections() {

        return list.hasSelections();
    }
    
    @SuppressWarnings("unchecked")
    protected List<NamedObject> getSelectedTables() {

        return list.getSelectedValues();
    }
    
    /**
     * Returns the selected tables in an array.
     *
     * @return the selected tables
     */
    @SuppressWarnings("unchecked")
    protected String[] getSelectedxTables() {

        Vector<NamedObject> v = list.getSelectedValues();
        
        String[] tables = new String[v.size()];

        for (int i = 0; i < tables.length; i++) {

            tables[i] = v.get(i).getName();
        }

        return tables;
    }

}






