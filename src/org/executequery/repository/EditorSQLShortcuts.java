/*
 * EditorSQLShortcuts.java
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

package org.executequery.repository;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author   Takis Diakoumis
 */
public class EditorSQLShortcuts {

    private static EditorSQLShortcuts instance;
    
    private EditorSQLShortcutRepository editorSQLShortcutRepository;

    private List<EditorSQLShortcut> shortcuts;

    private EditorSQLShortcuts() {

        editorSQLShortcutRepository = (EditorSQLShortcutRepository) 
            RepositoryCache.load(EditorSQLShortcutRepository.REPOSITORY_ID);
    }

    public static synchronized EditorSQLShortcuts getInstance() {

        if (instance == null) {

            instance = new EditorSQLShortcuts();
        }

        return instance;
    }
    
    public void addShortcut(EditorSQLShortcut shortcut) throws RepositoryException {
        loadShortcuts();
        shortcuts.add(shortcut);
        save(shortcuts);
    }

    public void save() throws RepositoryException {
        save(shortcuts);
    }
    
    public void save(List<EditorSQLShortcut> shortcuts) throws RepositoryException {
        editorSQLShortcutRepository.save(shortcuts);
        this.shortcuts = shortcuts;
    }

    public List<EditorSQLShortcut> getEditorShortcuts() {        
        loadShortcuts();
        return shortcuts;
    }

    public boolean hasShortcuts() {
        loadShortcuts();
        return (shortcuts != null && !shortcuts.isEmpty());
    }

    public boolean shortcutTextExists(String shortcutText) {

        if (!hasShortcuts()) {

            return false;
        }
        
        return (findShortcutByShortcutText(shortcutText) != null);
    }

    public void removeShortcutByShortcutText(String name) {
        
        EditorSQLShortcut shortcut = findShortcutByShortcutText(name);
        
        if (shortcut != null) {
            
            removeShortcut(shortcut);
        }
        
    }

    public void removeShortcut(EditorSQLShortcut shortcut) {
        shortcuts.remove(shortcut);
    }

    public EditorSQLShortcut findShortcutByShortcutText(String text) {
        
        loadShortcuts();
        
        for (EditorSQLShortcut shortcut : shortcuts) {
            
            if (shortcut.getShortcut().equals(text)) {

                return shortcut;
            }
            
        }
        
        return null;
    }

    private void loadShortcuts() {

        if (shortcuts == null || shortcuts.isEmpty()) {

            try {

                shortcuts = editorSQLShortcutRepository.open();

            } catch (RepositoryException e) {

                shortcuts = new ArrayList<EditorSQLShortcut>();
            }

        }
    }

}











