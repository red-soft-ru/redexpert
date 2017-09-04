/*
 * DefaultJpegImageWriter.java
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
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import org.executequery.ApplicationException;

public class DefaultJpegImageWriter extends AbstractImageWriter {

    public void write(ImageWriterInfo imageWriterInfo) {

        if (!(imageWriterInfo instanceof JpegImageWriterInfo)) {

            throw new IllegalArgumentException(
                    "Image writer info must be an instance of JpegImageWriterInfo");
        }

        JpegImageWriterInfo jpegImageWriterInfo = (JpegImageWriterInfo) imageWriterInfo;

        javax.imageio.ImageWriter writer = getImageWriter();
        if (writer == null) {
            
            throw new ApplicationException("No writer found for output image type JPG");
        }
        
        JPEGImageWriteParam param = (JPEGImageWriteParam) writer.getDefaultWriteParam();
        
        float quality = (jpegImageWriterInfo.getImageQuality() *10) / 100.0f;
        
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        
        FileImageOutputStream output = null;
        
        try {

            output = new FileImageOutputStream(jpegImageWriterInfo.getWriteToFile());
            writer.setOutput(output);

            IIOImage image = new IIOImage(jpegImageWriterInfo.getBufferedImage(), null, null);
            writer.write(null, image, param);

        } catch (FileNotFoundException e) {

            handleException(e);
            
        } catch (IOException e) {

            handleException(e);
            
        } finally {
            
            if (output != null) {
                
                try {
                    output.close();
                } catch (IOException e) {}

            }
            
        }

    }

    private javax.imageio.ImageWriter getImageWriter() {
        
        for (Iterator<ImageWriter> i = ImageIO.getImageWritersByFormatName("JPG"); i.hasNext();) {
            
            return i.next();
        }

        return null;
    }
    
}

/*
// using batik - requires batik-transcoder.jar 

public class BatikJpgImageWriter extends AbstractImageWriter {

    public void write(ImageWriterInfo imageWriterInfo) {

        FileOutputStream fos = null;
        
        try {

            JPEGTranscoder transcoder = new JPEGTranscoder();
            transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));

            fos = new FileOutputStream(imageWriterInfo.getWriteToFile());
            BufferedOutputStream bfos = new BufferedOutputStream(fos);
            
            TranscoderOutput lOutput = new TranscoderOutput(bfos);
            transcoder.writeImage(imageWriterInfo.getBufferedImage(), lOutput);

            bfos.flush();
            
        } catch (IOException e) {

            handleException(e);
        
        } catch (TranscoderException e) {

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



*/





