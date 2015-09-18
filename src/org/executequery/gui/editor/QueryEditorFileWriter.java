/*
 * QueryEditorFileWriter.java
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

import java.io.File;

import org.executequery.gui.SaveFunction;
import org.executequery.gui.text.TextFileWriter;

public class QueryEditorFileWriter {

    public boolean write(String text, ScriptFile scriptFile, boolean saveAs) {

        String savePath = savePathFromScriptFile(scriptFile);
        
        TextFileWriter writer = new TextFileWriter(
                text, savePath, (!scriptFile.hasOpenFile() || saveAs));

        boolean saved = (writer.write() == SaveFunction.SAVE_COMPLETE);
        
        if (saved) {

            File file = writer.getSavedFile();

            scriptFile.setFileName(file.getName());
            scriptFile.setAbsolutePath(file.getAbsolutePath());
        }
        
        return saved;
    }

    private String savePathFromScriptFile(ScriptFile scriptFile) {

        if (scriptFile.hasOpenFile()) {
            
            return scriptFile.getAbsolutePath();
        }
        
        return scriptFile.getFileName();
    }
    
}





