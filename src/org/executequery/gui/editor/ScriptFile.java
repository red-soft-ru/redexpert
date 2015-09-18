/*
 * ScriptFile.java
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

package org.executequery.gui.editor;

import org.underworldlabs.util.MiscUtils;

public class ScriptFile {

    private String fileName;
    
    private String absolutePath;

    public boolean hasOpenFile() {
        return !MiscUtils.isNull(getAbsolutePath());
    }
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {

        this.absolutePath = absolutePath;
        
        resetFileName(absolutePath);        
    }

    private void resetFileName(String absolutePath) {
        if (hasOpenFile()) {
            
            String separator = System.getProperty("file.separator");
            int index = absolutePath.lastIndexOf(separator);
            
            if (index != -1) {

                fileName = absolutePath.substring(index + 1);

            } else {

                fileName = absolutePath;
            }
            
        }
    }

}










