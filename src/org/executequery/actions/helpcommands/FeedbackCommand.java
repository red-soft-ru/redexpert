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

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.executequery.GUIUtilities;
import org.executequery.actions.othercommands.AbstractBaseCommand;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.FeedbackPanel;
import org.executequery.log.Log;

/** 
 * Command to open the feedback dialog.
 *
 * @author   Takis Diakoumis
 */
public class FeedbackCommand extends AbstractBaseCommand {
    
    public void execute(ActionEvent e) {

        String actionCommand = e.getActionCommand();
        try {

            Method method = getClass().getMethod(
                    actionCommand, new Class[]{ActionEvent.class});
            
            method.invoke(this, e);

        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException 
                | IllegalAccessException | InvocationTargetException e1) {
            
            handleException(e1);
        }
        
    }

    private void handleException(Throwable e) {

        Log.error(bundledString("errorExecutingFeedbackCommand"), e);
    }

    public void featureRequest(ActionEvent e) {
        
        showDialog(FeedbackPanel.FEATURE_REQUEST, bundledString("featureRequest"));
    }

    public void userComments(ActionEvent e) {
        
        showDialog(FeedbackPanel.USER_COMMENTS, bundledString("userComments"));
    }

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

