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

package org.executequery.update;

import org.executequery.*;
import org.executequery.gui.IconManager;
import org.executequery.gui.InformationDialog;
import org.executequery.http.JSONAPI;
import org.executequery.http.RemoteHttpClientException;
import org.executequery.http.spi.DefaultRemoteHttpClient;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Checks to see if a newer version of Execute Query is available.
 *
 * @author Takis Diakoumis
 */
@SuppressWarnings("WriteOnlyObject")
public class CheckForUpdateNotifier implements Interruptible {
    private static final int LABEL_INDEX = 2;

    private static final int CHECK_FINISH = 0;
    private static final int CHECK_CONTINUE = CHECK_FINISH + 1;
    private static final int CHECK_FAIL = CHECK_CONTINUE + 1;

    private static final String UP_TO_DATE = bundledString("RedExpertUpToDate");
    private static final String ERROR_CHECKING_KEY = "ErrorCheckingForUpdate";
    private static final String CHECK_VERSION_KEY = "CheckingForNewVersion";
    private static final String NO_INTERNET_KEY = "noInternetMessage";

    private SwingWorker worker;
    private UpdateSource updateSource;
    private ApplicationVersion version;
    private UpdateLoader updateLoader = null;
    private InterruptibleProgressDialog progressDialog;

    private boolean useNewApi = true;
    private boolean checkUnstable = false;
    private boolean useReleaseHub = false;
    private boolean useHttpsProtocol = true;
    private boolean monitorProgress = false;
    private static boolean waitingForUpdate = false;
    private static boolean waitingForRestart = false;

    // --- update check running ---

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

