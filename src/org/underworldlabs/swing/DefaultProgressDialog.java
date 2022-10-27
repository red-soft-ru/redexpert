package org.underworldlabs.swing;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Alexey Kozlov
 */

public class DefaultProgressDialog extends JDialog
        implements Runnable,
        ActionListener {

    private ProgressBar progressBar;    //progressBar widget
    private final String description;    //description label text
    private boolean isCancel = false;   //flag that displays if the cancelButton was clicked

    public boolean isCancel() {
        return isCancel;
    }

    public DefaultProgressDialog(String description) {

        super(GUIUtilities.getParentFrame(), bundledString("Executing"), true);

        this.description = description;
        init();

    }

    private void init() {

        JPanel mainPanel = new JPanel(new GridBagLayout());

        progressBar = ProgressBarFactory.create(true, true);
        ((JComponent) progressBar).setPreferredSize(new Dimension(280, 20));

        JButton cancelButton = new CancelButton();
        cancelButton.addActionListener(this);

        // ---------------------------------------------
        // Components arranging
        // ---------------------------------------------

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5);
        gridBagHelper.anchorCenter();

        mainPanel.add(new JLabel(description), gridBagHelper.setLabelDefault().get());
        mainPanel.add((JComponent) progressBar, gridBagHelper.nextRowFirstCol().fillHorizontally().get());
        mainPanel.add(cancelButton, gridBagHelper.nextRowFirstCol().setLabelDefault().get());

        add(mainPanel, BorderLayout.CENTER);

        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));

    }

    public void run() {

        progressBar.start();
        setVisible(true);
    }

    public void dispose() {

        if (progressBar != null) {

            progressBar.stop();
            progressBar.cleanup();
        }

        isCancel = true;

        super.dispose();
    }

    public void actionPerformed(ActionEvent e) {

        Log.info("Action canceled");
        dispose();
    }

    // ---------------------------------------------
    // Cancel button class
    // ---------------------------------------------

    private static class CancelButton extends JButton {

        private final int DEFAULT_WIDTH = 75;
        private final int DEFAULT_HEIGHT = 30;

        public CancelButton() {

            super(Bundles.get("common.cancel.button"));
            setMargin(Constants.EMPTY_INSETS);
        }

        public int getWidth() {
            return Math.max(super.getWidth(), DEFAULT_WIDTH);
        }

        public int getHeight() {
            return Math.max(super.getHeight(), DEFAULT_HEIGHT);
        }

        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), getHeight());
        }

    }

    // ---------------------------------------------
    // Localization settings
    // ---------------------------------------------

    public static String bundledString(String key) {
        return Bundles.get(DefaultProgressDialog.class, key);
    }

}












