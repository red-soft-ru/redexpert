/*
 * AbstractImportExportPanel.java
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

import java.awt.LayoutManager;

import javax.swing.JPanel;

import org.executequery.localization.Bundles;

abstract class AbstractImportExportPanel extends JPanel {

    private final ImportExportWizard importExportWizard;
    
    public AbstractImportExportPanel(ImportExportWizard importExportWizard) {
        super();
        this.importExportWizard = importExportWizard;
    }

    public AbstractImportExportPanel(LayoutManager layout, ImportExportWizard importExportWizard) {
        super(layout);
        this.importExportWizard = importExportWizard;
    }

    protected final boolean isExport() {
        return importExportDataModel().isExport();
    }
    
    protected final ImportExportWizard importExportWizard() {
        return importExportWizard;
    }
    
    protected final ImportExportDataModel importExportDataModel() {
        return importExportWizard.getExportDataModel();
    }

    protected final String bundledString(String key) {
        return Bundles.get(key);
    }
    
}

