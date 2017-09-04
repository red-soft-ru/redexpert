/*
 * ApplicationLogsCommand.java
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

package org.executequery.actions.toolscommands;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.executequery.GUIUtilities;
import org.executequery.gui.SystemLogsViewer;
import org.executequery.repository.LogRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.swing.actions.BaseCommand;
import org.underworldlabs.swing.actions.ReflectiveAction;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ApplicationLogsCommand extends ReflectiveAction implements BaseCommand {

    public void execute(ActionEvent e) {

        actionPerformed(e);
    }

    public void resetAllLogs(ActionEvent e) {

        String message = bundledString("messageResetAll");
        if (confirmReset(message)) {

            logRepository().resetAll();
        }

    }

    public void resetSystemLog(ActionEvent e) {

        reset(LogRepository.ACTIVITY);
    }

    public void resetImportLog(ActionEvent e) {

        reset(LogRepository.IMPORT);
    }

    public void resetExportLog(ActionEvent e) {

        reset(LogRepository.EXPORT);
    }

    public void viewSystemLog(ActionEvent e) {

        viewLog(LogRepository.ACTIVITY);
    }

    public void viewImportLog(ActionEvent e) {

        viewLog(LogRepository.IMPORT);
    }

    public void viewExportLog(ActionEvent e) {

        viewLog(LogRepository.EXPORT);
    }

    protected void viewLog(final int type) {

        ThreadUtils.invokeLater(new Runnable() {

            public void run() {

                if (isLogViewerOpen()) {

                    logViewer().setSelectedLog(type);

                } else {

                    GUIUtilities.addCentralPane(SystemLogsViewer.TITLE,
                                                SystemLogsViewer.FRAME_ICON,
                                                new SystemLogsViewer(type),
                                                null,
                                                true);
                }
            }

        });
    }

    private SystemLogsViewer logViewer() {

        return (SystemLogsViewer)GUIUtilities.getCentralPane(SystemLogsViewer.TITLE);
    }

    private boolean isLogViewerOpen() {

        return (GUIUtilities.getCentralPane(SystemLogsViewer.TITLE) != null);
    }

    private LogRepository logRepository() {

        return (LogRepository) RepositoryCache.load(LogRepository.REPOSITORY_ID);
    }

    private void reset(int type) {

        if (resetLogConfirmed(type)) {

            logRepository().reset(type);

            if (type == LogRepository.ACTIVITY) {

                GUIUtilities.clearSystemOutputPanel();
            }

        }

    }

    private boolean resetLogConfirmed(int type) {

        String message = bundledString("messageReset");

        switch (type) {

            case LogRepository.ACTIVITY:
                message += bundledString("systemActivityLog");
                break;

            case LogRepository.EXPORT:
                message += bundledString("dataExportLog");
                break;

            case LogRepository.IMPORT:
                message += bundledString("dataImportLog");
                break;

        }

        return confirmReset(message);
    }

    private boolean confirmReset(String message) {

        return GUIUtilities.displayConfirmDialog(message) == JOptionPane.YES_OPTION;
    }

}

