/*
 * UserSettingsDirectoryBuildNumberComparator.java
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

import java.io.File;
import java.util.Comparator;

class UserSettingsDirectoryBuildNumberComparator implements Comparator<File> {

    public int compare(File o1, File o2) {

        String name1 = o1.getName();
        String name2 = o2.getName();

        return name1.compareTo(name2);
    }
    
}





