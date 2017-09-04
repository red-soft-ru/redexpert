/*
 * ByteArrayFileWriter.java
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

package org.executequery.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayFileWriter {

    public void write(File file, byte[] data) throws IOException {
        
        OutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;

        try {
        
            outputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(outputStream); 

            bufferedOutputStream.write(data);
            bufferedOutputStream.flush();
            
        } finally {

            try {
            
                if (bufferedOutputStream != null) {
                    
                    bufferedOutputStream.close();
                }

                if (outputStream != null) {
                    
                    outputStream.close();
                }
                
            } catch (IOException e) {}

        }

    }
    
}






