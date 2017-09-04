/*
 * ImageAssembler.java
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

package org.executequery.assembler;

import javax.swing.ImageIcon;

public class ImageAssembler {

    public ImageIcon assemble(byte[] data) {

        return new ImageIcon(data);
    }
    
    public boolean isImage(byte[] data) {

        return isGifImage(data) || isJpegImage(data) || isPngImage(data);
    }

    private static final byte[] GIF_BEGIN_BYTES = {
        (byte)0x47, (byte)0x49, (byte)0x46, (byte)0x38, (byte)0x39 };

    private boolean isGifImage(byte[] data) {

        return dataBeginsWith(data, GIF_BEGIN_BYTES);
    }
    
    private static final byte[] PNG_BEGIN_BYTES = {
        (byte)0xffffff89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D,
        (byte)0x0A, (byte)0x1A, (byte)0x0A };

    private boolean isPngImage(byte[] data) {

        return dataBeginsWith(data, PNG_BEGIN_BYTES);
    }

    private static final byte[] JPEG_BEGIN_BYTES = {
        (byte)0xffffffff, (byte)0xffffffd8, (byte)0xffffffff, (byte)0xffffffe0,
        (byte)0x00, (byte)0x10, (byte)0x4A, (byte)0x46, (byte)0x49, (byte)0x46 };

    private boolean isJpegImage(byte[] data) {

        return dataBeginsWith(data, JPEG_BEGIN_BYTES);
    }
    
    private boolean dataBeginsWith(byte[] data, byte[] beginsWith) {
        
        if (data.length < beginsWith.length) {
            
            return false;
        }

        for (int i = 0; i < beginsWith.length; i++) {
        
            if (data[i] != beginsWith[i]) {
                
                return false;
            }
            
        }

        return true;
    }
    
}