        if (isWaitingForRestart()) {
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

                if (version.hasUpdate()) {
                    displayDownloadDialog(null);

                } else if (monitorProgress)
                    GUIUtilities.displayInformationMessage(UP_TO_DATE);

                return Constants.WORKER_SUCCESS;
            }
        };

        worker.start();
        if (monitorProgress)
            progressDialog.run();
    }

    // --- update checking ---

    private void checkForUpdate() {

        String repo = ApplicationContext.getInstance().getRepo();
        if (!repo.isEmpty()) {
            checkFromRepo(repo);
            return;
        }

        useNewApi = true;
        useHttpsProtocol = UserProperties.getInstance().getBooleanProperty("update.use.https");
        useReleaseHub = UserProperties.getInstance().getBooleanProperty("update.use.releasehub");
        checkUnstable = UserProperties.getInstance().getBooleanProperty("startup.unstableversions.load");

        // --- check from the builds.red-soft.biz ---

        int lastCheck = CHECK_CONTINUE;
        if (useReleaseHub)
            lastCheck = checkFromReleaseHub();

        // --- check from the rdb.red-soft.ru ---

        if (lastCheck == CHECK_CONTINUE)
            lastCheck = checkFromWebsite();

        // --- check from the reddatabase.ru ---

        if (lastCheck == CHECK_CONTINUE)
            lastCheck = checkFromReddatabase();

        // --- return result of the check ---

        if (lastCheck == CHECK_FINISH) {
            updateLoader = new UpdateLoader("");
            updateLoader.setVersion(version.toString());
            updateLoader.setDownloadLink(useReleaseHub || useNewApi ? updateSource.getDownloadUrl() : getDownloadLink());

            Log.info(bundledString("newVersionAvailableText", version));
            updateDownloadNotifier();

        } else if (lastCheck == CHECK_CONTINUE)
            Log.info(UP_TO_DATE);
    }

    private void checkFromRepo(String repo) {
        Log.info(bundledString(CHECK_VERSION_KEY, repo));

        updateLoader = new UpdateLoader(repo);
        if (updateLoader.isNeedUpdate()) {
            version = new ApplicationVersion(updateLoader.getVersion());
            updateDownloadNotifier();

        } else {
            if (updateLoader.getVersion() != null)
                Log.info(UP_TO_DATE);
        }
    }

    private int checkFromReleaseHub() {
        Log.info(bundledString(CHECK_VERSION_KEY, "builds.red-soft.biz"));

        try {
            updateSource = UpdateSource.load(true, false);
            if (updateSource.canUpdate()) {
                version = updateSource.getVersion();
                return CHECK_FINISH;
            }

        } catch (RemoteHttpClientException e) {
            handleException(e, bundledString(NO_INTERNET_KEY), false);
            useReleaseHub = false;
            return CHECK_FAIL;
        }

        return CHECK_CONTINUE;
    }

    private int checkFromWebsite() {
        Log.info(bundledString(CHECK_VERSION_KEY, "rdb.red-soft.ru"));

        try {
            updateSource = UpdateSource.load(false, checkUnstable);
            if (updateSource.canUpdate()) {
                version = updateSource.getVersion();
                return CHECK_FINISH;
            }

        } catch (RemoteHttpClientException e) {
            handleException(e, bundledString(NO_INTERNET_KEY), false);
            useNewApi = false;
            return CHECK_FAIL;
        }

        return CHECK_CONTINUE;
    }

    /**
     * @deprecated will be removed when new website comes up
     */
    @Deprecated
    private int checkFromReddatabase() {
        String url = UserProperties.getInstance().getStringProperty(
                checkUnstable ? "reddatabase.check.rc.url" : "reddatabase.check.url"
        );

        try {
            Log.info(bundledString(CHECK_VERSION_KEY, "reddatabase.ru"));

            new DefaultRemoteHttpClient().setHttp(useHttpsProtocol ? "https" : "http");
            new DefaultRemoteHttpClient().setHttpPort(443);

            version = new ApplicationVersion(JSONAPI.getJsonPropertyFromUrl(url, "version"));
            return version.hasUpdate() ? CHECK_FINISH : CHECK_CONTINUE;

        } catch (RemoteHttpClientException | UnknownHostException e) {
            handleException(e, bundledString(NO_INTERNET_KEY), false);

        } catch (IOException e) {
            handleException(e, bundledString(ERROR_CHECKING_KEY, "https://reddatabase.ru"), true);
        }

        return CHECK_FAIL;
    }

    // ---

    private void handleException(Exception e, String message, boolean error) {
        Log.error(message);
        Log.debug(e.getMessage(), e);

        if (monitorProgress) {
            if (error)
                GUIUtilities.displayExceptionErrorDialog(message, e, CheckForUpdateNotifier.class);
            else
                GUIUtilities.displayWarningMessage(message);
        }
    }

    // ---

    private void startUpdate() {
        try {
            JProgressBar progressbar = new JProgressBar();
            GUIUtilities.getStatusBar().addComponent(progressbar, LABEL_INDEX);

            updateLoader.setProgressBar(progressbar);
            updateLoader.setReleaseHub(useReleaseHub);
            updateLoader.downloadUpdate();
            updateLoader.unzipLocale();
            setWaitingForRestart(true);

            boolean restartNow = GUIUtilities.displayYesNoDialog(
                    bundledString("restart.message"),
                    bundledString("restart.message.title")
            ) == JOptionPane.YES_OPTION;

            scheduleUpdate(buildArgumentsArray(restartNow), getUpdateLogFile());

            if (restartNow)
                ExecuteQuery.stop();
            else
                GUIUtilities.displayInformationMessage(bundledString("restart.message.postpone"));

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog("Update error", e, this.getClass());
            GUIUtilities.getStatusBar().reset();
            updateDownloadNotifier();
        }
    }

    private void scheduleUpdate(String[] updaterArguments, File outputLog) {

        ProcessBuilder updateProcessBuilder = new ProcessBuilder(updaterArguments);
        updateProcessBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(outputLog));
        updateProcessBuilder.redirectError(ProcessBuilder.Redirect.appendTo(outputLog));

        ExecuteQuery.setShutdownHook(updateProcessBuilder);
    }

    private String[] buildArgumentsArray(boolean restartNow) throws URISyntaxException, FileNotFoundException {

        String javaPath = "java";
        if (!System.getProperty("os.name").toLowerCase().contains("win"))
            javaPath = System.getProperty("java.home") + "/bin/java";

        File jarFile = new File(CheckForUpdateNotifier.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (!jarFile.exists())
            throw new FileNotFoundException("Couldn't locale application JAR file (RedExpert.jar)");

        // ---

        List<String> argsList = new ArrayList<>();
        argsList.add(javaPath);
        argsList.add("-cp");
        argsList.add(jarFile.getPath());
        argsList.add("org.executequery.UpdateLoader");

        String repo = ApplicationContext.getInstance().getRepo();
        if (!MiscUtils.isNull(repo))
            argsList.add("-repo=" + repo);

        argsList.add("-version=" + updateLoader.getVersion());
        argsList.add("-root=" + updateLoader.getRoot());
        argsList.add("-release-hub=" + useReleaseHub);
        argsList.add("-launch=" + restartNow);

        return argsList.toArray(new String[0]);
    }

    // ---

    /**
     * @deprecated will be removed when new website comes up
     */
    @Deprecated
    private String getDownloadLink() {
        try {
            //изменить эту строку в соответствии с форматом имени файла на сайте
            String filename = UserProperties.getInstance().getStringProperty("reddatabase.filename") + version + ".zip";
            String prop = UserProperties.getInstance().getStringProperty("reddatabase.get-files.url");
            String url = Objects.requireNonNull(JSONAPI.getJsonObjectFromArray(
                    JSONAPI.getJsonArray(prop + version),
                    "filename", filename)).getString("url");

            return JSONAPI.getJsonPropertyFromUrl(url + "genlink/", "link");

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
            return null;
        }
    }

    private void displayDownloadDialog(MouseListener listener) {

        boolean denyUpdateDownload = GUIUtilities.displayYesNoDialog(
                bundledString("downloadVersionMessage", version),
                bundledString("title")
        ) != JOptionPane.YES_OPTION;

        if (denyUpdateDownload)
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

    private void updateDownloadNotifier() {

        if (isWaitingForUpdate())
            return;

        JLabel label = GUIUtilities.getStatusBar().getLabel(LABEL_INDEX);
        label.addMouseListener(new DownloadNotifierMouseAdapter());
        label.setIcon(IconManager.getIcon("icon_notification"));
        label.setToolTipText(bundledString("newVersionAvailableText", version));

        GUIUtilities.getStatusBar().setThirdLabelText(bundledString("updateAvailable"));
        Log.info(bundledString("ApplicationNeedsUpdate"));

        setWaitingForUpdate(true);
    }

    private void resetDownloadNotifier(MouseListener listener) {

        JLabel label = GUIUtilities.getStatusBar().getLabel(LABEL_INDEX);
        label.setIcon(null);
        label.setToolTipText(null);
        if (listener != null)
            label.removeMouseListener(listener);

        GUIUtilities.getStatusBar().setThirdLabelText("");
        setWaitingForUpdate(false);
    }

    // ---

    private static File getUpdateLogFile() {
        return Paths.get(ApplicationContext.getInstance().getUserSettingsHome(), "updater.log").toFile();
    }

    private void restoreProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dispose();
            progressDialog = null;
        }
    }

    // ---

    public static boolean isWaitingForUpdate() {
        return waitingForUpdate;
    }

    public static boolean isWaitingForRestart() {
        return waitingForRestart;
    }

    public static void setWaitingForUpdate(boolean waitingForUpdate) {
        CheckForUpdateNotifier.waitingForUpdate = waitingForUpdate;
    }

    public static void setWaitingForRestart(boolean waitingForRestart) {
        CheckForUpdateNotifier.waitingForRestart = waitingForRestart;
    }

    private static String bundledString(String key, Object... args) {
        return Bundles.get(CheckForUpdateNotifier.class, key, args);
    }

    // --- Interruptible impl ---

    @Override
    public void setCancelled(boolean cancelled) {
        interrupt();
    }

    @Override
    public void interrupt() {
        if (worker != null)
            worker.interrupt();
    }

    // ---

    private class DownloadNotifierMouseAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!version.isSnapshot())
                displayReleaseNotes();
            displayDownloadDialog(this);
        }

        private void displayReleaseNotes() {

            boolean denyShowChangelog = GUIUtilities.displayYesNoDialog(
                    bundledString("newVersionMessage", version),
                    bundledString("title")
            ) != JOptionPane.YES_OPTION;

            if (denyShowChangelog)
                return;

            progressDialog = new InterruptibleProgressDialog(
                    GUIUtilities.getParentFrame(),
                    bundledString("checkingUpdatesTitle"),
                    bundledString("progressDialogForReleaseNotesLabel"),
                    CheckForUpdateNotifier.this
            );

            worker = new SwingWorker("displayReleaseNotes") {
                @Override
                public Object construct() {
                    try {
                        restoreProgressDialog();
                        GUIUtils.invokeAndWait(() ->
                                new InformationDialog(
                                        bundledString("latestVersionInfoTitle"),
                                        getChangelog(),
                                        InformationDialog.TEXT_CONTENT_VALUE,
                                        null
                                ).display()
                        );

                    } catch (Exception e) {
                        restoreProgressDialog();
                        GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e, this.getClass());
                        return Constants.WORKER_FAIL;
                    }

                    return Constants.WORKER_SUCCESS;
                }
            };

            worker.start();
            progressDialog.run();
        }

        private String getChangelog() {
            try {
                if (useNewApi) {
                    String language = SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "startup.display.language");
                    return updateSource.getChangelog(language);
                }

                String url = SystemProperties.getProperty(Constants.SYSTEM_PROPERTIES_KEY, "check.version.notes.url");
                return JSONAPI.getJsonPropertyFromUrl(url, "body");

            } catch (IOException e) {
                Log.error(e.getMessage(), e);
                return null;
            }
        }

    } // DownloadNotifierMouseAdapter class

}
