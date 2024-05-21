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
import org.executequery.gui.InformationDialog;
import org.executequery.http.JSONAPI;
import org.executequery.http.RemoteHttpClientException;
import org.executequery.http.spi.DefaultRemoteHttpClient;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.LatestVersionRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.UserProperties;
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
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks to see if a newer version of Execute Query is available.
 *
 * @author Takis Diakoumis
 */
@SuppressWarnings("WriteOnlyObject")
public class CheckForUpdateNotifier implements Interruptible {

    private static final int LABEL_INDEX = 2;

    private SwingWorker worker;
    private ApplicationVersion version;
    private UpdateLoader updateLoader = null;
    private InterruptibleProgressDialog progressDialog;

    private boolean checkUnstable = false;
    private boolean useReleaseHub = false;
    private boolean monitorProgress = false;
    private static boolean waitingForUpdate = false;
    private static boolean waitingForRestart = false;

    public void startupCheckForUpdate() {

        worker = new SwingWorker("startupCheckUpdate") {
            @Override
            public Object construct() {
                checkForUpdate();
                return Constants.WORKER_SUCCESS;
            }
        };

        worker.start();
    }

    public void forceCheckForUpdate(boolean monitorProgress) {
        this.monitorProgress = monitorProgress;

        if (waitingForRestart) {
            GUIUtilities.displayInformationMessage(bundledString("restart.message.postpone"));
            return;
        }

        progressDialog = new InterruptibleProgressDialog(
                GUIUtilities.getParentFrame(),
                bundledString("checkingUpdatesTitle"),
                null,
                this
        );

        worker = new SwingWorker("forceCheckForUpdate") {
            @Override
            public Object construct() {

                checkForUpdate();
                restoreProgressDialog();

                if (version == null)
                    return Constants.WORKER_CANCEL;

                if (isNewVersion(version)) {
                    displayDownloadDialog(null);

                } else if (monitorProgress)
                    GUIUtilities.displayInformationMessage(bundledString("RedExpertUpToDate"));

                return Constants.WORKER_SUCCESS;
            }
        };

        worker.start();
        if (monitorProgress)
            progressDialog.run();
    }

    private void checkForUpdate() {

        String repo = ApplicationContext.getInstance().getRepo();
        if (!repo.isEmpty()) {
            checkFromRepo(repo);
            return;
        }

        useReleaseHub = UserProperties.getInstance().getBooleanProperty("releasehub");
        checkUnstable = UserProperties.getInstance().getBooleanProperty("startup.unstableversions.load");

        if (useReleaseHub)
            checkFromReleaseHub();
        else
            checkFromReddatabase();
    }

    private void checkFromRepo(String repo) {
        Log.info(String.format(bundledString("CheckingForNewVersion"), repo));

        updateLoader = new UpdateLoader(repo);
        if (updateLoader.isNeedUpdate()) {
            version = new ApplicationVersion(updateLoader.getVersion());
            updateDownloadNotifier();

        } else {
            if (updateLoader.getVersion() != null)
                Log.info(bundledString("RedExpertUpToDate"));
        }
    }

    private void checkFromReddatabase() {
        Log.info(String.format(bundledString("CheckingForNewVersion"), "https://reddatabase.ru"));

        new DefaultRemoteHttpClient().setHttp("https");
        new DefaultRemoteHttpClient().setHttpPort(443);

        try {
            updateLoader = new UpdateLoader("");

            String url = UserProperties.getInstance().getStringProperty(checkUnstable ? "reddatabase.check.rc.url" : "reddatabase.check.url");
            version = new ApplicationVersion(JSONAPI.getJsonPropertyFromUrl(url, "version"));

            if (isNewVersion(version)) {
                updateLoader.setVersion(version.getVersion());
                Log.info(newVersionAvailableText());
                updateDownloadNotifier();

            } else
                Log.info(bundledString("RedExpertUpToDate"));

        } catch (RemoteHttpClientException | UnknownHostException e) {
            String message = bundledString("noInternetMessage");

            Log.error(message);
            if (monitorProgress)
                GUIUtilities.displayWarningMessage(message);

        } catch (IOException e) {
            String message = String.format(bundledString("ErrorCheckingForUpdate"), "https://reddatabase.ru");

            Log.error(message);
            Log.debug(e.getMessage(), e);
            if (monitorProgress)
                GUIUtilities.displayExceptionErrorDialog(message, e);
        }
    }

