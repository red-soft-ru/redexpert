/*
 * DatabaseObjectChangeProvider.java
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

package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.DatabaseTableObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.InterruptibleProgressDialog;
import org.underworldlabs.swing.util.Interruptible;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import java.text.ParseException;

public class DatabaseObjectChangeProvider implements Interruptible {

    private SwingWorker worker;

    private NamedObject namedObject;

    private InterruptibleProgressDialog interruptibleProgressDialog;

    boolean applied = false;
    int lastOption;

    public DatabaseObjectChangeProvider(NamedObject namedObject) {

        this.namedObject = namedObject;
    }

    public boolean applyChanges() {

        return applyChanges(false);
    }

    public boolean applyDataChanges() {
        executeApplyDataChanges();
        return applied;
    }

    public boolean applyDefinitionChanges() {
        executeApplyDefinitionChanges();
        return applied;
    }

    public boolean applyChanges(boolean showDialog) {
        return applyChanges(showDialog, true);
    }

    public boolean applyChanges(boolean showDialog, boolean showCancelButton) {

        if (isTable() && table().isAltered()) {
            if (showDialog)
                return apply(showCancelButton);
            else
                execute();
        }

        return true;
    }

    private boolean apply(boolean showCancelButton) {

        int yesNo = showCancelButton ?
                GUIUtilities.displayConfirmCancelDialog(Bundles.getCommon("message.apply-changes")) :
                GUIUtilities.displayConfirmDialog(Bundles.getCommon("message.apply-changes"));

        lastOption = yesNo;

        if (yesNo == JOptionPane.NO_OPTION) {
            table().revert();

        } else if (yesNo == JOptionPane.CANCEL_OPTION) {
            return false;

        } else if (yesNo == JOptionPane.YES_OPTION) {
            execute();
        }

        return true;
    }

    public void interrupt() {

        if (worker != null) {

            table().cancelChanges();
            worker.interrupt();
        }

    }

    public void setCancelled(boolean cancelled) {
    }

    private void execute() {

        interruptibleProgressDialog = new InterruptibleProgressDialog(GUIUtilities.getParentFrame(), "Applying changes", "Please wait...", this);

        worker = new SwingWorker("applyChangesIn "+table()!=null?table().getName():"") {

            @Override
            public Object construct() {

                try {

                    table().applyChanges();
                    applied = true;

                } catch (DataSourceException e) {
                    applied = false;

                    StringBuilder sb = new StringBuilder();
                    sb.append("An error occurred applying the specified changes.\n\nThe system returned:\n");

                    Throwable cause = e.getCause();
                    if (cause instanceof NumberFormatException) {

                        sb.append("Invalid number for value - ");

                    } else if (cause instanceof ParseException) {

                        sb.append("Invalid date format for value - ");
                    }
                    sb.append(e.getExtendedMessage());

                    if (tableObject().hasTableDataChanges()) {

                        sb.append("\nRollback was issued for all data changes.");
                    }

                    if (table().hasTableDefinitionChanges()) { // for ddl changes in tx 

                        table().cancelChanges();
                        sb.append("\nRollback was issued for all changes.");
                    }

                    dispose();
                    GUIUtilities.displayExceptionErrorDialog(sb.toString(), e, this.getClass());
                }

                return "done";
            }

            @Override
            public void finished() {

                dispose();
            }

        };

        worker.start();
        interruptibleProgressDialog.run();
    }

    private void executeApplyDataChanges() {

        interruptibleProgressDialog = new InterruptibleProgressDialog(GUIUtilities.getParentFrame(), "Applying changes", "Please wait...", this);

        worker = new SwingWorker("applyChangesIn "+tableObject()!=null?tableObject().getName():"") {

            @Override
            public Object construct() {

                try {

                    tableObject().applyTableDataChanges();
                    applied = true;

                } catch (DataSourceException e) {
                    applied = false;

                    StringBuilder sb = new StringBuilder();
                    sb.append("An error occurred applying the specified changes.\n\nThe system returned:\n");

                    Throwable cause = e.getCause();
                    if (cause instanceof NumberFormatException) {

                        sb.append("Invalid number for value - ");

                    } else if (cause instanceof ParseException) {

                        sb.append("Invalid date format for value - ");
                    }
                    sb.append(e.getExtendedMessage());

                    if (tableObject().hasTableDataChanges()) {

                        sb.append("\nRollback was issued for all data changes.");
                    }

                    dispose();
                    GUIUtilities.displayExceptionErrorDialog(sb.toString(), e, this.getClass());
                }

                return "done";
            }

            @Override
            public void finished() {

                dispose();
            }

        };

        worker.start();
        interruptibleProgressDialog.run();
    }

    private void executeApplyDefinitionChanges() {

        interruptibleProgressDialog = new InterruptibleProgressDialog(GUIUtilities.getParentFrame(), "Applying changes", "Please wait...", this);

        worker = new SwingWorker("applyChangesIn "+table()!=null?table().getName():"") {

            @Override
            public Object construct() {

                try {

                    table().applyTableDefinitionChanges();
                    applied = true;

                } catch (DataSourceException e) {
                    applied = false;

                    StringBuilder sb = new StringBuilder();
                    sb.append("An error occurred applying the specified changes.\n\nThe system returned:\n");

                    Throwable cause = e.getCause();
                    if (cause instanceof NumberFormatException) {

                        sb.append("Invalid number for value - ");

                    } else if (cause instanceof ParseException) {

                        sb.append("Invalid date format for value - ");
                    }
                    sb.append(e.getExtendedMessage());

                    if (tableObject().hasTableDataChanges()) {

                        sb.append("\nRollback was issued for all data changes.");
                    }

                    dispose();
                    GUIUtilities.displayExceptionErrorDialog(sb.toString(), e, this.getClass());
                }

                return "done";
            }

            @Override
            public void finished() {

                dispose();
            }

        };

        worker.start();
        interruptibleProgressDialog.run();
    }

    private void dispose() {

        if (interruptibleProgressDialog != null) {

            interruptibleProgressDialog.dispose();
            interruptibleProgressDialog = null;
        }
    }

    public boolean isApplied() {
        return applied;
    }

    private boolean isTable() {

        return (table() != null);

    }

    private DatabaseTable table() {

        if (namedObject instanceof DatabaseTable) {

            return (DatabaseTable) namedObject;
        }
        return null;
    }

    private DatabaseTableObject tableObject() {

        if (namedObject instanceof DatabaseTableObject) {

            return (DatabaseTableObject) namedObject;
        }
        return null;
    }


}


