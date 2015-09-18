/*
 * EditorSQLShortcutXMLRepository.java
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

package org.executequery.repository.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.executequery.repository.EditorSQLShortcut;
import org.executequery.repository.EditorSQLShortcutRepository;
import org.executequery.repository.RepositoryException;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.MiscUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public final class EditorSQLShortcutXMLRepository extends AbstractXMLRepository<EditorSQLShortcut> 
    implements EditorSQLShortcutRepository {
    
    // -------------------------------------------
    // XML tag names and attributes
    
    private static final String FILE_PATH = "editorsqlshortcuts.xml";
    
    private static final String EDITOR_SQL_SHORTCUTS = "editor-shortcuts";
    private static final String EDITOR_SHORTCUT = "editor-shortcut";
    private static final String ID = "id";
    private static final String SHORTCUT = "shortcut";
    
    public EditorSQLShortcutXMLRepository() {}

    public void save(List<EditorSQLShortcut> shortcuts) 
        throws RepositoryException {
        write(filePath(), new EditorSQLShortcutParser(), 
                new EditorSQLShortcutInputSource(shortcuts));
    }

    public List<EditorSQLShortcut> open() {
        
        return (List<EditorSQLShortcut>)read(filePath(), new EditorSQLShortcutHandler());
    }
    
    private String filePath() {
        
        UserSettingsProperties settings = new UserSettingsProperties();
        
        return settings.getUserSettingsDirectory() + FILE_PATH;
    }
    
    public String getId() {

        return REPOSITORY_ID;
    }
    
    class EditorSQLShortcutHandler 
        extends AbstractXMLRepositoryHandler<EditorSQLShortcut> {

        private List<EditorSQLShortcut> shortcuts;

        private EditorSQLShortcut shortcut;
        
        EditorSQLShortcutHandler() {
            shortcuts = new ArrayList<EditorSQLShortcut>();
        }

        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {

            contents().reset();
            
            if (localNameIsKey(localName, EDITOR_SHORTCUT)) {

                shortcut = new EditorSQLShortcut();
                shortcut.setId(attrs.getValue(ID));
                shortcut.setShortcut(attrs.getValue(SHORTCUT));
            }

        }
        
        public void endElement(String nameSpaceURI, String localName,
                               String qName) {

            if (localNameIsKey(localName, EDITOR_SHORTCUT)) {

                shortcut.setQuery(contentsAsString().trim());
                shortcuts.add(shortcut);
                shortcut = null;
            }

        }

        public List<EditorSQLShortcut> getRepositoryItemsList() {

            return shortcuts;
        }

    } // EditorSQLShortcutHandler
    
    class EditorSQLShortcutInputSource extends InputSource {
        
        private List<EditorSQLShortcut> shortcuts;

        public EditorSQLShortcutInputSource(List<EditorSQLShortcut> shortcuts) {

            super();
            this.shortcuts = shortcuts;
        }
        
        public List<EditorSQLShortcut> getEditorSQLShortcuts() {
            
            return shortcuts;
        }
        
    } // class EditorSQLShortcutInputSource
    
    class EditorSQLShortcutParser extends AbstractXMLRepositoryParser {

        public EditorSQLShortcutParser() {}

        public void parse(InputSource input) throws SAXException, IOException {

            if (!(input instanceof EditorSQLShortcutInputSource)) {

                throw new SAXException(
                        "Parser can only accept a EditorSQLShortcutInputSource");
            }
            
            parse((EditorSQLShortcutInputSource)input);
        }
        
        public void parse(EditorSQLShortcutInputSource input) 
            throws IOException, SAXException {

            validateHandler();
            
            List<EditorSQLShortcut> shortcuts = input.getEditorSQLShortcuts();
            
            handler().startDocument();
            newLine();
            handler().startElement(NSU, EDITOR_SQL_SHORTCUTS, EDITOR_SQL_SHORTCUTS, attributes());
            newLine();

            if (shortcuts != null) {

                writeXMLRows(shortcuts);
            }
            
            newLine();
            
            handler().endElement(NSU, EDITOR_SQL_SHORTCUTS, EDITOR_SQL_SHORTCUTS);
            handler().endDocument();

        }

        private void writeXMLRows(List<EditorSQLShortcut> shortcuts)
            throws SAXException {

            for (EditorSQLShortcut shortcut : shortcuts) {

                if (shortcut.isNew()) {

                    shortcut.setId(generateUniqueId());
                }
                
                newLineIndentOne();

                attributes().addAttribute(NSU, ID, ID, 
                        CDDATA, shortcut.getId());

                attributes().addAttribute(NSU, SHORTCUT, SHORTCUT, 
                        CDDATA, shortcut.getShortcut().toUpperCase());

                handler().startElement(NSU, EDITOR_SHORTCUT, EDITOR_SHORTCUT, attributes());
                newLine();

                resetAttributes();

                String query = shortcut.getQuery();
                handler().characters(query.toCharArray(), 0, query.length());
                
                newLineIndentOne();
                handler().endElement(NSU, EDITOR_SHORTCUT, EDITOR_SHORTCUT);
                newLine();
            }

        }

        private String generateUniqueId() {
            return MiscUtils.generateUniqueId();
        }
        
    } // class EditorSQLShortcutParser
    
}





