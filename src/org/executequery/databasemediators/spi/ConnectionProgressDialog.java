/*
 * ConnectionProgressDialog.java
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

package org.executequery.databasemediators.spi;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.ConnectionBuilder;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.ProgressBar;
import org.underworldlabs.swing.ProgressBarFactory;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class ConnectionProgressDialog extends JDialog
        implements Runnable {

    private final ConnectionBuilder connectionBuilder;
    private ProgressBar progressBar;
    private JButton cancelButton;

    public ConnectionProgressDialog(ConnectionBuilder connectionBuilder) {

        super(GUIUtilities.getParentFrame(), Bundles.getCommon("connecting"), true);
        this.connectionBuilder = connectionBuilder;

        init();
        arrange();
    }

    private void init() {

        progressBar = ProgressBarFactory.create(true, true);
        ((JComponent) progressBar).setPreferredSize(new Dimension(280, 20));

        cancelButton = WidgetFactory.createButton("cancelButton", Bundles.getCommon("cancel.button"));
        cancelButton.setPreferredSize(new Dimension(75, 30));
        cancelButton.setMargin(Constants.EMPTY_INSETS);
        cancelButton.addActionListener(e -> cancel());
    }

    private void arrange() {

        JPanel base = new JPanel(new GridBagLayout());
        base.setBorder(BorderFactory.createEtchedBorder());

        GridBagHelper gbh = new GridBagHelper().setInsets(10, 10, 10, 5);
        base.add(new JLabel(bundleString("connectionLabel", connectionBuilder.getConnectionName())), gbh.get());
        base.add(((JComponent) progressBar), gbh.setInsets(10, 0, 10, 5).nextRowFirstCol().spanX().get());
        base.add(cancelButton, gbh.nextRow().setInsets(10, 0, 10, 10).spanX().get());

        add(base);

        pack();
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
    }

    public void cancel() {
        Log.info(bundleString("connection-canceled"));
        connectionBuilder.cancel();
        dispose();
    }

    @Override
    public void run() {
        progressBar.start();
        setVisible(true);
    }

    @Override
    public void dispose() {

        if (progressBar != null) {
            progressBar.stop();
            progressBar.cleanup();
        }

        super.dispose();
    }

    public static String bundleString(String key) {
        return Bundles.get(ConnectionProgressDialog.class, key);
    }

    public static String bundleString(String key, Object... args) {
        return Bundles.get(ConnectionProgressDialog.class, key, args);
    }

}
