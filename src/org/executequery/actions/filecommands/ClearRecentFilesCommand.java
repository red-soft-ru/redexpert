package org.executequery.actions.filecommands;

import org.executequery.EventMediator;
import org.executequery.event.DefaultRecentOpenFileEvent;
import org.executequery.event.RecentOpenFileEvent;
import org.executequery.repository.RecentlyOpenFileRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class ClearRecentFilesCommand implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {
        Repository repo = RepositoryCache.load(RecentlyOpenFileRepository.REPOSITORY_ID);
        if (repo instanceof RecentlyOpenFileRepository) {
            ((RecentlyOpenFileRepository) repo).clear();
            EventMediator.fireEvent(new DefaultRecentOpenFileEvent(this, RecentOpenFileEvent.RECENT_FILES_UPDATED));
        }
    }

}
