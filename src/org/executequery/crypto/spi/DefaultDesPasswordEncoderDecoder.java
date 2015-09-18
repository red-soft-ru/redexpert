/*
 * DefaultDesPasswordEncoderDecoder.java
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

package org.executequery.crypto.spi;

import org.executequery.crypto.PasswordEncoderDecoder;
import org.underworldlabs.util.DesEncrypter;

public class DefaultDesPasswordEncoderDecoder implements PasswordEncoderDecoder {

    DefaultDesPasswordEncoderDecoder() {}
    
    public String decode(String key, String value) {
        return DesEncrypter.decrypt(key, value);
    }

    public String encode(String key, String value) {
        return DesEncrypter.encrypt(key, value);
    }

}