    private void checkFromReleaseHub() {
        Log.info(String.format(bundledString("CheckingForNewVersion"), "http://builds.red-soft.biz"));
        new DefaultRemoteHttpClient().setHttp("http");
        new DefaultRemoteHttpClient().setHttpPort(80);

        try {
            updateLoader = new UpdateLoader("");

            String url = "http://builds.red-soft.biz/api/v1/builds/latest/?project=red_expert" +
                    "&branch=" + SystemProperties.getProperty(Constants.SYSTEM_PROPERTIES_KEY, "branch")
                    + "&stage=0";

            version = new ApplicationVersion(JSONAPI.getJsonPropertyFromUrl(url, "version"));
            if (isNewVersion(version)) {
                updateLoader.setVersion(version.getVersion());
                updateDownloadNotifier();

            } else
                Log.info(bundledString("RedExpertUpToDate"));

        } catch (RemoteHttpClientException | UnknownHostException e) {
            String message = bundledString("noInternetMessage");

            Log.error(message);
            if (monitorProgress)
                GUIUtilities.displayWarningMessage(message);

        } catch (Exception e) {
            String message = String.format(bundledString("ErrorCheckingForUpdate"), "https://builds.red-soft.biz");

            Log.error(message);
            Log.debug(e.getMessage(), e);
            if (monitorProgress)
                GUIUtilities.displayExceptionErrorDialog(message, e);

            useReleaseHub = false;
            checkFromReddatabase();

        } finally {
            new DefaultRemoteHttpClient().setHttp("https");
            new DefaultRemoteHttpClient().setHttpPort(443);
        }
    }

    private void displayDownloadDialog(MouseListener listener) {

        if (displayNewVersionMessage("downloadVersionMessage") != JOptionPane.YES_OPTION)
            return;

        if (!updateLoader.canDownload(true))
            return;

        if (ApplicationInstanceCounter.getCount() > 1) {
            GUIUtilities.displayWarningMessage(bundledString("CloseAllInstances"));
            return;
        }

        resetDownloadNotifier(listener);
        worker = new SwingWorker("downloadUpdate") {
            @Override
            public Object construct() {
                startUpdate();
                return Constants.WORKER_SUCCESS;
            }
        };

        worker.start();
    }

    private void displayReleaseNotes() {

        progressDialog = new InterruptibleProgressDialog(
                GUIUtilities.getParentFrame(),
                bundledString("checkingUpdatesTitle"),
                bundledString("progressDialogForReleaseNotesLabel"),
                this
        );

        worker = new SwingWorker("displayReleaseNotes") {
            @Override
            public Object construct() {
                try {

                    LatestVersionRepository repository = (LatestVersionRepository) RepositoryCache.load(LatestVersionRepository.REPOSITORY_ID);
                    if (repository == null)
                        return Constants.WORKER_CANCEL;

                    String releaseNotes = JSONAPI.getJsonPropertyFromUrl(repository.getReleaseNotesUrl(), "body");
                    restoreProgressDialog();

                    GUIUtils.invokeAndWait(() -> new InformationDialog(
                            bundledString("latestVersionInfoTitle"),
                            releaseNotes,
                            InformationDialog.TEXT_CONTENT_VALUE,
                            null
                    ));

                } catch (Exception e) {
                    restoreProgressDialog();
                    GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
                    return Constants.WORKER_FAIL;
                }

                return Constants.WORKER_SUCCESS;
            }
        };

        worker.start();
        progressDialog.run();
    }

    private int displayNewVersionMessage(String key) {

        String repo = updateLoader.getRepo();
        if (MiscUtils.isNull(repo))
            repo = useReleaseHub ? "http://builds.red-soft.biz" : "http://reddatabase.ru";

        return GUIUtilities.displayYesNoDialog(
                new SimpleHtmlContentPane(bundledString(key, version.getVersion(), repo)),
                bundledString("title")
        );
    }

