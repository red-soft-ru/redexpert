/*
 * EncodingDetector.java
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

package org.underworldlabs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.executequery.log.Log;
import org.mozilla.universalchardet.UniversalDetector;

public class EncodingDetector {

    public String detectCharset(File file) throws IOException {

        byte[] buf = new byte[4096];
        FileInputStream fis = new FileInputStream(file);        
        UniversalDetector detector = new UniversalDetector(null);

        try {
            
            int read = 0;
            while ((read = fis.read(buf)) > 0 && !detector.isDone()) {

                detector.handleData(buf, 0, read);
            }
            detector.dataEnd();

            String encoding = detector.getDetectedCharset();
            if (encoding != null) {

                Log.info("Detected file encoding - " + encoding);

            } else {

                Log.debug("No specific file encoding detected.");
            }

            return encoding;
            
        } finally {

            detector.reset();
            if (fis != null) {

                fis.close();
            }
            
        }
    }

}

