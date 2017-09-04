/*
 * ImportExportWizardModel.java
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

import org.underworldlabs.swing.wizard.DefaultWizardProcessModel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ImportExportWizardModel extends DefaultWizardProcessModel {

    /** Creates a new instance of ImportExportWizardModel */
    public ImportExportWizardModel(int stepsCount, int transferType) {

        String firstTitle = "Database Connection and Export Type";
        String fifthTitle = "Exporting Data...";
        if (transferType == ImportExportDataProcess.IMPORT) {
            firstTitle = "Database Connection and Import Type";
            fifthTitle = "Importing Data...";
        }

        String[] titles = new String[stepsCount];
        titles[0] = firstTitle;
        titles[1] = "Table Selection";
        titles[2] = "Data File Selection";
        titles[3] = "Options";
        titles[4] = fifthTitle;
        setTitles(titles);

        String[] steps = {"Select database connection and transfer type",
                          "Select the tables/columns",
                          transferType == ImportExportDataProcess.IMPORT ?
                              "Select the data file(s) to import from" :
                              "Select the data file(s) to export to",
                          "Set any further transfer options",
                          transferType == ImportExportDataProcess.IMPORT ?
                              "Import the data" :
                              "Export the data"};
        setSteps(steps);
    }
    
}











