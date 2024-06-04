/*
 * PluginLookAndFeelManager.java
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

package org.executequery.util;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.plaf.LookAndFeelDefinition;
import org.executequery.repository.LookAndFeelProperties;
import org.underworldlabs.util.DynamicLibraryLoader;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.io.File;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class PluginLookAndFeelManager {

    private LookAndFeelDefinition lookAndFeelDefinition;

    public void loadLookAndFeel() throws Exception {

        if (lookAndFeelDefinition == null) {
            lookAndFeelDefinition = LookAndFeelProperties.getLookAndFeel();
            if (lookAndFeelDefinition == null)
                lookAndFeelDefinition = new LookAndFeelDefinition(null);
        }

        Vector pathsVector = new Vector();
        String paths = lookAndFeelDefinition.getLibraryPath();
        if (paths.contains(String.valueOf(Constants.COLON_CHAR))) {

            StringTokenizer tokenizer = new StringTokenizer(paths, String.valueOf(Constants.COLON_CHAR));
            while (tokenizer.hasMoreTokens())
                pathsVector.add(tokenizer.nextToken());

        } else
            pathsVector.add(paths);

        URL[] urls = new URL[pathsVector.size()];
        for (int i = 0; i < urls.length; i++) {
            File file = new File((String) pathsVector.elementAt(i));
            urls[i] = file.toURL();
        }

        if (loadCustomLookAndFeel(urls)) {
            UserProperties.getInstance().setBooleanProperty("decorate.frame.look", lookAndFeelDefinition.isDecorateFrame());
            UserProperties.getInstance().setBooleanProperty("decorate.dialog.look", lookAndFeelDefinition.isDecorateDialogs());
        }
    }

    private boolean loadCustomLookAndFeel(URL[] urls) throws Exception {
        try {

            DynamicLibraryLoader loader = new DynamicLibraryLoader(urls);
            Class loadedClass = loader.loadLibrary(lookAndFeelDefinition.getClassName());

            LookAndFeel laf = (LookAndFeel) loadedClass.newInstance();

            if (!laf.isSupportedLookAndFeel()) {
                GUIUtilities.displayErrorMessage("The selected Look and Feel is not supported");
                return false;
            }

            LookAndFeelInfo info = new LookAndFeelInfo(laf.getName(), loadedClass.getName());
            UIManager.installLookAndFeel(info);
            UIManager.setLookAndFeel(laf);
            UIManager.getLookAndFeelDefaults().put("ClassLoader", loader);

        } catch (ClassNotFoundException cExc) {
            GUIUtilities.displayErrorMessage("The specified Look and Feel class was not found");
            return false;

        } catch (UnsupportedLookAndFeelException ulfExc) {
            GUIUtilities.displayErrorMessage("The selected Look and Feel is not supported");
            return false;
        }

        return true;
    }

}
