/*
 * UserSettingsProperties.java
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

import org.executequery.ApplicationContext;

public final class UserSettingsProperties {

    public String getUserSettingsDirectory() {
        
        /* 
         * /home/user_name/.executequery/1234
         */
        
        StringBuilder sb = new StringBuilder();

        sb.append(getUserSettingsBaseHome()).
           append(eqBuild()).
           append(fileSeparator());

        return sb.toString();
    }
    
    public String getUserSettingsBaseHome() {
    
        /* 
         * /home/user_name/.executequery/
         */
        
        return ApplicationContext.getInstance().getUserSettingsHome();
    }

    private String eqBuild() {

        return ApplicationContext.getInstance().getBuild();
    }

    private String fileSeparator() {

        return System.getProperty("file.separator");
    }
    
}

