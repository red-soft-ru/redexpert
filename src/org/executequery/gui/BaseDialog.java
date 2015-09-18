/*
 * BaseDialog.java
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

package org.executequery.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.executequery.ActiveComponent;
import org.executequery.GUIUtilities;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.GlassPanePanel;

/**
 * Base dialog to be extended.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class BaseDialog extends AbstractBaseDialog
                        implements FocusListener,
                                   ActionContainer {

    /** the content panel */
    private JPanel contentPanel;

    /** Creates a new instance of BaseDialog */
    public BaseDialog(String name, boolean modal) {
        this(name, modal, null);
    }

    /** Creates a new instance of BaseDialog */
    public BaseDialog(String name, boolean modal, boolean resizeable) {
        this(name, modal, null);
        setResizable(resizeable);
    }

    /** Creates a new instance of BaseDialog */
    public BaseDialog(String name, boolean modal, JPanel panel) {
        super(GUIUtilities.getParentFrame(), name, modal);
        addDisplayComponentWithEmptyBorder(panel);
        addFocusListener(this);
        getRootPane().setGlassPane(new GlassPanePanel());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Indicates that a [long-running] process has begun.
     * This triggers the glass pane on and sets the cursor appropriately.
     */
    public void block() {
        GUIUtils.invokeLater(new Runnable() {
            public void run() {
                if (getRootPane().getGlassPane().isVisible()) {
                    return;
                }
                getRootPane().getGlassPane().setVisible(true);
            }
        });
        updateCursor(true);
    }

    /**
     * Indicates that a [long-running] process has ended.
     * This triggers the glass pane off and sets the cursor appropriately.
     */
    public void unblock() {
        GUIUtils.invokeLater(new Runnable() {
            public void run() {
                if (!getRootPane().getGlassPane().isVisible()) {
                    return;
                }
                getRootPane().getGlassPane().setVisible(false);
            }
        });
        updateCursor(false);
    }

    private void updateCursor(boolean inProcess) {
        if (inProcess) {
            GUIUtilities.showWaitCursor();
        } else {
            GUIUtilities.showNormalCursor();
        }
    }

    // ------------------------------------------
    // FocusListener implementation

    public void focusGained(FocusEvent e) {
        dialogFocusChanged(true);
    }

    public void focusLost(FocusEvent e) {
        dialogFocusChanged(false);
    }

    // ------------------------------------------

    /**
     *  Removes this dialog from the application
     *  controller <code>GUIUtilities</code> object before
     *  a call to <code>super.dispose()</code>.
     */
    public void dispose() {
        if (contentPanel instanceof ActiveComponent) {
            ((ActiveComponent)contentPanel).cleanup();
        }
        contentPanel = null;
        GUIUtilities.deregisterDialog(this);
        GUIUtils.scheduleGC();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BaseDialog.this.superDispose();
            }
        });
    }

    private void superDispose() {
        super.dispose();
    }

    /**
     * Indicates the process has completed.
     */
    public void finished() {
        dispose();
    }

    /**
     * Indicates whether this is a dialog.
     *
     * @return true | false
     */
    public boolean isDialog() {
        return true;
    }

    /**
     *  Called for a change in focus as specified. This
     *  method will pass this object into <code>GUIUtilities</code>
     *  methods <code>setFocusedDialog(JDialog)</code> and
     *  <code>removeFocusedDialog(JDialog)</code> depending on
     *  the focus parameter specified.
     *
     *  @param whether this dialog has focus
     */
    private void dialogFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            GUIUtilities.setFocusedDialog(this);
        } else {
            GUIUtilities.removeFocusedDialog(this);
        }
    }

    /**
     * Adds the primary panel for display in this dialog.
     *
     * @param the main panel display
     */
    public void addDisplayComponentWithEmptyBorder(JPanel panel) {
        if (panel == null) {
            return;
        }
        contentPanel = panel;
        getContentPane().setLayout(new GridBagLayout());
        getContentPane().add(panel,
                new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
    }

    /**
     * Adds the primary panel for display in this dialog.
     *
     * @param the main panel display
     */
    public void addDisplayComponent(JPanel panel) {
        if (panel == null) {
            return;
        }
        contentPanel = panel;
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
    }

    /**
     * Returns the content panel of this dialog.
     *
     * @return the dialog's content panel
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Packs, positions and displays the dialog.
     */
    public void display() {

        // check for multiple calls
        if (isVisible()) {

            return;
        }

        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        GUIUtilities.registerDialog(this);
        setVisible(true);

        if (contentPanel instanceof FocusComponentPanel) {

            GUIUtils.requestFocusInWindow(
                    ((FocusComponentPanel)contentPanel).getDefaultFocusComponent());
        }

        toFront();
    }

}





