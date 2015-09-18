/*
 * SystemWebBrowserLauncher.java
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

package org.executequery.util;

import org.executequery.ApplicationException;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

public class SystemWebBrowserLauncher {

    public void launch(final String url) {

        ThreadUtils.startWorker(new Runnable() {
           
            public void run() {

                try {

                    BrowserLauncher launcher = new BrowserLauncher();
                    launcher.openURLinBrowser(url);

                } catch (BrowserLaunchingInitializingException e) {
                    
                    throw new ApplicationException(e);

                } catch (UnsupportedOperatingSystemException e) {
                  
                    throw new ApplicationException(e);
                }

            }
            
        });

    }
    
}





