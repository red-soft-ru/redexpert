package org.underworldlabs.swing;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

/**
 * @author Alexey Kozlov
 */

public class DefaultProgressDialog extends JDialog
        implements Runnable {

    private static final String TITLE = bundledString("Executing");

    // --- GUI components ---

    protected JLabel infoLabel;
    protected ProgressBar progressBar;
    protected JPanel buttonPanel;

    // ---

    protected final String description;
    protected boolean isCancel = false;

    public DefaultProgressDialog(String description) {
        super(GUIUtilities.getParentFrame(), TITLE, true);

        this.description = description;
        init();
    }

    private void init() {

        // --- init ---

        infoLabel = new JLabel("");
        infoLabel.setVisible(false);

        progressBar = ProgressBarFactory.create(true, true);
        ((JComponent) progressBar).setPreferredSize(new Dimension(280, 20));

        JButton cancelButton = new ProgressDialogButton(Bundles.getCommon("cancel.button"));
        cancelButton.setToolTipText(bundledString("CancelButtonToolTipText"));
        cancelButton.addActionListener(e -> {
            Log.info("Action canceled");
            isCancel = true;
            dispose();
        });

        // --- arrange ---

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5);
        gridBagHelper.anchorCenter();

        // button panel
        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.add(cancelButton, gridBagHelper.get());

        // main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(new JLabel(description), gridBagHelper.setLabelDefault().get());
        mainPanel.add(infoLabel, gridBagHelper.topGap(1).nextRowFirstCol().get());
        mainPanel.add((JComponent) progressBar, gridBagHelper.topGap(5).nextRowFirstCol().fillHorizontally().get());
        mainPanel.add(buttonPanel, gridBagHelper.nextRowFirstCol().setLabelDefault().get());

        // base
        add(mainPanel, BorderLayout.CENTER);
        setResizable(false);
        setUndecorated(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));

    }

    public void setCancel() {
        isCancel = true;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setInformationLabelText(String text) {
        infoLabel.setText(text);
        infoLabel.setVisible(text != null && !text.isEmpty());
        repaint();
        pack();
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

    @SuppressWarnings("FieldCanBeLocal")
    protected static class ProgressDialogButton extends JButton {

        private final int DEFAULT_WIDTH = 75;
        private final int DEFAULT_HEIGHT = 30;

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

    } // class ProgressDialogButton

    public static String bundledString(String key) {
        return Bundles.get(DefaultProgressDialog.class, key);
    }

}
