/*
 * ErdPrintableDialog.java
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

package org.executequery.gui.erd;

import org.executequery.GUIUtilities;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * A non-modal dialog containing an <code>SQLTextAreal</code>
 * object. This base class provides the functionality as
 * indicated within the <code>TextEditor</code> interface
 * including cut/copy/paste, save and print functions.
 *
 * @author Takis Diakoumis
 */
public class ErdPrintableDialog extends BaseDialog
        implements TextEditorContainer {

    /**
     * The SQL text panel
     */
    protected SimpleSqlTextPanel sqlText;

    /**
     * <p>Constructs a new instance with the specified name.
     *
     * @param name name of this dialog
     */
    public ErdPrintableDialog(String name) {
        super(name, false);
        sqlText = new SimpleSqlTextPanel();

        GUIUtilities.setFocusedDialog(this);

        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                dialogFocusChanged(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                dialogFocusChanged(false);
            }
        });
    }

    /**
     * <p>Constructs a new instance with the specified name
     * and whether the SQL text panel should be created.
     *
     * @param name           name of this dialog
     * @param createSQLPanel to create the SQL text panel
     */
    public ErdPrintableDialog(String name, boolean createSQLPanel) {
        super(name, false);

        if (createSQLPanel) {
            sqlText = new SimpleSqlTextPanel();
        }

        GUIUtilities.setFocusedDialog(this);

        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                dialogFocusChanged(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                dialogFocusChanged(false);
            }
        });
    }

    /**
     * <p>Called for a change in focus as specified. This
     * method will pass this object into <code>GUIUtilities</code>
     * methods <code>setFocusedDialog(JDialog)</code> and
     * <code>removeFocusedDialog(JDialog)</code> depending on
     * the focus parameter specified.
     *
     * @param hasFocus this dialog has focus
     */
    private void dialogFocusChanged(boolean hasFocus) {

        if (hasFocus)
            GUIUtilities.setFocusedDialog(this);
        else
            GUIUtilities.removeFocusedDialog(this);

    }

    /**
     * <p>Simple call to make this dialog visible.
     */
    @Override
    public void display() {
        pack();
        this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
    }

    /**
     * <p>Removes this dialog from the application
     * controller <code>GUIUtilities</code> object before
     * a call to <code>super.dispose()</code>.
     */
    @Override
    public void dispose() {
        GUIUtilities.removeFocusedDialog(this);
        super.dispose();
    }

    public String getDisplayName() {
        return "";
    }

    // ------------------------------------------------
    // ----- TextEditorContainer implementations ------
    // ------------------------------------------------

    /**
     * Returns the SQL text pane as the TextEditor component
     * that this container holds.
     */
    @Override
    public TextEditor getTextEditor() {
        return sqlText;
    }

}
