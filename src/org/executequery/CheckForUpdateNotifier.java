/*
 * CheckForUpdateNotifier.java
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

package org.executequery;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.executequery.components.SimpleHtmlContentPane;
import org.executequery.components.StatusBarPanel;
import org.executequery.gui.InformationDialog;
import org.executequery.log.Log;
import org.executequery.repository.LatestVersionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.InterruptibleProgressDialog;
import org.underworldlabs.swing.util.Interruptible;
import org.underworldlabs.swing.util.SwingWorker;

/**
 * Checks to see if a newer version of Execute Query is available. 
 * 
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class CheckForUpdateNotifier implements Interruptible {

    private static final int LABEL_INDEX = 2;
    
    private ApplicationVersion version;

    private LatestVersionRepository repository;

    private SwingWorker worker;
    
    private InterruptibleProgressDialog progressDialog;

    private boolean monitorProgress;
    
    public void startupCheckForUpdate() {
        
        SwingWorker worker = new SwingWorker() {

            public Object construct() {
                startupCheck();
                return Constants.WORKER_SUCCESS;
            }

            public void finished() {}

        };

        worker.start();
    }
    
    private void startupCheck() {
        
        try {
        
            version = getVersionInfo();

            if (isNewVersion(version)) {
                
                logNewVersonInfo();
                setNotifierInStatusBar();
                
            } else {
                
                Log.info("Execute Query is up to date.");
            }

        } catch (ApplicationException e) {

            Log.warning("Error checking for update: " + e.getMessage());
        }
        
    }

    private void setNotifierInStatusBar() {
         
        JLabel label = getUpdateNotificationLabel();

        label.addMouseListener(new NotificationLabelMouseAdapter());        
        label.setIcon(GUIUtilities.loadIcon("YellowBallAnimated16.gif"));
        label.setToolTipText(newVersionAvailableText());

        statusBar().setThirdLabelText("Update available");
    }

    private JLabel getUpdateNotificationLabel() {

        return statusBar().getLabel(LABEL_INDEX);
    }

    private StatusBarPanel statusBar() {
        
        return GUIUtilities.getStatusBar();
    }
    
    class NotificationLabelMouseAdapter extends MouseAdapter {
        
        public void mouseReleased(MouseEvent e) {

            resetLabel();

            int yesNo = displayNewVersionMessage();
            if (yesNo == JOptionPane.YES_OPTION) {

                worker = new SwingWorker() {

                    public Object construct() {

                        return displayReleaseNotes();
                    }
                    public void finished() {

                        closeProgressDialog();
                        GUIUtilities.showNormalCursor();
                    }

                };
                worker.start();

            }
                
        }

        private void resetLabel() {

            JLabel label = getUpdateNotificationLabel();
            
            label.setIcon(null);
            label.setToolTipText(null);

            label.removeMouseListener(this);

            statusBar().setThirdLabelText("");
        }
        
    }

    public void checkForUpdate(boolean monitorProgress) {
        
        this.monitorProgress = monitorProgress;
        worker = new SwingWorker() {

            public Object construct() {

                return doWork();
            }
            public void finished() {

                closeProgressDialog();
                GUIUtilities.showNormalCursor();
            }

        };

        if (monitorProgress) {

            createProgressDialog();
        }

        worker.start();
        progressDialog.run();
    }

    private void createProgressDialog() {

        progressDialog = new InterruptibleProgressDialog(
            GUIUtilities.getParentFrame(),
            "Check for update", 
            "Checking for updated version from http://executequery.org",
            this);
    }

    private Object doWork() {
        
        try {
            
            version = getVersionInfo();

            if (isNewVersion(version)) {
                
                logNewVersonInfo();

                closeProgressDialog();
                
                int yesNo = displayNewVersionMessage();
                if (yesNo == JOptionPane.YES_OPTION) {

                    return displayReleaseNotes();
                }

            } else {

                Log.info("Execute Query is up to date.");
                
                if (monitorProgress) {

                    closeProgressDialog();
    
                    GUIUtilities.displayInformationMessage(noUpdateMessage());
                }

            }
            
            return Constants.WORKER_SUCCESS;
            
        } catch (ApplicationException e) {
            
            if (monitorProgress) {

                showExceptionErrorDialog(e);    
            }

            return Constants.WORKER_FAIL;
        }

    }

    private int displayNewVersionMessage() {
        
        return GUIUtilities.displayYesNoDialog(
                new SimpleHtmlContentPane(newVersionMessage(version)), 
                "Execute Query Update");
    }

    private void logNewVersonInfo() {

        Log.info(newVersionAvailableText());
    }

    private String newVersionAvailableText() {

        return "New version " + version.getVersion() + " available.";
    }

    private boolean isNewVersion(ApplicationVersion version) {
        
        String currentBuild = getCurrentBuild();

        return version.isNewerThan(currentBuild);
    }
    
    private ApplicationVersion getVersionInfo() {
        
        Log.info("Checking for new version update from http://executequery.org ...");
        
        return repository().getLatestVersion();
    }
    
    private LatestVersionRepository repository() {
        
        if (repository == null) {
            
            repository = (LatestVersionRepository) 
                RepositoryCache.load(LatestVersionRepository.REPOSITORY_ID);
        }
        
        return repository;
    }
    
    private Object displayReleaseNotes() {

        try {
            
            GUIUtilities.showWaitCursor();

            createProgressDialogForReleaseNotesLoad();

            final String releaseNotes = repository().getReleaseNotes();

            closeProgressDialog();

            GUIUtils.invokeAndWait(new Runnable() {
                public void run() {
                    new InformationDialog("Latest Version Info", 
                        releaseNotes, InformationDialog.TEXT_CONTENT_VALUE);
                }
            });

            return Constants.WORKER_SUCCESS;

        } catch (ApplicationException e) {
            
            showExceptionErrorDialog(e);

            return Constants.WORKER_FAIL;
            
        } finally {
            
            GUIUtilities.showNormalCursor();
        }

    }

    private void createProgressDialogForReleaseNotesLoad() {

        GUIUtils.invokeLater(new Runnable() {
            
            public void run() {

                progressDialog = new InterruptibleProgressDialog(
                    GUIUtilities.getParentFrame(),
                    "Check for update", 
                    "Retrieving new version release notes from http://executequery.org",
                    CheckForUpdateNotifier.this);

                progressDialog.run();
            }
            
        });
    }
    
    private void showExceptionErrorDialog(ApplicationException e) {

        GUIUtilities.showNormalCursor();
        
        GUIUtilities.displayExceptionErrorDialog(genericIOError(), e);
    }

    private String genericIOError() {

        return "An error occured trying to communicate " +
            " with the server at http://executequery.org.";
    }

    private String newVersionMessage(ApplicationVersion version) {

        return "New version " + version.getVersion() + 
            " (Build " + version.getBuild() + ") " +
            " is available for download at " +
            "<a href=\"http://executequery.org\">http://executequery.org</a>." +
            "\nClick <a href=\"http://executequery.org/download.jsp\">here</a>" +
            " to go to the download page.\n\nDo you wish to view the " +
            "version notes for this release?";
    }

    private String noUpdateMessage() {
        return "No update available.\n" +
            "This version of Execute Query is up to date.\n" +
            "Please check back here periodically to ensure you have " +
            "the latest version.";
    }
    
    private String getCurrentBuild() {

        return System.getProperty("executequery.build");
    }

    private void closeProgressDialog() {
        
        if (progressDialog != null) {

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (progressDialog.isVisible()) {

                        progressDialog.dispose();
                    }
                    progressDialog = null;                
                }
            });
            
        }
    }

    public void setCancelled(boolean cancelled) {
        
        interrupt();
    }
    
    public void interrupt() {

        if (worker != null) { 

            worker.interrupt();
        }
    }

}










