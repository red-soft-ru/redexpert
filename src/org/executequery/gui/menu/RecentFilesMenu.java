package org.executequery.gui.menu;

import org.executequery.EventMediator;
import org.executequery.actions.filecommands.OpenCommand;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.RecentOpenFileEvent;
import org.executequery.event.RecentOpenFileEventListener;
import org.executequery.repository.RecentlyOpenFileRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.menu.MainMenu;
import org.underworldlabs.swing.menu.MainMenuItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecentFilesMenu extends MainMenu
        implements RecentOpenFileEventListener {

    private final List<JMenuItem> recentMenuItemList;
    private final ActionListener openRecentActionListener;

    public RecentFilesMenu() {
        super();

        recentMenuItemList = new ArrayList<>();
        openRecentActionListener = RecentFilesMenu::openRecentFile;

        createRecentFileMenu();
        EventMediator.registerListener(this);
    }

    private void createRecentFileMenu() {

        for (JMenuItem menuItem : recentMenuItemList)
            remove(menuItem);
        recentMenuItemList.clear();
        setEnabled(false);

        Repository repo = RepositoryCache.load(RecentlyOpenFileRepository.REPOSITORY_ID);
        if (!(repo instanceof RecentlyOpenFileRepository))
            return;

        String[] files = ((RecentlyOpenFileRepository) repo).getFiles();
        if (files == null || files.length < 1)
            return;

        for (int i = 0; i < files.length; i++) {

            File file = new File(files[i]);
            String absolutePath = file.getAbsolutePath();

            JMenuItem menuItem = new MainMenuItem(file.getName());
            menuItem.addActionListener(openRecentActionListener);
            menuItem.setActionCommand(absolutePath);
            menuItem.setToolTipText(absolutePath);

            add(menuItem, i);
            recentMenuItemList.add(menuItem);
        }

        setEnabled(!recentMenuItemList.isEmpty());
    }

    private static void openRecentFile(ActionEvent e) {
        new OpenCommand().openFile(new File(e.getActionCommand()));
    }

    // --- RecentOpenFileEventListener impl ---

    @Override
    public void recentFilesUpdated(RecentOpenFileEvent e) {
        createRecentFileMenu();
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return event instanceof RecentOpenFileEvent;
    }

}
