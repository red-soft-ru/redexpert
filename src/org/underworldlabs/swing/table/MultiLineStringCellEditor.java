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
