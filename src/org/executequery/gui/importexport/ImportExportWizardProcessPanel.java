/*
 * ImportExportWizardProcessPanel.java
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

package org.executequery.gui.importexport;

import org.executequery.util.StringBundle;
import org.executequery.util.SystemResources;
import org.underworldlabs.swing.wizard.WizardProcessPanel;

public abstract class ImportExportWizardProcessPanel extends WizardProcessPanel {

    private StringBundle bundle;

    protected final StringBundle getBundle() {

        return bundle();
    }

    private StringBundle bundle() {
        
        if (bundle == null) {            
        
            bundle = SystemResources.loadBundle(getClass());
        }

        return bundle;
    }

    protected final String getString(String key) {

        return getBundle().getString(key);
    }

    protected final String getString(String key, Object arg) {

        return getBundle().getString(key, arg);
    }

}





