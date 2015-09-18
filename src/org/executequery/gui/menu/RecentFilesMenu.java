/*
 * RecentFilesMenu.java
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

package org.executequery.gui.menu;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import org.executequery.EventMediator;
import org.executequery.actions.filecommands.OpenRecentFileCommand;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.RecentOpenFileEvent;
import org.executequery.event.RecentOpenFileEventListener;
import org.executequery.repository.RecentlyOpenFileRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.menu.MainMenu;
import org.underworldlabs.swing.menu.MainMenuItem;

public class RecentFilesMenu extends MainMenu 
                             implements RecentOpenFileEventListener {

    private List<JMenuItem> recentMenuItemList;
    
    private ActionListener openRecentActionListener;
    
    public RecentFilesMenu() {
        
        super();
        
        createRecentFileMenu();
        
        EventMediator.registerListener(this);
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        
        return (event instanceof RecentOpenFileEvent);
    }

    public void recentFilesUpdated(RecentOpenFileEvent e) {
        
        reloadRecentFileMenu();
    }
    
    private void reloadRecentFileMenu() {

        createRecentFileMenu();
    }

    private String[] loadRecentFileList() {
        
        return recentlyOpenFileRepository().getFiles();
    }
    
    private void createRecentFileMenu() {
        
        String[] files = loadRecentFileList();
        
        if (files == null || files.length == 0) {

            return;
        }

        if (recentMenuItemList != null) {
        
            for (JMenuItem menuItem : recentMenuItemList) {

                remove(menuItem);
            }
            
        }

        resetRecentMenuItemList();
        
        if (openRecentActionListener == null) {

            openRecentActionListener = new OpenRecentFileCommand();
        }

        for (int i = 0; i < files.length; i++) {

            File file = new File(files[i]);
            String absolutePath = file.getAbsolutePath();

            JMenuItem menuItem = new MainMenuItem(file.getName());
            
            menuItem.setToolTipText(absolutePath);
            menuItem.setActionCommand(absolutePath);

            menuItem.addActionListener(openRecentActionListener);
            
            add(menuItem, i);
            
            recentMenuItemList.add(menuItem);
        }

    }


    private void resetRecentMenuItemList() {

        if (recentMenuItemList == null) {
            
            recentMenuItemList = new ArrayList<JMenuItem>();
            
        } else {
            
            recentMenuItemList.clear();
        }
    }
    
    private RecentlyOpenFileRepository recentlyOpenFileRepository() {

        return (RecentlyOpenFileRepository)RepositoryCache.load(
                    RecentlyOpenFileRepository.REPOSITORY_ID);        
    }

}










