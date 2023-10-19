/*
 * CheckForUpdateNotifier.java
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

package org.executequery;

import org.apache.commons.lang.ArrayUtils;
import org.executequery.components.SimpleHtmlContentPane;
import org.executequery.components.StatusBarPanel;
import org.executequery.gui.InformationDialog;
import org.executequery.gui.PulsatingCircle;
import org.executequery.http.JSONAPI;
import org.executequery.http.ReddatabaseAPI;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.LatestVersionRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.InterruptibleProgressDialog;
import org.underworldlabs.swing.util.Interruptible;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks to see if a newer version of Execute Query is available.
 *
 * @author Takis Diakoumis
 */
public class CheckForUpdateNotifier implements Interruptible {

    private static final int LABEL_INDEX = 2;

    private ApplicationVersion version;

    private LatestVersionRepository repository;

    private SwingWorker worker;

    private InterruptibleProgressDialog progressDialog;

    private boolean monitorProgress;

    private UpdateLoader updateLoader = null;

    public void startupCheckForUpdate() {

        SwingWorker worker = new SwingWorker("startupCheckUpdate") {

            public Object construct() {
                startupCheck();
                return Constants.WORKER_SUCCESS;
            }

            public void finished() {
            }

        };

        worker.start();
    }

    private boolean unstable = false;

    private boolean releaseHub = false;

    private void startupCheck() {


        ApplicationContext instance = ApplicationContext.getInstance();

        String repo = instance.getRepo();

        if (!repo.isEmpty()) {
            checkFromRepo(repo);

        } else {
            unstable = UserProperties.getInstance().getBooleanProperty("startup.unstableversions.load");
            releaseHub = UserProperties.getInstance().getBooleanProperty("releasehub");
            if (releaseHub) {
                try {
                    checkFromReleaseHub(unstable);
                } catch (IOException e) {
                    Log.warning(String.format("Cannot check for updates: %s", e.getMessage()));
                } catch (Exception e) {
                    checkFromReddatabase(unstable);
                }

            } else checkFromReddatabase(unstable);


        }

    }

