/*
 * BaseDialog.java
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

package org.executequery.gui;

import org.executequery.ActiveComponent;
import org.executequery.GUIUtilities;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.GlassPanePanel;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Base dialog to be extended.
 *
 * @author Takis Diakoumis
 */
public class BaseDialog extends AbstractBaseDialog
        implements FocusListener,
        ActionContainer {

    private JPanel contentPanel;

    public BaseDialog(String name, boolean modal) {
        this(name, modal, null);
    }

    public BaseDialog(String name, boolean modal, boolean resizeable) {
        this(name, modal, null);
        setResizable(resizeable);
    }

    public BaseDialog(String name, boolean modal, JPanel panel) {
        super(GUIUtilities.getParentFrame(), name, modal);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getRootPane().setGlassPane(new GlassPanePanel());
        addDisplayComponentWithEmptyBorder(panel);
        addFocusListener(this);
    }

    /**
     * Adds the primary panel for display in this dialog.
     *
     * @param panel the panel to display
     */
    public void addDisplayComponent(JPanel panel) {

        if (panel == null)
            return;

        contentPanel = panel;
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
    }

    /**
     * Adds the primary panel for display in this dialog.
     *
     * @param panel the panel display
     */
    public void addDisplayComponentWithEmptyBorder(JPanel panel) {

        if (panel == null)
            return;

        GridBagConstraints constraints = new GridBagHelper()
                .setInsets(5, 5, 5, 5)
                .fillBoth().spanX().spanY().get();

        contentPanel = panel;
        getContentPane().setLayout(new GridBagLayout());
        getContentPane().add(panel, constraints);
    }

    /// Packs, positions and displays the dialog.
    public void display() {

        // check for multiple calls
        if (isVisible())
            return;

        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        GUIUtilities.registerDialog(this);
        setVisible(true);

        if (contentPanel instanceof FocusComponentPanel) {
            FocusComponentPanel focusPanel = (FocusComponentPanel) contentPanel;
            GUIUtils.requestFocusInWindow(focusPanel.getDefaultFocusComponent());
        }

        toFront();
    }

    // --- helper methods ---

    private void enableGlassPane() {
        Component glassPane = getRootPane().getGlassPane();
        if (!glassPane.isVisible())
            glassPane.setVisible(true);
    }

    private void disableGlassPane() {
        Component glassPane = getRootPane().getGlassPane();
        if (glassPane.isVisible())
            glassPane.setVisible(false);
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    // --- ActionContainer impl ---

    /**
     * Indicates that a [long-running] process has begun.
     * This triggers the glass pane on and sets the cursor appropriately.
     */
    @Override
    public void block() {
        GUIUtils.invokeLater(this::enableGlassPane);
        GUIUtilities.showWaitCursor();
    }

    /**
     * Indicates that a [long-running] process has ended.
     * This triggers the glass pane off and sets the cursor appropriately.
     */
    @Override
    public void unblock() {
        GUIUtils.invokeLater(this::disableGlassPane);
        GUIUtilities.showNormalCursor();
    }

    @Override
    public void finished() {
        dispose();
    }

    @Override
    public boolean isDialog() {
        return true;
    }

    // --- AbstractBaseDialog impl ---

    /**
     * Removes this dialog from the application
     * controller <code>GUIUtilities</code> object before
     * a call to <code>super.dispose()</code>.
     */
    @Override
    public void dispose() {

        if (contentPanel instanceof ActiveComponent)
            ((ActiveComponent) contentPanel).cleanup();

        cleanupComponent(contentPanel);
        contentPanel = null;

        GUIUtilities.deregisterDialog(this);
        GUIUtils.scheduleGC();

        SwingUtilities.invokeLater(super::dispose);
    }

    // --- FocusListener impl ---

    @Override
    public void focusGained(FocusEvent e) {
        GUIUtilities.setFocusedDialog(this);
    }

    @Override
    public void focusLost(FocusEvent e) {
        GUIUtilities.removeFocusedDialog(this);
    }

}