    private void startUpdate() {

        try {

            List<String> argsList = new ArrayList<>();
            if (useReleaseHub)
                argsList.add("useReleaseHub");

            String version = bundledString("Version") + "=" + updateLoader.getVersion();
            argsList.add(version);

            String repo = ApplicationContext.getInstance().getRepo();
            if (!repo.isEmpty())
                argsList.add("-repo=" + repo);

            JProgressBar progressbar = new JProgressBar();
            GUIUtilities.getStatusBar().addComponent(progressbar, LABEL_INDEX);

            updateLoader.setProgressBar(progressbar);
            updateLoader.setReleaseHub(useReleaseHub);
            updateLoader.downloadUpdate();
            updateLoader.unzipLocale();
            waitingForRestart = true;

            boolean restartNow = GUIUtilities.displayYesNoDialog(
                    bundledString("restart.message"),
                    bundledString("restart.message.title")
            ) == JOptionPane.YES_OPTION;

            argsList.add("-root=" + updateLoader.getRoot());
            argsList.add("-launch=" + restartNow);

            File file = new File("RedExpert.jar");
            if (!file.exists())
                file = new File("../RedExpert.jar");

            String javaPath = "java";
            if (!System.getProperty("os.name").toLowerCase().contains("win"))
                javaPath = System.getProperty("java.home") + "/bin/java";

            String[] updaterArguments = new String[]{javaPath, "-cp", file.getPath(), "org.executequery.UpdateLoader"};
            updaterArguments = (String[]) ArrayUtils.addAll(updaterArguments, argsList.toArray(new String[0]));

            String logFilePath = ApplicationContext.getInstance().getUserSettingsHome()
                    + FileSystems.getDefault().getSeparator()
                    + "updater.log";

            File outputLog = new File(logFilePath);
            ProcessBuilder updateProcessBuilder = new ProcessBuilder(updaterArguments);
            updateProcessBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(outputLog));
            updateProcessBuilder.redirectError(ProcessBuilder.Redirect.appendTo(outputLog));

            ExecuteQuery.setShutdownHook(updateProcessBuilder);
            if (restartNow)
                ExecuteQuery.stop();
            else
                GUIUtilities.displayInformationMessage(bundledString("restart.message.postpone"));

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog("Update error", e);
        }
    }

    private void updateDownloadNotifier() {

        if (waitingForUpdate)
            return;

        JLabel label = GUIUtilities.getStatusBar().getLabel(LABEL_INDEX);
        label.addMouseListener(new DownloadNotifierMouseAdapter());
        label.setIcon(GUIUtilities.loadIcon("YellowBallAnimated16.gif"));
        label.setToolTipText(newVersionAvailableText());

        GUIUtilities.getStatusBar().setThirdLabelText(bundledString("updateAvailable"));
        Log.info(bundledString("ApplicationNeedsUpdate"));

        waitingForUpdate = true;
    }

    private void resetDownloadNotifier(MouseListener listener) {

        JLabel label = GUIUtilities.getStatusBar().getLabel(LABEL_INDEX);
        label.setIcon(null);
        label.setToolTipText(null);
        if (listener != null)
            label.removeMouseListener(listener);

        GUIUtilities.getStatusBar().setThirdLabelText("");
        waitingForUpdate = false;
    }

    private String newVersionAvailableText() {
        return bundledString("newVersionAvailableText", version.getVersion());
    }

    private boolean isNewVersion(ApplicationVersion version) {
        return version != null && version.isNewerThan(System.getProperty("executequery.minor.version"));
    }

    private void restoreProgressDialog() {

        if (progressDialog == null)
            return;

        progressDialog.dispose();
        progressDialog = null;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        interrupt();
    }

    @Override
    public void interrupt() {
        if (worker != null)
            worker.interrupt();
    }

    private class DownloadNotifierMouseAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {

            if (version.getTagValue() == ApplicationVersion.RELEASE)
                if (displayNewVersionMessage("newVersionMessage") == JOptionPane.YES_OPTION)
                    displayReleaseNotes();

            displayDownloadDialog(this);
        }
    }

    private String bundledString(String key) {
        return Bundles.get(this.getClass(), key);
    }

    private String bundledString(String key, Object... args) {
        return Bundles.get(this.getClass(), key, args);
    }

}
