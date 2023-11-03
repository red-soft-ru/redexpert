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

    protected ProgressBar progressBar;
    protected final String description;
    protected boolean isCancel = false;
    protected JPanel buttonPanel;

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
        buttonPanel = new JPanel(new GridBagLayout());

        progressBar = ProgressBarFactory.create(true, true);
        ((JComponent) progressBar).setPreferredSize(new Dimension(280, 20));

        JButton cancelButton = new ProgressDialogButton();
        cancelButton.addActionListener(this);
        cancelButton.setToolTipText(bundledString("CancelButtonToolTipText"));

        // ---------------------------------------------
        // Components arranging
        // ---------------------------------------------

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5);
        gridBagHelper.anchorCenter();

        buttonPanel.add(cancelButton, gridBagHelper.get());

        mainPanel.add(new JLabel(description), gridBagHelper.setLabelDefault().get());
        mainPanel.add((JComponent) progressBar, gridBagHelper.nextRowFirstCol().fillHorizontally().get());
        mainPanel.add(buttonPanel, gridBagHelper.nextRowFirstCol().setLabelDefault().get());

        add(mainPanel, BorderLayout.CENTER);

        setResizable(false);
        setUndecorated(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));

    }

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

    @Override
    public void actionPerformed(ActionEvent e) {

        Log.info("Action canceled");
        isCancel = true;
        dispose();
    }

    // ---------------------------------------------
    // Cancel button class
    // ---------------------------------------------

    protected static class ProgressDialogButton extends JButton {

        private final int DEFAULT_WIDTH = 75;
        private final int DEFAULT_HEIGHT = 30;

        public ProgressDialogButton() {
            super(Bundles.get("common.cancel.button"));
            setMargin(Constants.EMPTY_INSETS);
        }

        public ProgressDialogButton(String text) {
            super(text);
            setMargin(Constants.EMPTY_INSETS);
        }

        @Override
        public int getWidth() {
            return Math.max(super.getWidth(), DEFAULT_WIDTH);
        }

        @Override
        public int getHeight() {
            return Math.max(super.getHeight(), DEFAULT_HEIGHT);
        }

        @Override
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