    private void checkFromReddatabase(boolean unstable) {
        try {
            updateLoader = new UpdateLoader("");
            Log.info(bundledString("console-CheckingForNewVersion") + "https://reddatabase.ru ...");
            String url;
            if (unstable)
                url = UserProperties.getInstance().getStringProperty("reddatabase.check.rc.url");
            else url = UserProperties.getInstance().getStringProperty("reddatabase.check.url");
            version = new ApplicationVersion(JSONAPI.getJsonPropertyFromUrl(url, "version"));

            if (isNewVersion(version)) {
                updateLoader.setVersion(version.getVersion());
                logNewVersonInfo();
                setNotifierInStatusBar();
                setDownloadNotifierInStatusBar();

            } else {

                Log.info(bundledString("console-RedExpertUpToDate"));
            }

        } catch (ApplicationException e) {
            Log.warning(bundledString("console-ErrorCheckingForUpdate") + ": " + e.getMessage());
        } catch (UnknownHostException e) {
            if (progressDialog != null) {
                GUIUtilities.displayExceptionErrorDialog(bundledString("noInternetMessage"), e);
                closeProgressDialog();
            }
            Log.error(bundledString("noInternetMessage"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFromReleaseHub(boolean unstable) throws IOException {
        updateLoader = new UpdateLoader("");
        boolean checkmajor = UserProperties.getInstance().getBooleanProperty("startup.majorversions.load");
        String url;
        String branch;
        if (checkmajor && !MiscUtils.isNull(SystemProperties.getProperty(Constants.SYSTEM_PROPERTIES_KEY, "branch.next.major.version")))
            branch = SystemProperties.getProperty(Constants.SYSTEM_PROPERTIES_KEY, "branch.next.major.version");
        else branch = SystemProperties.getProperty(Constants.SYSTEM_PROPERTIES_KEY, "branch");
        url = "http://builds.red-soft.biz/api/v1/builds/latest/?project=red_expert&branch=" + branch;
        if (unstable)
            url += "&stage=0";
        else
            url += "&stage=2";
        version = new ApplicationVersion(JSONAPI.getJsonPropertyFromUrl(url, "version"));
        if (isNewVersion(version)) {

            updateLoader.setVersion(version.getVersion());
            setDownloadNotifierInStatusBar();

        } else {

            Log.info(bundledString("console-RedExpertUpToDate"));
        }
    }

    private void checkFromRepo(String repo) {
        Log.info(bundledString("Checking ") + repo + " ...");

        // updating from repository to latest version
        updateLoader = new UpdateLoader(repo);
        if (updateLoader.isNeedUpdate()) {
            version = new ApplicationVersion(updateLoader.getVersion());
            setDownloadNotifierInStatusBar();
        } else {
            if (updateLoader.getVersion() != null)
                Log.info(bundledString("console-RedExpertUpToDate"));
        }
    }

    private void setDownloadNotifierInStatusBar() {
        JLabel label = getUpdateNotificationLabel();

        JButton button = new DefaultButton();
        button.setText(bundledString("console-DownloadUpdate"));
        button.setSize(200, 20);

        label.addMouseListener(new DownloadNotificationLabelMouseAdapter());
        label.setIcon(GUIUtilities.loadIcon("YellowBallAnimated16.gif"));
        label.setToolTipText(newVersionAvailableText());

        statusBar().setThirdLabelText(bundledString("updateAvailable"));
        Log.info(bundledString("console-ApplicationNeedsUpdate"));
    }

    void displayDialogDownload(MouseListener listener) {

        if (ApplicationInstanceCounter.getCount() > 1) {
            GUIUtilities.displayWarningMessage(bundledString("CloseAllInstances"));
            return;
        }

        if (displayNewDownloadVersionMessage() == JOptionPane.YES_OPTION) {

            resetLabel(listener);
            worker = new SwingWorker("downloadUpdate") {

                @Override
                public Object construct() {

                    try {

                        updateLoader.setReleaseHub(releaseHub);
                        List<String> argsList = new ArrayList<>();
                        if (releaseHub)
                            argsList.add("useReleaseHub");
                        else if (ReddatabaseAPI.getHeadersWithToken() == null)
                            return Constants.WORKER_CANCEL;

                        String version = bundledString("Version") + "=" + updateLoader.getVersion();
                        argsList.add(version);

                        ApplicationContext instance = ApplicationContext.getInstance();
                        String repo = instance.getRepo();
                        if (!repo.isEmpty()) {
                            repo = "-repo=" + instance.getRepo();
                            argsList.add(repo);
                        }

                        if (!updateLoader.canDownload(true)) {
                            setDownloadNotifierInStatusBar();
                            return Constants.WORKER_CANCEL;
                        }

                        JProgressBar progressbar = new JProgressBar();
                        updateLoader.setProgressBar(progressbar);
                        statusBar().addComponent(progressbar, LABEL_INDEX);

                        updateLoader.downloadUpdate();
                        updateLoader.unzipLocale();

                        boolean restartNow = GUIUtilities.displayYesNoDialog(
                                bundledString("restart.message"),
                                bundledString("restart.message.title")
                        ) == JOptionPane.YES_OPTION;

                        argsList.add("-root=" + updateLoader.getRoot());
                        argsList.add("-launch=" + restartNow);

                        File file = new File("RedExpert.jar");
                        if (!file.exists())
                            file = new File("../RedExpert.jar");

                        String[] updaterArguments = new String[]{"java", "-cp", file.getPath(), "org.executequery.UpdateLoader"};
                        updaterArguments = (String[]) ArrayUtils.addAll(updaterArguments, argsList.toArray(new String[0]));

                        File outputLog = new File(ApplicationContext.getInstance().getUserSettingsHome() + System.getProperty("file.separator") + "updater.log");

                        ProcessBuilder updateProcessBuilder = new ProcessBuilder(updaterArguments);
                        updateProcessBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(outputLog));
                        updateProcessBuilder.redirectError(ProcessBuilder.Redirect.appendTo(outputLog));
                        ExecuteQuery.setShutdownHook(updateProcessBuilder);

                        if (restartNow)
                            ExecuteQuery.stop();
                        else
                            GUIUtilities.displayInformationMessage(bundledString("restart.message.postpone"));

                        return Constants.WORKER_SUCCESS;

                    } catch (Exception e) {
                        GUIUtilities.displayExceptionErrorDialog("Update error", e);
                        return Constants.WORKER_CANCEL;
                    }
                }
            };
            worker.start();
        }
    }

    private void resetLabel(MouseListener listener) {

        JLabel label = getUpdateNotificationLabel();

        label.setIcon(null);
        label.setToolTipText(null);
        if (listener != null)
            label.removeMouseListener(listener);

        statusBar().setThirdLabelText("");
    }

    private void setNotifierInStatusBar() {

        JLabel label = getUpdateNotificationLabel();

        label.addMouseListener(new NotificationLabelMouseAdapter());
        label.setIcon(new PulsatingCircle(label, 6));
        label.setToolTipText(newVersionAvailableText());

        statusBar().setThirdLabelText(bundledString("updateAvailable"));
    }

    private JLabel getUpdateNotificationLabel() {

        return statusBar().getLabel(LABEL_INDEX);
    }

    private StatusBarPanel statusBar() {

        return GUIUtilities.getStatusBar();
    }

    public String getBinaryUrl() {
        return repository.getBinaryZipUrl();
    }

    class NotificationLabelMouseAdapter extends MouseAdapter {

        public void mouseReleased(MouseEvent e) {

            resetLabel();

            int yesNo = displayNewVersionMessage();
            if (yesNo == JOptionPane.YES_OPTION) {

                worker = new SwingWorker("displayReleaseNotes") {

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

    private String newDownloadVersionMessage(ApplicationVersion version) {
        String repo = updateLoader.getRepo();
        if (MiscUtils.isNull(repo))
            if (releaseHub)
                repo = "http://builds.red-soft.biz/release_hub/red_expert/";
            else repo = "http://reddatabase.ru/downloads/redexpert/";
        return bundledString("downloadVersionMessage", version.getVersion(), repo);

    }

    private int displayNewDownloadVersionMessage() {
        return GUIUtilities.displayYesNoDialog(
                new SimpleHtmlContentPane(newDownloadVersionMessage(version)),
                bundledString("title"));
    }

    private Object doWork() {

        try {

            startupCheck();

            if (isNewVersion(version)) {

                logNewVersonInfo();

                closeProgressDialog();

                displayDialogDownload(null);

            } else {

                Log.info(bundledString("console-RedExpertUpToDate"));

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

    public void checkForUpdate(boolean monitorProgress) {

        this.monitorProgress = monitorProgress;
        worker = new SwingWorker("checkForUpdate") {

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
                bundledString("checkingUpdatesTitle"),
                bundledString("checkingUpdatesMessage"),
                this);
    }

    public class DownloadNotificationLabelMouseAdapter extends MouseAdapter {
        public void mouseReleased(MouseEvent e) {

            displayDialogDownload(this);
        }


    }

    private int displayNewVersionMessage() {

        return GUIUtilities.displayYesNoDialog(
                new SimpleHtmlContentPane(newVersionMessage(version)),
                bundledString("title"));
    }

    private void logNewVersonInfo() {

        Log.info(newVersionAvailableText());
    }

    private String newVersionAvailableText() {

        return bundledString("newVersionAvailableText", version.getVersion());
    }

    private boolean isNewVersion(ApplicationVersion version) {
        String currentVersion = System.getProperty("executequery.minor.version");

        return version.isNewerThan(currentVersion);
    }

    private ApplicationVersion getVersionInfo() {

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

            String link = repository().getReleaseNotesUrl();

            String releaseNotes = "";
            releaseNotes = JSONAPI.getJsonPropertyFromUrl(link, "body");

            closeProgressDialog();

            final String finalReleaseNotes = releaseNotes;
            GUIUtils.invokeAndWait(() -> new InformationDialog(bundledString("latestVersionInfoTitle"),
                    finalReleaseNotes, InformationDialog.TEXT_CONTENT_VALUE, null));

            return Constants.WORKER_SUCCESS;

        } catch (ApplicationException e) {

            showExceptionErrorDialog(e);

            return Constants.WORKER_FAIL;

        } catch (IOException e) {

            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);

            return Constants.WORKER_FAIL;

        } finally {

            GUIUtilities.showNormalCursor();
        }

    }

    private void createProgressDialogForReleaseNotesLoad() {

        GUIUtils.invokeLater(() -> {

            progressDialog = new InterruptibleProgressDialog(
                    GUIUtilities.getParentFrame(),
                    bundledString("checkingUpdatesTitle"),
                    bundledString("progressDialogForReleaseNotesLabel"),
                    CheckForUpdateNotifier.this);

            progressDialog.run();
        });
    }

    private void showExceptionErrorDialog(ApplicationException e) {

        GUIUtilities.showNormalCursor();
        GUIUtilities.displayExceptionErrorDialog(genericIOError(), e);
    }

    private String genericIOError() {

        return "An error occured trying to communicate " +
                " with the server";
    }

    private String newVersionMessage(ApplicationVersion version) {

        String repo = updateLoader.getRepo();
        if (MiscUtils.isNull(repo))
            if (releaseHub)
                repo = "http://builds.red-soft.biz/release_hub/red_expert/";
            else repo = "https://reddatabase.ru/downloads/redexpert/";
        return bundledString("newVersionMessage", version.getVersion(), repo, repo);
    }

    private String noUpdateMessage() {
        return bundledString("noUpdateMessage");
    }

    private String getCurrentBuild() {

        return System.getProperty("executequery.build");
    }

    private void closeProgressDialog() {

        if (progressDialog != null) {

            SwingUtilities.invokeLater(() -> {
                if (progressDialog.isVisible()) {

                    progressDialog.dispose();
                }
                progressDialog = null;
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

    protected String bundledString(String key) {
        return Bundles.get(this.getClass(), key);
    }

    protected String bundledString(String key, Object... args) {
        return Bundles.get(this.getClass(), key, args);
    }
}











