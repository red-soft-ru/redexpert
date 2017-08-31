/*
 * AbstractBaseDialog.java
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

package org.underworldlabs.swing;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

public abstract class AbstractBaseDialog extends JDialog {

    public AbstractBaseDialog() throws HeadlessException {
        super();
    }

    public AbstractBaseDialog(Dialog owner, boolean modal)
            throws HeadlessException {
        super(owner, modal);
    }

    public AbstractBaseDialog(Dialog owner, String title, boolean modal,
            GraphicsConfiguration gc) throws HeadlessException {
        super(owner, title, modal, gc);
    }

    public AbstractBaseDialog(Dialog owner, String title, boolean modal)
            throws HeadlessException {
        super(owner, title, modal);
    }

    public AbstractBaseDialog(Dialog owner, String title)
            throws HeadlessException {
        super(owner, title);
    }

    public AbstractBaseDialog(Dialog owner) throws HeadlessException {
        super(owner);
    }

    public AbstractBaseDialog(Frame owner, boolean modal)
            throws HeadlessException {
        super(owner, modal);
    }

    public AbstractBaseDialog(Frame owner, String title, boolean modal,
            GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    public AbstractBaseDialog(Frame owner, String title, boolean modal)
            throws HeadlessException {
        super(owner, title, modal);
    }

    public AbstractBaseDialog(Frame owner, String title)
            throws HeadlessException {
        super(owner, title);
    }

    public AbstractBaseDialog(Frame owner) throws HeadlessException {
        super(owner);
    }

    protected JRootPane createRootPane() {

        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

        JRootPane rootPane = super.createRootPane();
        rootPane.registerKeyboardAction(
                closeDialogActionListener(), keyStroke, 
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        return rootPane;
    }

    private ActionListener closeDialogActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
    }
    
}

