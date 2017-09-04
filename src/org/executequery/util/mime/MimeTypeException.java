/*
 * MimeTypeException.java
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

package org.executequery.util.mime;

/**
 * A class to encapsulate MimeType related exceptions.
 *
 * @author Hari Kodungallur
 * @author Jerome Charron - http://frutch.free.fr/
 */
public class MimeTypeException extends Exception {

    /**
     * Constructs a MimeTypeException with no specified detail message.
     */
    public MimeTypeException() {
        super();
    }
    
    /**
     * Constructs a MimeTypeException with the specified detail message.
     * @param msg the detail message.
     */
    public MimeTypeException(String msg) {
        super(msg);
    }

}






