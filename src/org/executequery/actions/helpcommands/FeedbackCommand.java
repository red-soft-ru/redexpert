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
import org.executequery.log.Log;

import java.awt.event.ActionEvent;

/**
 * Command to open the feedback dialog.
 *
 * @author Takis Diakoumis
 */
public class FeedbackCommand extends AbstractBaseCommand {

    @Override
    public void execute(ActionEvent e) {

        try {
            getClass().getMethod(e.getActionCommand(), ActionEvent.class).invoke(this, e);

        } catch (Exception exception) {
            Log.error(bundledString("errorExecutingFeedbackCommand"), exception);
        }
    }

    @SuppressWarnings("unused")
    public void featureRequest(ActionEvent e) {
        showDialog(FeedbackPanel.FEATURE_REQUEST, bundledString("featureRequest"));
    }

    @SuppressWarnings("unused")
    public void userComments(ActionEvent e) {
        showDialog(FeedbackPanel.USER_COMMENTS, bundledString("userComments"));
    }

    @SuppressWarnings("unused")
    public void bugReport(ActionEvent e) {
        showDialog(FeedbackPanel.BUG_REPORT, bundledString("reportBug"));
    }

    private void showDialog(int type, String title) {

        GUIUtilities.showWaitCursor();
        try {
            BaseDialog dialog = new BaseDialog(title, true, true);
            FeedbackPanel panel = new FeedbackPanel(dialog, type);

            dialog.addDisplayComponent(panel);
            dialog.display();

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

}
