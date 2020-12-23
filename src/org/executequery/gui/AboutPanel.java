/*
 * AboutPanel.java
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
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.HeapMemoryPanel;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * System About panel.
 *
 * @author Takis Diakoumis
 */
public class AboutPanel extends BaseDialog
        implements ActiveComponent,
        ActionListener {

    public static final String TITLE = "About";
    public static final String FRAME_ICON = "Information16.png";

    private JTabbedPane tabPane;
    private HeapMemoryPanel heapPanel;
    private AboutImagePanel imagePanel;
    private ScrollingCreditsPanel creditsPanel;

    public AboutPanel() {

        super(TITLE, true);
        init();
    }

    private void init() {

        tabPane = new JTabbedPane();
        tabPane.add("System", systemDetails());
        tabPane.add("Resources", systemResources());
        tabPane.add("License", license());
        //tabPane.add("Credits", credits());
        tabPane.add("Copyright", copyright());

        imagePanel = new AboutImagePanel();

        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.setPreferredSize(new Dimension(720, 480));
        basePanel.add(imagePanel, BorderLayout.NORTH);
        basePanel.add(tabPane, BorderLayout.CENTER);
        basePanel.add(addButtonPanel(), BorderLayout.SOUTH);

        addDisplayComponentWithEmptyBorder(basePanel);
        setResizable(false);
        display();
    }

    private Component copyright() {
        JPanel base = new JPanel(new GridBagLayout());

        String versionText = "Version " +
                System.getProperty("executequery.minor.version") +
                ". Red Soft 2015-" + Calendar.getInstance().get(Calendar.YEAR) + ". http://www.red-soft.ru";

        String forkText = "Fork of Execute Query: http://executequery.org";
        base.setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        base.add(new JLabel(versionText, JLabel.CENTER), gbc);

        gbc.insets.top = 7;
        gbc.insets.left = 7;
        gbc.insets.right = 7;
        gbc.gridy++;
        gbc.ipady = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        base.add(new JLabel(forkText, JLabel.CENTER), gbc);

        JPanel main = new JPanel(new GridBagLayout());
        main.add(base, resetConstraints(gbc));

        return main;
    }

    private GridBagConstraints resetConstraints(GridBagConstraints gbc) {
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets.top = 1;
        gbc.insets.bottom = 3;
        gbc.insets.left = 3;
        gbc.insets.right = 3;
        gbc.insets.top = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        return gbc;
    }

    private JPanel credits() {
        creditsPanel = new ScrollingCreditsPanel();
        JPanel main = new JPanel(new GridBagLayout());
        main.add(creditsPanel, resetConstraints(new GridBagConstraints()));
        return main;
    }

    private JPanel addButtonPanel() {

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setPreferredSize(new Dimension(350, 50));

        JButton okButton = new DefaultPanelButton(Bundles.get("common.ok.button"));
        okButton.setMnemonic('O');

        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(7, 0, 0, 0);
        gbc.insets = ins;

        buttonPanel.add(okButton, gbc);
        okButton.addActionListener(this);

        return buttonPanel;
    }

    public void dispose() {
        cleanup();
        super.dispose();
    }

    public void actionPerformed(ActionEvent e) {
        dispose();
    }

    public void cleanup() {
        if (imagePanel != null) {
            imagePanel.stopTimer();
        }
        imagePanel = null;

        if (creditsPanel != null) {
            creditsPanel.stopTimer();
        }
        creditsPanel = null;

        if (heapPanel != null) {
            heapPanel.stopTimer();
        }
        heapPanel = null;
    }

    private JPanel license() {

        JPanel base = new JPanel(new GridBagLayout());

        String labelText = null;

        try {

            labelText = FileUtils.loadResource(
                    "org/executequery/gui/resource/licensePanelText.html");

        } catch (IOException e) {

            if (Log.isDebugEnabled()) {

                Log.debug("Error loading license panel text", e);
            }

        }

        JButton button = new JButton(ActionBuilder.get("license-command"));
        button.setText("View License");
        button.setIcon(null);

        base.setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        base.add(new JLabel(labelText, JLabel.CENTER), gbc);
        gbc.insets.top = 7;
        gbc.insets.left = 7;
        gbc.insets.right = 7;
        gbc.gridy++;
        gbc.ipady = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        base.add(button, gbc);

        JPanel main = new JPanel(new GridBagLayout());
        main.add(base, resetConstraints(gbc));

        return main;
    }


    private JPanel systemResources() {
        heapPanel = new HeapMemoryPanel();
        return heapPanel;
    }

    private JPanel systemDetails() {

        return new SystemPropertiesPanel();
    }

    private void renderingHintsForText(Graphics2D g2d) {

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
    }

    class ScrollingCreditsPanel extends JPanel {

        private Timer timer;

        private Font nameFont;

        private Font titleFont;

        private String[] names;

        private String[] titles;

        protected ScrollingCreditsPanel() {
            setBorder(BorderFactory.createEtchedBorder());

            nameFont = new Font("dialog", Font.BOLD, 12);
            titleFont = new Font("dialog", Font.PLAIN, 12);

            loadNamesAndTitles();

            startTimer();
        }

        private void loadNamesAndTitles() {

            String namesAndTitles = SystemProperties.getStringProperty(
                    "system", "about.panel.credits");

            String[] namesAndTitlesAsArray = namesAndTitles.split(",");
            names = new String[namesAndTitlesAsArray.length];
            titles = new String[namesAndTitlesAsArray.length];

            for (int i = 0; i < namesAndTitlesAsArray.length; i++) {

                String nameAndTitle = namesAndTitlesAsArray[i];
                int pipeIndex = nameAndTitle.indexOf('|');

                names[i] = nameAndTitle.substring(0, pipeIndex);
                titles[i] = nameAndTitle.substring(pipeIndex + 1);
            }

        }

        protected void startTimer() {
            final Runnable scroller = new Runnable() {
                public void run() {
                    yOffset--;
                    ScrollingCreditsPanel.this.repaint();
                }
            };

            TimerTask paintCredits = new TimerTask() {
                public void run() {
                    EventQueue.invokeLater(scroller);
                }
            };
            timer = new Timer();
            timer.schedule(paintCredits, 500, 40);
        }

        protected void ensureTimerRunning() {
            if (timer == null) {
                startTimer();
            }
        }

        private int yOffset;
        int count = 0;

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            Graphics2D g2d = (Graphics2D) g;

            renderingHintsForText(g2d);

            g2d.setPaint(new GradientPaint(0, 0, Color.LIGHT_GRAY, width / 2,
                    0, Color.WHITE, true));

            g2d.fillRect(0, 0, width, height);

            int x = 0;
            int y = 0;
            int stringWidth = 0;

            //g.setColor(Color.GRAY.brighter());
            //g.drawLine(width / 6, 0, width / 6, height);

            g2d.setColor(Color.BLACK);

            for (int i = 0; i < names.length; i++) {
                g2d.setFont(nameFont);
                FontMetrics fm = g2d.getFontMetrics();
                stringWidth = fm.stringWidth(names[i]);
                x = (width - stringWidth) / 2;
                y += fm.getHeight() + 1;
                g2d.drawString(names[i], x, y + yOffset + height);

                g2d.setFont(titleFont);
                fm = g2d.getFontMetrics();
                stringWidth = fm.stringWidth(titles[i]);
                x = (width - stringWidth) / 2;
                y += fm.getHeight() + 1;
                g2d.drawString(titles[i], x, y + yOffset + height);

                if (i == names.length - 1) {
                    if (Math.abs(yOffset) >= (y + height)) {
                        yOffset = 0;
                    }
                }

                y += 15;
            }

        }

        public void stopTimer() {
            if (timer != null) {
                timer.cancel();
            }
            timer = null;
        }

    }

    class AboutImagePanel extends JPanel {

        private static final int HEIGHT = 206;
        private static final int WIDTH = 720;

        private Timer timer;
        private Image eqImage;
        private Image background;

        // background and logo fade
        boolean stageOneComplete;

        // version text
        boolean stageTwoComplete;

        int bottomOffsetVersion;

        private float alpha;

        protected AboutImagePanel() {

            ImageIcon icon = GUIUtilities.loadImage("AboutText.png");
            eqImage = icon.getImage();

            ImageIcon backgroundIcon = GUIUtilities.loadImage("AboutBackground.png");
            background = backgroundIcon.getImage();

            final Runnable fader = new Runnable() {
                public void run() {

                    if (!stageOneComplete) {

                        if (alpha >= 0.999f) {

                            stageOneComplete = true;

                            try {

                                Thread.sleep(500);

                            } catch (InterruptedException e) {
                            }

                        } else {

                            alpha += 0.020f;
                        }


                    }

                    if (stageOneComplete && !stageTwoComplete) {

                        if (bottomOffsetVersion >= 30) {

                            stageTwoComplete = true;

                            timer.cancel();
                            GUIUtils.scheduleGC();
                            return;

                        } else {

                            bottomOffsetVersion += 1;
                        }

                    }

                    AboutImagePanel.this.repaint();
                }
            };

            TimerTask paintImage = new TimerTask() {
                public void run() {
                    EventQueue.invokeLater(fader);
                }
            };

            timer = new Timer();
            timer.schedule(paintImage, 500, 70);
        }

        public void stopTimer() {
            if (timer != null) {
                timer.cancel();
            }
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            int imageWidth = eqImage.getWidth(this);
            int imageHeight = eqImage.getHeight(this);

            int imageX = (WIDTH - imageWidth) / 2;
            int imageY = (HEIGHT - imageHeight) / 2;

            renderingHintsForText(g2d);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g2d.drawImage(background, 0, 0, WIDTH, HEIGHT, this);
            g2d.drawImage(eqImage, imageX - 1, imageY - 1, this);

            if (stageOneComplete) {

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            }

            g2d.setClip(0, 0, WIDTH, HEIGHT);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        }

        public Dimension getPreferredSize() {
            return new Dimension(WIDTH, HEIGHT);
        }

    } // class AboutImagePanel

}







