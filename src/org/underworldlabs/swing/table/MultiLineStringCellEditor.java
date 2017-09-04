/*
 * MultiLineStringCellEditor.java
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

package org.underworldlabs.swing.table;

import java.util.Dictionary;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class MultiLineStringCellEditor extends StringCellEditor {

    protected Document createDefaultModel() {

        Document document = new PlainDocument() {

            public Dictionary<Object,Object> getDocumentProperties() {
                
                Dictionary<Object, Object> dictionary = super.getDocumentProperties();
                dictionary.put("filterNewlines", Boolean.FALSE);
                return dictionary;
            }
            
        };

        return document;
    }
    
}

