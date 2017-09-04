/*
 * AcmeGifImageWriter.java
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

package org.executequery.imageio;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.executequery.imageio.acme.encoders.GifEncoder;

public class AcmeGifImageWriter extends AbstractImageWriter {

    public void write(ImageWriterInfo imageWriterInfo) {

        FileOutputStream fos = null;
        
        try {

            fos = new FileOutputStream(imageWriterInfo.getWriteToFile());

            GifEncoder gifEncoder = new GifEncoder(imageWriterInfo.getBufferedImage(), fos);
            gifEncoder.encode();

        } catch (FileNotFoundException e) {

            handleException(e);
            
        } catch (IOException e) {

            handleException(e);
            
        } finally {
            
            if (fos != null) {
                
                try {
                    fos.close();
                } catch (IOException e) {}

            }
            
        }

    }

}







