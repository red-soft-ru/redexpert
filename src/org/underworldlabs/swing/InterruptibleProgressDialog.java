/*
 * InterruptibleProgressDialog.java
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

package org.underworldlabs.swing;

import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.Constants;
import org.underworldlabs.swing.util.Interruptible;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Takis Diakoumis
 */
public class InterruptibleProgressDialog extends JDialog
        implements Runnable,
        ActionListener {

    /**
     * The event parent to this object
     */
    private final Interruptible process;

    /**
     * The progress bar widget
     */
    private ProgressBar progressBar;

    /**
     * The parent frame of this dialog
     */
    private Frame parentFrame;

    /**
     * The progress bar label text
     */
    private final String labelText;

    public InterruptibleProgressDialog(Frame parentFrame,
                                       String title,
                                       String labelText,
                                       Interruptible process) {

        super(parentFrame, title, true);

        this.process = process;
        this.labelText = labelText;

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public InterruptibleProgressDialog(Dialog parentDialog,
                                       String title,
                                       String labelText,
                                       Interruptible process) {

        super(parentDialog, title, true);

        this.process = process;
        this.labelText = labelText;

        try {

            init();

        } catch (Exception e) {

            Log.error("Error init class InterruptibleProgressDialog:", e);
        }

    }

    public void run() {
        pack();
        setLocation(GUIUtils.getPointToCenter(parentFrame, getSize()));
        progressBar.start();
        setVisible(true);
    }

    private void init() throws Exception {

        progressBar = ProgressBarFactory.create();
        ((JComponent) progressBar).setPreferredSize(new Dimension(260, 18));

        JPanel base = new JPanel(new GridBagLayout());

        JButton cancelButton = new CancelButton();
        cancelButton.addActionListener(this);

        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(10, 20, 10, 20);
        gbc.insets = ins;
        base.add(new JLabel(labelText), gbc);
        gbc.gridy = 1;
        gbc.insets.top = 0;
        base.add(((JComponent) progressBar), gbc);
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        base.add(cancelButton, gbc);

        base.setBorder(BorderFactory.createEtchedBorder());

        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        c.add(base, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));

        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {

        try {

            process.setCancelled(true);
            process.interrupt();

        } finally {

            dispose();
        }
    }

    public void dispose() {

        if (progressBar != null) {

            progressBar.stop();
            progressBar.cleanup();
        }

        super.dispose();
    }


    class CancelButton extends JButton {

        private final int DEFAULT_WIDTH = 75;
        private final int DEFAULT_HEIGHT = 30;

        public CancelButton() {
            super(Bundles.getCommon("cancel.button"));
            setMargin(Constants.EMPTY_INSETS);
        }

        public int getWidth() {
            int width = super.getWidth();
            if (width < DEFAULT_WIDTH) {
                return DEFAULT_WIDTH;
            }
            return width;
        }

        public int getHeight() {
            int height = super.getHeight();
            if (height < DEFAULT_HEIGHT) {
                return DEFAULT_HEIGHT;
            }
            return height;
        }

        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), getHeight());
        }
    }

}






