/*
 * ApplicationLogsCommand.java
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
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ApplicationLogsCommand extends ReflectiveAction
                                    implements BaseCommand {

    public void execute(ActionEvent e) {

        actionPerformed(e);
    }

    public void resetAllLogs(ActionEvent e) {

        String message = "Are you sure you want to reset ALL system logs?";

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

        String message = "Are you sure you want to reset the ";

        switch (type) {

            case LogRepository.ACTIVITY:
                message += "system activity log?";
                break;

            case LogRepository.EXPORT:
                message += "data export log?";
                break;

            case LogRepository.IMPORT:
                message += "data import log?";
                break;

        }

        return confirmReset(message);
    }

    private boolean confirmReset(String message) {

        return GUIUtilities.displayConfirmDialog(message) == JOptionPane.YES_OPTION;
    }

}










