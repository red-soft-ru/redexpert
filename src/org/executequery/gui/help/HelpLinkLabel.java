/*
 * HelpLinkLabel.java
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

package org.executequery.gui.help;

import java.awt.event.ActionEvent;

import org.executequery.ApplicationException;
import org.executequery.GUIUtilities;
import org.executequery.util.SystemWebBrowserLauncher;
import org.underworldlabs.util.MiscUtils;

import com.sun.java.help.impl.JHSecondaryViewer;

/** 
*
* @author   Takis Diakoumis
*/
public class HelpLinkLabel extends JHSecondaryViewer {

    private String mouseOverText;

    private String urlRedirect;

    public HelpLinkLabel() {
        super();
        setViewerActivator("javax.help.LinkLabel");
    }

    public void actionPerformed(ActionEvent e) {

        String redirect = getUrlRedirect();
        
        if (!MiscUtils.isNull(redirect)) {
            
            try {
                
                new SystemWebBrowserLauncher().launch(redirect);
                
            } catch (ApplicationException applicationException) {
                
                GUIUtilities.displayExceptionErrorDialog(
                        "Error launching local web browser:\n" + 
                        applicationException.getMessage(), applicationException);
                
            }

        }

    }

    public String getUrlRedirect() {
        return urlRedirect;
    }

    public void setUrlRedirect(String urlRedirect) {
        this.urlRedirect = urlRedirect;
    }

    public String getMouseOverText() {
        return mouseOverText;
    }

    public void setMouseOverText(String mouseOverText) {
        this.mouseOverText = mouseOverText;
        setToolTipText(mouseOverText);
    }
    
}






