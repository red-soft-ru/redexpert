/*
 * ExportAsSQLPanelFour.java
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

package org.executequery.gui.importexport;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.executequery.gui.WidgetFactory;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
class ExportAsSQLPanelFour extends AbstractImportExportPanel {

    private JComboBox errorCombo;
    
    private JCheckBox createTableStatementsCheck;
    private JCheckBox includePrimaryConstraintsCheck;
    private JCheckBox includeForeignConstraintsCheck;
    private JCheckBox includeUniqueConstraintsCheck;
    
    public ExportAsSQLPanelFour(ImportExportWizard importExportWizard) {

        super(new GridBagLayout(), importExportWizard);
        
        init();
    }

    private void init() {

        errorCombo = createErrorCombo();

        createTableStatementsCheck = createCreateTableStatementsCheck();
        includePrimaryConstraintsCheck = createIncludePrimaryConstraintsCheck();
        includeForeignConstraintsCheck = createIncludeForeignConstraintsCheck();
        includeUniqueConstraintsCheck = createIncludeUniqueConstraintsCheck();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,10,15,10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 2;
        gbc.gridy = 0;
        gbc.gridx = 0;
        add(new JLabel(bundledString("ImportExportPanelFour.headerLabel")), gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.insets.bottom = 10;
        gbc.insets.left = 20;
        add(new JLabel(bundledString("ImportExportPanelFour.onErrorLabel")), gbc);
        gbc.gridx = 1;
        gbc.insets.left = 0;
        gbc.insets.top = 3;
        add(errorCombo, gbc);
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.insets.left = 20;
        gbc.insets.top = 5;
        add(createTableStatementsCheck, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        add(includePrimaryConstraintsCheck, gbc);
        gbc.gridy++;
        add(includeForeignConstraintsCheck, gbc);
//        gbc.gridy++;
//        add(includeUniqueConstraintsCheck, gbc);
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.gridy++;
        gbc.insets.top = 40;
        gbc.insets.left = 10;
        add(new JLabel(bundledString("ImportExportPanelFour.selectNextToBegin")), gbc);

    }

    private JCheckBox createIncludeUniqueConstraintsCheck() {

        return new JCheckBox(
                bundledString("ImportExportPanelFour.includeUniqueConstraintsCheck"), false);
    }

    private JCheckBox createIncludePrimaryConstraintsCheck() {

        return new JCheckBox(
                bundledString("ImportExportPanelFour.includePrimaryConstraintsCheck"), false);
    }

    private JCheckBox createIncludeForeignConstraintsCheck() {

        return new JCheckBox(
                bundledString("ImportExportPanelFour.includeForeignConstraintsCheck"), false);
    }

    private JCheckBox createCreateTableStatementsCheck() {

        return new JCheckBox(
                bundledString("ImportExportPanelFour.createTableStatementsCheck"), false);
    }

    private JComboBox createErrorCombo() {

        String[] options = {
                bundledString("ImportExportPanelFour.onErrorOptionOne"),
                bundledString("ImportExportPanelFour.onErrorOptionTwo")
        };

        JComboBox comboBox = WidgetFactory.createComboBox(options);
        comboBox.setPreferredSize(new Dimension(150, 20));

        return comboBox;
    }

    public void panelSelected() {
        
    }

    public boolean getIncludePrimaryKeyConstraints() {
        
        return includePrimaryConstraintsCheck.isSelected(); 
    }
    
    public boolean getIncludeForeignKeyConstraints() {
        
        return includeForeignConstraintsCheck.isSelected(); 
    }

    public boolean getIncludeCreateTableStatement() {

        return createTableStatementsCheck.isSelected();
    }

    public boolean getIncludeUniqueConstraints() {

        return includeUniqueConstraintsCheck.isSelected();
    }

    public OnErrorOption getOnErrorOption() {

        return errorCombo.getSelectedIndex() == 0 ?
                OnErrorOption.LOG_AND_CONTINUE : OnErrorOption.STOP_TRANSFER;
    }
    
}







