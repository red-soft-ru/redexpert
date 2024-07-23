/*
 * FeedbackCommand.java
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

package org.executequery.actions.helpcommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.othercommands.AbstractBaseCommand;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.FeedbackPanel;

import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Command to open the feedback dialog.
 *
 * @author Takis Diakoumis
 */
public class FeedbackCommand extends AbstractBaseCommand {

    @Override
    public void execute(ActionEvent e) {
        feedback();
    }

    public final void feedback() {
        showFeedbackDialog();
    }

    public final void bugReport(String message, Vector<Throwable> throwableVector, Class<?> sourceClass) {
        showBugReportDialog(message, throwableVector, sourceClass);
    }

    private void showFeedbackDialog() {
        GUIUtilities.showWaitCursor();
        try {
            BaseDialog dialog = new BaseDialog(FeedbackPanel.DEFAULT_TITLE, true, true);
            FeedbackPanel panel = new FeedbackPanel(dialog);

            dialog.addDisplayComponent(panel);
            dialog.display();

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    private void showBugReportDialog(String message, Vector<Throwable> throwableVector, Class<?> sourceClass) {
        GUIUtilities.showWaitCursor();
        try {
            BaseDialog dialog = new BaseDialog(FeedbackPanel.BUG_REPORT_TITLE, true, true);
            FeedbackPanel panel = new FeedbackPanel(dialog, message, throwableVector, sourceClass);

            dialog.addDisplayComponent(panel);
            dialog.display();

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

}
